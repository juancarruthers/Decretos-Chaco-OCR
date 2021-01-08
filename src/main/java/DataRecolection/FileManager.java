package DataRecolection;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public final class FileManager{

    public static File downloadFile (String path, String fileName, String fileUrl){

        try {
            final File directory = new File(path);
            //Check Downloads folder exists
            if (!directory.exists()){
                directory.mkdir();
            }

            final File decreto = new File(path + "/" + fileName + ".pdf");
            //Check Decreto's relative id (row table id) not exists
            if (decreto.exists()){
                FileUtils.deleteQuietly(decreto);
            }

            final URL decretosPage = new URL(fileUrl);
            FileUtils.copyURLToFile(decretosPage, decreto);

            return decreto;

        } catch (IOException e) {
            System.out.println(e);
            return null;
        }
    }

}
