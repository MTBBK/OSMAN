import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.DirectoryStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Builder {
    private static Strategy strategy;

	public static void log (String functionName, String message) {
		System.out.println("\n" + functionName + ": " + message);
	}
	
	public static void log (String message) {
		System.out.println("\n" + message);
	}
	
    public static void main(String args[]) {
        long startTime = System.currentTimeMillis();
        try {
            // sets error's print location to log.txt in ErrorLogs
            writeFile("ErrorLogs/errorLog.txt", null);
            PrintStream err = new PrintStream(new FileOutputStream("ErrorLogs/errorLog.txt"));
            System.setErr(err);
            writeFile("ErrorLogs/log.txt", null);
            PrintStream out = new PrintStream(new FileOutputStream("ErrorLogs/log.txt"));
            System.setOut(out);
            Builder.log("main","Begin.");

            // print to confirm that log functions properly
            System.err.println("Error's Stack Trace Will Be Below:");

            // create an Output folder in the case that it doesn't exist.
            createFolder("Output/");

            Path rootPath = Paths.get("Output");
            final List<Path> pathsToDelete = Files.walk(rootPath).sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            for (Path path : pathsToDelete) {
                Files.deleteIfExists(path);
            }
            // https://stackoverflow.com/questions/35988192/java-nio-most-concise-recursive-directory-delete
			
			// recreate Output folder after cleaning
			createFolder("Output/");

            // begin site building process
            buildSite();

        } catch (Exception e) {
            e.printStackTrace();
        }
        Builder.log("main", "Finished the process in " + (System.currentTimeMillis() - startTime) + " milliseconds.");
		Builder.log("main", "End.");
    }    

    static void buildSite() throws Exception {
        Builder.log("buildFile", "Begin.");

        String config = readFile("config.osman");
        Builder.log("buildSite", "Successfully read the config.");
        
        String baseURL = getOption("baseURL", config).replaceAll("\"", "").trim();
        String siteTitle = getOption("SITE_TITLE", config).replaceAll("\"", "").trim();
        String siteDescription = getOption("SITE_DESCRIPTION", config).replaceAll("\"", "").trim();

        // Choose Template according to Config
		String templateName = getOption("TEMPLATE_NAME", config);
        String templatePath = "Templates/" + templateName + "/";
		if (Files.notExists(Paths.get(templatePath + "base.html"))) {
			throw new FileNotFoundException("Selected template could not be found in Templates folder.");
		}

        // [i][0] - file name, [i][1] file content
        // String[][] contents = parseContentFiles("/Content/Texts/");
        String[][] templates = parseContentFiles(templatePath);
        Builder.log("buildSite", "Successfully run parseContentFiles(\"/Templates/\").");

        String[][] textContent = parseContentFiles("Content/Texts/");
        Builder.log("buildSite", "Successfully run parseContentFiles(\"/Content/Texts/\").");

        // Copy all files and folders in Content folder into Output
        // except Content/Texts
        copyDirectory(Paths.get("Content"), Paths.get("Output"), Paths.get("Content", "Texts"));

        StringBuilder base = new StringBuilder();
        StringBuilder index = new StringBuilder();
        String tagsPage = "";
        String page = "";

        for (int i = 0; templates.length > i; i++) {
            if (templates[i][0].equals("base.html")) {
                base = makeFile(templates[i], config);
                baseSEOMetaModification("ANALYTIC_SCRIPT", "ANALYTIC_ENABLE", config, base);
				stringEditor("{{ SEO_META }}", "{{ SEO_META }}\n\t" + getAssets("Content/Assets"), base);
                Builder.log("buildSite", "Successfully run makeFile on \"" + templates[i][0] + "\".");
            }
            if (templates[i][0].equals("index.html")) {
                index = makeFile(templates[i], config);
                Builder.log("buildSite", "Successfully run makeFile on \"" + templates[i][0] + "\".");
            }
            if (templates[i][0].equals("tags.html")) {
                tagsPage = templates[i][1];
                Builder.log("buildSite", "Successfully grabbed \"" + templates[i][0] + "\".");
            }
            if (templates[i][0].equals("page.html")) {
                page = templates[i][1];
                Builder.log("buildSite", "Successfully grabbed \"" + templates[i][0] + "\".");
            }
        }

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

        // get pages' dates
        String[] pageDates = getOptionArray("POST_DATE", textContent);

		String[] pageAuthors = getOptionArray("POST_AUTHOR", textContent);
		
        // get pages' titles
        String[] pageTitles = getOptionArray("POST_TITLE", textContent);

        // --------------------------------------------------------------------------------------------
        // get pages' tags
        String[][] pageTags = new String[textContent.length][];
		getPageTags(pageTags, textContent);

        // --------------------------------------------------------------------------------------------
        // get pages' summaries begin
        String[] pageSummary = new String[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
			if (isEnabled("OTOMATIC_SUMMARY_ENABLE", textContent[i][1])){
				if (-1 == textContent[i][1].indexOf("POST_CONTENT")) {
					pageSummary[i] = "";
				} else {
					int firstNextLine = textContent[i][1].indexOf('\n', textContent[i][1].indexOf("POST_CONTENT")) + 1;
					String content = textContent[i][1].substring(firstNextLine);
					String cleanText = MarkdownConverter.convert(content).replaceAll("<[^>]*>", "").trim();
					int spaces = 0;
					int spaceIndex = 0;
					// Writes first 20 word as summary
					while (-1 != spaceIndex && 20 > spaces) {
						spaces++;
						spaceIndex = cleanText.indexOf(' ', spaceIndex + 1);
					}
					if (-1 == spaceIndex) {
						pageSummary[i] = cleanText;
					} else {
						pageSummary[i] = (cleanText.substring(0, spaceIndex) + "...");
					}
				}
			}else{
				pageSummary[i] = getOption("PAGE_SUMMARY", textContent[i][1]);
			}
        }
        // get pages' summaries end

        // POST_LIST template
        String postListTemplate = "<div class=\"post-card\">\n" + //
                "                    {{ POST_IMAGE_HTML }}\n" + //
                "                    <a href=\"{{ POST_PATH }}\" class=\"post-card-title\">{{ POST_TITLE }}</a>\n" + //
                "                    <p class=\"post-card-excerpt\">{{ POST_SUMMARY }}</p>\n" + //
                "                    <div class=\"post-card-meta\">{{ POST_DATE }}, {{ POST_AUTHOR }}</div>\n" + //
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
            stringEditor("{{ POST_AUTHOR }}", pageAuthors[i], postLists[i]);
                        
            String imgUrl = getOption("FEATURED_IMAGE", textContent[i][1]);
            String imgHtml = !"-1".equals(imgUrl) ? "<img src=\"" + imgUrl + "\" alt=\"" + pageTitles[i] + "\" class=\"post-card-image\">" : "";
            stringEditor("{{ POST_IMAGE_HTML }}", imgHtml, postLists[i]);
            
            StringBuilder pTags = new StringBuilder();
            for (int j = 0; j < pageTags[i].length; j++) {
				String safeTag = pageTags[i][j].replaceAll("[^a-zA-Z0-9]", "_");
                pTags.append("<a href=\"tag_").append(safeTag).append(".html\">").append(pageTags[i][j]).append("</a>");
                if (j != pageTags[i].length - 1) {
                    pTags.append(", ");
                }
            }

            stringEditor("{{ POST_TAGS }}", pTags.toString(), postLists[i]);
        }
        // POST_LIST maker end

        // handling page.htmls begin
        Builder.log("buildSite", "Starting to make site pages.");
        StringBuilder[] pages = new StringBuilder[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
            String[] arr = { textContent[i][0], page };
            pages[i] = new StringBuilder(base);
                        
            String imgUrl = getOption("FEATURED_IMAGE", textContent[i][1]);
            // SEO_TAGS template
			String seoTags = "\t<meta property=\"og:title\" content=\"" + pageTitles[i] + "\">\n" +
                             "\t<meta property=\"og:description\" content=\"" + pageSummary[i].replace("\"", "&quot;") + "\">\n" +
                             "\t<meta property=\"og:type\" content=\"article\">\n";
            if (!"-1".equals(imgUrl)) {
                seoTags += "\t<meta property=\"og:image\" content=\"" + imgUrl + "\">\n";
            }
            stringEditor("{{ SEO_META }}", seoTags, pages[i]);
          
            stringEditor("{{ CONTENT }}", makeFile(arr, textContent[i][1]).toString(), pages[i]);
        
            if (isEnabled("AUTHOR_CARD_ENABLE", textContent[i][1])){
				// AUTHOR_CARD template
				String authorCardTemplate = "<div class=\"author-bio\">\n" + //
					"{{ POST_AUTHOR_IMAGE }}" + //
					"\t<div>\n" + //
					"\t\t<strong>" + getOption("POST_AUTHOR", textContent[i][1]) + "</strong>\n" + //
					"\t\t<span>"+ getOption("POST_AUTHOR_DESCRIPTION", textContent[i][1]) + "</span>\n" + //
					"\t</div>\n" + //
					"</div>";

				stringEditor("{{ AUTHOR_CARD }}", authorCardTemplate, pages[i]);
				
				if(isEnabled("AUTHOR_CARD_IMAGE_ENABLE", textContent[i][1])){
					stringEditor("{{ POST_AUTHOR_IMAGE }}", "\t<img src=\"" + getOption("POST_AUTHOR_IMAGE", textContent[i][1]) + "\" alt=\"Author Avatar\">\n", pages[i]);
				}else{
					stringEditor("{{ POST_AUTHOR_IMAGE }}", "", pages[i]);
				}
			}else{
				stringEditor("{{ AUTHOR_CARD }}", "", pages[i]);
			}

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
            
            Builder.log("buildSite", "Successfully made \"" + textContent[i][0] + "\" page.");
        }
        
        
        Builder.log("buildSite", "Successfully made all of the site pages.");
        // handling page.htmls end

        // handle index.html begin
        StringBuilder indexPage = new StringBuilder(base);
		String indexSeo = "\t<meta property=\"og:title\" content=\"" + siteTitle + "\">\n" +
				  "\t<meta property=\"og:description\" content=\"" + siteDescription.replace("\"", "&quot;") + "\">\n" +
				  "\t<meta property=\"og:type\" content=\"website\">";
        stringEditor("{{ SEO_META }}", indexSeo, indexPage);
        stringEditor("{{ CONTENT }}", index.toString(), indexPage);
        Builder.log("buildSite", "Successfully merged base and index.");

        stringEditor("{{ TOTAL_POSTS_COUNT }}", "Total Posts: " + pages.length, indexPage);

        // make POST_LIST for index begin.
        {
            StringBuilder megaPostList = new StringBuilder();
            for (int i = postLists.length - 1; i > -1; i--) {
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

            String tagCloudTemplate = "<a href=\"tag_{{ POST_TAGS_LINK }}.html\">{{ POST_TAGS }}</a>\t\t\t\n";
            for (int i = 0; i < existingTags.size(); i++) {
                StringBuilder toBeAdded = new StringBuilder(tagCloudTemplate);
                String safeTag = existingTags.get(i).replaceAll("[^a-zA-Z0-9]", "_");
                stringEditor("{{ POST_TAGS_LINK }}", safeTag, toBeAdded);
                stringEditor("{{ POST_TAGS }}", existingTags.get(i), toBeAdded);
                tagCloud.append(toBeAdded);
            }
            stringEditor("{{ TAG_CLOUD }}", tagCloud.toString(), indexPage);
            
            // generate dedicated tag pages begin
            for (int i = 0; i < existingTags.size(); i++) {
                String tag = existingTags.get(i);
                StringBuilder tagPage = new StringBuilder(base);
                
                String tagSeo = "\t<meta property=\"og:title\" content=\"Posts tagged: " + tag + "\">\n" +
                                "\t<meta property=\"og:type\" content=\"website\">\n";
                stringEditor("{{ SEO_META }}", tagSeo, tagPage);
                stringEditor("{{ CONTENT }}", tagsPage.toString(), tagPage);
                
                StringBuilder tagPostList = new StringBuilder();
                int tagPostCount = 0;
                for (int p = postLists.length - 1; p > -1; p--) {
                    for (String t : pageTags[p]) {
                        if (t.equals(tag)) {
                            tagPostList.append(postLists[p]);
                            tagPostCount++;
                            break;
                        }
                    }
                }
                stringEditor("{{ POST_LIST }}", tagPostList.toString(), tagPage);
                stringEditor("{{ TAG_TITLE }}", "Tag: " + tag + " (" + tagPostCount + ")", tagPage);
                stringEditor("{{ TAG_CLOUD }}", tagCloud.toString(), tagPage);
                
                String safeTag = tag.replaceAll("[^a-zA-Z0-9]", "_");
                writeFile("Output/tag_" + safeTag + ".html", tagPage.toString());
            }
            // generate dedicated tag pages end.
            
        }
        // make TAG_CLOUD for index.html end.

        writeFile("Output/index.html", indexPage.toString());
        // handle index.html end

        Builder.log("buildSite", "Successfully made \"index.html\".");

		// generate post pages
        for (int i = 0; i < textContent.length; i++) {
            String fileName = textContent[i][0].substring(0, textContent[i][0].indexOf(".md"));
            writeFile("Output/" + fileName + ".html", pages[i].toString());
            Builder.log("buildSite", "Successfully made \"" + fileName + "\".");
        }
        
        // generate sitemap.xml
		generateSitemap(baseURL, textContent);
		
        // generate rss.xml
        StringBuilder rss = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n<rss version=\"2.0\">\n<channel>\n");
        rss.append("<title>").append(siteTitle).append("</title>\n");
        rss.append("<link>").append(baseURL).append("</link>\n");
        rss.append("<description>").append(siteDescription).append("</description>\n");
        for (int i = 0; i < Math.min(textContent.length, 20); i++) {
            String fName = textContent[i][0].substring(0, textContent[i][0].indexOf(".md"));
            rss.append("<item>\n<title>").append(pageTitles[i]).append("</title>\n");
            rss.append("<link>").append(baseURL).append("/").append(fName).append(".html</link>\n");
            rss.append("<description>").append(pageSummary[i].replace("<", "&lt;").replace(">", "&gt;")).append("</description>\n");
            rss.append("</item>\n");
        }
        rss.append("</channel>\n</rss>");
        writeFile("Output/rss.xml", rss.toString());
        Builder.log("buildSite", "Successfully made \"rss.xml\".");
		
		// generate robots.txt
		String robotsTxt = "User-agent: *\nAllow: /\nSitemap: " + baseURL + "/sitemap.xml\n";
        writeFile("Output/robots.txt", robotsTxt);
        Builder.log("buildSite", "Successfully made \"robots.txt\".");
        
        Builder.log("buildFile", "End.\n");
    }
	
	static void generateSitemap(String baseURL, String[][] textContent)  throws IOException{
	    StringBuilder sitemap = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        sitemap.append("<url><loc>").append(baseURL).append("/</loc></url>\n");
        for (int i = 0; i < textContent.length; i++) {
            String fName = textContent[i][0].substring(0, textContent[i][0].indexOf(".md"));
            sitemap.append("<url><loc>").append(baseURL).append("/").append(fName).append(".html</loc></url>\n");
        }
        sitemap.append("</urlset>");
        writeFile("Output/sitemap.xml", sitemap.toString());
        Builder.log("buildSite", "Successfully made \"sitemap.xml\".");
	}
	
    static void getPageTags(String[][] pageTags, String[][] textContent) {
        for (int j = 0; j < textContent.length; j++) {
            String option = "POST_TAGS";
            ArrayList<String> list = new ArrayList<>();
            String page = textContent[j][1];
            int lIndex = page.indexOf('\n', page.indexOf(option)) + 1;

            if (-1 == lIndex) {
                Builder.log("buildSite", "Could not find \"" + option + "\" in the text content, it will be skipped.");
                pageTags[j] = new String[0];
                continue;
            }

            boolean nextExists = true;

            while (nextExists) {
                int nextLQuote = page.indexOf('"', lIndex) + 1;
                int nextLDash = page.indexOf('-', lIndex);
                int nextLLine = page.indexOf('\n', lIndex);

                // check if there is a variable to read and skip to the next iteration if there
                // isnt

                if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                    nextExists = false;
                    continue;
                }

                String lValue = page.substring(nextLQuote, page.indexOf('"', nextLQuote));

                lIndex = page.indexOf('\n', lIndex) + 1;
                lIndex = nextLLine + 1;

                list.add(lValue);
            }
            pageTags[j] = new String[list.size()];
            pageTags[j] = (list.toArray(pageTags[j]));
        }
    }
    
	static String getOption (String configOption, String config) throws IOException {
        String optionValue = "";
        int valueIndex = config.indexOf(configOption);
        if (-1 == valueIndex) {
            Builder.log("getOption", "Cannot find config option for " + configOption);
            // Default Value
            return "-1";
        } else {
            // index of the first quote symbol after configOption
            int firstQuote = config.indexOf('"', valueIndex) + 1;
            int nextLine = config.indexOf('\n', firstQuote);
			if (nextLine == -1) {
				nextLine = config.length();
			}
			int lastQuote = config.lastIndexOf('"', nextLine - 1);
			if (lastQuote <= firstQuote) {
				lastQuote = config.indexOf('"', firstQuote);
			}
            // selected options name
            optionValue = config.substring(firstQuote, lastQuote);
            Builder.log("getOption", "Returned value of the option " + configOption + " as " + optionValue);
            return optionValue;
        }
	}
	
	static String[] getOptionArray (String configOption, String[][] textContent) throws IOException {
		String[] optionValues = new String[textContent.length];
        for (int i = 0; i < textContent.length; i++) {
			optionValues[i] = getOption(configOption, textContent[i][1]);
        }
        Builder.log("getOptionArray", "Returned values of " + configOption);
        return optionValues;
	}
	
	static boolean isEnabled (String configOption, String config) throws IOException{	
		String option = getOption(configOption, config).toLowerCase();
		boolean isEnable = false;
        if (option.equals("true") || option.equals("1") ||  option.equals("evet")){
			isEnable = true;
        }
        Builder.log("isEnabled", "Returned statue of " + configOption);
		return isEnable;
	}
	
	static void baseSEOMetaModification (String configOption, String enableSignal, String config, StringBuilder base) throws Exception{
		if(isEnabled(enableSignal, config)){
			String newModule = ("{{ SEO_META }}\n\t" + getOption(configOption, config));
			stringEditor("{{ SEO_META }}", newModule, base);
			Builder.log("baseSEOMetaModification", "Successfully added \"" + configOption + "\" to base.html");
		}
	}
	
	static String getAssets (String assetsFolderPath) throws IOException{
		Path assetsFolderPathObject = createFolder(assetsFolderPath);
		StringBuilder customAssets = new StringBuilder();
		try(Stream<Path> assetFiles = Files.list(assetsFolderPathObject)){
			assetFiles.forEach(asset -> {
				String fileName = asset.getFileName().toString();
				if (fileName.endsWith(".css")) {
					customAssets.append("<link rel=\"stylesheet\" href=\"./Assets/").append(fileName).append("\">\n");
				} 
				else if (fileName.endsWith(".js")) {
					customAssets.append("<script defer src=\"./Assets/").append(fileName).append("\"></script>\n");
				}
			});
		}
		return customAssets.toString();
	}
	
	static Path createFolder (String folderPath) throws IOException{
		Path newPath = Paths.get(folderPath); 
		if (Files.notExists(newPath)) { 
			try {Files.createDirectory(newPath);}
			catch (Exception e) {e.printStackTrace();}
		}
		return newPath;
	}
	
	static int countFiles(Path folderPath) throws IOException {
        int count = 0;
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath)) {
            for (Path path : directoryStream) {
                if (Files.isRegularFile(path)) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return count;
    }
	
static String[][] parseContentFiles(String folderPath) throws IOException {
    Path newPath = Paths.get(folderPath);

    if (!Files.isDirectory(newPath)) {
        throw new IOException("Folder \"" + folderPath + "\" Could Not Be Found\n");
    }

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(newPath)) {
        List<Path> entries = new ArrayList<>();
        for (Path p : stream) entries.add(p);

        if (entries.isEmpty()) {
            Builder.log("parseContentFiles", "Folder \"" + folderPath + "\" is empty.");
            return new String[0][0];
        }

        String[][] fileContents = new String[entries.size()][2]; // [i][0] file's name, [i][1] file's content

        for (int i = 0; i < entries.size(); i++) {
            Path p = entries.get(i);
            fileContents[i][0] = p.getFileName().toString();
            fileContents[i][1] = readFile(p.toString());
        }

        return fileContents;
    }
}


    // takes text file's name and reads text file and return its contents as a
    // string
    public static String readFile(String filePath) throws IOException {
        try {
            Path path = Paths.get(filePath);
            return Files.readString(path);
        } catch (IOException e) {
            throw new IOException("Failed To Read File " + filePath);
        }
    }

    // takes a file path and file's text contents and creates an output in "Output"
    // folder. WORKS
    public static void writeFile(String filePath, String fileContent) throws IOException {
        // Creates required folders if they don't already exist.

        Path path = Paths.get(filePath);
        Files.createDirectories(path.getParent());
        try {
            Files.createFile(path);
        } catch (FileAlreadyExistsException e) {
            // expected exception maybe add Expected exception or smt idk
        }

        // creates an empty file if the content is null
        if (null == fileContent) {
            return;
        }

        Files.writeString(path, fileContent, StandardCharsets.UTF_8);
    }

    // takes source folder's path and the destination folder's path to then copy all
    // files in the source folder including folders and their contents but excluding
    // excludeFol.
    public static void copyDirectory(Path sourceFol, Path targetFol, Path excludeFol) {
        try {
            Files.walk(sourceFol).forEach(source -> {
                if (null != excludeFol && source.startsWith(excludeFol)) {
                    return;
                }

                // 1. Relativize: Finds the difference between the root and the current file
                // 2. Resolve: Appends that difference to the target directory
                Path relativePath = sourceFol.relativize(source);
                Path destination = targetFol.resolve(relativePath);

                try {
                    if (Files.isDirectory(source)) {
                        // Create the directory in the output folder if it doesn't exist
                        if (!Files.exists(destination)) {
                            Files.createDirectories(destination);
                        }
                    } else {
                        // Copy the file, replacing it if it already exists
                        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to copy: " + source);
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.err.println("Failed to walk directory.");
            e.printStackTrace();
        }
    }

    // replaces file[1]'s with the appropriate parts in config and returns the
    // result as a StringBuilder
    static StringBuilder makeFile(String[] file, String config) throws Exception {
        Builder.log("makeFile", "Begin.");
        StringBuilder sbFile = new StringBuilder(file[1]);
        int index = 0;

        if (-1 != file[0].indexOf("base")) { // for base
            index = config.indexOf("BASE");
            Builder.log("makeFile", "Successfully found the start of \"" + file[0] + "\" file's part.");
        } else if (-1 != file[0].indexOf("index")) { // for index
            index = config.indexOf("INDEX");
            Builder.log("makeFile", "Successfully found the start of \"" + file[0] + "\" file's part.");
        } else { // for unknown templates
            // cut file[0] from 0 to first '.' to remove extension and make it all uppercase
            // then look for it in config
            index = config.indexOf(file[0].substring(0, file[0].indexOf('.')).toUpperCase());
            Builder.log("makeFile", "Successfully found the start of \"" + file[0] + "\" file's part.");
            if (-1 == index) {
                Builder.log("makeFile", "Failed to find the start of \"" + file[0]+ "\" file's part. Will begin from the top of the file");
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
                Builder.log("makeFile", "Successfully skipped to the next line.");
            } else {
                String option = config.substring(index + 1, nextColon);
                Builder.log("makeFile", "Successfully found the option: \"" + option + "\".");
                setStrategy(option);
                Builder.log("makeFile", "Successfully executed setStrategy(\"" + option + "\") on the file.");
                performStrategy(sbFile, config);
                Builder.log("makeFile", "Successfully executed performStrategy(\"" + option + "\") on the file.");
                index = nextLine;
                Builder.log("makeFile", "Successfully finished changing \"" + option + "\" in the file.");
                
                // Maybe we can stop searching for strategy options after POST_CONTENT reached.
                // if(option.equals("POST_CONTENT")){break;}
            }
        } while (-1 != index && -1 != nextColon);
        Builder.log("makeFile", "Successfully finished making the file.");
        Builder.log("makeFile", "End.\n");
        return sbFile;
    }

    // takes current file's content, the part that will be changed and the new part
    // that will be placed instead of it. Then replaces the part.
    public static void stringEditor(String contentName, String newContent, StringBuilder file) throws Exception {
        int index = file.indexOf(contentName);
        if (index != -1) {
			file.replace(index, index + contentName.length(), newContent);
		} else {
			Builder.log("stringEditor", "Placeholder \"" + contentName + "\" not found in file.");
		}
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

class ThemeNameStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        Builder.log("ThemeNameStrategy", "Begin.");
        int THEME_NAMEindex = config.indexOf(option);

        if (-1 == file.indexOf("{{ " + option + " }}")) {
            Builder.log("ThemeNameStrategy", "Could not find both \"THEME_NAME\" AND \"SOCIAL_LINKS\" in the file, they will be skipped.");
            return;
        }

        if (-1 == THEME_NAMEindex) {
            throw new Exception("ThemeNameStrategy: Failed to find \"THEME_NAME\" in the config file.");
        }

        // index of the first " after THEME_NAME
        int kesme1Index = config.indexOf('"', THEME_NAMEindex);
        // selected theme's name
        // "Project/Themes/" + themeName + "/" + themeName + ".css"
        String themeName = config.substring(kesme1Index + 1, config.indexOf('"', kesme1Index + 1));
        String themePath = "Themes/" + themeName + "/" + themeName + ".css";

		Path newPath = Paths.get(themePath); 
		if (Files.notExists(newPath)) { 
			throw new Exception("Selected theme could not be found in Themes folder.");
		}

        // copy theme file to Output folder
        Builder.log("ThemeNameStrategy", "Starting copying \"" + themeName + ".css\" to the Output folder.");
        String themeFile = Builder.readFile(themePath);
        Builder.writeFile("Output/" + themeName + ".css", themeFile);
        Builder.log("ThemeNameStrategy", "Finished copying \"" + themeName + ".css\" to the Output folder.");

        option = "{{ " + option + " }}";
        Builder.stringEditor(option, themeName + ".css", file);
        Builder.log("ThemeNameStrategy", "End.\n");
    }
}

class NonArrayStrategy extends Strategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        Builder.log("NonArrayStrategy", "Begin.");
        int firstQuote = config.indexOf('"', config.indexOf(option)) + 1;
        int nextLine = config.indexOf('\n', firstQuote);
        if (nextLine == -1) {
            nextLine = config.length();
        }
        int lastQuote = config.lastIndexOf('"', nextLine - 1);
        if (lastQuote <= firstQuote) {
            lastQuote = config.indexOf('"', firstQuote);
        }
        String value = config.substring(firstQuote, lastQuote);
        option = "{{ " + option + " }}";
        if (-1 == file.indexOf(option)) {
            Builder.log("NonArrayStrategy", "Could not find the option \"" + option + "\" in the file, it will be skipped.");
            Builder.log("NonArrayStrategy", "End.\n");
            return;
        } else {
            do {
                Builder.stringEditor(option, value, file);
            } while (-1 != file.indexOf(option));
            Builder.log("NonArrayStrategy", "End.\n");
        }

    }
}

class PostContentStrategy extends Strategy {
    @Override
    void makeChanges(StringBuilder file, String config) throws Exception {
        Builder.log("PostContentStrategy", "Begin.");
        int contentStart = config.indexOf('\n', config.indexOf(option)) + 1;
        String value = config.substring(contentStart);
        option = "{{ " + option + " }}";

        if (-1 == file.indexOf(option)) {
            // if the option isnt available in the file
            Builder.log("PostContentStrategy", "Could not find the option \"" + option
                            + "\" in the file, it will be skipped.");
            Builder.log("PostContentStrategy", "End.\n");
            return;
        } else {
            // {{ POST_READ_TIME }} handling part.
            Builder.log("PostContentStrategy", "Starting to calculate \"POST_READ_TIME\".");
            String cleanText = MarkdownConverter.convert(value).replaceAll("<[^>]*>", "").trim();
            int wordNum = 0;
            if (!cleanText.isEmpty()) {
                wordNum = cleanText.split("\\s+").length;
            }
            wordNum /= 238; // https://scholarwithin.com/average-reading-speed#adult-average-reading-speed

            if (wordNum > 1) {
                Builder.stringEditor("{{ POST_READ_TIME }}", "Expected Read Time: " + wordNum + " minutes", file);
            } else if (wordNum == 1){
                Builder.stringEditor("{{ POST_READ_TIME }}", "Expected Read Time: " + wordNum + " minute", file);
            }else {
                Builder.stringEditor("{{ POST_READ_TIME }}", "Expected Read Time: Under one minute.", file);
            }
            Builder.log("PostContentStrategy", "Successfully calculated \"POST_READ_TIME\".");

            Builder.stringEditor(option, MarkdownConverter.convert(value), file);
            
            boolean TOCEnable = Builder.isEnabled("TABLE_OF_CONTENT_ENABLE", config);
			
            if (TOCEnable){
                Builder.stringEditor("{{ POST_TOC }}", MarkdownConverter.currentTOC, file);
                Builder.stringEditor("{{ TABLE_OF_CONTENT_TITLE }}", Builder.getOption("TABLE_OF_CONTENT_TITLE", config), file);
                Builder.log("PostContentStrategy", "Successfully added \"POST_TOC\".");
            }else{
				Builder.stringEditor("{{ POST_TOC }}", "", file);
				Builder.log("PostContentStrategy", "Table Of Content Is Disable skipping \"POST_TOC\".");
			}
            
            Builder.log("PostContentStrategy", "End.\n");
        }
        Builder.log("PostContentStrategy", "End.\n");
    }
}

abstract class SingleArrayStrategy extends Strategy {
    String codePiece;

    @Override
    void makeChanges(StringBuilder file, String config) throws Exception {
        Builder.log("SingleArrayStrategy", "Begin.");
        String sl = "{{ " + option + " }}";

        if (-1 == file.indexOf(sl)) {
            Builder.log("SingleArrayStrategy", "Could not find \"" + option + "\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf(option)) + 1;

        if (-1 == lIndex) {
            Builder.log("SingleArrayStrategy", "Could not find \"" + option + "\" in the config, it will be skipped.");
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

            StringBuilder ref = new StringBuilder(codePiece);

            String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));

            lIndex = config.indexOf('\n', lIndex) + 1;
            lIndex = nextLLine + 1;

            Builder.stringEditor(sl, lValue, ref);

            result.append(ref);
        }

        Builder.stringEditor("{{ " + option + " }}", result.toString(), file);
        Builder.log("SingleArrayStrategy", "End.\n");
    }
}

class PostTagsStrategy extends SingleArrayStrategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
		Builder.log("PostTagsStrategy", "Begin.");
		
		codePiece = "\t\t\t<a href=\"tag_{{ POST_TAGS_LINK }}.html\">{{ POST_TAGS }}</a>\t\t\t\n";
		// \n's and \t's to allign it better. It is just visual.
        String sl = "{{ " + option + " }}";
        String slLink = "{{ " + option + "_LINK }}";

        if (-1 == file.indexOf(sl)) {
            Builder.log("PostTagsStrategy", "Could not find \"" + option + "\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf(option)) + 1;

        if (-1 == lIndex) {
            Builder.log("PostTagsStrategy", "Could not find \"" + option + "\" in the config, it will be skipped.");
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

            StringBuilder ref = new StringBuilder(codePiece);

            String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));
			String safelValue = lValue.replaceAll("[^a-zA-Z0-9]", "_");

            lIndex = config.indexOf('\n', lIndex) + 1;
            lIndex = nextLLine + 1;

            Builder.stringEditor(sl, lValue, ref);
			Builder.stringEditor(slLink, safelValue, ref);

            result.append(ref);
        }

        Builder.stringEditor("{{ " + option + " }}", result.toString(), file);
        Builder.log("PostTagsStrategy", "End.\n");
    }
}

