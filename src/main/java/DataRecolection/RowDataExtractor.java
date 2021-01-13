package DataRecolection;
import org.openqa.selenium.*;

import java.util.ArrayList;
import java.util.List;

public class RowDataExtractor implements Runnable{

    private WebDriver driver;
    private int rowId;

    public RowDataExtractor(WebDriver driver, int id){
        this.driver = driver;
        this.rowId = id;
    }


    @Override
    public void run() {
        List<String> decretoInfo = new ArrayList<>();
        for (int i = 1; i <= 5; i++){
            String value = this.driver.findElement(By.xpath("//div[@id='Res']/table/tbody/tr[" + this.rowId + "]/td[" + i + "]")).getText();
            decretoInfo.add(value);
        }

        final String url = this.driver.findElement(By.xpath("//div[@id='Res']/table/tbody/tr[" + this.rowId + "]/td[" + 6 + "]/a")).getAttribute("href");
        decretoInfo.add(url);

        FileManager fileManager = new FileManager();
        fileManager.downloadFile(Integer.toString(this.rowId), url);

        OCRScanner scanner = new OCRScanner(getClass().getClassLoader().getResource(".").getPath());
        List<String> decretoPages = scanner.scanPDFDocument(getClass().getClassLoader().getResource("temp-downloads/").getPath() + this.rowId + ".pdf");

        DecretosChacoDatabase.insertNewDecreto(decretoInfo, decretoPages);
    }
}
