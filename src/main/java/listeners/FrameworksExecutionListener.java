package listeners;

import core.initializer.FrameworkInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IExecutionListener;
import java.io.IOException;

public class FrameworksExecutionListener implements IExecutionListener {

    @Override
    public void onExecutionStart()
    {
        try{
            FrameworkInitializer.init();
            System.out.println("INFO: Framework initialization successful");
        } catch (IOException e) {
           Logger log = LogManager.getLogger("FrameworksExecutionListener");
           log.error("FATAL: Framework initialization failed during onExecutionStart.");
        }
    }
    @Override
    public void onExecutionFinish()
    {
       /* ExtentReports extent = ExtentManager.getReportInstance();
        if(extent != null) {
            extent.flush();
        }*/
    }

}
