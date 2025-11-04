package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import managers.DriverManager;
import managers.ExtentManager;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static core.screenshot.ScreenshotUtil.getBase64Screenshot;

public class ExtentReportListeners implements ITestListener {
    private static ExtentReports extent = ExtentManager.getReportInstance();
    private static Map<String, ExtentTest> classNodeMap = new ConcurrentHashMap<>();
   // private static ThreadLocal<ExtentTest> methodLevelTest = new ThreadLocal<>();

    @Override
    public void onTestStart(ITestResult result) {
        String className = result.getTestClass().getRealClass().getSimpleName();
        ExtentTest classTest = classNodeMap.computeIfAbsent(className, k -> extent.createTest(k));
        String methodName = result.getMethod().getMethodName();
        ExtentTest methodTest = classTest.createNode(methodName);
       // methodLevelTest.set(methodTest);
        ExtentManager.setTest(methodTest);
        Object[] params = result.getParameters();
        if (params.length > 0) {
            methodTest.info("Parameters: " + Arrays.toString(params));
        }
        Object[] groups = result.getMethod().getGroups();
        if (groups.length > 0) {
            methodTest.info("groups: " + Arrays.toString(groups));
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
       // methodLevelTest.get().pass("Test Passed");
        test.pass("Test Passed");
        WebDriver driver = DriverManager.getDriver();
      /*  if (driver != null) {
            String base64Screenshot = getBase64Screenshot(driver);
            methodLevelTest.get().addScreenCaptureFromBase64String(base64Screenshot);
        }*/
        attachScreenshot(test,driver);
        //methodLevelTest.remove();
        ExtentManager.removeTest();
    }

    @Override
    public void onTestFailure(ITestResult result) {
       // methodLevelTest.get().fail(result.getThrowable());
        ExtentTest test = ExtentManager.getTest();
        test.fail(result.getThrowable());
        WebDriver driver = DriverManager.getDriver();
        /*if (driver != null) {
            String base64Screenshot = getBase64Screenshot(driver);
            methodLevelTest.get().addScreenCaptureFromBase64String(base64Screenshot);
        }
        methodLevelTest.remove();*/
        attachScreenshot(test,driver);
        //methodLevelTest.remove();
        ExtentManager.removeTest();
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
       // methodLevelTest.get().skip("Test Skipped: " + result.getThrowable());
        test.skip("Test Skipped: " + result.getThrowable());
        //methodLevelTest.remove();
        ExtentManager.removeTest();
    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

    private void attachScreenshot(ExtentTest test, WebDriver driver)
    {
        if (driver != null) {
            String base64Screenshot = getBase64Screenshot(driver);
            test.addScreenCaptureFromBase64String(base64Screenshot);
        }
    }

}