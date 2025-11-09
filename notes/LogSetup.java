package core.logging;
import core.config.ConfigReader;

public class LogSetup {
    // --- STATIC INITIALIZATION BLOCK ---
    // This block executes the moment the LogSetup class is loaded by the JVM,
    // which should be before the static logger in your test class is initialized.
    static {
        System.out.println("LOG4J2: Setting system properties for log directory and file name...");
        try {
            initializeLog4jPath();
        } catch (Exception e) {
            System.err.println("FATAL LOG SETUP ERROR: Could not set Log4j2 path properties.");
            e.printStackTrace();
        }
    }
    public static void initializeLog4jPath() {
        /*// 1. Read the path from your configuration file
        String logDir = ConfigReader.getStrProp("LOG_FILE_DIR", "execution-output/test-logs");
        String logFileName=ConfigReader.getStrProp("LOG_FILE_NAME", "Logs.log");*/
        // 2. Set it as a System Property.
        // Log4j2 can read System Properties directly using the ${sys:key} syntax.
        /*System.setProperty("log4j2.logDir", logDir);
        System.setProperty("log4j2.fileName", logFileName);*/
       // System.setProperty("log4);
    }
}