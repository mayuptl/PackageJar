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
        // Initialize reports at the beginning of the <test> tag in testng.xml
        ExtentManager.initializeReports();
    }

    @Override
    public void onFinish(ITestContext context) {
        // Flush reports at the end of the <test> tag
        ExtentManager.flushReports();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();

        // 💡 SIMPLIFIED: Call createTest with ONLY the method name
        // This creates a top-level test entry (flat structure).
        ExtentManager.createTest(methodName);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String base64Img = getBase64Screenshot(DriverManager.getDriver());
        test.info("Test case pass");
        test.addScreenCaptureFromBase64String(base64Img); // Attach the media

        ExtentManager.removeTest(); // Clean up ThreadLocal
    }

    @Override
    public void onTestFailure(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        String base64Img = getBase64Screenshot(DriverManager.getDriver());
        Throwable throwable = result.getThrowable();

        test.fail(throwable);
        test.addScreenCaptureFromBase64String(base64Img);

        ExtentManager.removeTest(); // Clean up ThreadLocal
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String methodName = result.getMethod().getMethodName();

        // 💡 SIMPLIFIED: Call createTest with ONLY the method name
        ExtentManager.createTest(methodName);

        ExtentTest test = ExtentManager.getTest();

        // Safely retrieve the skip reason
        String skip = "Test skipped.";
        if (result.getThrowable() != null) {
            skip += " Reason: " + result.getThrowable().getMessage();
        }

        // Log the skip status
        test.log(Status.SKIP, skip);

        // Note: No screenshot logic here (correct for skipped tests)
        ExtentManager.removeTest(); // Clean up ThreadLocal
    }
}