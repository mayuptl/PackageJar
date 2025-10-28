package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import core.log.LogExtractorUtils;
import managers.DriverManager;
import managers.ExtentManager;
import managers.RecorderManager;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.screenshot.ScreenshotUtil.getBase64Screenshot;
import static core.video.GetVideoFilePath.toGetVideoFilePath;

public class ExtentVideoLogAttachListeners implements ITestListener {

    private static final ExtentReports extent = ExtentManager.getReportIntance();
    private static final Map<String, ExtentTest> classNodeMap = new ConcurrentHashMap<>();
    private static final ThreadLocal<ExtentTest> methodLevelTest = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String className = result.getTestClass().getRealClass().getSimpleName();
        ExtentTest test;
        ExtentTest classTest = classNodeMap.computeIfAbsent(className, extent::createTest);
        test = classTest.createNode(methodName);
        methodLevelTest.set(test);
        // Add optional info
        Object[] params = result.getParameters();
        if (params.length > 0) {
            test.info("Parameters: " + Arrays.toString(params));
        }
        Object[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            test.info("groups: " + Arrays.toString(groups));
        }
        try {
            // 💡 Call the thread-safe manager's method
            RecorderManager.initializeRecorder(methodName);
            RecorderManager.getRecorder().start(); // Assuming startRecording is on the instance
        } catch (Exception e) {
            test.log(Status.WARNING, "Video recording failed to start: " + e.getMessage());
            // Do NOT throw RuntimeException here. Let the test proceed.
        }
    }
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = methodLevelTest.get();
        stopAndAttachVideo(test, result);
        attachScreenshot(test);
        test.pass("Test Passed");
        String testLogs = LogExtractorUtils.toGetTestCaseLogs(result.getMethod().getMethodName());
        test.info("Logs:<br>" + testLogs.replace("\n", "<br>"));
        methodLevelTest.remove(); // ThreadLocal cleanup
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = methodLevelTest.get();
        stopAndAttachVideo(test, result);
        attachScreenshot(test);
        String testLogs = LogExtractorUtils.toGetTestCaseLogs(result.getMethod().getMethodName());
        test.info("Logs:<br>" + testLogs.replace("\n", "<br>"));
        test.fail(result.getThrowable());
        methodLevelTest.remove(); // ThreadLocal cleanup
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = methodLevelTest.get();
        stopAndAttachVideo(test, result);
        test.skip("Test Skipped: " + result.getThrowable());
        methodLevelTest.remove(); // ThreadLocal cleanup
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    /** Stops the recorder, attaches the video link to the report, and cleans up Recorder ThreadLocal. */
    private void stopAndAttachVideo(ExtentTest test, ITestResult result) {
        try {
            // 💡 Call the thread-safe manager's stop method
            RecorderManager.getRecorder().stop();
            // Assuming toGetVideoFilePath retrieves the HTML link with the video path
            String videoLinkHtml = toGetVideoFilePath(result.getMethod().getMethodName());
            if (videoLinkHtml != null) {
                test.info("Test Recording: " + videoLinkHtml);
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

    private void attachScreenshot(ExtentTest test) {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            String base64Screenshot = getBase64Screenshot(driver);
            test.addScreenCaptureFromBase64String(base64Screenshot);
        }
    }
}