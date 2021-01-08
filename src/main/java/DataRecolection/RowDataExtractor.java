package DataRecolection;
import org.openqa.selenium.*;

public class RowDataExtractor implements Runnable{

    private WebDriver driver;
    private int rowId;

    public RowDataExtractor(WebDriver driver, int id){
        this.driver = driver;
        this.rowId = id;
    }


    @Override
    public void run() {

        for (int i = 1; i <= 5; i++){
            System.out.print("Fila: " + this.rowId + "/Col: " + i + " ->> " + this.driver.findElement(By.xpath("//div[@id='Res']/table/tbody/tr[" + this.rowId + "]/td[" + i + "]")).getText() + "\t");
        }

        final String url = this.driver.findElement(By.xpath("//div[@id='Res']/table/tbody/tr[" + this.rowId + "]/td[" + 6 + "]/a")).getAttribute("href");
        FileManager.downloadFile(Integer.toString(this.rowId), url);
    }
}
