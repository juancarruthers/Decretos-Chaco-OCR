package DataRecolection;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.*;

final class DecretosChacoDatabase {

    private static final MongoClient mongoClient = MongoClients.create("mongodb+srv://"+ getCredentials().get("mongodb-user") +":"+ getCredentials().get("mongodb-password") +"@"+ getCredentials().get("mongodb-cluster") +"/"+ getCredentials().get("mongodb-db") +"?retryWrites=true&w=majority");
    private static final MongoDatabase database = mongoClient.getDatabase("Decretos-Chaco");
    private Document decretoInfo;
    private List<Document> decretoPages;

    protected DecretosChacoDatabase(List<String> decretoInfo, List<String> decretoPages){
        this.createDocumentDecretoInfo(decretoInfo);
        this.createDocumentDecretoPages(decretoPages);
    }

    private void createDocumentDecretoInfo(List<String> decretoInfo){
        this.decretoInfo = new Document();
        this.decretoInfo.append("_id", decretoInfo.get(1) + decretoInfo.get(2));
        this.decretoInfo.append("gestion", decretoInfo.get(0));
        this.decretoInfo.append("anio", decretoInfo.get(1));
        this.decretoInfo.append("decreto", decretoInfo.get(2));
        this.decretoInfo.append("fecha", decretoInfo.get(3));
        this.decretoInfo.append("tema", decretoInfo.get(4));
        this.decretoInfo.append("enlace", decretoInfo.get(5));
    }

    private void createDocumentDecretoPages(List<String> decretoPages){
        this.decretoPages = new ArrayList<>();
        int i = 1;
        for (String decretoPage : decretoPages) {
            Document document = new Document();
            document.append("_id", (String) decretoInfo.get("_id") + i);
            document.append("id-decreto-info", decretoInfo.get("_id"));
            document.append("numero-pagina", i);
            document.append("contenido-pagina", decretoPage);
            this.decretoPages.add(document);
            i++;
        }
    }

    protected void insertDecretoInfo() throws MongoException{

        try {

            MongoCollection collection = database.getCollection("Decreto-Info");

            //insertion
            collection.insertOne(this.decretoInfo);

        } catch (MongoException e) {
            System.out.println(e.getMessage());
            throw e;
        }

    }

    protected void deleteDecretoInfo(){

        try {

            MongoCollection collection = database.getCollection("Decreto-Info");

            //insertion
            collection.deleteOne(this.decretoInfo);

        } catch (MongoException e) {
            System.out.println(e.getMessage());

        }

    }

    protected void insertDecretoPages() throws MongoException{

        try {

            MongoCollection collection = database.getCollection("Decreto-Pages");

            //insertion
            collection.insertMany(this.decretoPages);

        } catch (MongoException e) {
            System.out.println(e.getMessage());
            this.deleteDecretoInfo();
            throw e;
        }
    }

    protected static void updateLastDate(String lastDate) {

        try {

            MongoCollection collection = database.getCollection("Fecha-Ultima");

            Document query = new Document();
            query.append("_id", "fecha");

            Document newDocument = new Document();
            newDocument.put("fecha", lastDate);

            Document updateObject = new Document();
            updateObject.put("$set", newDocument);

            collection.updateOne(query, updateObject);

        } catch (MongoException e) {
            System.out.println(e.getMessage());
        }
    }

    protected static boolean checkDecretoInserted(String idDecreto) {
        try {

            MongoCollection collection = database.getCollection("Decreto-Info");

            Document query = new Document();
            query.append("_id", idDecreto);

            Document result = (Document) collection.find(query).first();

            return result != null;

        } catch (MongoException e) {
            System.out.println(e.getMessage());
            return true;
        }
    }

    protected static String getLastDate() {
        try {

            MongoCollection collection = database.getCollection("Fecha-Ultima");

            Document query = (Document) collection.find().first();

            return (String) query.get("fecha");

        } catch (MongoException e) {
            System.out.println(e.getMessage());
            return "";
        }
    }

    protected static void close() {
        mongoClient.close();
    }

    public static HashMap<String, String> getCredentials() {

        FileManager fileMan = new FileManager();
        List <String> listOfProperties = new ArrayList<>();

        listOfProperties.add("mongodb-user");
        listOfProperties.add("mongodb-password");
        listOfProperties.add("mongodb-cluster");
        listOfProperties.add("mongodb-db");

        return fileMan.getPropValues("mongodb.credentials", listOfProperties);

    }
}
