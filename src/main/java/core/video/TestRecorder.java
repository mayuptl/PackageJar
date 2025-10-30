package core.video;
// ... (all existing imports remain) ...
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.Registry;
import org.monte.media.math.Rational;
import org.monte.screenrecorder.ScreenRecorder;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static core.config.ConfigReader.getStrProp;
import static org.monte.media.FormatKeys.*;
import static org.monte.media.VideoFormatKeys.*;

public class TestRecorder extends ScreenRecorder {
    private final String name;
    // Constructor remains mostly the same
    public TestRecorder(GraphicsConfiguration cfg, Rectangle captureArea, Format fileFormat,
                        Format screenFormat, Format mouseFormat, Format audioFormat, File movieFolder, String name)
            throws IOException, AWTException {
        super(cfg, captureArea, fileFormat, screenFormat, mouseFormat, audioFormat, movieFolder);
        this.name = name;
    }

    @Override
    protected File createMovieFile(Format fileFormat) throws IOException {
        if (!movieFolder.exists()) {
            movieFolder.mkdirs();
        } else if (!movieFolder.isDirectory()) {
            throw new IOException("\"" + movieFolder + "\" is not a directory.");
        }
        // This is the correct logic for creating the file path
        return new File(movieFolder,
                name + "." + Registry.getInstance().getExtension(fileFormat));
    }

    // 💡 NEW FACTORY METHOD: Creates and configures a new thread-safe recorder instance
    public static TestRecorder createConfiguredRecorder(String recordedVideoName, String userPath)
            throws IOException, AWTException {

        File file = new File(userPath);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Rectangle captureSize = new Rectangle(0, 0, screenSize.width, screenSize.height);

        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice()
                .getDefaultConfiguration();

        return new TestRecorder(
                gc, captureSize,
                new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_AVI),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                        CompressorNameKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, DepthKey, 24, FrameRateKey,
                        Rational.valueOf(15), QualityKey, 1.0f, KeyFrameIntervalKey, 15 * 60),
                new Format(MediaTypeKey, MediaType.VIDEO, EncodingKey, "black", FrameRateKey, Rational.valueOf(30)),
                null,
                file,
                recordedVideoName
        );
    }
}