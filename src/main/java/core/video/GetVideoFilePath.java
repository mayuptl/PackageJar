package core.video;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;

import static core.config.ConfigReader.getStrProp;

public class GetVideoFilePath {
    //private static final String DEFAULT_VIDEO_FOLDER = getStrProp("DEFAULT_VIDEO_FOLDER");
    //**NEW (Will safely default to `target/videos` if config file or key is missing):**
    private static final String DEFAULT_VIDEO_FOLDER = getStrProp("TEST_RECORDINGS", "execution-output/test-recordings/");

    public static String toGetVideoFilePath(String testCaseName)
    {
        // Calls the primary implementation with the default extension
        return toGetVideoFilePath(testCaseName,DEFAULT_VIDEO_FOLDER);
    }

    // --- Overload 2: Handles TWO arguments (Uses the provided extension) ---
    public static String toGetVideoFilePath(String testCaseName, String userPath) {
        return toGetVideoFilePathCoreLogic(testCaseName, userPath);
    }
    // This method will return execution video file path as hyperlink, so that we can attach this in report
    private static String toGetVideoFilePathCoreLogic(String testCaseName, String userPath)
    {
        // Define the target filename
        final String fullFileName = testCaseName + ".avi";
        // Use a Path object for better cross-platform file handling
        File directory = new File(userPath);
        // Use Objects.requireNonNullElse for a safer check against null (if listFiles fails)
        File[] files = Objects.requireNonNullElse(directory.listFiles(), new File[0]);
        try {
            // Check if the directory itself exists and is readable
            if (!directory.exists() || !directory.isDirectory())
            {
                System.out.println("Directory '" + userPath + "' not found or is not a directory.");
                //  throw new RuntimeException("Error: Directory '" + userPath + "' not found or is not a directory.");
            }
            for (File file : files) {
                // Check if the file is a regular file and its name matches (case-insensitive)
                if (file.isFile() && file.getName().equalsIgnoreCase(fullFileName))
                {
                    // Use Path to get the absolute URI for proper URL construction
                    Path filepath = file.toPath().toAbsolutePath();
                    // file.toUri().toString() correctly handles protocol and slashes for all OS
                    String formattedPath = filepath.toUri().toString();
                    String linkText = "Execution Video";
                    // Return the HTML anchor tag
                    return "<a href=\"" + formattedPath + "\" target=\"_blank\">" + linkText + "</a>";
                }
            }
            // If the loop finishes without finding the file
            // throw new RuntimeException("File not found: " + fullFileName + " in " + userPath);
            System.out.println("File not found: " + fullFileName + " in " + userPath);
            return null;
        } catch (Exception e) {
            // Catching a generic Exception is okay here, but logging is better.
            System.err.println("An unexpected error occurred while processing video files: " + e.getMessage());
            e.printStackTrace();
        }
        // Default return value if file is not found or an error occurred
        return null;
    }

}
