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

    private Tesseract scanner;

    public OCRScanner(String tesseractResourcesPath){
        this.configurateTesseractScanner(tesseractResourcesPath);
    }


    private void configurateTesseractScanner(String tesseractResourcesPath){

        this.scanner = new Tesseract();

        try {

            PropertyConfigurator.configure(tesseractResourcesPath + "log4j.properties"); // sets properties file for log4j
            this.scanner.setDatapath(tesseractResourcesPath + "tessdata");
            this.scanner.setLanguage("spa");
            this.scanner.setTessVariable("user_defined_dpi", "70");

        }catch (NullPointerException e){
            System.err.println(e.getMessage());
        }
    }

    public List<String> scanPDFDocument(String documentPath) {

        List <String> documentPagesScanned = new ArrayList<>();

        try {

            for(File documentPageFile : splitPages(documentPath)) {
                documentPagesScanned.add(this.scanner.doOCR(documentPageFile));
                FileUtils.deleteQuietly(documentPageFile);
            }



        } catch (TesseractException | NullPointerException e) {
            System.err.println(e.getStackTrace());
        }

        return documentPagesScanned;
    }

    private List<File> splitPages(String documentPath) {

        List<File> documentPages = new ArrayList<>();

        try {

            //Loading an existing PDF document
            File document = new File(documentPath);
            PDDocument pdfDocument = PDDocument.load(document);

            //Instantiating Splitter class
            Splitter splitter = new Splitter();

            //splitting the pages of a PDF document
            List<PDDocument> pages = splitter.split(pdfDocument);

            //Creating an iterator
            Iterator<PDDocument> iterator = pages.listIterator();

            //Saving each page as an individual document
            int i = 1;
            final int fileExtensionIndex = documentPath.indexOf('.');
            final String filePathWithoutExtension = documentPath.substring(0, fileExtensionIndex);

            while (iterator.hasNext()) {
                PDDocument pd = iterator.next();
                pd.save(filePathWithoutExtension + "(" + i + ").pdf");
                pd.close();
                documentPages.add(new File(filePathWithoutExtension + "(" + i + ").pdf"));
                i++;
            }

            pdfDocument.close();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return documentPages;
    }

}
