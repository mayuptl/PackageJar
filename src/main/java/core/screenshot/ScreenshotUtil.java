package core.screenshot;

import managers.DriverManager;
import managers.ExtentManager;
import org.apache.logging.log4j.core.util.internal.Status;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

public class ScreenshotUtil
{
    public static String getBase64Screenshot(WebDriver driver) {
        return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
    }
    /**
     * Takes a screenshot using the current thread-local driver, logs a step (PASS),
     * and attaches the image to the Extent Report.
     * This method is STATIC for easy access from any test or POM class.
     *
     * @param stepName The descriptive name for the test step and screenshot log entry.
     */
    public static void stepScreenshot(String stepName) {
        //WebDriver driver=DriverManager.getDriver();
        String base64Image = getBase64Screenshot1();
        // Pass the Base64 string to the Extent Manager for attachment
        // ExtentManager knows the current test node via ThreadLocal, which is set by the Listener.
        ExtentManager.attachScreenshotToReport(base64Image, stepName);
    }
    /**
     * Captures the current browser viewport as a Base64 encoded string.
     * This method is public static, allowing tests to get the raw image data if needed
     * for custom logging or file storage, or it is used internally by stepScreenshot().
     *
     * @return The screenshot as a Base64 encoded String, or an empty string on failure.
     */
    public static String getBase64Screenshot1()
    {
        WebDriver driver = DriverManager.getDriver();
        try
        {
            if (driver instanceof TakesScreenshot)
            {
                return ((TakesScreenshot) driver).getScreenshotAs(OutputType.BASE64);
            } else
            {
                return "";
            }
        } catch (Exception e)
        {
            // Log the error to the report if a test is running (This logic remains crucial)
            if (ExtentManager.getTest() != null) {
                System.err.println("failed to attached screenshot");
               // ExtentManager.getTest().info(Status.ERROR, "Failed to capture screenshot: " + e.getMessage());
            }
        }
        return "";
    }
}
