import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Builder {
    private static Strategy strategy;

    public static void main(String args[]) {
        long startTime = System.currentTimeMillis();
        try {
            // sets error's print location to log.txt in ErrorLogs
            writeFile("OSMAN/Project/ErrorLogs/errorLog.txt", null);
            PrintStream err = new PrintStream(new FileOutputStream("OSMAN/Project/ErrorLogs/errorLog.txt"));
            System.setErr(err);
            writeFile("OSMAN/Project/ErrorLogs/log.txt", null);
            PrintStream out = new PrintStream(new FileOutputStream("OSMAN/Project/ErrorLogs/log.txt"));
            System.setOut(out);
            System.out.println("main: Begin.");

            // print to confirm that System.err.println functions properly
            System.err.println("Error's Stack Trace Will Be Below:");

            // create an Output folder in the case that it doesn't exist.
            File folder = new File("OSMAN/Project/Output/");
            if (!folder.exists()) {
                folder.mkdir();
            }

            // clean the Output/Images folder to be able to delete Output's contents
            folder = new File("OSMAN/Project/Output/Images");
            if (folder.isDirectory()) {
                for (File file : folder.listFiles()) {
                    if (file.delete()) {
                        System.out.println(
                                "main: Successfully deleted file \"" + file.getName() + "\" in Output/Images folder.");
                    } else {
                        throw new IOException("Could not clear the OSMAN/Project/Output/Images folder");
                    }
                }
            }

            // clean the Output folder for the new output
            folder = new File("OSMAN/Project/Output");
            for (File file : folder.listFiles()) {
                // ignore .gitignore because duh.
                if (file.getName().equals(".gitignore")) {
                    continue;
                }

                if (file.delete()) {
                    System.out.println("main: Successfully deleted file \"" + file.getName() + "\" in Output folder.");
                } else {
                    throw new IOException("Could not clear the OSMAN/Project/Output folder");
                }
            }

            // begin site building process
            buildSite();

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(
                "main: Finished the process in " + (System.currentTimeMillis() - startTime) + " milliseconds.");
        System.out.println("main: End.");
    }

    static void buildSite() throws Exception {
        System.out.println("\nbuildFile: Begin.");

        String config = readFile("OSMAN/Project/config.osman");
        System.out.println("buildSite: Successfully read the config.");

        // [i][0] - file name, [i][1] file content
        // String[][] contents = parseContentFiles("OSMAN/Project/Content/Texts/");
        String[][] templates = parseContentFiles("OSMAN/Project/Templates/");
        System.out.println("buildSite: Successfully run parseContentFiles(\"OSMAN/Project/Templates/\").");

        String[][] textContent = parseContentFiles("OSMAN/Project/Content/Texts/");
        System.out.println("buildSite: Successfully run parseContentFiles(\"OSMAN/Project/Content/Texts/\").");

        // copy images to the Output/Images folder
        if ((new File("OSMAN/Project/Content/Images/")).exists()) {
            File imagesCFolder = new File("OSMAN/Project/Content/Images/");

            File imagesOFolder = new File("OSMAN/Project/Output/Images/");
            // check if the Output/Images folder exists and make one if not.
            if (!imagesOFolder.exists()) {
                imagesOFolder.mkdirs();
            }

            for (String file : imagesCFolder.list()) {
                Files.copy(Paths.get("OSMAN/Project/Content/Images/" + file),
                        Paths.get("OSMAN/Project/Output/Images/" + file));
                System.out.println("buildSite: Successfully copied image \"" + file + "\".");
            }
            System.out.println("buildSite: Successfully copied all images.");
        }

        StringBuilder base = new StringBuilder();
        StringBuilder index = new StringBuilder();
        String page = "";

        for (int i = 0; templates.length > i; i++) {
            if (templates[i][0].equals("base.html")) {
                base = makeFile(templates[i], config);
                System.out.println("buildSite: Successfully run makeFile on \"" + templates[i][0]
                        + "\".");
            }
            if (templates[i][0].equals("index.html")) {
                index = makeFile(templates[i], config);
                System.out.println("buildSite: Successfully run makeFile on \"" + templates[i][0]
                        + "\".");
            }
            if (templates[i][0].equals("page.html")) {
                page = templates[i][1];
                System.out.println("buildSite: Successfully grabbed \"" + templates[i][0] + "\".");
            }
        }

        // TODO: we need to get page titles, page dates, page tags and page contents'
        // first 20 words. to put them in the stupid index posts section thingamabob

        // Sort textContent based on POST_DATEs.
        Arrays.sort(textContent, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                int first1Quote = o1[1].indexOf('"', o1[1].indexOf("POST_DATE")) + 1;
                String[] date1 = o1[1].substring(first1Quote, o1[1].indexOf('"', first1Quote)).split("/");
                int first2Quote = o2[1].indexOf('"', o2[1].indexOf("POST_DATE")) + 1;
                String[] date2 = o2[1].substring(first2Quote, o2[1].indexOf('"', first2Quote)).split("/");
                int comp = -1;
                if (3 != date1.length) {
                    return -1;
                } else if (3 != date2.length) {
                    return 1;
                }
                for (int i = 2; i > -1; i--) {
                    comp = date1[i].compareTo(date2[i]);
                    if (0 != comp) {
                        return comp;
                    }
                }
                return comp;
            }
        });

        // --------------------------------------------------------------------------------------------
        // get pages' dates begin
        String[] pageDates = new String[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
            if (textContent[i][1].indexOf("POST_DATE") == -1) {
                pageDates[i] = "";
            } else {
                int first1Quote = textContent[i][1].indexOf('"', textContent[i][1].indexOf("POST_DATE")) + 1;
                pageDates[i] = textContent[i][1].substring(first1Quote, textContent[1][1].indexOf('"', first1Quote));
            }
        }
        // get pages' dates end

        // --------------------------------------------------------------------------------------------
        // get pages' titles begin
        String[] pageTitles = new String[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
            if (textContent[i][1].indexOf("POST_TITLE") == -1) {
                pageTitles[i] = "";
            } else {
                int first1Quote = textContent[i][1].indexOf('"', textContent[i][1].indexOf("POST_TITLE")) + 1;
                pageTitles[i] = textContent[i][1].substring(first1Quote, textContent[1][1].indexOf('"', first1Quote));
            }
        }
        // get pages' titles end

        // --------------------------------------------------------------------------------------------
        // get pages' tags begin
        String[][] pageTags = new String[textContent.length][];
        for (int j = 0; j < textContent.length; j++) {
            String option = "POST_TAGS";
            ArrayList<String> list = new ArrayList<>();
            String configPlus = textContent[j][1];
            int lIndex = configPlus.indexOf('\n', configPlus.indexOf(option)) + 1;

            if (-1 == lIndex) {
                System.out.println(
                        "buildSite: Could not find \"" + option + "\" in the text content, it will be skipped.");
                pageTags[j] = new String[0];
                return;
            }

            boolean nextExists = true;

            while (nextExists) {
                int nextLQuote = configPlus.indexOf('"', lIndex) + 1;
                int nextLDash = configPlus.indexOf('-', lIndex);
                int nextLLine = configPlus.indexOf('\n', lIndex);

                // check if there is a variable to read and skip to the next iteration if there
                // isnt

                if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                    nextExists = false;
                    continue;
                }

                String lValue = configPlus.substring(nextLQuote, configPlus.indexOf('"', nextLQuote));

                lIndex = configPlus.indexOf('\n', lIndex) + 1;
                lIndex = nextLLine + 1;

                list.add(lValue);
            }
            pageTags[j] = new String[list.size()];
            pageTags[j] = (list.toArray(pageTags[j]));
        }
        // get pages' tags end

        // --------------------------------------------------------------------------------------------
        // get pages' summaries begin
        String[] pageSummary = new String[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
            if (-1 == textContent[i][1].indexOf("POST_CONTENT")) {
                pageSummary[i] = "";
            } else {
                int firstNextLine = textContent[i][1].indexOf('\n', textContent[i][1].indexOf("POST_CONTENT")) + 1;
                String content = textContent[i][1].substring(firstNextLine);
                int spaces = 0;
                int spaceIndex = 0;
                while (-1 != spaceIndex && 20 > spaces) {
                    spaces++;
                    spaceIndex = content.indexOf(' ', spaceIndex + 1);
                }
                if (-1 == spaceIndex) {
                    pageSummary[i] = content;
                } else {
                    pageSummary[i] = content.substring(0, spaceIndex);
                }
            }

        }
        // get pages' summaries end

        // POST_LIST template
        String postListTemplate = "<div class=\"post-card\">\n" + //
                "                    <a href=\"{{ POST_PATH }}\" class=\"post-card-title\">{{ POST_TITLE }}</a>\n" + //
                "                    <p class=\"post-card-excerpt\">{{ POST_SUMMARY }}</p>\n" + //
                "                    <div class=\"post-card-meta\">{{ POST_DATE }}</div>\n" + //
                "                    <div class=\"post-card-categories\">{{ POST_TAGS }}</div>\n" + //
                "                </div>";

        // POST_LIST maker start
        StringBuilder[] postLists = new StringBuilder[textContent.length];
        for (int i = 0; i < postLists.length; i++) {
            postLists[i] = new StringBuilder(postListTemplate);
            String fileName = textContent[i][0].substring(0, textContent[i][0].indexOf(".md"));
            stringEditor("{{ POST_PATH }}", fileName + ".html", postLists[i]);
            stringEditor("{{ POST_TITLE }}", pageTitles[i], postLists[i]);
            stringEditor("{{ POST_DATE }}", pageDates[i], postLists[i]);
            stringEditor("{{ POST_SUMMARY }}", pageSummary[i], postLists[i]);
            StringBuilder pTags = new StringBuilder();
            for (int j = 0; j < pageTags[i].length; j++) {
                pTags.append(pageTags[i][j]);
                if (j != pageTags[i].length - 1) {
                    pTags.append(", ");
                }
            }
            stringEditor("{{ POST_TAGS }}", pTags.toString(), postLists[i]);
        }
        // POST_LIST maker end

        // handling page.htmls begin
        System.out.println("buildSite: Starting to make site pages.");
        StringBuilder[] pages = new StringBuilder[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
            String[] arr = { textContent[i][0], page };
            pages[i] = new StringBuilder(base);
            stringEditor("{{ CONTENT }}", makeFile(arr, textContent[i][1]).toString(), pages[i]);

            if (0 != i) {
                // <a href="ElHamraSarayiGezisi01.html">Previous Post</a>
                stringEditor(
                        "{{ PREVIOUS_POST }}", "<a href=\"./"
                                + textContent[i - 1][0].substring(0, textContent[i - 1][0].indexOf(".md"))
                                + ".html\">Previous Post</a>",
                        pages[i]);
            } else {
                stringEditor(
                        "{{ PREVIOUS_POST }}", "<a href=\"./index.html\">Previous Post</a>", pages[i]);
            }

            if (textContent.length - 1 != i) {
                // <a href="ElHamraSarayiGezisi01.html">Next Post</a>
                stringEditor(
                        "{{ NEXT_POST }}", "<a href=\"./"
                                + textContent[i + 1][0].substring(0, textContent[i + 1][0].indexOf(".md"))
                                + ".html\">Next Post</a>",
                        pages[i]);
            } else {
                stringEditor(
                        "{{ NEXT_POST }}", "<a href=\"./index.html\">Next Post</a>", pages[i]);

            }

            System.out.println("buildSite: Successfully made \"" + textContent[i][0] + "\" page.");

        }
        System.out.println("buildSite: Successfully made all of the site pages.");
        // handling page.htmls end

        // handle index.html begin
        StringBuilder indexPage = new StringBuilder(base);
        stringEditor("{{ CONTENT }}", index.toString(), indexPage);
        System.out.println("buildSite: Successfully merged base and index.");

        stringEditor("{{ TOTAL_POSTS_COUNT }}", "Total Posts: " + pages.length, indexPage);

        // make POST_LIST for index begin.
        {
            StringBuilder megaPostList = new StringBuilder();
            for (int i = postLists.length - 1; i > 0; i--) {
                megaPostList.append(postLists[i]);
            }
            stringEditor("{{ POST_LIST }}", megaPostList.toString(), indexPage);
        }
        // make POST_LIST for index end.

        // make TAG_CLOUD for index.html begin.
        {
            StringBuilder tagCloud = new StringBuilder();
            ArrayList<String> existingTags = new ArrayList<>();
            for (int i = 0; i < pageTags.length; i++) {
                for (int j = 0; j < pageTags[i].length; j++) {
                    boolean alreadyExists = false;
                    for (int k = 0; k < existingTags.size() && !alreadyExists; k++) {
                        if (existingTags.get(k).equals(pageTags[i][j])) {
                            alreadyExists = true;
                        }
                    }
                    if (!alreadyExists)
                        existingTags.add(pageTags[i][j]);
                }

            }
            String tagCloudTemplate = "<a href=\"#\">{{ POST_TAGS }}</a>\t\t\t\n";
            for (int i = 0; i < existingTags.size(); i++) {
                StringBuilder toBeAdded = new StringBuilder(tagCloudTemplate);
                stringEditor("{{ POST_TAGS }}", existingTags.get(i), toBeAdded);
                tagCloud.append(toBeAdded);
            }
            stringEditor("{{ TAG_CLOUD }}", tagCloud.toString(), indexPage);
        }
        // make TAG_CLOUD for index.html end.

        writeFile("OSMAN/Project/Output/index.html", indexPage.toString());
        // handle index.html end

        System.out.println("buildSite: Successfully made \"index.html\".");

        for (int i = 0; i < textContent.length; i++) {
            String fileName = textContent[i][0].substring(0, textContent[i][0].indexOf(".md"));
            writeFile("OSMAN/Project/Output/" + fileName + ".html", pages[i].toString());
            System.out.println("buildSite: Successfully made \"" + fileName + "\".");
        }

        System.out.println("buildFile: End.\n");
    }

    static String[][] parseContentFiles(String folderPath) throws IOException {
        File folder = new File(folderPath);

        // throw an IOException if folderPath is not an existing folder's path
        if (!folder.isDirectory()) {
            throw new IOException("Folder \"" + folderPath + "\" Could Not Be Found\n");
        }

        File[] files = folder.listFiles();

        // return an empty array if the folder is empty and send a log about it.
        if (null == files || 0 == files.length) {
            System.out.println("parseContentFiles: Folder \"" + folderPath + "\" is empty.");
            return new String[0][0];
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
    public static String readFile(String filePath) throws IOException {
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
    public static void writeFile(String filePath, String fileContent) throws IOException {
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
            System.out.println("makeFile: Successfully found the start of \"" + file[0] + "\" file's part.");
        } else if (-1 != file[0].indexOf("index")) { // for index
            index = config.indexOf("INDEX");
            System.out.println("makeFile: Successfully found the start of \"" + file[0] + "\" file's part.");
        } else { // for unknown templates
            // cut file[0] from 0 to first '.' to remove extension and make it all uppercase
            // then look for it in config
            index = config.indexOf(file[0].substring(0, file[0].indexOf('.')).toUpperCase());
            System.out.println("makeFile: Successfully found the start of \"" + file[0] + "\" file's part.");
            if (-1 == index) {
                System.out.println(
                        "makeFile: Failed to find the start of \"" + file[0]
                                + "\" file's part. Will begin from the top of the file");
            }
        }

        if (-1 != index) {
            index = config.indexOf('\n', index);
        } else {
            index = -1;
        }
        int nextLine;
        int nextColon = 0;
        int nextDash;
        do {
            nextLine = config.indexOf('\n', index + 1);
            nextDash = config.indexOf('-', index + 1);
            nextColon = config.indexOf(':', index + 1);
            // check if there is a variable to read and skip to the next iteration if so
            if ((nextColon > nextDash && -1 != nextDash) || nextColon > nextLine || -1 == nextColon) {
                index = nextLine;
                System.out.println("makeFile: Successfully skipped to the next line.");
            } else {
                String option = config.substring(index + 1, nextColon);
                System.out.println("makeFile: Successfully found the option: \"" + option + "\".");
                setStrategy(option);
                System.out.println("makeFile: Successfully executed setStrategy(\"" + option + "\") on the file.");
                performStrategy(sbFile, config);
                System.out.println("makeFile: Successfully executed performStrategy(\"" + option + "\") on the file.");
                index = nextLine;
                System.out.println("makeFile: Successfully finished changing \"" + option + "\" in the file.");
            }
        } while (-1 != index && -1 != nextColon);
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

        String sl = "{{ " + option + " }}";

        if (-1 == file.indexOf(sl)) {
            System.out.println(
                    "NavbarStrategy: Could not find \"" + option + "\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf(option)) + 1;
        int iIndex = config.indexOf(',', lIndex);

        if (-1 == lIndex) {
            System.out.println(
                    "NavbarStrategy: Could not find \"" + option + "\" in the config, it will be skipped.");
            Builder.stringEditor("{{ " + option + " }}", "", file);
            return;
        }

        boolean nextExists = true;

        StringBuilder result = new StringBuilder();

        while (nextExists) {
            int nextLQuote = config.indexOf('"', lIndex) + 1;
            int nextLDash = config.indexOf('-', lIndex);
            int nextLLine = config.indexOf('\n', lIndex);

            int nextIQuote = config.indexOf('"', iIndex) + 1;

            // check if there is a variable to read and skip to the next iteration if there
            // isnt
            if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                nextExists = false;
                continue;
            }

            StringBuilder ref = new StringBuilder(
                    "<a href=\"{{ NAV_BAR_LINK2 }}\">{{ NAV_BAR_LINK1 }}</a>\n\t\t\t");

            String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));

            String iValue = config.substring(nextIQuote, config.indexOf('"', nextIQuote));

            lIndex = nextLLine + 1;
            iIndex = config.indexOf(',', lIndex) + 1;

            Builder.stringEditor("{{ NAV_BAR_LINK2 }}", iValue, ref);
            Builder.stringEditor("{{ NAV_BAR_LINK1 }}", lValue, ref);

            result.append(ref);
        }

        Builder.stringEditor("{{ " + option + " }}", result.toString(), file);

        System.out.println("NavbarStrategy: End.\n");
    }
}

class SocialLinksStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        // <a href="{{ SOCIAL_LINKS2 }}">{{ SOCIAL_LINKS1 }}</a>
        System.out.println("\nSocialLinksStrategy: Begin.");

        String sl = "{{ SOCIAL_LINKS }}";

        if (-1 == file.indexOf(sl)) {
            System.out.println(
                    "SocialLinksStrategy: Could not find \"SOCIAL_LINKS\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf("SOCIAL_LINKS")) + 1;
        int iIndex = config.indexOf(',', lIndex);

        if (-1 == lIndex) {
            System.out.println(
                    "SocialLinksStrategy: Could not find \"SOCIAL_LINKS\" in the config, it will be skipped.");
            Builder.stringEditor("{{ " + option + " }}", "", file);
            return;
        }

        boolean nextExists = true;

        StringBuilder result = new StringBuilder();

        while (nextExists) {
            int nextLQuote = config.indexOf('"', lIndex) + 1;
            int nextLDash = config.indexOf('-', lIndex);
            int nextLLine = config.indexOf('\n', lIndex);

            int nextIQuote = config.indexOf('"', iIndex) + 1;

            // check if there is a variable to read and skip to the next iteration if there
            // isnt
            if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                nextExists = false;
                continue;
            }

            StringBuilder ref = new StringBuilder(
                    "<a href=\"{{ SOCIAL_LINKS2 }}\">{{ SOCIAL_LINKS1 }}</a>\n\t\t\t");

            String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));

            String iValue = config.substring(nextIQuote, config.indexOf('"', nextIQuote));

            lIndex = nextLLine + 1;
            iIndex = config.indexOf(',', lIndex) + 1;

            Builder.stringEditor("{{ SOCIAL_LINKS2 }}", iValue, ref);
            Builder.stringEditor("{{ SOCIAL_LINKS1 }}", lValue, ref);

            result.append(ref);
        }

        Builder.stringEditor("{{ SOCIAL_LINKS }}", result.toString(), file);

        System.out.println("SocialLinksStrategy: End.\n");
    }
}

