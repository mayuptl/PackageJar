package core.base;
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

import java.time.Duration;


public class AppUtilTestBase {

    public WebDriver driver;

    @BeforeClass(alwaysRun = true)
    public void launchApplication() {
        String browserName = "chrome";
        String Url = "https://www.amazon.co.uk/";
        WebDriver driverInstance = initializeDriver(browserName);
        DriverManager.setDriver(driverInstance);
        driverInstance.get(Url);
    }
    private WebDriver initializeDriver(String BrowserName) {
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
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        return driver;
    }
    @AfterClass(alwaysRun = true)
    public void tearDown() {
        DriverManager.quitDriver();
    }
}
