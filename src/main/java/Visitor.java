import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Visitor extends ASTVisitor {

    class Method{
        String method;
        boolean groundTruth = false;
        Method(String method){
            this.method = method;
        }
    }

    class StartEndLineNumber{
        Integer startLineNumber;
        Integer endLineNumber;
        StartEndLineNumber(Integer startLineNumber, Integer endLineNumber){
            this.startLineNumber = startLineNumber;
            this.endLineNumber = endLineNumber;
        }
    }

    HashMap<String, Method> signatureToMethod = new HashMap<>();
    ArrayList<StartEndLineNumber> startEndLineNumbers = new ArrayList<>();
    HashMap<Integer, String> startEndLineNumberIndexToSignature = new HashMap<>();
    private CompilationUnit parser;

    CompilationUnit getCompilationUnit(String src) {
        ASTParser parser = ASTParser.newParser(AST.JLS11); //设置Java语言规范版本
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        parser.setCompilerOptions(null);
        parser.setResolveBindings(true);

        Map<String, String> compilerOptions = JavaCore.getOptions();
        compilerOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8); //设置Java语言版本
        compilerOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        compilerOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        parser.setCompilerOptions(compilerOptions); //设置编译选项

        parser.setSource(src.toCharArray());
        return (CompilationUnit) parser.createAST(null);
    }

    Visitor(ArrayList<String> javaFileLines) {
        String javaCode = StringUtils.join(javaFileLines, "\n");
        parser = getCompilationUnit(javaCode);
        List<AbstractTypeDeclaration> ts = parser.types();
        for (AbstractTypeDeclaration t : ts) {
            if(t instanceof TypeDeclaration)
                t.accept(this);
        }
    }

    @Override
    public boolean visit(MethodDeclaration node){
        String method_name = node.getName().toString();
        String return_type;
        if(node.getReturnType2() == null){
            return_type = "";
        }else {
            return_type = node.getReturnType2().toString();
        }
        StringBuilder argsBuilder = new StringBuilder();
        List<SingleVariableDeclaration> ps = node.parameters();
        for(int i= 0; i < ps.size(); i++){
            argsBuilder.append(ps.get(i).getType().toString());
            if(i != ps.size()-1) argsBuilder.append(",");
        }
        String args = argsBuilder.toString();
        Method method = new Method(node.toString());
        String signature = return_type + "#" + method_name + "#" + args;
        signatureToMethod.put(signature, method);

        // 行号从1开始
        int startLineNumber = parser.getLineNumber(node.getStartPosition());
        int endLineNumber = parser.getLineNumber(node.getStartPosition()+node.getLength()-1);
        startEndLineNumberIndexToSignature.put(startEndLineNumbers.size(), signature);
        startEndLineNumbers.add(new StartEndLineNumber(startLineNumber, endLineNumber));
        return false;
    }

}
