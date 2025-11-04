package core.base;

import io.github.bonigarcia.wdm.WebDriverManager;
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
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Base utility class for initializing, managing, and tearing down WebDriver instances.
 * This class provides methods to launch the application using different configurations.
 */
public class TestBaseAppUtil {
    public WebDriver driver;

    @BeforeClass
    public void lunchAppUtil()
    {
        String CUSTOM_OPTIONS = "ARG:--force-device-scale-factor=0.8,ARG:--start-maximized,ARG:--incognito,ARG:--disable-infobars,ARG:--enable-logging=stderr,PREF:download.default_directory=/execution-output/test-downloads/,CAP:acceptInsecureCerts=true";
        String driverPath= "D:\\Work\\Automation\\app-utils\\app-utils\\notes\\msedgedriver.exe";
        //driver = initDriverOptions("firefox",CUSTOM_OPTIONS);
        driver =  initDriver("edge",driverPath,CUSTOM_OPTIONS);
       // driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        driver.get("https://mvnrepository.com/artifact/io.github.bonigarcia/webdrivermanager/6.3.2");
    }

    /**
     * Quits the current WebDriver instance and removes it from the ThreadLocal storage
     * via a single call to the DriverManager utility.
     */
    @AfterClass
    public void tearDownAppUtil() {
        DriverManager.quitDriver();
    }
    /**
     * Public method to initialize the driver with user define browser.
     *
     * @param BrowserName The name of the browser. edge,chrome,firefox etc
     * @return The initialized WebDriver thread safe instance.
     */
    public WebDriver initDriver(String BrowserName) {

        return initDriverCore(BrowserName, "", "");
    }
    /**
     * Public method to initialize the driver with a manual path.
     *
     * @param BrowserName The name of the browser. edge,chrome,firefox etc
     * @param driverPath The manual path to the driver executable.
     * @return The initialized WebDriver thread safe instance.
     */
    public WebDriver initDriver(String BrowserName, String driverPath) {
        return initDriverCore(BrowserName, driverPath, "");
    }
    /**
     * Public method to initialize the driver with user define browser.
     *
     * @param BrowserName The name of the browser. edge,chrome,firefox etc
     * @param customOptions The custom options string ("ARG:...,ARG:...,PREF:...,PREF:...,CAP:...,CAP:...")
     * @return The initialized WebDriver thread safe instance.
     */
    public WebDriver initDriverOptions(String BrowserName, String customOptions) {
        return initDriverCore(BrowserName,"",customOptions );
    }
    /**
     * Public method to initialize the driver with a manual path and custom options.
     *
     * @param BrowserName The name of the browser. edge,chrome,firefox etc
     * @param driverPath The manual path to the driver executable.
     * @param customOptions The custom options string ("ARG:...,ARG:...,PREF:...,PREF:...,CAP:...,CAP:...")
     * @return The initialized WebDriver thread safe instance.
     */
    public WebDriver initDriver(String BrowserName, String driverPath, String customOptions) {
        return initDriverCore(BrowserName, driverPath, customOptions);
    }
    /**
     * Core method for driver initialization.
     * It handles WebDriverManager setup, manual driver path configuration,
     * and parsing of custom options for arguments, preferences, and capabilities.
     *
     * @param BrowserName The name of the browser (e.g., "chrome", "edge headless").
     * @param driverPath The manual path to the driver executable.
     * @param customOptions Comma-separated string of custom options ("ARG:...,ARG:...,PREF:...,PREF:...,CAP:...,CAP:...")
     * @return The initialized WebDriver instance.
     */
    private WebDriver initDriverCore(String BrowserName, String driverPath, String customOptions) {
        WebDriver driver;
        // Map to hold preferences (for Chrome, Edge, Firefox)
        Map<String, Object> prefs = new HashMap<>();
        // Map to hold capabilities (used for options configuration)
        Map<String, Object> caps = new HashMap<>();
        // --- Options Parsing ---
        if (customOptions != null && !customOptions.isEmpty()) {
            String[] optionsArray = customOptions.split(",");
            for (String option : optionsArray) {
                String trimmedOption = option.trim();
                if (trimmedOption.startsWith("PREF:")) {
                    // Handle Browser Preferences (e.g., PREF:download.default_directory=/tmp)
                    try {
                        String prefString = trimmedOption.substring(5);
                        String[] parts = prefString.split("=", 2);
                        if (parts.length == 2) {
                            // Simple type parsing: if it looks like a boolean or number, try to cast it
                            Object value = parts[1];
                            if (value.toString().equalsIgnoreCase("true")) value = true;
                            else if (value.toString().equalsIgnoreCase("false")) value = false;
                            prefs.put(parts[0], value);
                        }
                    } catch (Exception e) {
                        // Exception caught silently as requested.
                    }
                } else if (trimmedOption.startsWith("CAP:")) {
                    // Handle General Capabilities (e.g., CAP:acceptInsecureCerts=true)
                    try {
                        String capString = trimmedOption.substring(4);
                        String[] parts = capString.split("=", 2);
                        if (parts.length == 2) {
                            // Simple string/boolean type conversion
                            Object value = parts[1].equalsIgnoreCase("true") ? true : (parts[1].equalsIgnoreCase("false") ? false : parts[1]);
                            caps.put(parts[0], value);
                        }
                    } catch (Exception e) {
                        // Exception caught silently as requested.
                    }
                }
                // Arguments are handled below (ARG: is optional, default is ARG)
            }
        }
        // --- Driver Initialization Logic ---
        if (BrowserName.contains("edge")) {
            if (driverPath != null && !driverPath.isEmpty()) {
                System.setProperty("webdriver.edge.driver", driverPath);
            } else {
                WebDriverManager.edgedriver().setup();
            }
            EdgeOptions options = new EdgeOptions();
            // Edge uses 'ms:edgeOptions' for preferences
           /* if (!prefs.isEmpty()) {
                options.setCapability("ms:edgeOptions", prefs);
            }*/
            if (!prefs.isEmpty()) {
                options.setExperimentalOption("prefs", prefs);
            }
            if (BrowserName.contains("headless")) {
                options.addArguments("--headless=new");
            }
            // Apply arguments and capabilities
            applyOptions(options, customOptions, caps);
            driver = new EdgeDriver(options);
        } else if (BrowserName.contains("chrome")) {
            if (driverPath != null && !driverPath.isEmpty()) {
                System.setProperty("webdriver.chrome.driver", driverPath);
            } else {
                WebDriverManager.chromedriver().setup();
            }
            ChromeOptions options = new ChromeOptions();
            // Chrome uses 'prefs' for preferences
            if (!prefs.isEmpty()) {
                options.setExperimentalOption("prefs", prefs);
            }
            if (BrowserName.contains("headless")) {
                options.addArguments("--headless=new");
            }
            // Apply arguments and capabilities
            applyOptions(options, customOptions, caps);

            driver = new ChromeDriver(options);
        } else if (BrowserName.contains("firefox")) {
            if (driverPath != null && !driverPath.isEmpty()) {
                System.setProperty("webdriver.gecko.driver", driverPath);
            } else {
                WebDriverManager.firefoxdriver().setup();
            }
            FirefoxOptions options = new FirefoxOptions();
            // Firefox preferences are applied directly using addPreference
            prefs.forEach((key, value) -> options.addPreference(key, value.toString()));
            if (BrowserName.contains("headless")) {
                options.addArguments("-headless"); // Firefox uses -headless
            }
            // Apply arguments and capabilities
            applyOptions(options, customOptions, caps);
            driver = new FirefoxDriver(options);
        } else if (BrowserName.contains("safari")) {
            // Safari driver is managed by the OS and WebDriverManager setup is redundant
            if (driverPath != null && !driverPath.isEmpty()) {
                System.setProperty("webdriver.safari.driver", driverPath);
            }
            // Safari does not support traditional command-line arguments like Chrome/Edge
            SafariOptions options = new SafariOptions();
            // Apply capabilities (arguments are ignored for Safari)
            caps.forEach(options::setCapability);
            driver = new SafariDriver(options);
        } else {
            throw new IllegalArgumentException("Unsupported browser specified: " + BrowserName +
                    ". Supported browsers are: edge, chrome, firefox, safari, edge headless, chrome headless, firefox headless");
        }
        DriverManager.setDriver(driver);
        return driver;
    }
    /**
     * Helper method to apply command-line arguments and capabilities to the browser options.
     * This method assumes the options object has a compatible method (like addArguments/setCapability).
     */
    private void applyOptions(org.openqa.selenium.MutableCapabilities options, String customOptions, Map<String, Object> caps) {
        // 1. Apply Command-Line Arguments (ARG:)
        if (customOptions != null && !customOptions.isEmpty()) {
            String[] optionsArray = customOptions.split(",");
            for (String option : optionsArray) {
                String trimmedOption = option.trim();
                // Only process items that are arguments (ARG: or no prefix)
                if (!trimmedOption.isEmpty()) {
                    if (trimmedOption.startsWith("ARG:") || (!trimmedOption.contains(":") && !trimmedOption.contains("="))) {
                        String arg = trimmedOption.startsWith("ARG:") ? trimmedOption.substring(4) : trimmedOption;
                        // We check the options type to ensure we call the right method
                        if (options instanceof ChromeOptions) {
                            ((ChromeOptions) options).addArguments(arg);
                        } else if (options instanceof EdgeOptions) {
                            ((EdgeOptions) options).addArguments(arg);
                        } else if (options instanceof FirefoxOptions) {
                            ((FirefoxOptions) options).addArguments(arg);
                        }
                    }
                }
            }
        }
        // 2. Apply Capabilities (CAP:)
        caps.forEach(options::setCapability);
    }
}