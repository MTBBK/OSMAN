import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Builder {
    public static void main(String arg[]) {

        try {
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

    static String[][] parseContentFiles(String folderName) throws IOException {
        File folder = new File(folderName);

        // throw an IOException if folderName is not an existing folder's path
        if (!folder.isDirectory()) {
            throw new IOException("Folder \"" + folderName + "\" Could Not Be Found\n");
        }

        File[] files = folder.listFiles();

        // throw an IOException if the folder is empty
        if (null == files || 0 == files.length) {
            throw new IOException("Folder \"" + folderName + "\" Is Empty\n");
        }

        String[][] fileContents = new String[files.length][2]; // [i][0] - dosyanın adı, [i][1] - dosyanın içeriği

        // reads all files and puts them in a string array
        for (int i = 0; i < files.length; i++) {

            fileContents[i][0] = files[i].getName();
            fileContents[i][1] = readFile(folderName + fileContents[i][0]);

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

    // takes a file name and file's text contents and creates an output in "Output"
    // folder
    static void writeFile(String fileName, String fileContent) throws IOException {
        // StringBuilder sb = new StringBuilder(fileContent);
        // Bu niye var bilmiyom

        File outputFile = new File("Project/Output/" + fileName);
        if (!outputFile.exists() && !outputFile.createNewFile()) {
            throw new IOException("Could Not Make File \"" + fileName + "\" On Output Folder");
        }
        FileWriter fw = new FileWriter(outputFile);

        try {
            fw.write(fileContent);
            fw.flush();
        } catch (IOException e) {
            throw new IOException("Could Not Write File \"" + fileName + "\" On Output Folder");
        } finally {
            fw.close();
        }
    }

    static StringBuilder makeBaseFile(String[][] templates, String style) throws IOException {
        int baseIndex = -1;
        for (int i = 0; i < templates.length; i++) {
            if (templates[i][0].equals("base.html")) {
                baseIndex = i;
                break;
            }
        }

        if (-1 == baseIndex) {
            throw new IOException("base.html could not be found in Project/Templates/\n");
        }

        StringBuilder baseSB = new StringBuilder(templates[baseIndex][1]);

        StringBuilder themePath = new StringBuilder("Themes/");
        themePath.append(style);
        themePath.append("/");
        themePath.append(style);
        themePath.append(".css");

        String themeName = "{{ THEME_NAME }}";
        int styleIndex = baseSB.indexOf(themeName);
        baseSB.replace(styleIndex, styleIndex + themeName.length(), themePath.toString());

        return baseSB;
    }
}
