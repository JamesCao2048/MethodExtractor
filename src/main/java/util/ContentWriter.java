package util;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContentWriter {
    public static boolean writeContent(String outFile, List<String> items) {
        // writing content to output
        boolean written = false;
        try {
            FileWriter fwriter = new FileWriter(new File(outFile));
            for (String item : items) {
                fwriter.write(item + "\n");
            }
            fwriter.close();
            written = true;

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return written;
    }

    public static boolean writeContentIndexList(String outFile, List<String> items, int shift) {
        // writing content to output with index
        boolean written = false;
        try {
            FileWriter fwriter = new FileWriter(new File(outFile));

            for (int i=0; i< items.size(); i++) {
                fwriter.write((i+shift) + "::"+items.get(i)+"\n");
            }
            fwriter.close();
            written = true;

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return written;
    }

    public static boolean writeMap(String outFile, Map<String,String> items, String delimeter) {
        // writing content to output with index
        boolean written = false;
        try {
            FileWriter fwriter = new FileWriter(new File(outFile));

            for (String key: items.keySet()) {
                fwriter.write(key+ delimeter +items.get(key)+"\n");
            }
            fwriter.close();
            written = true;
        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return written;
    }

    public static boolean appendContent(String outFile, ArrayList<String> items) {
        // writing content to output
        boolean written = false;
        try {
            FileWriter fwriter = new FileWriter(new File(outFile), true);
            for (String item : items) {
                fwriter.write(item + "\n");
            }
            fwriter.close();
            written = true;

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return written;
    }

    public static boolean writeContentInt(String outFile,
                                          ArrayList<Integer> items) {
        // writing content to output
        boolean written = false;
        try {
            FileWriter fwriter = new FileWriter(new File(outFile));
            for (Integer item : items) {
                fwriter.write(item + "\n");
            }
            fwriter.close();
            written = true;

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return written;
    }

    public static boolean appendContentInt(String outFile,
                                           ArrayList<Integer> items) {
        // writing content to output
        boolean written = false;
        try {
            FileWriter fwriter = new FileWriter(new File(outFile), true);
            for (Integer item : items) {
                fwriter.write(item + "\n");
            }
            fwriter.close();
            written = true;

        } catch (Exception exc) {
            exc.printStackTrace();
        }
        return written;
    }

    public static void writeContent(String outFile, String content) {
        try {
            FileWriter fwriter = new FileWriter(new File(outFile));
            fwriter.write(content);
            fwriter.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

    public static void appendContent(String outFile, String content) {
        try {
            FileWriter fwriter = new FileWriter(new File(outFile), true);
            fwriter.write(content);
            fwriter.close();
        } catch (Exception exc) {
            exc.printStackTrace();
        }
    }

}
