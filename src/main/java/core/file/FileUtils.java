package core.file;

import java.io.File;
import java.io.IOException;

public class FileUtils {

    public static void fileCleaner(String folderName) throws IOException {
        String path= System.getProperty("user.dir") + File.separator+folderName;
        File directory = new File(path);
        if(!directory.exists())
        {
            return;
        }
        File[] files = directory.listFiles();
        if(files != null)
        {
            for(File file : files)
            {
                deleteRecursively(file);
            }
        }
    }
    private static void deleteRecursively(File file)
    {
        if(file.isDirectory())
        {
            for(File subFile :file.listFiles())
            {
                deleteRecursively(subFile);
            }
        }
        file.delete();
    }
}
