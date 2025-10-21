package video;

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class TestRecorder extends ScreenRecorder {

    private static ScreenRecorder screenRecorder;
    private final String name;
    public TestRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                        Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
            throws IOException, AWTException
    {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.name = name;
    }
    @Override
    protected File createMovieFile(Format fileFormat) throws IOException
    {
        if (!movieFolder.exists())
        {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory())
        {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }
        // SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
        //  return new File(movieFolder,
        //  name + "-" + dateFormat.format(new Date()) + "." + Registry.getInstance().getExtension(fileFormat));
        return new File(movieFolder,
                name + "." + Registry.getInstance().getExtension(fileFormat));
    }
    private static final String DEFAULT_VIDEO_FOLDER = "./test-recordings/";
    public static void startRecord(String recordedVideoName) throws Exception
    {
        startRecord(recordedVideoName,DEFAULT_VIDEO_FOLDER);
    }
    public static void startRecord(String recordedVideoName,String userPath) throws IOException, AWTException {
        startRecordCoreLogic(recordedVideoName,userPath);
    }
    private static void startRecordCoreLogic(String recordedVideoName,String userPath) throws IOException, AWTException {
        File file = new File(userPath);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = screenSize.width;
        int height = screenSize.height;
        Rectangle captureSize = new Rectangle(0, 0, width, height);
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().
                getDefaultScreenDevice()
                .getDefaultConfiguration();
        screenRecorder = new TestRecorder(gc, captureSize,
                new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey,
                        Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
                null, file, recordedVideoName);
        screenRecorder.start();
    }
    public static void stopRecord() throws Exception
    {
        screenRecorder.stop();
    }
// =============================================================================== //
    public static String toGet_VideoFilePath(String testCaseName)
    {
        // Calls the primary implementation with the default extension
        return toGet_VideoFilePath(testCaseName,DEFAULT_VIDEO_FOLDER);
    }

    // --- Overload 2: Handles TWO arguments (Uses the provided extension) ---
    public static String toGet_VideoFilePath(String testCaseName, String userPath) {
        return toGet_VideoFilePath_CoreLogic(testCaseName, userPath);
    }
    // This method will return execution video file path as hyperlink, so that we can attach this in report
    private static String toGet_VideoFilePath_CoreLogic(String testCaseName,String userPath)
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
