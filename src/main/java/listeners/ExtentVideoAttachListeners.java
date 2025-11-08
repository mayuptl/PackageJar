package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import managers.DriverManager;
import managers.ExtentManager;
import managers.RecorderManager; // 💡 Re-import and use the thread-safe manager
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;

import static core.screenshot.ScreenshotUtil.getBase64Screenshot;
import static core.video.GetVideoFilePath.toGetVideoFilePath;
/**
 * ExtentVideoAttachListeners is a streamlined listener focusing only on:
 * 1. Managing Extent Reports (start, success, failure, skip, finish).
 * 2. Starting and stopping video recording, and attaching the video link to the report.
 * 3. Attaching a screenshot to the report only on test failure.
 *
 * This listener relies on TestBaseAppUtil for setting and clearing the ThreadContext.
 */
public class ExtentVideoAttachListeners implements ITestListener {
    private static final ExtentReports extent = ExtentManager.getReportInstance();
    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        ExtentTest classNode = ExtentManager.getOrCreateClassNode(className);

        String methodName = result.getMethod().getMethodName();
        ExtentTest methodNode = classNode.createNode(methodName);
        ExtentManager.setTest(methodNode);

        Object[] params = result.getParameters();
        if (params.length > 0) {
            methodNode.info("Parameters: " + Arrays.toString(params));
        }
        Object[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            methodNode.info("groups: " + Arrays.toString(groups));
        }
        try {
            // 💡 Call the thread-safe manager's method
            RecorderManager.initializeRecorder(methodName);
            RecorderManager.getRecorder().start(); // Assuming startRecording is on the instance
        } catch (Exception e) {
            System.err.println("Video recording failed to start: "+methodName);
        }
    }
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String methodName = result.getMethod().getMethodName();
        stopAndAttachVideo(test, methodName);
        /* attachScreenshot(test,driver);*/
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String methodName = result.getMethod().getMethodName();
        stopAndAttachVideo(test, methodName);
        attachScreenshot(test);
        test.fail(result.getThrowable());
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String methodName = result.getMethod().getMethodName();
        stopAndAttachVideo(test, methodName);
        test.skip("Test Skipped: " + result.getThrowable());
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    // --- Helper Methods ---

    /** Stops the recorder, attaches the video link to the report, and cleans up Recorder ThreadLocal. */
    private void stopAndAttachVideo(ExtentTest test, String methodName) {
        try {
            RecorderManager.getRecorder().stop();
            String videoLinkHtml = toGetVideoFilePath(methodName);
            if (videoLinkHtml != null) {
                test.info(videoLinkHtml +" : " +methodName);
            } else {
                test.log(Status.INFO, "Video recording file was not found after test completion.");
            }
        } catch (IllegalStateException e) {
            test.log(Status.WARNING, "Video recorder was not running for this test.");
        } catch (Exception e) {
            test.log(Status.WARNING, "Failed to stop or attach video: " + e.getMessage());
        } finally {
            RecorderManager.removeInstance();
        }
    }

    private void attachScreenshot(ExtentTest test)
    {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            String base64Screenshot = getBase64Screenshot(driver);
            //test.log(Status.INFO,stepName, MediaEntityBuilder.createScreenCaptureFromBase64String(base64Screenshot).build());
            test.addScreenCaptureFromBase64String(base64Screenshot);
        }else {
            System.err.println("Driver is null. failed to attached screenshot");
        }
    }
    // Unused methods required by ITestListener interface
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onTestFailedWithTimeout(ITestResult result) {}
}