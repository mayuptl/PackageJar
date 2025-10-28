package core.wait;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class WaitUtil {

    private final WebDriver driver;
    public WaitUtil(WebDriver driver)
    {
        this.driver = driver;
    }
    // Common method for wait
    private WebDriverWait getWait(int timeOutInSec) {
        return new WebDriverWait(driver, Duration.ofSeconds(timeOutInSec));
    }

    /** Hard stop, same as Thread sleep */
    public static void staticWait(int seconds)
    {
        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(seconds));
    }
    /**
     * Wait for element to be visible
     *
     * @return WebElement
     */
    public WebElement waitForVisibilityOf(int timeOutInSec, WebElement element) {
       return getWait(timeOutInSec).until(ExpectedConditions.visibilityOf(element));
    }
    /**
     * Wait for element to be visible
     * Uses By locator
     * @return WebElement
     */
    public WebElement waitForVisibilityOfLocated(int timeOutInSec, By locator) {
        return getWait(timeOutInSec).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
    /** Wait for all elements in list to be visible */
    public void waitForVisibilityOfAll(int timeOutInSec, List<WebElement> elements) {
        getWait(timeOutInSec).until(ExpectedConditions.visibilityOfAllElements(elements));
    }
    /** Wait for at least one element in list to be visible */
    public void waitForVisibilityOfAtLeastOne(int timeOutInSec, List<WebElement> elements) {
        getWait(timeOutInSec).until(d -> {
            try {
                return elements.stream().anyMatch(WebElement::isDisplayed);
            } catch (StaleElementReferenceException e) {
                return false;
            }
        });
        //getWait(timeOutInSec).until(ExpectedConditions.visibilityOfAnyElements(elements));
    }
    /** Wait for element to be invisible */
    public void waitForInVisibilityOf(int timeOutInSec, WebElement element) {
        getWait(timeOutInSec).until(ExpectedConditions.invisibilityOf(element));
    }
    /** Wait for all elements in list to be invisible (or not displayed) */
    public void waitForInVisibilityOfAll(int timeOutInSec, List<WebElement> elements) {
        for (WebElement element : elements) {
            try {
                getWait(timeOutInSec).until(ExpectedConditions.invisibilityOf(element));
            } catch (StaleElementReferenceException ignored) {
                // If element is stale, it's already gone — no need to wait further
            }
        }
        //getWait(timeOutInSec).until(ExpectedConditions.invisibilityOfAllElements(elements));
    }
    /** Wait for element to be clickable */
    public void waitForToBeClickable(int timeOutInSec, WebElement element) {
        getWait(timeOutInSec).until(ExpectedConditions.elementToBeClickable(element));
    }
    /** Wait for all elements in list to be clickable */
    public void waitForToBeClickableAll(int timeOutInSec, List<WebElement> elements) {
        for (WebElement element : elements) {
            getWait(timeOutInSec).until(ExpectedConditions.elementToBeClickable(element));
        }
    }
    /** Wait for url to be */
    public void waitForUrlToBe(int timeOutInSec, String url)
    {
        getWait(timeOutInSec).until(ExpectedConditions.urlContains(url));
    }
    /** Wait for text to be present in element */
    public void waitForTextToBePresentIn(int timeOutInSec, WebElement element, String text) {
        getWait(timeOutInSec).until(ExpectedConditions.textToBePresentInElement(element, text));
    }
    /** Wait for alert to be present */
    public void waitForAlert(int timeOutInSec) {
        getWait(timeOutInSec).until(ExpectedConditions.alertIsPresent());
    }
    /** Wait for title to contain text */
    public void waitForTitleContains(int timeOutInSec, String title) {
        getWait(timeOutInSec).until(ExpectedConditions.titleContains(title));
    }
    /** Wait for URL to contain text */
    public void waitForUrlContains(int timeOutInSec, String urlFragment) {
        getWait(timeOutInSec).until(ExpectedConditions.urlContains(urlFragment));
    }
    /** Wait for element attribute to have specific value */
    public void waitForAttributeToBe(int timeOutInSec, WebElement element, String attribute, String value) {
        getWait(timeOutInSec).until(ExpectedConditions.attributeToBe(element, attribute, value));
    }
    /** Wait for the page load */
    public boolean waitForPageLoad(int timeOutInSec)
    {
        try {
            // Add a custom message to the TimeoutException for better debugging
            getWait(timeOutInSec).withMessage("Timeout waiting for page to load completely (readyState='complete').")
                    .until((WebDriver d) -> ((JavascriptExecutor) d)
                            .executeScript("return document.readyState").equals("complete"));
            // log.info("Page loaded successfully after waiting for " + timeOutInSec + " seconds.");
            return true; // Return true on success
        } catch (TimeoutException e) {
            // Log a specific message for the expected timeout
            System.out.println("Page did not load within " + timeOutInSec + " seconds. TimeoutException: " + e.getMessage());
            return false; // Return false on failure
        } catch (Exception e) {
            // Catch any other unexpected exceptions (e.g., driver issues)
            System.out.println("An unexpected error occurred while waiting for page load: " + e.getMessage());
            return false; // Return false for unexpected errors
        }
    }
    /** Static method to Wait for the page load, Need driver as arg */
    // Assuming 'driver' is accessible and log is an existing logger object
    public static boolean waitForPageLoad(WebDriver driver1,int timeOutInSec)
    {
        try {
            WebDriverWait wait = new WebDriverWait(driver1, Duration.ofSeconds(timeOutInSec));
            // Add a custom message to the TimeoutException for better debugging
            wait.withMessage("Timeout waiting for page to load completely (readyState='complete').")
                    .until((WebDriver d) -> ((JavascriptExecutor) d)
                    .executeScript("return document.readyState").equals("complete"));
            // log.info("Page loaded successfully after waiting for " + timeOutInSec + " seconds.");
            return true; // Return true on success
        } catch (TimeoutException e) {
            // Log a specific message for the expected timeout
            System.out.println("Page did not load within " + timeOutInSec + " seconds. TimeoutException: " + e.getMessage());
            return false; // Return false on failure
        } catch (Exception e) {
            // Catch any other unexpected exceptions (e.g., driver issues)
            System.out.println("An unexpected error occurred while waiting for page load: " + e.getMessage());
            return false; // Return false for unexpected errors
        }
    }

    /** Fluent wait for visibility , use pollingMillis 100  */
    public void fluentWaitForVisibility(WebElement element, int timeoutInSec, int pollingMillis)
    {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutInSec))
                .pollingEvery(Duration.ofMillis(pollingMillis))
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.visibilityOf(element));
    }
}