abstract class DoubleArrayStrategy extends Strategy {
    String codePiece;
    // codePiece's options must contain second array part before first one.
    // Example: ... {{ VERY_COOL2 }} ... {{ VERY_COOL1 }} ...
    // this can be achieved by swapping their places in config
    // VERY_COOL in the given example is the option.

    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        Builder.log("DoubleArrayStrategy", "Begin.");

        String sl = "{{ " + option + " }}";

        if (-1 == file.indexOf(sl)) {
            Builder.log("DoubleArrayStrategy", "Could not find \"" + option + "\" in the file, it will be skipped.");
            return;
        }

        int lIndex = config.indexOf('\n', config.indexOf(option)) + 1;
        int iIndex = config.indexOf(',', lIndex);

        if (-1 == lIndex) {
            Builder.log("DoubleArrayStrategy", "Could not find \"" + option + "\" in the config, it will be skipped.");
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

            // check if there is a variable to read, skip to the next iteration if there
            // isnt
            if (nextLDash > nextLQuote || nextLDash > nextLLine || -1 == nextLDash) {
                nextExists = false;
            } else {
                StringBuilder ref = new StringBuilder(codePiece);

                String lValue = config.substring(nextLQuote, config.indexOf('"', nextLQuote));

                String iValue = config.substring(nextIQuote, config.indexOf('"', nextIQuote));

                lIndex = nextLLine + 1;
                iIndex = config.indexOf(',', lIndex) + 1;

                Builder.stringEditor("{{ " + option + "2 }}", iValue, ref);
                Builder.stringEditor("{{ " + option + "1 }}", lValue, ref);

                result.append(ref);
            }
        }

