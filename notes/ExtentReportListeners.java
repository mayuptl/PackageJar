package listeners;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import managers.DriverManager;
import managers.ExtentManager;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import static core.screenshot.ScreenshotUtil.getBase64Screenshot;

public class ExtentReportListeners implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        ExtentManager.initializeReports();
    }
    @Override
    public void onFinish(ITestContext context) {
        ExtentManager.flushReports();
    }
    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        // 💡 CHANGE: Get the FQCN of the test class being run
        String classNameKey = result.getTestClass().getName();
        // Pass both the method name and the class name to the createTest method
        ExtentManager.createTest(methodName, classNameKey);
    }
    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String base64Img = getBase64Screenshot(DriverManager.getDriver());
        test.info("Test case pass");
        test.addScreenCaptureFromBase64String(base64Img); // Attach the media
        ExtentManager.removeTest();
    }
    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String base64Img = getBase64Screenshot(DriverManager.getDriver());
        Throwable throwable = result.getThrowable();
        test.fail(throwable);
        test.addScreenCaptureFromBase64String(base64Img);
        ExtentManager.removeTest();
    }
    @Override
    public void onTestSkipped(ITestResult result) {
        // 1. Get identifiers needed for the ExtentManager
        String methodName = result.getMethod().getMethodName();
        String className = result.getTestClass().getName(); // Get FQCN for the class map
        // 2. CRITICAL: Create the method node using the class name
        // This calls ExtentManager.createTest(methodName, className)
        ExtentManager.createTest(methodName, className);
        // 3. Retrieve the newly created test node from the thread
        ExtentTest test = ExtentManager.getTest();
        // 4. Safely retrieve the skip reason
        String skip = "Test skipped.";
        if (result.getThrowable() != null) {
            // Log the message from the SkipException or other exception
            skip += " Reason: " + result.getThrowable().getMessage();
        }
        // 5. Log the skip status
        test.log(Status.SKIP, skip);
        // 6. Clean up the thread's logger (Crucial for ThreadLocal)
        ExtentManager.removeTest();
    }
}
