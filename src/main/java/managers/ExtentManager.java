package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import static core.config.ConfigReader.getStrProp;

// Removed all imports related to Map and ConcurrentHashMap

public class ExtentManager {
    private static ExtentReports extent;
    private static final String DEFAULT_REPORT_PATH = getStrProp("EXTENT_REPORT","execution-output/test-reports/ExtentReport.html");
    // NEW: ThreadLocal to store the current running test's ExtentTest instance
    private static final ThreadLocal<ExtentTest> testThreadLocal = new ThreadLocal<>();

    /**
     * <b>Initializes ExtentReports using a custom path</b>
     */
    public static ExtentReports getReportInstance(String reportFilePath) {
        if (extent == null) {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportFilePath);
            sparkReporter.config().setTheme(Theme.STANDARD);
            sparkReporter.config().setDocumentTitle("Test Automation Report");
            sparkReporter.config().setReportName("Test Results");

            extent = new ExtentReports();
            extent.attachReporter(sparkReporter);
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
        }
        return extent;
    }

    /**
     * <b>Initializes ExtentReports using the default path: execution-output/reports/ExtentReport.html</b>
     */
    public static ExtentReports getReportInstance() {
        return getReportInstance(DEFAULT_REPORT_PATH);
    }

    // --- THREAD LOCAL TEST MANAGEMENT METHODS (Used by Listener) ---

    /**
     * Returns the ExtentTest instance associated with the current thread.
     */
    public static ExtentTest getTest() {
        return testThreadLocal.get();
    }

    /**
     * Sets the current ExtentTest instance for the thread. Called by the Listener (onTestStart).
     * @param test The ExtentTest instance for the current method.
     */
    public static void setTest(ExtentTest test) {
        testThreadLocal.set(test);
    }

    /**
     * Removes the ExtentTest instance from the current thread's storage. Called by the Listener (onTestSuccess/Failure/Skip).
     */
    public static void removeTest() {
        testThreadLocal.remove();
    }

    // --- SCREENSHOT ATTACHMENT METHOD (Called by AppUtilTestBase) ---

    /**
     * Logs a step message and attaches a Base64 encoded screenshot to the current test node.
     * This method is called from the AppUtilTestBase's stepScreenshot utility.
     *
     * @param base64Image The Base64 encoded image string.
     * @param stepName The name of the test step/log entry.
     */
    public static void attachScreenshotToReport(String base64Image, String stepName) {
        ExtentTest test = getTest();
        if (test == null) {
            System.err.println("❌ ExtentManager: Cannot attach screenshot for step '" + stepName + "'. No ExtentTest is currently associated with this thread (Listener setup missing).");
            return;
        }

        if (base64Image == null || base64Image.isEmpty()) {
            test.log(Status.WARNING, stepName + " (Screenshot failed: Base64 data missing)");
            return;
        }

        try {
            // Log the step and embed the screenshot using MediaEntityBuilder
            test.log(Status.INFO, stepName,
                    MediaEntityBuilder.createScreenCaptureFromBase64String(base64Image, stepName).build());
        } catch (Exception e) {
           // test.log(Status.ERROR, "Error attaching step screenshot for '" + stepName + "': " + e.getMessage());
        }
    }


}