package DataRecolection;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.URL;
import java.util.*;

public class FileManager{

    private String resourcesPath;

    public FileManager(){
        this.resourcesPath = getClass().getResource("..").getPath() + "../resources";
    }

    //Include dash "/" at the end of a folder
    public String getResourcesPath(String name) {
        return resourcesPath + "/" + name;
    }

    public void downloadFile (String fileName, String fileUrl){

        try {
            final File directory = new File(this.getResourcesPath("temp-downloads"));
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

        final File resource = new File(this.getResourcesPath(name));

        if (resource.exists()) {
            FileUtils.deleteQuietly(resource);
        }

    }

    public HashMap<String, String> getPropValues(String propertiesFileName, List<String> propertiesKeys){

        Properties prop = new Properties();
        HashMap<String, String> properties = new HashMap();

        try {

            InputStream inputStream = new FileInputStream(this.getResourcesPath(propertiesFileName));
            if (inputStream != null) {
                prop.load(inputStream);
                // get the properties values
                for (String property: propertiesKeys) {
                    properties.put(property, prop.getProperty(property));
                }

            }else{
                throw new FileNotFoundException("Properties File '" + this.getResourcesPath(propertiesFileName) + "' Not Found!");
            }

            inputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return properties;

    }

    public void writePropValues(String propertiesFileName, HashMap<String, String> propertiesKeys){

        Properties properties = new Properties();

        try {

            InputStream inputStream = new FileInputStream(this.getResourcesPath(propertiesFileName));
            if (inputStream != null) {
                properties.load(inputStream);
                // get the properties values
                for (Map.Entry<String, String> propertyEntry: propertiesKeys.entrySet()) {
                    properties.setProperty(propertyEntry.getKey(), propertyEntry.getValue());

                }
                properties.store(new FileOutputStream(this.getResourcesPath(propertiesFileName)), null);

            }else{
                throw new FileNotFoundException("Properties File '" + this.getResourcesPath(propertiesFileName) + "' Not Found!");
            }

            inputStream.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }


    }

}