class SocialIconsStrategy extends Strategy {
    @Override
    void makeChanges(StringBuilder file, String config) throws Exception {
        System.out.println("\nSocialIconsStrategy: Begin.");

        String sl = "{{ " + option + " }}";

        if (-1 == file.indexOf(sl)) {
            System.out.println(
                    "SocialIconsStrategy: Could not find \"" + option + "\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf(option)) + 1;
        int iIndex = config.indexOf(',', lIndex);

        if (-1 == lIndex) {
            System.out.println(
                    "SocialIconsStrategy: Could not find \"" + option + "\" in the config, it will be skipped.");
            Builder.stringEditor("{{ " + option + " }}", "", file);
            return;
        }

        boolean nextExists = true;

        StringBuilder result = new StringBuilder();

        while (nextExists) {
            int nextLQuote = config.indexOf('"', lIndex) + 1;
            int nextLDash = config.indexOf('-', lIndex);
            int nextLLine = config.indexOf('\n', lIndex);

            int nextIQuote = config.indexOf('"', iIndex) + 1;

            // check if there is a variable to read and skip to the next iteration if there
            // isnt
            if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                nextExists = false;
                continue;
            }

            StringBuilder ref = new StringBuilder(
                    "<a href=\"{{ SOCIAL_ICONS2 }}\"><img src=\"{{ SOCIAL_ICONS1 }}\"style=\"width:2rem;height:2rem;\"></a>\n\t\t\t");

            String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));

            String iValue = config.substring(nextIQuote, config.indexOf('"', nextIQuote));

            lIndex = nextLLine + 1;
            iIndex = config.indexOf(',', lIndex) + 1;

            Builder.stringEditor("{{ SOCIAL_ICONS2 }}", iValue, ref);
            Builder.stringEditor("{{ SOCIAL_ICONS1 }}", lValue, ref);

            result.append(ref);
        }

