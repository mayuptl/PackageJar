package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import core.logging.LogExtractorUtil;
import managers.DriverManager;
import managers.ExtentManager;
import managers.RecorderManager;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.config.ConfigReader.getBoolProp;
import static core.config.ConfigReader.getStrProp;
import static core.screenshot.ScreenshotUtil.getBase64Screenshot;
import static core.video.GetVideoFilePath.toGetVideoFilePath;

public class ExtentVideoLogAttachListeners implements ITestListener {

    private static final ExtentReports extent = ExtentManager.getReportInstance();
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
        //-------------------//
        String currentInstanceID = String.valueOf(System.identityHashCode(DriverManager.getDriver()));
        ThreadContext.put("driverId", currentInstanceID);
        //------------------//
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

        String safeDriverID = getDriverIdFromContext();
        String methodName = result.getMethod().getMethodName();
        attachLogs(test,safeDriverID,methodName);

        //attachLogs(test,result);
        methodLevelTest.remove(); // ThreadLocal cleanup
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = methodLevelTest.get();
        stopAndAttachVideo(test, result);
        attachScreenshot(test);

        String safeDriverID = getDriverIdFromContext();
        String methodName = result.getMethod().getMethodName();
        attachLogs(test,safeDriverID,methodName);

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

    private void attachScreenshot(ExtentTest test) {
        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            String base64Screenshot = getBase64Screenshot(driver);
            test.addScreenCaptureFromBase64String(base64Screenshot);
        }
    }
    private String getDriverIdFromContext() {
        // Retrieves the value safely bound to the current thread
        return ThreadContext.get("driverId");
    }
    private void attachLogs(ExtentTest test,String driverID,String methodName)
    {
        String testLogs=LogExtractorUtil.toGetTestCaseLogs(methodName,driverID);
        String styledLogs=
                "<div style='overflow-x:auto;'><pre style='white-space: pre-wrap; word-break: break-word;'>"
                        + testLogs + "</pre></div>";
        test.info(styledLogs);
    }
    /** Stops the recorder, attaches the video link to the report, and cleans up Recorder ThreadLocal. */
    private void stopAndAttachVideo(ExtentTest test, ITestResult result) {
        try {
            String methodName =result.getMethod().getMethodName();
            // 💡 Call the thread-safe manager's stop method
            RecorderManager.getRecorder().stop();
            // Assuming toGetVideoFilePath retrieves the HTML link with the video path
            String videoLinkHtml = toGetVideoFilePath(methodName);
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
}