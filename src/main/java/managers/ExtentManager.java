package managers;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;

import static core.config.ConfigReader.getStrProp;

// Removed all imports related to Map and ConcurrentHashMap

public class ExtentManager {
    private static ExtentReports extent;
    private static final String DEFAULT_REPORT_PATH = getStrProp("EXTENT_REPORT","execution-output/test-reports/ExtentReport.html");
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
}