        Builder.stringEditor("{{ " + option + " }}", result.toString(), file);

        Builder.log("DoubleArrayStrategy", "End.\n");
    }
}

class NavbarStrategy extends DoubleArrayStrategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        codePiece = "<a href=\"{{ NAV_BAR_LINKS2 }}\">{{ NAV_BAR_LINKS1 }}</a>\n\t\t\t";
        // \n's and \t's to allign it better. It is just visual.
        super.makeChanges(file, config);
    }
}

class SocialLinksStrategy extends DoubleArrayStrategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        codePiece = "<a href=\"{{ SOCIAL_LINKS2 }}\">{{ SOCIAL_LINKS1 }}</a>\n\t\t\t";
        // \n's and \t's to allign it better. It is just visual.
        super.makeChanges(file, config);
    }
}

class SocialIconsStrategy extends DoubleArrayStrategy {
    @Override
    public void makeChanges(StringBuilder file, String config) throws Exception {
        codePiece = "<a href=\"{{ SOCIAL_ICONS2 }}\"><img src=\"{{ SOCIAL_ICONS1 }}\"style=\"width:2rem;height:2rem;\"></a>\n\t\t\t";
        // \n's and \t's to allign it better. It is just visual.
        super.makeChanges(file, config);
    }
}

class Factory {
    // factory to decide which strategy is selected in which scenario
    static Strategy decideStrategy(String option) throws Exception {
        Builder.log("Factory: Begin.");

        Strategy strategy;
        switch (option) {
            case "NAV_BAR_LINKS":
                strategy = new NavbarStrategy();
                Builder.log("Factory", "Chose NavbarStrategy.");
                break;
            case "SOCIAL_ICONS":
                strategy = new SocialIconsStrategy();
                Builder.log("Factory", "Chose SocialIconsStrategy.");
                break;
            case "SOCIAL_LINKS":
                strategy = new SocialLinksStrategy();
                Builder.log("Factory", "Chose SocialLinksStrategy.");
                break;
            case "THEME_NAME":
                strategy = new ThemeNameStrategy();
                Builder.log("Factory", "Chose ThemeNameStrategy.");
                break;
            case "POST_CONTENT":
                strategy = new PostContentStrategy();
                Builder.log("Factory", "Chose PostContentStrategy.");
                break;
            case "POST_TAGS":
                strategy = new PostTagsStrategy();
                Builder.log("Factory", "Chose PostTagsStrategy.");
                break;
            case "":
                throw new Exception("Could not detect the next config variable.");
            default:
                strategy = new NonArrayStrategy();
                Builder.log("Factory", "Chose NonArrayStrategy.");
        }
        strategy.option = option;
        Builder.log("Factory", "End.\n");
        return strategy;
    }
}

