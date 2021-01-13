package DataRecolection;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.*;

public class FileManager{

    private String resourcesPath;

    public FileManager(){
        this.resourcesPath = getClass().getResource("..").getPath();
    }

    public String getResourcesPath() {
        return resourcesPath;
    }

    public void downloadFile (String fileName, String fileUrl){

        try {
            final File directory = new File(this.resourcesPath + "temp-downloads");
            //Check Downloads folder exists
            if (!directory.exists()){
                directory.mkdir();
            }

            final File decreto = new File(directory.getPath() + "/" + fileName + ".pdf");
            //Check Decreto's relative id (row table id) not exists
            if (decreto.exists()){
                this.deleteResource(fileName);
            }

            final URL decretosPage = new URL(fileUrl);
            FileUtils.copyURLToFile(decretosPage, decreto);


        } catch (IOException e) {
            System.out.println(e);

        }
    }

    public void deleteResource (String name){

        final File resource = new File(this.resourcesPath + name);

        if (resource.exists()) {
            FileUtils.deleteQuietly(resource);
        }

    }

    public HashMap<String, String> getPropValues(String propertiesFilePath, List<String> propertiesKeys){

        Properties prop = new Properties();
        HashMap<String, String> properties = new HashMap();

        try {

            InputStream inputStream = new FileInputStream(propertiesFilePath);
            if (inputStream != null) {
                prop.load(inputStream);
                // get the properties values
                for (String property: propertiesKeys) {
                    properties.put(property, prop.getProperty(property));
                }

            }else{
                throw new FileNotFoundException("Properties File '" + propertiesFilePath + "' Not Found!");
            }

            inputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return properties;

    }

}
