/**
 * Created by Nicolas on 2017-01-01.
 */

import com.google.gson.Gson;
import com.mongodb.*;
import org.slf4j.LoggerFactory;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class EntryController {

    public static Object updateEntry(Request request, Response response) {
        return true;
    }

    public static Object getSettings(Request request, Response response) {
        List<Setting> res = new ArrayList<>();
        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("setting");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", new Date());

        DBCursor cursor = table.find(searchQuery);

        if (cursor.size() > 0) {
            while (cursor.hasNext()) {
                DBObject next = cursor.next();
                List<BasicDBObject> list = (List<BasicDBObject>) next.get("settings");
                for (BasicDBObject basicDBObject : list) {
                    Setting e = new Setting(basicDBObject.getDate("month"),
                            basicDBObject.getDouble("amount"),
                            basicDBObject.getString("description"),
                            basicDBObject.getString("user"),
                            basicDBObject.getString("payingTo"),
                            basicDBObject.getString("type"));
                    res.add(e);
                }
            }
        } else {
            searchQuery = new BasicDBObject();
            Date date = new Date(0);
            searchQuery.put("date", date);

            cursor = table.find(searchQuery);
            while (cursor.hasNext()) {
                DBObject next = cursor.next();
                List<BasicDBObject> list = (List<BasicDBObject>) next.get("settings");
                for (BasicDBObject basicDBObject : list) {
                    res.add(new Setting(date,
                            basicDBObject.getDouble("amount"),
                            basicDBObject.getString("description"),
                            basicDBObject.getString("user"),
                            basicDBObject.getString("payingTo"),
                            basicDBObject.getString("type")));
                }
            }
        }


        return new Gson().toJson(res);
    }
}