class MarkdownConverter {
	public static String currentTOC = "";
	
	public static boolean isEndingList (boolean inList, StringBuilder html){
		if(inList){
			// Add list ending code
			html.append("</ul>\n");
			inList = false;
		}
		return inList;
	}
	
	public static boolean isEndingTable (boolean inTable, StringBuilder html){		
		if(inTable){
			// Add table ending code
			html.append("</tbody></table></div>\n");
			inTable = false;
		}
		return inTable;
	}
	
	public static boolean isEndingBlockquote (boolean inBlockquote, StringBuilder html){		
		if(inBlockquote){
			// Add blockquote ending code
			html.append("</blockquote>\n");
			inBlockquote = false;
		}
		return inBlockquote;
	}
	
	public static String generateTitle (String titleStarter, String titleOptionCode, String line, StringBuilder toc){
		StringBuilder title = new StringBuilder();
		int titleStarterLength = titleStarter.length();
		String rawText = parseInline(line.substring(titleStarterLength));
		String text = rawText;
		String id = text.replaceAll("<[^>]*>", "").replaceAll("[^a-zA-Z0-9]+", "-").toLowerCase();
		title.append("<").append(titleOptionCode).append(" id=\"").append(id).append("\">").append(text).append("</").append(titleOptionCode).append(">\n");
        toc.append("<li class=\"toc-level-").append(titleStarterLength).append("\"><a href=\"#").append(id).append("\">").append(text).append("</a></li>\n");
		return title.toString();
	}
	
