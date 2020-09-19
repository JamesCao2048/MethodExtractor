import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class TestVisitor {

    public static void main(String[] args) throws IOException {
        ArrayList<String> lines = (ArrayList<String>)FileUtils.readLines(
                new File("/Users/junming/code/LocalMethodLevelData/src/main/java/ParseDiffData.java"),
                Charset.defaultCharset());
        Visitor visitor = new Visitor(lines);
        System.out.println(visitor);
    }

}
