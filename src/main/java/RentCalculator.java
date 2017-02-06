import com.mongodb.*;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import spark.template.velocity.VelocityTemplateEngine;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static spark.Spark.*;

public class RentCalculator {


    public static void main(String[] args) {
        RentCalculator rc = new RentCalculator();

        //Test

        /*EntityManagerFactory entityManagerFactory = Persistence.createEntityManagerFactory("test");

        EntityManager entityManager = entityManagerFactory.createEntityManager();

        entityManager.getTransaction().begin();

        // create a Person
        Entry entry = new Entry("Test", "Test", "test", 1, new Date());

        // persist organizer (will be cascaded to hikes)
        entityManager.persist( entry );

        entityManager.getTransaction().commit();

        // get a new EM to make sure data is actually retrieved from the store and not Hibernate's internal cache
        entityManager.close();

        //End*/

        staticFileLocation("/public");

        rc.loadOrCheckDefault();

        get("/index", IndexController::serveHomePage, new VelocityTemplateEngine());

        get("/newEntry", IndexController::serverNewEntryPage, new VelocityTemplateEngine());
        post("/newEntry", IndexController::serverNewEntryPage, new VelocityTemplateEngine());

        post("/updateEntry", EntryController::updateEntry);
        post("/getSettings", EntryController::getSettings);

        get("/monthlySummary", IndexController::serverMonthlySummaryPage, new VelocityTemplateEngine());
        post("/monthlySummary", IndexController::serverMonthlySummaryPage, new VelocityTemplateEngine());

        get("/monthlyView", IndexController::serverMonthlyViewPage, new VelocityTemplateEngine());
        post("/monthlyView", IndexController::serverMonthlyViewPage, new VelocityTemplateEngine());

        get("/import/new", ImportController::serveHomePage, new VelocityTemplateEngine());
        post("/import/new", ImportController::serveHomePage, new VelocityTemplateEngine());
    }

    private void loadOrCheckDefault() {
        DB db = getDBConnection();
        DBCollection table = db.getCollection("setting");

        Date beginningOfTime = new Date(0);
        BasicDBObject searchQuery = new BasicDBObject("date", beginningOfTime);

        DBCursor cursor = table.find(searchQuery);

        if (cursor.size() == 0) {
            //Load the defaults in database.
            File file = new File("src/main/resources/defaults.xml");
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            try {
                DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                Document document = documentBuilder.parse(file);
                NodeList settings = document.getElementsByTagName("setting");
                List<Setting> list = new ArrayList<>();
                for (int temp = 0; temp < settings.getLength(); temp++) {

                    Node nNode = settings.item(temp);

                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {

                        Element eElement = (Element) nNode;

                        Setting s = new Setting(beginningOfTime,
                                Double.parseDouble(eElement.getElementsByTagName("amount").item(0).getTextContent()),
                                eElement.getElementsByTagName("description").item(0).getTextContent(),
                                eElement.getElementsByTagName("user").item(0).getTextContent(),
                                eElement.getElementsByTagName("payingTo").item(0).getTextContent(),
                                eElement.getElementsByTagName("type").item(0).getTextContent());
                        list.add(s);
                    }
                }

                BasicDBObject object = new BasicDBObject();
                object.put("date", beginningOfTime);
                object.put("settings", list);
                table.insert(object);
            } catch (ParserConfigurationException | IOException | SAXException e) {
                LoggerFactory.getLogger(RentCalculator.class).error("Unable to load default settings", e);
                System.exit(1);
            }

        }
    }

    public static DB getDBConnection() {
        MongoClient mongo = null;
        mongo = new MongoClient("localhost", 27017);
        return mongo.getDB("testNico");
    }
}