    public static String convert(String md) {
        if (md == null || md.trim().isEmpty()) {
            return "";
        }
        
        StringBuilder html = new StringBuilder();
		StringBuilder toc = new StringBuilder("<div class=\"post-toc\">\n  <h3 class=\"toc-title\">").append("{{ TABLE_OF_CONTENT_TITLE }}").append("</h3>\n  <ul class=\"toc-list\">\n");
        String[] allLines = md.split("\n");
        List<String> footnotes = new ArrayList<>();
        boolean inCodeBlock = false;
        boolean inList = false;
        boolean inTable = false;
        boolean inBlockquote = false;
        boolean tocHasItems = false;
        for (int i = 0; i < allLines.length; i++) {
            String line = allLines[i];
			
			// code block
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    html.append("</code></pre></div>\n");
                    inCodeBlock = false;
                } else {
                    String lang = line.trim().substring(3).trim();
                    String langClass = lang.isEmpty() ? "" : " class=\"language-" + lang + "\"";
                    html.append(
                            "<div class=\"code-block\"><button class=\"copy-btn\" onclick=\"navigator.clipboard.writeText(this.parentElement.querySelector('code').innerText); this.innerText='Copied!'; setTimeout(()=>this.innerText='Copy',2000);\">Copy</button><pre><code").append(langClass).append(">");
                    inCodeBlock = true;
                }
                continue;
            }
            
