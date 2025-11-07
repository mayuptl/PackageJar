package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import core.logging.LogExtractorUtil;
import managers.DriverManager;
import managers.ExtentManager;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.config.ConfigReader.getBoolProp;
import static core.screenshot.ScreenshotUtil.getBase64Screenshot;

public class ExtentLogAttachListeners implements ITestListener {
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
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String methodName = result.getMethod().getMethodName();
        /*attachScreenshot(test;*/
        attachLogs(test,methodName);
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String methodName = result.getMethod().getMethodName();
        attachScreenshot(test);
        attachLogs(test,methodName);
        test.fail(result.getThrowable());
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        test.skip("Test Skipped: " + result.getThrowable());
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    private String getDriverIdFromContext() {
        return ThreadContext.get("driverId");
    }
    private void attachLogs(ExtentTest test,String methodName)
    {
        String driverID = getDriverIdFromContext();
        String testLogs=LogExtractorUtil.toGetTestCaseLogs(methodName,driverID);
        String styledLogs=
                "<div style='overflow-x:auto;'><pre style='white-space: pre-wrap; word-break: break-word;'>"
                        + testLogs + "</pre></div>";
        test.info(styledLogs);
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