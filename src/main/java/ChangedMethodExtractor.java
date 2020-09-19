import util.ContentLoader;
import util.ContentWriter;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Enzo Cotter on 2020/9/19.
 */

//
public class ChangedMethodExtractor {
    public static String  commitFilePath = "/Users/junming/code/LocalMethodLevelData/src/main/resources";
    public static String indexFilePath = "/Users/junming/code/BLIZZARD-Replication-Package-ESEC-FSE2018/Lucene-Index2File-Mapping";
    public static String project = "birt";
    public static int MAX_COMMIT_COUNT = 21000;

    public static void main(String[] args) {

        String commitFile = String.format("%s/%s_commit.txt", commitFilePath, project);
        String indexFile = String.format("%s/%sm.ckeys", indexFilePath, project);
        String outputFile = String.format("%s/%s_method_date.txt", commitFilePath,project);
        LinkedHashMap<String, String> commitTimeMap = null;
        ParseDiffData p = new ParseDiffData();
        String firstCommit = "";
        String secondCommit = "";
        Map<String, String> methodLastChangeMap = new HashMap<>(); // <MethodSig, LastModifyDate>
        Set<String> latestMethods = new HashSet<>();
        String[] candidates = ContentLoader.getAllLines(indexFile);
        for(String candidate: candidates) {
            latestMethods.add(candidate.split(":")[2]);
        }
        int count = 0;
        try {
            commitTimeMap = loadCommits(commitFile);
            for(String s : commitTimeMap.keySet()){
                firstCommit = secondCommit;
                secondCommit = s;
                if(count != 0 && commitTimeMap.size() - count < MAX_COMMIT_COUNT) {
                    Map<String, Visitor> diffMap = p.getVersionUpdateInfo(project, firstCommit, secondCommit);
                    // latestMethods过滤,这样之后得到的methodLastChangeMap都是需要的Method
                    processDiffGraph(diffMap, methodLastChangeMap, commitTimeMap, secondCommit, latestMethods);
                }
                count++;
                if(count % 100 == 0)
                    System.out.println(String.format("Commit %s/%s", count, commitTimeMap.size()));
            }

            // compare pair to pair

        }
        catch (Exception e){
            System.out.println(e);
        }
        ContentWriter.writeMap(outputFile, methodLastChangeMap, "@");
        System.out.println(methodLastChangeMap.size());
        return;
    }

    public static void processDiffGraph(Map<String, Visitor> diffMap, Map<String, String> methodLastChangeMap,
    LinkedHashMap<String, String> commitTimeMap, String commitID, Set<String> latestMethods) {
        for (String javaFileName : diffMap.keySet()) {
            HashMap<String, Visitor.Method> signatureAndBody = diffMap.get(javaFileName).signatureToMethod;
            for (String methodSignature : signatureAndBody.keySet()) {
                String convertSig = convertMethodSignature(javaFileName, methodSignature);
                if (signatureAndBody.get(methodSignature).groundTruth&&
                latestMethods.contains(convertSig)) {
                    methodLastChangeMap.put(convertSig, commitTimeMap.get(commitID));
                }
            }
        }
    }

    public static String convertMethodSignature(String fileName, String MethodName)
    {
        // tomcat 前要加tomcat.
        String[] fileTokens = fileName.substring(0, fileName.length()-5).split("#");
        String[] methodTokens = MethodName.split("#");
        String methodName = methodTokens[1];
        String args = methodTokens.length ==3? methodTokens[2]: "";
        StringBuilder fileBuilder = new StringBuilder();
        if(project.equals("tomcat"))
            fileBuilder.append("tomcat.");
        if(project.equals("aspectj"))
            fileBuilder.append("org.aspectj.");
        for(int i = 1; i< fileTokens.length; i++){
            fileBuilder.append(fileTokens[i]);
            fileBuilder.append(".");
        }
        fileBuilder.deleteCharAt(fileBuilder.length()-1);
        String fileStr = fileBuilder.toString();
        if(project.equals("swt"))
            fileStr = fileStr.substring(8);

        return String.format("%s-%s(%s)", fileStr, methodName, args);
    }

    // <CommitID, commitTime> 顺序是由前到后，必须保证
    public static LinkedHashMap<String, String> loadCommits(String commitFile) throws Exception {
        String[] lines = ContentLoader.getAllLines(commitFile);
        LinkedHashMap<String, String> res = new LinkedHashMap<>();
        for(int i = lines.length-1; i>=0; i--) {
            String commitID = lines[i].substring(0, 10);
            //SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //Date d = sdf.parse(lines[i].substring(10));
            res.put(commitID, lines[i].substring(10));
        }
        return res;
    }
}