            // in code block modify some characters
            if (inCodeBlock) {
                html.append(line.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")).append("\n");
                continue;
            }
                       
            // footnote definition
            if (line.trim().matches("\\[\\^\\d+\\]:.*")) {
                inList = isEndingList(inList, html);
                inTable = isEndingTable(inTable, html);
                inBlockquote = isEndingBlockquote(inBlockquote, html);
                footnotes.add(line.trim());
                continue;
            }
            
			// long horizontal line as seperator
            if (line.trim().matches("---+") || line.trim().matches("\\*\\*\\*+")) {
                inList = isEndingList(inList, html);
                inTable = isEndingTable(inTable, html);
                html.append("<hr>\n");
                continue;
            }

			// caution message like vertical small line infront of the text
            if (line.trim().startsWith("> ")) {
                inList = isEndingList(inList, html);
                inTable = isEndingTable(inTable, html);
                if (!inBlockquote) {
                    html.append("<blockquote>\n");
                    inBlockquote = true;
                }
                html.append("<p>").append(parseInline(line.trim().substring(2))).append("</p>\n");
                continue;
            } else {
                inBlockquote = isEndingBlockquote(inBlockquote, html);
            }

			// big title
            if (line.startsWith("# ")) {
                inList = isEndingList(inList, html);
                inTable = isEndingTable(inTable, html);
                html.append(generateTitle("# ", "h1", line, toc));
                tocHasItems = true;
                continue;
            }
            // not big but not small title
            else if (line.startsWith("## ")) {
                inList = isEndingList(inList, html);
                inTable = isEndingTable(inTable, html);
				html.append(generateTitle("## ", "h2", line, toc));
				tocHasItems = true;
                continue;
            } 
            // small title
            else if (line.startsWith("### ")) {
                inList = isEndingList(inList, html);
                inTable = isEndingTable(inTable, html);
				html.append(generateTitle("### ", "h3", line, toc));
				tocHasItems = true;
                continue;
            }

            // tables
            if (line.trim().startsWith("|") && line.trim().endsWith("|")) {
                inList = isEndingList(inList, html);
                if (line.trim().matches("\\|[-:\\s|]+\\|")) {
                    continue; // Skip markdown table separator
                }
                String[] cols = line.trim().substring(1, line.trim().length() - 1).split("\\|");
                if (!inTable) {
                    html.append("<div class=\"post-table\"><table>\n<thead><tr>");
                    for (String col : cols) {
                        html.append("<th>").append(parseInline(col.trim())).append("</th>");
                    }
                    html.append("</tr></thead>\n<tbody>\n");
                    inTable = true;
                } else {
                    html.append("<tr>");
                    for (String col : cols) {
                        html.append("<td>").append(parseInline(col.trim())).append("</td>");
                    }
                    html.append("</tr>\n");
                }
                continue;
            } else {
				inTable = isEndingTable(inTable, html);
			}

            // task lists
            if (line.trim().startsWith("- [ ] ") || line.trim().startsWith("- [x] ") || line.trim().startsWith("- [X] ")) {
                inTable = isEndingTable(inTable, html);
                if (!inList) {
                    html.append("<ul class=\"task-list\">\n");
                    inList = true;
                }
                boolean checked = line.trim().startsWith("- [x] ") || line.trim().startsWith("- [X] ");
                String text = line.trim().substring(6);
                html.append("<li><input type=\"checkbox\" disabled ")
                    .append(checked ? "checked" : "").append("><span>").append(parseInline(text)).append("</span></li>\n");
                continue;
            }

			// unordered list
            if (line.trim().startsWith("- ")) {
                inTable = isEndingTable(inTable, html);
                if (!inList) {
                    html.append("<ul>\n");
                    inList = true;
                }
                html.append("<li>").append("• ").append(parseInline(line.trim().substring(2))).append("</li>\n");
                continue;
            } else {
				inList = isEndingList(inList, html);
            }

            if (line.trim().isEmpty()) {
                // ignore
            } else {
                if (line.trim().startsWith("<")) {
                    html.append(line).append("\n");
                } else {
                    html.append("<p>").append(parseInline(line)).append("</p>\n");
                }
            }
        }
        
