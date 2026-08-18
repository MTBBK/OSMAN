import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.DirectoryStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

public class Update {

    private static final List<String> EXCLUDED_FILES = Arrays.asList(
            "config.osman",
            "Content",
            "ErrorLogs",
            "Output",
            ".git",
            ".gitignore");

    public static void log(String functionName, String message) {
        System.out.println("\n" + functionName + ": " + message);
    }

    public static void log(String message) {
        System.out.println("\n" + message);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        final String osmanURL = "https://github.com/MTBBK/OSMAN/archive/refs/heads/master.zip";
        Update.log("main", "Begin.");

        try {
            // Download zip to a temporary file
            Path temporaryZip = Files.createTempFile("osman_update", ".zip");
            Update.log("Downloading latest version of OSMAN from " + osmanURL);
            try (InputStream inputStream = URI.create(osmanURL).toURL().openStream()) {
                Files.copy(inputStream, temporaryZip, StandardCopyOption.REPLACE_EXISTING);
            }

            // Extract zip
            Update.log("main", "Extracting zip.");
            Path temporaryDir = Files.createTempDirectory("TemporaryUpdateFile");

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(temporaryZip.toFile()))) {
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    Path newFile = temporaryDir.resolve(zipEntry.getName());
                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(newFile);
                    } else {
                        // Create parent directories if they do not exist
                        if (newFile.getParent() != null) {
                            if (Files.notExists(newFile.getParent())) {
                                Files.createDirectories(newFile.getParent());
                            }
                        }
                        Files.copy(zis, newFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }

            // Move files to current directory, excluding the ones we want to preserve
            // The zip extracts to a root folder named "OSMAN-master"
            Path extractedRoot = temporaryDir.resolve("OSMAN-master");
            if (Files.exists(extractedRoot)) {
                Update.log("Applying updates while preserving configuration and content");
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(extractedRoot)) {
                    for (Path entry : stream) {
                        String fileName = entry.getFileName().toString();
                        if (EXCLUDED_FILES.contains(fileName)) {
                            Update.log("Skipping preserved item: " + fileName);
                            continue;
                        }

                        Path targetPath = Paths.get(".").resolve(fileName);
                        if (Files.isDirectory(entry)) {
                            copyDirectory(entry, targetPath);
                        } else {
                            Files.copy(entry, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                    }
                }
            } else {
                Update.log("Error: Could not find OSMAN-master folder in the downloaded zip.");
            }

            // Delete temporary zip file and directory
            Update.log("Cleaning up temporary files...");
            Files.deleteIfExists(temporaryZip);
            deleteDirectory(temporaryDir);

            Update.log("Update complete! You may need to recompile Builder.java.");
            Update.log("Run: javac Builder.java");
            Update.log("Then: java Builder");

        } catch (Exception e) {
            System.err.println("An error occurred during the update process:");
            e.printStackTrace();
        }
        Update.log("main", "Finished the process in " + (System.currentTimeMillis() - startTime) + " milliseconds.");
        Update.log("main", "End.");
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source).forEach(s -> {
            try {
                Path d = target.resolve(source.relativize(s));
                if (Files.isDirectory(s)) {
                    if (!Files.exists(d)) {
                        Files.createDirectory(d);
                    }
                } else {
                    Files.copy(s, d, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                System.err.println("Failed to copy " + s + " : " + e.getMessage());
            }
        });
    }

    private static void deleteDirectory(Path directoryToBeDeleted) throws IOException {
        Files.walk(directoryToBeDeleted)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        System.err.println("Failed to delete " + path + " : " + e.getMessage());
                    }
                });
    }
}
