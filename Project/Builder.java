import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class Builder {
    private static Strategy strategy;

    public static void main(String args[]) {

        try {
            // sets error's print location to log.txt in ErrorLogs
            writeFile("Project/ErrorLogs/log.txt", null);
            PrintStream err = new PrintStream(new FileOutputStream("Project/ErrorLogs/log.txt"));
            System.setErr(err);

            // begin site building process
            buildSite();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void buildSite() throws Exception {
        // this string will be specified in config.toml later on
        String config = readFile("Project/config.toml");
        System.out.println(config);
        // index of THEME_NAME in the config file.
        int THEME_NAMEindex = config.indexOf("THEME_NAME");
        if (-1 == THEME_NAMEindex) {
            throw new Exception("Failed to find \"THEME_NAME\" in the config file.");
        }
        // index of the first " after THEME_NAME
        int kesme1Index = config.indexOf('"', THEME_NAMEindex);
        // selected theme's name
        // "Project/Themes/" + themeName + "/" + themeName + ".css"
        String themeName = config.substring(kesme1Index + 1, config.indexOf('"', kesme1Index + 1));
        if (!(new File("Project/Themes/" + themeName + "/" + themeName + ".css")).exists()) {
            throw new Exception("Selected theme could not be found in Themes folder.");
        }
        // [i][0] - file name, [i][1] file content
        // String[][] contents = parseContentFiles("Project/Content/Texts/");
        String[][] templates = parseContentFiles("Project/Templates/");

        for (int i = 0; 0 != i; i++) {
            ;
            writeFile("Project/Output/" + templates[i][0], makeFile(templates[i], config).toString());
        }

        throw new Exception("buildSite FONKSİYONU HAZIR DEĞİL");
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

    // replaces file[1]'s with the appropriate parts in config and returns the
    // result as a StringBuilder
    static StringBuilder makeFile(String[] file, String config) throws Exception {
        // properly made file content
        StringBuilder newFile = new StringBuilder(file[1]);
        // figure out file name
        String fileName = (file[0].toUpperCase()).substring(0, file[0].indexOf('.'));
        int index = config.indexOf(fileName) + 1;
        index = config.indexOf('\n', index) + 1;

        while (-1 != index) {
            // take lines one by one
            String line = config.substring(index, config.indexOf('\n', index));
            String toBeChanged = config.substring(index, config.indexOf(':', index));
            toBeChanged = "{{ " + toBeChanged + " }}";

            if (config.indexOf('-') > config.indexOf('\"')) {
                // handle array parts

                // <a href="{{ SOCIAL_ICON }}"><img src="{{ SOCIAL_LINKS }}"
                // style="width:2rem;height:2rem;"></a>

            } else {
                // handle non-array parts
                int lineIndex = line.indexOf('"');
                String content = line.substring(lineIndex + 1, config.indexOf('"', lineIndex + 1));
                newFile.replace(newFile.indexOf(toBeChanged), newFile.indexOf(toBeChanged) + toBeChanged.length(),
                        content);
            }

            index = config.indexOf('\n', index);
        }

        throw new Exception("makeFile METODU HAZIR DEĞİL");
    }

    // takes current file's content, the part that will be changed and the new part
    // that will be placed instead of it. Then replaces the part.
    public static void stringEditor(String contentName, String newContent, StringBuilder file) throws Exception {
        int index = file.indexOf(contentName);
        file.replace(index, index + contentName.length(), newContent);
    }

    // sets a Strategy using Factory class
    private static void setStrategy(String option) throws Exception {
        strategy = Factory.decideStrategy(option);
    }

    // performs the set Strategy
    private static void performStrategy(StringBuilder file, String config) throws Exception {
        strategy.makeChanges(file, config);
    }
}

abstract class Strategy {
    String option;

    abstract void makeChanges(StringBuilder file, String config) throws Exception;
}

class NavbarStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        // TODO Auto-generated method stub
    }
}

class SocialLinksStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        // TODO Auto-generated method stub
    }
}

class ThemeNameStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        // TODO Auto-generated method stub
    }
}

class NonArrayStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        // TODO Auto-generated method stub
    }
}

class Factory {
    // factory to decide which strategy is selected in which scenario
    static Strategy decideStrategy(String option) throws Exception {
        Strategy strategy;
        switch (option) {
            case "NAV_BAR_LINKS":
                strategy = new NavbarStrategy();
                break;
            case "SOCIAL_LINKS":
                strategy = new SocialLinksStrategy();
                break;
            case "THEME_NAME":
                strategy = new ThemeNameStrategy();
            default:
                strategy = new NonArrayStrategy();
        }
        strategy.option = option;
        return strategy;
    }
}