        Builder.stringEditor("{{ " + option + " }}", result.toString(), file);

        System.out.println("SocialIconsStrategy: End.\n");
    }
}

class ThemeNameStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        System.out.println("\nThemeNameStrategy: Begin.");
        int THEME_NAMEindex = config.indexOf(option);

        if (-1 == file.indexOf("{{ " + option + " }}")) {
            System.out.println(
                    "ThemeNameStrategy: Could not find both \"THEME_NAME\" AND \"SOCIAL_LINKS\" in the file, they will be skipped.");
            return;
        }

        if (-1 == THEME_NAMEindex) {
            throw new Exception("Failed to find \"THEME_NAME\" in the config file.");
        }

        // index of the first " after THEME_NAME
        int kesme1Index = config.indexOf('"', THEME_NAMEindex);
        // selected theme's name
        // "Project/Themes/" + themeName + "/" + themeName + ".css"
        String themeName = config.substring(kesme1Index + 1, config.indexOf('"', kesme1Index + 1));
        String themePath = "OSMAN/Project/Themes/" + themeName + "/" + themeName + ".css";
        if (!(new File(themePath)).exists()) {
            throw new Exception("Selected theme could not be found in Themes folder.");
        }

        // copy theme file to Output folder
        System.out.println("ThemeNameStrategy: Starting copying \"" + themeName + ".css\" to the Output folder.");
        String themeFile = Builder.readFile(themePath);
        Builder.writeFile("OSMAN/Project/Output/" + themeName + ".css", themeFile);
        System.out.println("ThemeNameStrategy: Starting copying \"" + themeName + ".css\" to the Output folder.");

        option = "{{ " + option + " }}";
        Builder.stringEditor(option, themeName + ".css", file);
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
        if (-1 == file.indexOf(option)) {
            System.out.println(
                    "NonArrayStrategy: Could not find the option \"" + option + "\" in the file, it will be skipped.");
            System.out.println("NonArrayStrategy: End.\n");
            return;
        } else {
            do {
                Builder.stringEditor(option, value, file);
            } while (-1 != file.indexOf(option));
            System.out.println("NonArrayStrategy: End.\n");
        }

    }
}

