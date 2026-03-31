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
        StringBuilder sbFile = new StringBuilder(file[1]);
        int index = 0;

        if (-1 != file[0].indexOf("base")) { // for base
            index = config.indexOf("BASE");
        } else if (-1 != file[0].indexOf("index")) { // for index
            index = config.indexOf("INDEX");
        } else { // for unknown templates
            // cut file[0] from 0 to first '.' to remove extension and make it all uppercase
            // then look for it in config
            index = config.indexOf(file[0].substring(0, file[0].indexOf('.')).toUpperCase());
            if (-1 == index) {
                throw new Exception("Could not find the mention in the config for the file \"" + file[0] + "\"");
            }
        }

        index = config.indexOf('\n', index) + 1;
        while (-1 != index) {
            int nextLine = config.indexOf('\n', index) + 1;
            int nextDots = config.indexOf(config.indexOf(':', index));

            // check if there is a variable to read and skip to the next iteration if there
            // isnt
            if (nextDots > nextLine) {
                index = nextLine;
                continue;
            }

            String option = config.substring(index, nextDots);
            setStrategy(option);
            performStrategy(sbFile, config);
            index = nextLine;
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
        if (null == strategy) {
            throw new Exception("Failed to set a Strategy.");
        }
    }

    // performs the set Strategy
    private static void performStrategy(StringBuilder file, String config) throws Exception {
        strategy.makeChanges(file, config);
        strategy = null;
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
        // <a href="{{ SOCIAL_ICONS }}"><img src="{{ SOCIAL_LINKS }}"
        // style="width:2rem;height:2rem;"></a>

        int lIndex = config.indexOf('\n', config.indexOf("SOCIAL_LINKS")) + 1;
        int iIndex = config.indexOf('\n', config.indexOf("SOCIAL_ICONS")) + 1;

        if (-1 == lIndex) {
            throw new Exception("Could not find SOCIAL_LINKS in config.toml");
        }
        if (-1 == iIndex) {
            throw new Exception("Could not find SOCIAL_ICONS in config.toml");
        }

        boolean nextExists = true;

        String sl = "{{ SOCIAL_LINKS }}";
        String si = "{{ SOCIAL_ICONS }}";

        StringBuilder result = new StringBuilder();

        while (nextExists) {
            int nextLQuote = config.indexOf('"', lIndex) + 1;
            int nextLDash = config.indexOf(config.indexOf('-', lIndex));

            int nextIQuote = config.indexOf('"', iIndex) + 1;
            int nextIDash = config.indexOf(config.indexOf('-', iIndex));

            // check if there is a variable to read and skip to the next iteration if there
            // isnt
            if (nextLDash > nextLQuote || -1 == nextLDash) {
                nextExists = false;
                continue;
            }
            if (nextIDash > nextIQuote || -1 == nextIDash) {
                throw new Exception("SOCIAL_LINKS and SOCIAL_ICONS have different amount of elements");
            }

            StringBuilder ref = new StringBuilder(
                    "<a href=\"{{ SOCIAL_ICONS }}\"><img src=\"{{ SOCIAL_LINKS }}\" style=\"width:2rem;height:2rem;\"></a>");

            int firstLQuote = config.indexOf('"', nextLDash) + 1;
            String lValue = config.substring(firstLQuote, config.indexOf('"', firstLQuote));

            int firstIQuote = config.indexOf('"', nextIDash) + 1;
            String iValue = config.substring(firstIQuote, config.indexOf('"', firstIQuote));

            lIndex = nextLQuote;
            iIndex = nextIQuote;

            ref.replace(ref.indexOf(si), ref.indexOf(si) + si.length(), iValue);
            ref.replace(ref.indexOf(sl), ref.indexOf(sl) + sl.length(), lValue);

            result.append(ref);
            result.append('\n');
        }

        file.replace(file.indexOf("SOCIAL_LINKS"), file.indexOf("SOCIAL_LINKS") + 12, result.toString());
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
        int firstQuote = config.indexOf('"', config.indexOf(option)) + 1;
        String value = config.substring(firstQuote, config.indexOf('"', firstQuote));
        option = "{{ " + option + " }}";
        file.replace(file.indexOf(option), file.indexOf(option) + option.length(), value);
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
                break;
            case "":
                throw new Exception("Could not detect the next config variable.");
            default:
                strategy = new NonArrayStrategy();
        }
        strategy.option = option;
        return strategy;
    }
}