import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;

public class Builder {
    public static void main(String arg[]) {

        try {
            // sets error's print location to log.txt in ErrorLogs
            writeFile("Project/ErrorLogs/log.txt", null);
            PrintStream err = new PrintStream(new FileOutputStream("Project/ErrorLogs/log.txt"));
            System.setErr(err);

            buildSite();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void buildSite() throws Exception {
        // this string will be specified in config.toml later on
        String theme = "Space";
        // [i][0] - file name, [i][1] file content
        String[][] contents = parseContentFiles("Project/Content/Texts/");
        String[][] templates = parseContentFiles("Project/Templates/");
        // String[][] themes = parseContentFiles("Project/Themes/" + theme + "/");

        for (int i = 0; i < templates.length; i++) {
            writeFile(templates[i][0], templates[i][1]);
        }
    }

    static String[][] parseContentFiles(String folderPath) throws IOException {
        File folder = new File(folderPath);

        // throw an IOException if folderPath is not an existing folder's path
        if (!folder.isDirectory()) {
            throw new IOException("Folder \"" + folderPath + "\" Could Not Be Found\n");
        }

        File[] files = folder.listFiles();

        // throw an IOException if the folder is empty
        if (null == files || 0 == files.length) {
            throw new IOException("Folder \"" + folderPath + "\" Is Empty\n");
        }

        String[][] fileContents = new String[files.length][2]; // [i][0] - dosyanın adı, [i][1] - dosyanın içeriği

        // reads all files and puts them in a string array
        for (int i = 0; i < files.length; i++) {

            fileContents[i][0] = files[i].getName();
            fileContents[i][1] = readFile(folderPath + fileContents[i][0]);

        }

        return fileContents;
    }

    // takes text file's name and reads text file and return its contents as a sting
    static String readFile(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (null != line) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }

            return sb.toString();
        } catch (IOException e) {
            throw new IOException("Failed To Read File " + filePath);
        } finally {
            br.close();
        }
    }

    // takes a file path and file's text contents and creates an output in "Output"
    // folder. WORKS
    static void writeFile(String filePath, String fileContent) throws IOException {
        // Checks if the file path exists for the file.
        // Creates required folders ifthe don't already exist.
        // Currently requires filepaths to not end with a '/' so that
        // file's name can be extracted.
        File folder = new File(filePath.substring(0, filePath.lastIndexOf('/')));
        if (!folder.isDirectory()) {
            folder.mkdirs();
        }

        // checks if the file exists, tries to make a new file and throws an exception
        // if it fails.
        File file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("File: " + filePath + " could not be created on the given path.");
        }

        // creates an empty file if the content is null
        if (null == fileContent) {
            return;
        }

        // tries to write "fileContent" into the created file.
        // throws an exception if it fails.
        FileWriter fw = new FileWriter(filePath);
        try {
            fw.write(fileContent);
            fw.flush();
        } catch (IOException e) {
            throw new IOException("File: " + filePath + " could not be written over.");
        } finally {
            fw.close();
        }
    }

    static StringBuilder makeFile(String[] file, String config) throws Exception {
        throw new Exception("makeFile METODUNU DAHA YAPMADIM");
    }

    // takes current file's content, the part that will be changed and the new part
    // that will be placed instead of it. Then replaces the part.
    static void stringEditor(String contentName, String newContent, StringBuilder file) throws Exception {
        int index = file.indexOf(contentName);
        file.replace(index, index + contentName.length(), newContent);
    }
}
