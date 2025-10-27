package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

public class ExtentManager {
    private static ExtentReports extent;
    private static final String DEFAULT_REPORT_PATH = "test-output/ExtentReport.html";

    // 💡 REQUIRED: ThreadLocal to hold the ExtentTest instance for the current method
    private static final ThreadLocal<ExtentTest> methodLoggerThread = new ThreadLocal<>();

    // --- ThreadLocal Accessors (Required by Listeners) ---

    /** Public method to retrieve the thread's METHOD logger instance. */
    public static ExtentTest getTest() {
        return methodLoggerThread.get();
    }

    /** Setter for the thread's METHOD logger instance. */
    public static void setTest(ExtentTest test) {
        methodLoggerThread.set(test);
    }

    /** Method to remove the instance after a test is done (Crucial for ThreadLocal cleanup) */
    public static void removeTest() {
        methodLoggerThread.remove();
    }

    // --- Test Creation (Required by the primary Listener) ---

    /**
     * Creates a top-level test logger for the current method and sets it in ThreadLocal.
     */
    public static ExtentTest createTest(String testName) {
        if (extent == null) {
            throw new IllegalStateException("ExtentReports is not initialized. Call getReportIntance() first.");
        }
        ExtentTest test = extent.createTest(testName);
        setTest(test); // Store the test node in its ThreadLocal
        return test;
    }

    // --- Initialization & Flush (Existing Logic) ---

    /**
     * Initializes ExtentReports using a custom or default path: test-output/ExtentReport.html
     */
    public static ExtentReports getReportIntance(String reportFilePath) {
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
     * Initializes ExtentReports using the default path: test-output/ExtentReport.html
     */
    public static ExtentReports getReportIntance() {
        return getReportIntance(DEFAULT_REPORT_PATH);
    }

    /** Method to flush the final report (called once after all tests finish) */
    public static void flushReports() {
        if (extent != null) {
            extent.flush();
        }
    }
}