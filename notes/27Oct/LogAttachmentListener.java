package listeners;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import managers.ExtentManager; // Your thread-safe manager for ExtentTest
import common.log.LogExtractorUtils; // Your utility class
import org.testng.ITestListener;
import org.testng.ITestResult;

public class LogAttachmentListener implements ITestListener {
 //   private static ExtentReports extent = ExtentManager.getReportIntance();
    private void attachLogs(ITestResult result) {
        // 1. Get the thread-local ExtentTest instance
        ExtentTest test = ExtentManager.getTest();
        if (test == null) {
            System.err.println("ExtentTest not found for thread: " + result.getMethod().getMethodName() + ". Cannot attach logs.");
            return;
        }
        // 2. Get the unique filter key (Test Method Name)
        String testCaseName = result.getMethod().getMethodName();
        // 3. Retrieve the logs using the utility method, passing ONLY the testCaseName
        String testLogs = LogExtractorUtils.toGet_TestCaseLogs(testCaseName);
        test.info("Extracted Logs for: " + testCaseName);
        test.info("Execution Logs:<br>" + testLogs.replace("\n", "<br>"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        attachLogs(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        attachLogs(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        attachLogs(result);
    }
}