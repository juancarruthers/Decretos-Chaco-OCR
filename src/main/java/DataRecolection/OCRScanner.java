package DataRecolection;


import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class OCRScanner {

    private String decretoName;
    private Tesseract tessInstance;

    protected OCRScanner(String decreto){
        this.decretoName = decreto;
    }

    protected List <String> scanDocument(ClassLoader classLoader) {

        PropertyConfigurator.configure(classLoader.getResource("log4j.properties").getPath()); // sets properties file for log4j
        this.tessInstance = new Tesseract();
        this.tessInstance.setDatapath(classLoader.getResource("tessdata").getPath());
        this.tessInstance.setLanguage("spa");

        try {
            List <String> decretoPages = new ArrayList<String>();
            for(File decretoPageFile : this.splitPages()) {
                decretoPages.add(this.tessInstance.doOCR(decretoPageFile));
                FileUtils.deleteQuietly(decretoPageFile);
            }

            return decretoPages;

        } catch (TesseractException e) {
            System.err.println(e.getMessage());
            return null;
        }

    }

    protected List<File> splitPages() {

        try {

            //Loading an existing PDF document
            File decreto = new File("temp-downloads/" + this.decretoName + ".pdf");
            PDDocument doc = PDDocument.load(decreto);

            //Instantiating Splitter class
            Splitter splitter = new Splitter();

            //splitting the pages of a PDF document
            List<PDDocument> Pages = splitter.split(doc);

            //Creating an iterator
            Iterator<PDDocument> iterator = Pages.listIterator();

            //Saving each page as an individual document
            int i = 1;
            List<File> decretoPages = new ArrayList<File>();

            while (iterator.hasNext()) {
                PDDocument pd = iterator.next();
                pd.save("temp-downloads/" + this.decretoName + "(" + i + ").pdf");
                decretoPages.add(new File("temp-downloads/" + this.decretoName + "(" + i + ").pdf"));
                i++;
            }

            doc.close();

            return decretoPages;
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

}
