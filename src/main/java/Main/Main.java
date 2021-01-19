package Main;

import DataRecolection.FileManager;
import DataRecolection.SeleniumManager;
import org.openqa.selenium.WebDriverException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String [] args){
        try{
            final FileManager fileManager = new FileManager();
            List<String> programProperties = new ArrayList<>();
            programProperties.add("CPU-Usage-Percentage");
            programProperties.add("Browser");
            HashMap<String, String> propertiesValues = fileManager.getPropValues("program.properties", programProperties);

            Scanner scanner = new Scanner(System.in);
            System.out.print("Porcentaje entero de CPU a utilizar [1, 100] (Enter para usar " + propertiesValues.get("CPU-Usage-Percentage") + "%): ");
            String scannedValue = scanner.nextLine();
            int stringParsed = tryParseInt(scannedValue);
            while (((stringParsed < 1) || (stringParsed > 100)) && (!scannedValue.equals(""))){
                System.out.print("\tIngresar valores numericos enteros en el intervalo [1, 100] (Enter para usar " + propertiesValues.get("CPU-Usage-Percentage") + "%): ");
                scannedValue = scanner.nextLine();
                stringParsed = tryParseInt(scannedValue);
            }
            if (!scannedValue.equals("")){
                propertiesValues.replace("CPU-Usage-Percentage", scannedValue);
            }

            System.out.println("\nIngresar numero para seleccionar navegador utilizado (Enter para usar " + propertiesValues.get("Browser") + "): \n\t1) Google Chrome\n\t2) Firefox\n\t3) Safari");
            scannedValue = scanner.nextLine();
            stringParsed = tryParseInt(scannedValue);
            while (((stringParsed < 1) || (stringParsed > 3)) && (!scannedValue.equals(""))){
                System.out.print("\tIngresar valores numericos enteros en el intervalo [1, 3]: ");
                scannedValue = scanner.nextLine();
                stringParsed = tryParseInt(scannedValue);
            }
            if (!scannedValue.equals("")){
                switch (scannedValue){
                    case "1": propertiesValues.replace("Browser", "CHROME");
                    break;
                    case "2": propertiesValues.replace("Browser", "FIREFOX");
                    break;
                    case "3": propertiesValues.replace("Browser", "SAFARI");
                    break;

                }
            }

            fileManager.writePropValues("program.properties", propertiesValues);

            SeleniumManager.recolectData();
        }catch (WebDriverException e){
            if (e.getMessage().contains("error 1: server disconnected")){
                SeleniumManager.recolectData();
            }else{
                System.out.println(e.getMessage());
            }
        }

    }

    public static int tryParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
