package managers;

import core.video.TestRecorder;
import java.awt.*;
import java.io.IOException;

import static core.config.ConfigReader.getStrProp;

public class RecorderManager {
    private static final ThreadLocal<TestRecorder> recorderThread = new ThreadLocal<>();
    // Default video folder (kept for the factory method)
    private static final String DEFAULT_VIDEO_FOLDER = getStrProp("TEST_RECORDINGS","execution-output/test-recordings/");
    /**
     * Initializes the thread-local recorder with a custom name and path.
     */
    public static synchronized void initializeRecorder(String recordedVideoName, String userPath) throws IOException, AWTException {
        // Ensures only one instance creation happens safely
        if (recorderThread.get() == null) {
            TestRecorder recorder = TestRecorder.createConfiguredRecorder(recordedVideoName, userPath);
            recorderThread.set(recorder);
        }
    }
    /**
     * Initializes the thread-local recorder using the default path: ${user.dir}/execution-output//test-recordings/
     * Uses Method Overloading for convenience.
     */
    public static synchronized void initializeRecorder(String recordedVideoName) throws IOException, AWTException {
        initializeRecorder(recordedVideoName, DEFAULT_VIDEO_FOLDER);
    }
    /** Retrieves the instance for the current thread. */
    public static TestRecorder getRecorder() {
        TestRecorder recorder = recorderThread.get();
        if (recorder == null) {
            throw new IllegalStateException("Recorder not initialized for this thread. Call initializeRecorder() first.");
        }
        return recorder;
    }
    /** Crucial for cleaning up the thread state after the test finishes */
    public static void removeInstance() {
        recorderThread.remove();
    }
}