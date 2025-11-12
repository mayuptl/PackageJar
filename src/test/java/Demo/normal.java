package Demo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.annotations.Test;

public class normal{

    @Test
    public void swara()
    {
        Logger log = LogManager.getLogger("Swara");
        log.info("INFO");
        log.warn("WARN");
        log.error("ERROR");
        log.debug("DEBUG");
        log.trace("TRACE");
        log.fatal("FATAL");
    }
}
