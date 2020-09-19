import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ParseDiffData {
    static String dataPath  = "/Users/junming/code/BLIA/repo";

    ArrayList<String> getCmdOutput(String cmd, String dir) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            Process process = Runtime.getRuntime().exec(cmd, null, new File(dir));
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    process.getInputStream(), "gbk"));
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lines;
    }

    String locateMethod(int lineNumber, Visitor visitor){
        // 二分查找
        ArrayList<Visitor.StartEndLineNumber> lns = visitor.startEndLineNumbers;
        int start = 0, end = lns.size()-1, mid, startLineNumber, endLineNumber;
        while(start <= end){
            mid = (start + end)/2;
            startLineNumber = lns.get(mid).startLineNumber;
            endLineNumber = lns.get(mid).endLineNumber;
            if(lineNumber >= startLineNumber && lineNumber <= endLineNumber){
                return visitor.startEndLineNumberIndexToSignature.get(mid);
            }else if(lineNumber < startLineNumber){
                end = mid -1;
            }else{
                start = mid + 1;
            }
        }
        return null;
    }

    void mark(ArrayList<Integer> lineNumbers, Visitor visitor,
                     HashMap<String, Boolean> methodMap) {
        for (int ln : lineNumbers) {
            String m = locateMethod(ln, visitor);
            if (m != null) {
                methodMap.put(m, true);
            }
        }
    }

    void process(String oldClass, String newClass,
                        ArrayList<String> oldJavaFileLines, ArrayList<String> newJavaFileLines,
                        ArrayList<Integer> deletedLineNumbers, ArrayList<Integer> addedLineNumbers,
                        HashMap<String, Visitor> visitors) {

        Visitor oldVisitor = new Visitor(oldJavaFileLines);
        HashMap<String, Boolean> oldMethodMap = new HashMap<>();
        for (String m : oldVisitor.signatureToMethod.keySet()) {
            oldMethodMap.put(m, false);
        }

        Visitor newVisitor = new Visitor(newJavaFileLines);
        HashMap<String, Boolean> newMethodMap = new HashMap<>();
        for (String m : newVisitor.signatureToMethod.keySet()) {
            newMethodMap.put(m, false);
        }

        if (newClass == null) {//类被删除
            for(String s : oldVisitor.signatureToMethod.keySet()){
                oldVisitor.signatureToMethod.get(s).groundTruth = true;
            }
            visitors.put(oldClass, oldVisitor);
            return;
        } else if (oldClass == null) {//类被新增
            return;
        }

        // 类被删除
        mark(deletedLineNumbers, oldVisitor, oldMethodMap);
        mark(addedLineNumbers, newVisitor, newMethodMap);
        for (String m : oldMethodMap.keySet()) {
            if (!newMethodMap.keySet().contains(m) || (oldMethodMap.get(m) || newMethodMap.get(m))) {
                // 成员方法被删除或者方法签名被修改、成员方法被修改（包括删除行、增加行、修改行)
                oldVisitor.signatureToMethod.get(m).groundTruth = true;
            }
        }
        visitors.put(oldClass, oldVisitor);
    }

    HashMap<String, Visitor> getVersionUpdateInfo(String projectName, String lastCommitId, String newCommitId) {
        HashMap<String, Visitor> visitors = new HashMap<>();
//        String dataPath = ".";
        ArrayList<String> lines = getCmdOutput(
                String.format("git diff -M4 %s %s --unified=100000000", lastCommitId, newCommitId),
                String.format("%s/%s", dataPath, projectName));

        Pattern aPattern = Pattern.compile("^--- (.*\\.java|/dev/null)");
        Pattern bPattern = Pattern.compile("^\\+\\+\\+ (.*\\.java|/dev/null)");
        Pattern endPattern = Pattern.compile("^\\\\ No newline at end of file|^diff --git ");

        String oldClass = null;
        String newClass = null;
        ArrayList<String> oldJavaFileLines = new ArrayList<>();
        ArrayList<String> newJavaFileLines = new ArrayList<>();
        ArrayList<Integer> deletedLineNumbers = new ArrayList<>();
        ArrayList<Integer> addedLineNumbers = new ArrayList<>();
        int oldLineNumber = 0, newLineNumber = 0; //代码的行号从1开始
        for (String line : lines) {
            if (line.startsWith("@@") && line.endsWith("@@")) {
                oldLineNumber = 0;
                newLineNumber = 0;
                continue;
            }
            Matcher am = aPattern.matcher(line);
            Matcher bm = bPattern.matcher(line);
            Matcher em = endPattern.matcher(line);
            if (em.find()) {
                if (oldClass != null || newClass != null) {
                    process(oldClass, newClass,
                            oldJavaFileLines, newJavaFileLines,
                            deletedLineNumbers, addedLineNumbers,
                            visitors);
                    oldClass = null;
                    newClass = null;
                    oldJavaFileLines = new ArrayList<>();
                    newJavaFileLines = new ArrayList<>();
                    deletedLineNumbers = new ArrayList<>();
                    addedLineNumbers = new ArrayList<>();
                }
            }
            if (am.find()) {
                String g = am.group(1);
                if (g.endsWith(".java")) {
//                    oldClass = g.substring(g.lastIndexOf("/")+1, g.length()-5);
                    oldClass = g.replace("/", "#");
                }

            } else if (bm.find()) {
                String g = bm.group(1);
                if (g.endsWith(".java")) {
//                    newClass = g.substring(g.lastIndexOf("/")+1, g.length()-5);
                    newClass = g.replace("/", "#");
                }
            }
            if ((oldClass != null || newClass != null) && !aPattern.matcher(line).find() &&
                    !bPattern.matcher(line).find()) {
                if (line.startsWith("-")) {
                    oldLineNumber += 1;
                    deletedLineNumbers.add(oldLineNumber);
                    oldJavaFileLines.add(line.substring(1));
                } else if (line.startsWith("+")) {
                    newLineNumber += 1;
                    addedLineNumbers.add(newLineNumber);
                    newJavaFileLines.add(line.substring(1));
                } else {
                    oldLineNumber += 1;
                    newLineNumber += 1;
                    oldJavaFileLines.add(line);
                    newJavaFileLines.add(line);
                }
            }
        }
        if (oldClass != null || newClass != null) {
            process(oldClass, newClass,
                    oldJavaFileLines, newJavaFileLines,
                    deletedLineNumbers, addedLineNumbers, visitors);
        }
        return visitors;
    }

    public static void main(String[] args) throws IOException {

        String collectedDataDir = "D:/Data/PyProjects/FaultLocalization/collected_data/8969801/offline";
//        String collectedDataDir = ".";
        String commitIdMethodsPath = collectedDataDir + "/commit_id_and_pre_commit_id.json";
        HashMap<String, String> projects = new HashMap<>();
//        projects.put("aspectj", "org.aspectj");
//        projects.put("swt", "eclipse.platform.swt");
//        projects.put("tomcat", "tomcat");
//        projects.put("birt", "birt");
//        projects.put("jdt", "eclipse.jdt.ui");
        projects.put("eclipse", "eclipse.platform.ui");

        String jsonS = FileUtils.readFileToString(new File(commitIdMethodsPath), "UTF-8");
        JSONObject jsonObj = JSONObject.parseObject(jsonS);
        for (String projectName : projects.keySet()) {
            System.out.println("processing " + projectName + "===================");
            HashMap<String, HashMap<String, ArrayList<HashMap<String, String>>>> jsonData = new HashMap<>();
            jsonData.put(projectName, new HashMap<>());
            JSONObject projectObj = (JSONObject) jsonObj.get(projectName);
            ArrayList<String> commitIds = (ArrayList<String>) projectObj.getJSONArray("commit_id").toJavaList(String.class);
            ArrayList<String> preCommitIds = (ArrayList<String>) projectObj.getJSONArray("pre_commit_id").toJavaList(String.class);

            int partNum = 100;
            int partIndex = 0;

            for (int i=0;i<commitIds.size();i++) {
                if(i!=0 && i % partNum == 0){
                    String s = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
                    FileUtils.write(new File(collectedDataDir + "/offline_" + projectName
                                    + "_extracted_methods_"+String.valueOf(partIndex++)+".json"),
                            s, "UTF-8", true);
                    jsonData = new HashMap<>();
                    jsonData.put(projectName, new HashMap<>());
                }

                String commitId = commitIds.get(i);
                System.out.println(String.format("%d/%d", i, commitIds.size()));
                String preCommitId = preCommitIds.get(i);
                jsonData.get(projectName).put(preCommitId, new ArrayList<>());
                ParseDiffData p = new ParseDiffData();
                HashMap<String, Visitor> visitors = p.getVersionUpdateInfo(projects.get(projectName), preCommitId, commitId);
                for (String javaFileName : visitors.keySet()) {
                    HashMap<String, Visitor.Method> signatureAndBody = visitors.get(javaFileName).signatureToMethod;
                    for (String methodSignature : signatureAndBody.keySet()) {
                        if (signatureAndBody.get(methodSignature).groundTruth) {
                            HashMap<String, String> sample = new HashMap<>();
                            sample.put("file_name", javaFileName);
                            sample.put("method_signature", methodSignature);
                            sample.put("Type", "1");
                            jsonData.get(projectName).get(preCommitId).add(sample);
                        }
                    }
                }
            }
            if(!jsonData.isEmpty()) {
                String s = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(jsonData);
                FileUtils.write(new File(collectedDataDir + "/offline_" + projectName
                                + "_extracted_methods_" + String.valueOf(partIndex) + ".json"),
                        s, "UTF-8", true);
            }
        }
    }
}
