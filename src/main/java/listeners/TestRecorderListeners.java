package listeners;

import managers.RecorderManager;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class TestRecorderListeners implements ITestListener {
    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        try {
            // Option 1: Use the default path
            RecorderManager.initializeRecorder(testName);
            // Option 2: Use custom path (if needed, use a config reader here)
            // String customPath = ConfigReader.getProperty("video.archive.path");
            // RecorderManager.initializeRecorder(testName, customPath);

            // Start recording on the thread-local instance
            RecorderManager.getRecorder().startRecord();
        } catch (Exception e) {
            // Handle exceptions
        }
    }
    @Override
    public void onTestSuccess(ITestResult result)
    {
        try {
            RecorderManager.getRecorder().stopRecord();
        } catch (Exception e)
        {
            /* log error */
        }
        RecorderManager.removeInstance();
    }
    @Override
    public void onTestFailure(ITestResult result) {
        try {
            RecorderManager.getRecorder().stopRecord();
        } catch (Exception e)
        {
            /* log error */
        }
        RecorderManager.removeInstance();
    }
    @Override
    public void onTestSkipped(ITestResult result) {
        // Attempt to stop the recording if it was somehow started
        try {
            RecorderManager.getRecorder().stopRecord();
        } catch (Exception e) {
            /* Log that the stop failed (often because it wasn't started) */
        }
        // CRUCIAL: Always clean up the thread-local storage
        RecorderManager.removeInstance();
    }
}
