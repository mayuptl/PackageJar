package core.initializer;

import core.config.ConfigReader;
import org.apache.logging.log4j.core.config.Configurator;
import java.io.IOException;

public class FrameworkInitializer {
    private static boolean initialized = false;
    public static synchronized void init() throws IOException
    {
        if(initialized) return;
        ConfigReader.getStrProp("BROWSER");
        Configurator.initialize(null,"src/main/resources/log4j2.xml");
        initialized = true;
    }
}
