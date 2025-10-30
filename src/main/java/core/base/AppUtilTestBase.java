package core.base;
import core.config.ConfigReader;
import managers.DriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.time.Duration;

import static core.config.ConfigReader.getIntProp;
import static core.config.ConfigReader.getStrProp;


public class AppUtilTestBase {

    public WebDriver driver;
    private static final String DEFAULT_BROWSER = getStrProp("DEFAULT_BROWSER");
    private static final String DEFAULT_URL = getStrProp("DEFAULT_URL"); // Replace with your default URL
    private static final int DEFAULT_IMPLICIT_WAIT = getIntProp("DEFAULT_IMPLICIT_WAIT");

    /**
     * Public method 1: <b>Launches the application using default browser:chrome, URL:amazon, and wait time:10 sec</b>.
     */
    @BeforeClass(alwaysRun = true)
    public void launchApplication() {
        // Calls the core logic with predefined constants
        launchApplicationCore(DEFAULT_BROWSER, DEFAULT_URL, DEFAULT_IMPLICIT_WAIT);
    }

    /**
     * Public method 2: <b>Launches the application using user-provided values.</b>
     * @param browserName The name of the browser (e.g., "chrome", "firefox","edge").
     * @param appUrl The URL of the application.
     * @param implicitlyWaitInSec The implicit wait timeout in seconds.
     */
    //@BeforeClass(alwaysRun = true)
    public void launchApplication(String browserName, String appUrl, Integer implicitlyWaitInSec) {
        // Calls the core logic with user-provided arguments
        launchApplicationCore(browserName, appUrl, implicitlyWaitInSec);
    }

    /**
     * Private core method: Contains the actual implementation logic.
     * @param browserName The browser name.
     * @param appUrl The application URL.
     * @param implicitlyWaitInSec The implicit wait timeout.
     */
    private void launchApplicationCore(String browserName, String appUrl, Integer implicitlyWaitInSec) {
        // Assume these methods are defined elsewhere
        WebDriver driverInstance = initializeDriver(browserName, implicitlyWaitInSec);
        DriverManager.setDriver(driverInstance);
        driverInstance.get(appUrl);
    }

    public void launchApp() throws IOException {
        // Assume these methods are defined elsewhere
        WebDriver driverInstance = initializeDriver(getStrProp("browserName"),ConfigReader.getIntProp("implicitlyWaitInSec"));
        DriverManager.setDriver(driverInstance);
        driverInstance.get(getStrProp("appUrl"));
    }
    private WebDriver initializeDriver(String BrowserName,Integer implicitlyWaitInSec) {
        int finalWaitTime = (implicitlyWaitInSec == null)
                ? DEFAULT_IMPLICIT_WAIT         // If true (it is null), assign the default
                : implicitlyWaitInSec;
        if (BrowserName.contains("edge")) {
            EdgeOptions options = new EdgeOptions();
            if (BrowserName.contains("headless")) {
                options.addArguments("--headless=new");
            }
            driver = new EdgeDriver(options);
        } else if (BrowserName.contains("chrome")) {
            ChromeOptions options = new ChromeOptions();
            if (BrowserName.contains("headless")) {
                options.addArguments("--headless=new");
            }
            driver = new ChromeDriver(options);
        } else if (BrowserName.contains("firefox")) {
            FirefoxOptions options = new FirefoxOptions();
            if (BrowserName.contains("headless")) {
                options.addArguments("--headless=new");
            }
            driver = new FirefoxDriver(options);
        } else if (BrowserName.contains("safari")) {
            SafariOptions options = new SafariOptions();
            driver = new SafariDriver(options);
        } else {
            throw new IllegalArgumentException("Unsupported browser specified: " + BrowserName +
                    ". Supported browsers are: edge, chrome, firefox, safari, edge headless, chrome headless, firefox headless");
        }
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(finalWaitTime));
        return driver;
    }
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
