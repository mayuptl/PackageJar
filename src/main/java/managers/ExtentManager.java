package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

// Removed all imports related to Map and ConcurrentHashMap

public class ExtentManager {
    private static ExtentReports extent;
    private static final String DEFAULT_REPORT_PATH = "test-output/ExtentReport.html";
    /**
     * <b>Initializes ExtentReports using a custom or default path: test-output/ExtentReport.html</b>
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
     * <b>Initializes ExtentReports using the default path: test-output/ExtentReport.html</b>
     */
    public static ExtentReports getReportIntance() {
        return getReportIntance(DEFAULT_REPORT_PATH);
    }
}