class PostTagsStrategy extends Strategy {

    @Override
    void makeChanges(StringBuilder file, String config) throws Exception {
        System.out.println("\nPostTagsStrategy: Begin.");
        String sl = "{{ " + option + " }}";

        if (-1 == file.indexOf(sl)) {
            System.out.println(
                    "PostTagsStrategy: Could not find \"" + option + "\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf(option)) + 1;

        if (-1 == lIndex) {
            System.out.println(
                    "PostTagsStrategy: Could not find \"" + option + "\" in the config, it will be skipped.");
            Builder.stringEditor("{{ " + option + " }}", "", file);
            return;
        }

        boolean nextExists = true;

        StringBuilder result = new StringBuilder();

        while (nextExists) {
            int nextLQuote = config.indexOf('"', lIndex) + 1;
            int nextLDash = config.indexOf('-', lIndex);
            int nextLLine = config.indexOf('\n', lIndex);

            // check if there is a variable to read and skip to the next iteration if there
            // isnt
            if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                nextExists = false;
                continue;
            }

            StringBuilder ref = new StringBuilder(
                    "<a href=\"#\">{{ POST_TAGS }}</a>\n\t\t\t");

            String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));

            lIndex = config.indexOf('\n', lIndex) + 1;
            lIndex = nextLLine + 1;

            Builder.stringEditor("{{ POST_TAGS }}", lValue, ref);

            result.append(ref);
        }

