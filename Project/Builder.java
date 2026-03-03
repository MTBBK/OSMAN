import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Builder {
    public static void main(String arg[]) {

        try {
            buildSite();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static String[][] parseContentFiles(String folderName) throws IOException {
        File folder = new File(folderName);

        // throw an IOException if folderName is not an existing folder's path
        if (!folder.isDirectory()) {
            throw new IOException("Folder \"" + folderName + "\" Could Not Be Found\n");
        }

        File[] fileNames = folder.listFiles();

        // throw an IOException if the folder is empty
        if (null == fileNames || 0 == fileNames.length) {
            throw new IOException("Folder \"" + folderName + "\" Is Empty\n");
        }

        String[][] fileContents = new String[fileNames.length][2]; // [i][0] - dosyanın adı, [i][1] - dosyanın içeriği

        // reads all files and puts them in a string array
        for (int i = 0; i < fileNames.length; i++) {
            BufferedReader br = new BufferedReader(new FileReader(fileNames[i]));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (null != line) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                fileContents[i][0] = fileNames[i].getName();
                fileContents[i][1] = sb.toString();
            } finally {
                br.close();
            }
        }

        return fileContents;
    }

    static void buildSite() throws Exception {
        // this string will be specified in config.toml later on
        String theme = "Space";
        // [i][0] - file name, [i][1] file content
        String[][] contents = parseContentFiles("Content");
        String[][] templates = parseContentFiles("Templates");
        String[][] themes = parseContentFiles("Themes/" + theme);

        for (int i = 0; i < templates.length; i++) {
            StringBuilder sb = new StringBuilder(templates[i][1]);
            // "<link rel=\"stylesheet\" href=\"style.css\">", len = 44

            File output = new File("Output/" + templates[i][0]);
            FileWriter fw = new FileWriter(output);

            try {
                fw.write(sb.toString());
                fw.flush();
            } finally {
                fw.close();
            }
        }
    }

    StringBuilder setBaseStyle(String[][] templates, String style) throws IOException {
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
