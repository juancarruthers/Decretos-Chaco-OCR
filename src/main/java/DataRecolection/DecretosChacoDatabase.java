package DataRecolection;

import com.mongodb.*;
import com.mongodb.client.*;
import org.bson.Document;
import java.util.*;

final class DecretosChacoDatabase {

    private static final MongoClient mongoClient = MongoClients.create("mongodb+srv://"+ getCredentials().get("mongodb-user") +":"+ getCredentials().get("mongodb-password") +"@"+ getCredentials().get("mongodb-cluster") +"/"+ getCredentials().get("mongodb-db") +"?retryWrites=true&w=majority");
    private static final MongoDatabase database = mongoClient.getDatabase("Decretos-Chaco");

    protected static void insertNewDecreto(List<String> decretoInfo, List<String> decretoPages) {
        final ClientSession clientSession = mongoClient.startSession();
        TransactionOptions txnOptions = TransactionOptions.builder()
                .readPreference(ReadPreference.primary())
                .readConcern(ReadConcern.LOCAL)
                .writeConcern(WriteConcern.MAJORITY)
                .build();

        TransactionBody txnBody = new TransactionBody<String>() {
            public String execute() {

                if (checkDecretoInserted(decretoInfo.get(1) + decretoInfo.get(2))) {
                    insertDecretoInfo(decretoInfo);
                    insertDecretoPages(decretoInfo, decretoPages);
                    updateLastDate(decretoInfo.get(3));
                }
                return "Inserted into collections in different databases";
            }
        };

        try {
            clientSession.withTransaction(txnBody, txnOptions);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        } finally {
            clientSession.close();
        }
    }

    private static void insertDecretoInfo(List<String> decretoInfo) {

        try {

            MongoCollection collection = database.getCollection("Decreto-Info");

            //insertion
            Document document = new Document();
            document.append("_id", decretoInfo.get(1) + decretoInfo.get(2));
            document.append("gestion", decretoInfo.get(0));
            document.append("anio", decretoInfo.get(1));
            document.append("decreto", decretoInfo.get(2));
            document.append("fecha", decretoInfo.get(3));
            document.append("tema", decretoInfo.get(4));
            document.append("enlace", decretoInfo.get(5));
            collection.insertOne(document);
        } catch (MongoException e) {
            System.out.println(e.getMessage());
        }

    }

    private static void insertDecretoPages(List<String> decretoInfo, List<String> decretoPages) {

        try {

            MongoCollection collection = database.getCollection("Decreto-Pages");

            //insertion
            Document document = new Document();
            int i = 1;
            for (String decretoPage : decretoPages) {
                document.append("_id", decretoInfo.get(1) + decretoInfo.get(2) + i);
                document.append("id-decreto-info", decretoInfo.get(1) + decretoInfo.get(2));
                document.append("numero-pagina", i);
                document.append("contenido-pagina", decretoPage);
                collection.insertOne(document);
            }
        } catch (MongoException e) {
            System.out.println(e.getMessage());
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

    private static boolean checkDecretoInserted(String idDecreto) {
        try {

            MongoCollection collection = database.getCollection("Decreto-Info");

            Document query = new Document();
            query.append("_id", idDecreto);

            Document result = (Document) collection.find(query).first();

            return result == null;

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

        return fileMan.getPropValues(fileMan.getResourcesPath() + "mongodb.credentials", listOfProperties);

    }
}