        Builder.stringEditor("{{ " + option + " }}", result.toString(), file);
        System.out.println("PostTagsStrategy: End.\n");
    }

}

class PostContentStrategy extends Strategy {
    @Override
    void makeChanges(StringBuilder file, String config) throws Exception {
        System.out.println("\nPostContentStrategy: Begin.");
        int contentStart = config.indexOf('\n', config.indexOf(option)) + 1;
        String value = config.substring(contentStart);
        option = "{{ " + option + " }}";

        if (-1 == file.indexOf(option)) {
            // if the option isnt available in the file
            System.out.println(
                    "PostContentStrategy: Could not find the option \"" + option
                            + "\" in the file, it will be skipped.");
            System.out.println("PostContentStrategy: End.\n");
            return;
        } else {

            // {{ POST_READ_TIME }} handling part. (Yes, it just counts spaces.
            // Yes, I trust people to not leave ten thousand spaces.)
            System.out.println("PostContentStrategy: Starting to calculate \"POST_READ_TIME\".");
            int wordNum = 0;
            int index = value.indexOf(' ');
            while (-1 != index) {
                wordNum++;
                index = value.indexOf(' ', index + 1);
            }
            wordNum /= 238; // https://scholarwithin.com/average-reading-speed#adult-average-reading-speed

            if (wordNum > 0) {
                Builder.stringEditor("{{ POST_READ_TIME }}", "Expected Read Time: " + wordNum + " minutes", file);
            } else {
                Builder.stringEditor("{{ POST_READ_TIME }}", "Expected Read Time: Under one minute.", file);
            }
            System.out.println("PostContentStrategy: Successfully calculated \"POST_READ_TIME\".");

            Builder.stringEditor(option, value, file);
            System.out.println("PostContentStrategy: End.\n");
        }
        System.out.println("PostContentStrategy: End.\n");
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
            case "SOCIAL_ICONS":
                strategy = new SocialIconsStrategy();
                System.out.println("Factory: Chose SocialIconsStrategy.");
                break;
            case "SOCIAL_LINKS":
                strategy = new SocialLinksStrategy();
                System.out.println("Factory: Chose SocialLinksStrategy.");
                break;
            case "THEME_NAME":
                strategy = new ThemeNameStrategy();
                System.out.println("Factory: Chose ThemeNameStrategy.");
                break;
            case "POST_CONTENT":
                strategy = new PostContentStrategy();
                System.out.println("Factory: Chose PostContentStrategy.");
                break;
            case "POST_TAGS":
                strategy = new PostTagsStrategy();
                System.out.println("Factory: Chose PostTagsStrategy.");
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