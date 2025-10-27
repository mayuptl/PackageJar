package listeners;

import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.ExtentTest; // Assuming Extent Reports library
import core.video.GetVideoFilePath;
import managers.ExtentManager; // Assuming your thread-safe manager for ExtentTest
import org.testng.ITestListener;
import org.testng.ITestResult;

public class VideoAttachmentListener implements ITestListener {

    // Helper method to attach the video path to the current test log
    private void attachVideoLink(ITestResult result) {
        ExtentTest test = ExtentManager.getTest();
        if (test == null) {
            System.err.println("ExtentTest not found for thread, cannot attach video link.");
            return;
        }
        String testName = result.getMethod().getMethodName();
        String videoLinkHtml = GetVideoFilePath.toGetVideoFilePath(testName);
        if (videoLinkHtml != null) {
            test.info("Test Recording: " + videoLinkHtml);
        } else {
            test.log(Status.INFO, "Video recording file was not found after test completion.");
        }
    }
    @Override
    public void onTestSuccess(ITestResult result) {
        attachVideoLink(result);
        // Note: ExtentManager.removeTest() should be in your main ExtentReportListener
        // (after all other listeners have finished their reporting work).
    }
    @Override
    public void onTestFailure(ITestResult result) {
        attachVideoLink(result);
        // Note: ExtentManager.removeTest() should be in your main ExtentReportListener
    }
}