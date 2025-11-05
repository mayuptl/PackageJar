package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import managers.DriverManager;
import managers.ExtentManager;
import managers.RecorderManager; // 💡 Re-import and use the thread-safe manager
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.screenshot.ScreenshotUtil.getBase64Screenshot;
import static core.video.GetVideoFilePath.toGetVideoFilePath;
public class ExtentVideoAttachListeners implements ITestListener {
    private static final ExtentReports extent = ExtentManager.getReportInstance();
    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        ExtentTest classNode = ExtentManager.getOrCreateClassNode(className);

        String methodName = result.getMethod().getMethodName();
        ExtentTest methodNode = classNode.createNode(methodName);
        ExtentManager.setTest(methodNode);
        //-------------------//
        String currentInstanceID = String.valueOf(System.identityHashCode(DriverManager.getDriver()));
        ThreadContext.put("driverId", currentInstanceID);
        //------------------//
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
        stopAndAttachVideo(test, result);
        /* attachScreenshot(test,driver);*/
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        stopAndAttachVideo(test, result);
        attachScreenshot(test);
        test.fail(result.getThrowable());
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        stopAndAttachVideo(test, result);
        test.skip("Test Skipped: " + result.getThrowable());
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    // --- Helper Methods ---

    /** Stops the recorder, attaches the video link to the report, and cleans up Recorder ThreadLocal. */
    private void stopAndAttachVideo(ExtentTest test, ITestResult result) {
        try {
            String methodName =result.getMethod().getMethodName();
            RecorderManager.getRecorder().stop();
            String videoLinkHtml = toGetVideoFilePath(result.getMethod().getMethodName());
            if (videoLinkHtml != null) {
                test.info(videoLinkHtml +" :- " +methodName);
            } else {
                test.log(Status.INFO, "Video recording file was not found after test completion.");
            }
        } catch (IllegalStateException e) {
            // Catches the error if RecorderManager.getRecorder() fails (e.g., if recording never started)
            test.log(Status.WARNING, "Video recorder was not running for this test.");
        } catch (Exception e) {
            test.log(Status.WARNING, "Failed to stop or attach video: " + e.getMessage());
        } finally {
            // 💡 CRITICAL FIX: Clean up Recorder ThreadLocal, always runs
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
}