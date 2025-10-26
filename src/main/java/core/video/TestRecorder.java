package core.video;
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
     private final String name;
    // The complex constructor (kept as is)
    public TestRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                        Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
            throws IOException, AWTException
    {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.name = name;
    }

    // ⭐ FIX: STATIC FACTORY METHOD to handle complex instantiation
    public static TestRecorder createConfiguredRecorder(String recordedVideoName, String userPath)
            throws IOException, AWTException {

        File movieFolder = new File(userPath);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle captureSize = new Rectangle(0, 0, screenSize.width, screenSize.height);

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        // Define standard MonteMedia formats
        Format fileFormat = new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI);
        Format screenFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey,
                Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60);
        Format mouseFormat = new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30));

        // Use the complex constructor to create a brand new, configured instance
        return new TestRecorder(gc, captureSize, fileFormat, screenFormat, mouseFormat, null, movieFolder, recordedVideoName);
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {
        // ... (Your file path creation logic remains here) ...
        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }
        return new File(movieFolder, name + "." + Registry.getInstance().getExtension(fileFormat));
    }

    // ⭐ FIX: Make start/stop methods use the 'super' object (this instance)
    public void startRecord() throws IOException {
        super.start();
    }

    public void stopRecord() throws Exception {
        super.stop();
    }
}