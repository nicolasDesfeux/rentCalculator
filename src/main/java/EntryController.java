/**
 * Created by Nicolas on 2017-01-01.
 */

import spark.Request;
import spark.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EntryController {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    public static Object insertEntry(Request request, Response response) throws ParseException {
        User user = HibernateUtil.findUserById(request.queryParams("user"));
        Entry entry = new Entry(user,
                request.queryParams("Category"),
                request.queryParams("Description"),
                Double.valueOf(request.queryParams("Amount")),
                DATE_FORMAT.parse(request.queryParams("date")), EntryType.ONETIME,
                request.queryParams("Description"));
        HibernateUtil.persist(entry);
        return entry;
    }

    public static Object updateEntry(Request request, Response response) throws ParseException {
        Entry entry = HibernateUtil.getEntryById(request.queryParams("_id"));
        entry.setUser(HibernateUtil.findUserById(request.queryParams("user")));
        entry.setCategory(request.queryParams("category"));
        entry.setDescription(request.queryParams("description"));
        entry.setAmount(Double.valueOf(request.queryParams("amount")));
        entry.setDate(new Date(0));
        entry.setEntryType(request.pathInfo().equals("/updateRecurringEntry") ? EntryType.RECURRING : EntryType.ONETIME);
        entry.setPayingTo(request.queryParams("payingTo") == null ? request.queryParams("description") : request.queryParams("payingTo"));
        HibernateUtil.persist(entry);
        return entry;
    }

    public static Object deleteEntry(Request request, Response response) {
        HibernateUtil.removeById(request.queryParams("_id"));
        return request.queryParams("_id");
    }
}