        inList = isEndingList(inList, html);
        inTable = isEndingTable(inTable, html);
        inBlockquote = isEndingBlockquote(inBlockquote, html);
        
		if (!footnotes.isEmpty()) {
			html.append("<div class=\"footnotes\">\n<hr>\n<ol>\n");
			for (String fn : footnotes) {
				int colonIdx = fn.indexOf("]:");
				String num = fn.substring(2, colonIdx);
				String fnText = fn.substring(colonIdx + 2).trim();
				html.append("<li id=\"fn:").append(num).append("\">");
				html.append(parseInline(fnText));
				html.append(" <a href=\"#fnref:").append(num).append("\" class=\"footnote-backref\">&#8617;</a></li>\n");
			}
			html.append("</ol>\n</div>\n");
        }
		
        toc.append("</ul></div>\n");
        if (tocHasItems) {
            currentTOC = toc.toString();
        }
        
        return html.toString();
    }

    private static String parseInline(String text) {
		// bold text
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<strong>$1</strong>");
        // italic text
        text = text.replaceAll("\\*(.*?)\\*", "<em>$1</em>");
        // onlined text
        text = text.replaceAll("~~(.*?)~~", "<del>$1</del>");
		// image
        text = text.replaceAll("!\\[(.*?)\\]\\((.*?)\\)", "<img src=\"$2\" alt=\"$1\" loading=\"lazy\">");
        // link
        text = text.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>");
        // footnote references
        text = text.replaceAll("\\[\\^(\\d+)\\]", "<sup id=\"fnref:$1\"><a href=\"#fn:$1\">$1</a></sup>");
        // youtube shortcode
        text = text.replaceAll("\\[youtube:(.*?)\\]", "<iframe width=\"560\" height=\"315\" src=\"https://www.youtube-nocookie.com/embed/$1\" frameborder=\"0\" allow=\"accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share;\" referrerpolicy=\"strict-origin-when-cross-origin\" allowfullscreen></iframe>");
        // oneline code
        text = text.replaceAll("`(.*?)`",
                "<onelinecode>$1</onelinecode>");
        return text;
    }
}
