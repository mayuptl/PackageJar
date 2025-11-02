package core.highlight;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Utility class for performing standard Selenium actions while applying a
 * persistent visual highlight (border) to the target element.
 * * The highlight remains on the element until a page navigation occurs (DOM reset).
 */
public class HighlightUtil {

    private final WebDriver driver;
    /**
     * Initializes the Highlight utility with the WebDriver instance.
     * * @param driver The active WebDriver instance.
     */
    public HighlightUtil(WebDriver driver) {
        this.driver = driver;
    }
    //Utility method to border to an element
    private void applyHighlight(WebElement element, String color) {
        scrollTo(element);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Script for highlight
        String script = "arguments[0].style.border='3px dashed "+color+"'";
        js.executeScript(script, element);
    }
    /**
     * Clicks the specified element after applying a green highlight.
     * The highlight is applied before the click action executes.
     * * @param element The {@link WebElement} to be clicked.
     */
    public void click(WebElement element) {
        applyHighlight(element, "green");
        element.click();
    }

    /**
     * Clears the element and then enters the specified text after applying a green highlight.
     * * @param element The input {@link WebElement} to clear and send keys to.
     * @param value The text to be entered into the input field.
     */
    public void sendKeys(WebElement element, String value) {
        applyHighlight(element, "green");
        element.clear();
        element.sendKeys(value);
    }
    /**
     * Appends text to the next of existing text in an input field after applying a green highlight.
     * This method does not clear the input field first.
     * * @param element The input {@link WebElement} to append keys to.
     * @param value The text to be appended.
     */
    public void sendKeysAppend(WebElement element, String value) {
        applyHighlight(element, "green");
        element.sendKeys(value);
    }
    /**
     * Retrieves the visible text of an element after applying a green highlight.
     * * @param element The {@link WebElement} from which to retrieve the text.
     * @return The visible text of the element.
     */
    public String getText(WebElement element) {
        applyHighlight(element, "green");
        return element.getText();
    }
    /**
     * Compares the actual text of an element with an expected value, applying
     * visual feedback based on the result.
     * - Applies a 'green' border on match.
     * - Applies a 'red' border on mismatch.
     * * @param element The {@link WebElement} whose text will be compared.
     * @param expectedText The expected text value.
     * @return true if the texts match, false otherwise.
     */
    public boolean compareText(WebElement element, String expectedText) {
        String actualText = element.getText();
        if (expectedText.equals(actualText)) {
            applyHighlight(element, "green");
            return true;
        } else {
            applyHighlight(element, "red");
            return false;
        }
    }
    /**
     * Checks if an element is currently displayed on the page.
     * - Applies a 'green' border if the element is displayed.
     * - Suppresses {@link NoSuchElementException} and returns false if the element is not found.
     * * @param element The {@link WebElement} to check.
     * @return true if the element is displayed, false if not displayed or not found.
     * @throws Exception If an unexpected Selenium exception occurs (e.g., StaleElementReferenceException).
     */
    public boolean isDisplayed(WebElement element) {
        try {
            boolean displayed = element.isDisplayed();
            if (displayed) applyHighlight(element, "green");
            return displayed;
        } catch (NoSuchElementException e) {
            // Only catch NoSuchElement to indicate it's not present
            return false;
        } catch (Exception e) {
            // Re-throw other unexpected exceptions like StaleElementReferenceException
            // so the test fails clearly.
            throw e;
        }
    }
    /**
     * Scrolls the element into the viewport only if it is not currently in view.
     * Uses smooth scrolling behavior to center the element.
     * * @param element The {@link WebElement} to scroll to.
     */
    public void scrollTo(WebElement element) {
        // Check if the element is in the viewport
        JavascriptExecutor js = (JavascriptExecutor) driver;
        boolean isElementInView = (boolean) js.executeScript(
                "var rect = arguments[0].getBoundingClientRect();" +
                        "return (rect.top >= 0 && rect.bottom <= window.innerHeight);", element);
        // If not in viewport, scroll to the element
        if (!isElementInView) {
            js.executeScript("arguments[0].scrollIntoView({ behavior: 'smooth', block: 'center' });", element);

        }
    }
//========================================================================================//
    // Script for blink effect
        /*  js.executeScript(
                "arguments[0].style.border='2px dashed ' + arguments[1];" +
                        "arguments[0].style.animation = 'blink 0.3s 3';" +
                        "var style= document.createElement('style');" +
                        "style.innerHTML = '@keyframes blink { 0% { border-color: transparent; } 50% { border-color: ' + arguments[1] +'; } 100% { border-color: transparent; }}';" +
                        "document.head.appendChild(style);", element, color);
        */

/*
    private void removeBorder(WebElement element) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String script = "arguments[0].style.border='';";
        js.executeScript(script, element);
    }

    private void applyBg(WebElement element, String color) {
        scrollTo(element);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        // Script for highlight
        js.executeScript("arguments[0].style.backgroundColor = arguments[1];", element, color);
        // Script for blink effect
        *//* js.executeScript("arguments[0].style.backgroundColor = arguments[1];" +
                "arguments[0].style.animation = 'blink 0.1s 3';" +
                "var style= document.createElement('style');" +
                "style.innerHTML = '@keyframes blink { 0% { border-color: transparent; } 50% { border-color: ' + arguments[1] +'; } 100% { border-color: transparent; }}';" +
                "document.head.appendChild(style);", element, color);
        *//*
    }*/
}
