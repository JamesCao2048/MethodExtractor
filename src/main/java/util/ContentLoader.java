package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ContentLoader {

    public static String loadFileContent(String fileName) {
        // code for loading the file name
        String fileContent = new String();
        try {
            File f = new File(fileName);
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(f));
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine().trim();
                if (line.trim().isEmpty())
                    continue;
                fileContent += line + "\n";
            }
            bufferedReader.close();
        } catch (Exception ex) {
            // handle the exception
            System.out.println(ex);
        }
        return fileContent;
    }


    public static String[] getAllLines(String fileName) {
        // reading items as array
        String content = loadFileContent(fileName);
        String[] lines = content.split("\n");
        return lines;
    }

    public static ArrayList<String> getAllLinesOptList(String fileName) {
        ArrayList<String> lines = new ArrayList<>();
        try {
            File f = new File(fileName);
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(f));
            while (bufferedReader.ready()) {
                String line = bufferedReader.readLine().trim();
                lines.add(line);
            }
            bufferedReader.close();
        } catch (Exception ex) {
            // handle the exception
            System.out.println(ex);
        }
        return lines;
    }

    public static Map<String, String> loadMap(String fileName, String delimeter) {
        Map<String, String> map = new HashMap<>();
        try {
            File f = new File(fileName);
            BufferedReader bufferedReader = new BufferedReader(
                    new FileReader(f));
            while (bufferedReader.ready()) {
                String[] items = bufferedReader.readLine().trim().split(delimeter);
                if(items.length >= 2)
                    map.put(items[0], items[1]);
            }
            bufferedReader.close();
        } catch (Exception ex) {
            // handle the exception
            System.out.println(ex);
        }
        return map;
    }

    public static ArrayList<String> getAllTokens(String fileName) {
        // getting all individual tokens
        ArrayList<String> tokens = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File(fileName));
            while (scanner.hasNext()) {
                String token = scanner.next();
                tokens.add(token);
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return tokens;
    }

    public static ArrayList<String> getAllLinesList(String fileName) {
        // reading items as list
        String[] items = getAllLines(fileName);
        return new ArrayList<>(Arrays.asList(items));
    }

    public static ArrayList<Integer> getAllLinesInt(String fileName) {
        ArrayList<String> lines = getAllLinesOptList(fileName);
        ArrayList<Integer> temp = new ArrayList<>();
        for (String line : lines) {
            if(line.trim().isEmpty())continue;
            temp.add(Integer.parseInt(line.trim()));
        }
        return temp;
    }

    public static String downloadURL(String issueURL) {
        // downloading the issue URL
        String content = new String();
        try {
            URL u = new URL(issueURL);
            HttpURLConnection connection = (HttpURLConnection) u
                    .openConnection();
            System.out.println(connection.getResponseMessage());
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        connection.getInputStream()));
                String line = null;
                while ((line = br.readLine()) != null) {
                    content += line + "\n";
                }
                // System.out.println(content);
            }
        } catch (Exception exc) {
            // handle the exception
            System.out.println(exc);
        }
        return content;
    }
}
