import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.utils.IOUtils;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Nicolas on 2017-01-01.
 */
public class ImportController {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");


    public static ModelAndView serveHomePage(Request re, Response res) throws IOException, ServletException, ParseException {
        String location = "image";          // the directory location where files will be stored
        long maxFileSize = 100000000;       // the maximum size allowed for uploaded files
        long maxRequestSize = 100000000;    // the maximum size allowed for multipart/form-data requests
        int fileSizeThreshold = 1024;       // the size threshold after which files will be written to disk



        if (re.requestMethod().equals("POST")) {
            MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
                    location, maxFileSize, maxRequestSize, fileSizeThreshold);
            re.raw().setAttribute("org.eclipse.jetty.multipartConfig",
                    multipartConfigElement);
            Collection<Part> parts = re.raw().getParts();


            StringWriter writer = new StringWriter();
            IOUtils.copy(re.raw().getPart("file").getInputStream(), writer);
            String theString = writer.toString();

            List<Entry> entryList = new ArrayList<>();
            for (String s : theString.split("\r\n")) {

                String[] entry = s.split("\t");
                if (!"Amount".equals(entry[4])) {
                    Entry e = new Entry(entry[1], entry[3], entry[2], Double.parseDouble(entry[4]), DATE_FORMAT.parse(entry[0]));
                    entryList.add(e);
                }
            }

            entryList.forEach(ImportController::addNewEntry);
            multipartConfigElement = null;
            parts = null;
        }
        return new ModelAndView(new HashMap<>(), "templates/import.vm");
    }

    private static void addNewEntry(Entry e) {

        MongoClient mongo = new MongoClient("localhost", 27017);
        DB db = mongo.getDB("testNico");
        DBCollection table = db.getCollection("entry");
        BasicDBObject document = new BasicDBObject();
        document.put("user", e.getUser());
        document.put("date", e.getDate());
        document.put("category", e.getCategory());
        document.put("amount", e.getAmount());
        document.put("description", e.getDescription());
        table.insert(document);

    }

}
