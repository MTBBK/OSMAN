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
            writeFile("OSMAN/Project/ErrorLogs/errorLog.txt", null);
            PrintStream err = new PrintStream(new FileOutputStream("OSMAN/Project/ErrorLogs/errorLog.txt"));
            System.setErr(err);
            writeFile("OSMAN/Project/ErrorLogs/log.txt", null);
            PrintStream out = new PrintStream(new FileOutputStream("OSMAN/Project/ErrorLogs/log.txt"));
            System.setOut(out);

            // print to confirm that System.err.println functions properly
            System.err.println("Error's Stack Trace Will Be Below:");

            // begin site building process
            buildSite();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static void buildSite() throws Exception {
        System.out.println("buildFile: Begin.");
        // this string will be specified in config.toml later on
        String config = readFile("OSMAN/Project/config.toml");
        System.out.println("buildSite: Successfully read the config.");

        // [i][0] - file name, [i][1] file content
        // String[][] contents = parseContentFiles("OSMAN/Project/Content/Texts/");
        String[][] templates = parseContentFiles("OSMAN/Project/Templates/");
        System.out.println("buildSite: Successfully run parseContentFiles(\"OSMAN/Project/Templates/\").");

        for (int i = 0; templates.length != i; i++) {
            if (templates[i][0].equals("base.html")) {
                writeFile("OSMAN/Project/Output/" + templates[i][0], makeFile(templates[i], config).toString());
                System.out.println("buildSite: Successfully run makeFile on \"" + templates[i][0]
                        + "\" and printed its output to Output folder.");
            }
        }
        System.out.println("buildFile: End.");
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
        System.out.println("\nmakeFile: Begin.");
        StringBuilder sbFile = new StringBuilder(file[1]);
        int index = 0;

        if (-1 != file[0].indexOf("base")) { // for base
            index = config.indexOf("BASE");
            System.out.println("makeFile: Successfully found the start of base file's part.");
        } else if (-1 != file[0].indexOf("index")) { // for index
            index = config.indexOf("INDEX");
            System.out.println("makeFile: Successfully found the start of index file's part.");
        } else { // for unknown templates
            // cut file[0] from 0 to first '.' to remove extension and make it all uppercase
            // then look for it in config
            index = config.indexOf(file[0].substring(0, file[0].indexOf('.')).toUpperCase());
            System.out.println("makeFile: Successfully found the start of file's part.");
            if (-1 == index) {
                throw new Exception("Could not find the mention in the config for the file \"" + file[0] + "\"");
            }
        }

        index = config.indexOf('\n', index) + 1;
        int nextLine;
        int nextColon = 0;
        int nextDash;
        while (0 != index && -1 != nextColon) {
            nextLine = config.indexOf('\n', index) + 1;
            nextDash = config.indexOf('-', index);
            nextColon = config.indexOf(':', index);
            // check if there is a variable to read and skip to the next iteration if so
            if ((nextColon > nextDash && -1 != nextDash) || nextColon > nextLine || -1 == nextColon) {
                index = nextLine;
                System.out.println("makeFile: Successfully skipped to the next line.");
            } else {
                String option = config.substring(index, nextColon);
                System.out.println("makeFile: Successfully found the option: \"" + option + "\".");
                setStrategy(option);
                System.out.println("makeFile: Successfully performed setStrategy(\"" + option + "\") in the file.");
                performStrategy(sbFile, config);
                System.out.println("makeFile: Successfully performed performStrategy(\"" + option + "\") in the file.");
                index = nextLine;
                System.out.println("makeFile: Successfully finished changing \"" + option + "\" in the file.");
            }
        }
        System.out.println("makeFile: Successfully finished making the file.");
        System.out.println("makeFile: End.\n");
        return sbFile;
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
        System.out.println("\nNavbarStrategy: Begin.");
        System.out.println("NavbarStrategy: NavbarStrategy doesn't do anything yet.");
        System.out.println("NavbarStrategy: End.\n");
    }
}

class SocialLinksStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        // <a href="{{ SOCIAL_ICONS }}"><img src="{{ SOCIAL_LINKS }}"
        // style="width:2rem;height:2rem;"></a>
        System.out.println("\nSocialLinksStrategy: Begin.");
        System.out.println("SocialLinksStrategy: Does not work as intended.");

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
        System.out.println("\nSocialLinksStrategy: End.\n");
    }
}

class ThemeNameStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        System.out.println("\nThemeNameStrategy: Begin.");
        int THEME_NAMEindex = config.indexOf(option);
        if (-1 == THEME_NAMEindex) {
            throw new Exception("Failed to find \"THEME_NAME\" in the config file.");
        }
        // index of the first " after THEME_NAME
        int kesme1Index = config.indexOf('"', THEME_NAMEindex);
        // selected theme's name
        // "Project/Themes/" + themeName + "/" + themeName + ".css"
        String themeName = config.substring(kesme1Index + 1, config.indexOf('"', kesme1Index + 1));
        themeName = "OSMAN/Project/Themes/" + themeName + "/" + themeName + ".css";
        if (!(new File(themeName)).exists()) {
            throw new Exception("Selected theme could not be found in Themes folder.");
        }
        option = "{{ " + option + " }}";
        Builder.stringEditor(option, themeName, file);
        System.out.println("ThemeNameStrategy: End.\n");
    }
}

class NonArrayStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        System.out.println("\nNonArrayStrategy: Begin.");
        int firstQuote = config.indexOf('"', config.indexOf(option)) + 1;
        String value = config.substring(firstQuote, config.indexOf('"', firstQuote));
        option = "{{ " + option + " }}";
        int fOptionIndex = file.indexOf(option);
        if (-1 == fOptionIndex) {
            System.out.println(
                    "NonArrayStrategy: Could not find the option \"" + option + "\" in the file, it will be skipped.");
            System.out.println("NonArrayStrategy: End.\n");
            return;
        } else {
            file.replace(fOptionIndex, fOptionIndex + option.length(), value);
            System.out.println("NonArrayStrategy: End.\n");
        }

    }
}

class Factory {
    // factory to decide which strategy is selected in which scenario
    static Strategy decideStrategy(String option) throws Exception {
        System.out.println("\nFactory: Begin.");

        Strategy strategy;
        switch (option) {
            case "NAV_BAR_LINKS":
                strategy = new NavbarStrategy();
                System.out.println("Factory: Chose NavbarStrategy.");
                break;
            case "SOCIAL_LINKS":
                strategy = new SocialLinksStrategy();
                System.out.println("Factory: Chose SocialLinksStrategy.");
                break;
            case "THEME_NAME":
                strategy = new ThemeNameStrategy();
                System.out.println("Factory: Chose ThemeNameStrategy.");
                break;
            case "":
                throw new Exception("Could not detect the next config variable.");
            default:
                strategy = new NonArrayStrategy();
                System.out.println("Factory: Chose NonArrayStrategy.");
        }
        strategy.option = option;
        System.out.println("Factory: End.\n");
        return strategy;
    }
}