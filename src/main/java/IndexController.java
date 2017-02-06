/**
 * Created by Nicolas on 2017-01-01.
 */

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

public class IndexController {

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static DBObject val;


    public static ModelAndView serveHomePage(Request re, Response res) {

        HashMap<String, Object> model = new HashMap<>();
        List<String> user = new ArrayList<>();

        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("entry");

        BasicDBObject searchQuery = new BasicDBObject();
        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            BasicDBObject next = (BasicDBObject) cursor.next();
            if (next.getDate("date") != null) {
                user.add(DATE_FORMAT.format((Date) next.get("date")));
            }
        }
        model.put("list", user);

        return new ModelAndView(model, "templates/index.vm");
    }

    public static ModelAndView serverNewEntryPage(Request re, Response res) throws ParseException {
        String submitType = "templates/newEntry.vm";
        HashMap<String, Object> model = new HashMap<>();

        String user = re.queryParams("user");
        String amount = re.queryParams("amount");
        String category = re.queryParams("category");
        Date date = new Date();
        String source = re.queryParams("date");
        if (source != null) {
            date = DATE_FORMAT.parse(source);
        }
        String description = re.queryParams("description");
        if (user != null) {
            Entry e = new Entry(user, category, description, Double.valueOf(amount), date);
            addNewEntry(e);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int indexMonth = cal.get(Calendar.MONTH);
        int indexYear = cal.get(Calendar.YEAR);
        val = BasicDBObjectBuilder.start("$gte", DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01")).add("$lt", DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 2) + "-01")).get();

        if ("Submit and Summary".equals(re.queryParams("submit"))) {
            res.redirect("/monthlyView?date=" + indexYear + "-" + (indexMonth + 1) + "-01");
        }

        List<Entry> list = getEntriesForMonth();
        model.put("list", list);

        return new ModelAndView(model, submitType);
    }

    private static List<Entry> getEntriesForMonth() {
        List<Entry> list = new ArrayList<>();
        //Add this month entries.
        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("entry");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", val);

        DBCursor cursor = table.find(searchQuery);

        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            if (next.get("date") instanceof Date && next.get("amount") instanceof Double) {
                Entry entry = new Entry((String) next.get("user"), (String) next.get("category"), (String) next.get("description"), (double) next.get("amount"), (Date) next.get("date"));
                list.add(entry);
                list.sort(Comparator.comparing(Entry::getDate));
            }
        }

        return list;
    }

    public static ModelAndView serverMonthlySummaryPage(Request re, Response res) {
        if (re.queryParams("actualizeSummary") != null) {
            calculateSummariesForEachMonth();
        }

        HashMap<String, Object> model = new HashMap<>();
        List<List<Object>> summaries = new ArrayList<>();
        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("summary");

        BasicDBObject searchQuery = new BasicDBObject("date", -1);
        DBCursor cursor = table.find().sort(searchQuery);

        while (cursor.hasNext()) {

            DBObject next = cursor.next();
            if (summaries.isEmpty()) {
                List<String> headers = new ArrayList<>();
                headers.add("Date");

            }
            List temp = new ArrayList();
            temp.add(next.get("date"));

            System.out.println(next);
        }

        return new ModelAndView(model, "templates/monthlySummary.vm");
    }

    public static ModelAndView serverMonthlyViewPage(Request re, Response res) throws ParseException, UnknownHostException {

        Date date = new Date();
        String source = re.queryParams("date");
        if (source != null) {
            date = DATE_FORMAT.parse(source);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int indexMonth = cal.get(Calendar.MONTH);
        int indexYear = cal.get(Calendar.YEAR);
        val = BasicDBObjectBuilder.start("$gte", DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01")).add("$lt", DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 2) + "-01")).get();


        // Updating setting for this specific month.
        if ("Update".equals(re.queryParams("submit"))) {
            List<Setting> settings = new ArrayList<>();

            for (int i = 1; i < 5; i++) {
                settings.add(new Setting(DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01"),
                        Double.parseDouble(re.queryParams("amount" + i)),
                        re.queryParams("description" + i),
                        re.queryParams("user" + i),
                        re.queryParams("payingTo" + i), "shared"));
            }


            updateSettings(settings, DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01"));

        }

        //Model to fill up.
        HashMap<String, Object> model = new HashMap<>();

        Map<String, Double> mapTotals = getTotalsPerUser(indexMonth, indexYear, model);

        String totalAmountPerUser = "[{";
        String columnList = "[";
        for (String key : mapTotals.keySet()) {
            columnList += "{name: \"" + key + "\", type: \"number\", itemTemplate: function(value) {\n" +
                    "                        return \"$\" + value;\n" +
                    "                    }},";
            totalAmountPerUser += "\"" + key + "\":\"" + mapTotals.get(key) + "\",";
        }

        totalAmountPerUser = totalAmountPerUser.substring(0, totalAmountPerUser.length()-1) + "}]";
        columnList = columnList.substring(0, columnList.length()-1) + "]";

        model.put("totalAmountPerUser", totalAmountPerUser);
        model.put("columnList", columnList);
        return new ModelAndView(model, "templates/monthlyView.vm");
    }

    private static Map<String, Double> getTotalsPerUser(int indexMonth, int indexYear, HashMap<String, Object> model) throws ParseException {
        List<Entry> list = getEntriesForMonth();

        Map<String, Double> map = new HashMap<>();
        Set<String> users = new HashSet<>();

        for (Entry entry : list) {
            users.add(entry.getUser());
            if (map.containsKey(entry.getUser())) {
                map.put(entry.getUser(), map.get(entry.getUser()) + entry.getAmount());
            } else {
                map.put(entry.getUser(), entry.getAmount());
            }
        }

        final String[] entryTable = {"["};
        map.forEach((s, aDouble) -> {
            entryTable[0] += "{\"User\":\"" + s + "\",\"Amount\":\"";
            entryTable[0] += aDouble + "\"},";
        });
        entryTable[0] = entryTable[0].substring(0, entryTable[0].length() - 1);
        entryTable[0] += "]";

        model.put("tableSumUpPerUser", entryTable[0]);

        model.put("date", DATE_FORMAT.format(DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01")));
        model.put("previousMonth", DATE_FORMAT.format(DATE_FORMAT.parse(indexYear + "-" + (indexMonth) + "-01")));
        model.put("nextMonth", DATE_FORMAT.format(DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 2) + "-01")));
        /*} catch (UnknownHostException e) {
            e.printStackTrace();
        }*/

        model.put("list", convertToJavascriptTable(list));

        List<Setting> settings = loadSettings(DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01"));
        String tableSettings = "[";
        for (Setting aList : settings) {
            tableSettings += "{\"Description\":\"" + aList.getDescription() + "\",\"Amount\":\"";
            tableSettings += aList.getAmount() + "\",\"Payer\":\"";
            tableSettings += aList.getUser() + "\",\"To\":\"";
            tableSettings += aList.getPayingTo() + "\"},";
        }
        tableSettings = tableSettings.substring(0, tableSettings.length() - 1);
        tableSettings += "]";
        model.put("tableSettings", tableSettings);

        //Calculate totals
        Map<String, Double> mapTotals = new LinkedHashMap<>();

        Map<String, Double> utilitiesPerUser = new HashMap<>();
        Double rent = 0d;
        for (BasicDBObject setting : settings) {
            if (setting.get("amount") != null && !"Rent".equals(setting.get("description")) && !users.contains(setting.getString("payingTo"))) {
                utilitiesPerUser.put(setting.getString("user"), utilitiesPerUser.getOrDefault(setting.getString("user"), 0d) + (Double) setting.get("amount"));
            } else if ("Rent".equals(setting.get("description"))) {
                rent += (Double) setting.get("amount");
            }
        }

        double sum1 = utilitiesPerUser.values().stream().mapToDouble(i -> i).sum();

        for (String user : users) {
            List<BasicDBObject> collect = settings.stream().filter(basicDBObject -> "unique".equals(basicDBObject.getString("type")) && user.equals(basicDBObject.getString("payingTo"))).collect(Collectors.toList());
            double add = collect.stream().mapToDouble(i -> i.getDouble("amount")).sum();
            List<BasicDBObject> collectR = settings.stream().filter(basicDBObject -> "unique".equals(basicDBObject.getString("type")) && user.equals(basicDBObject.getString("user"))).collect(Collectors.toList());
            double remove = collectR.stream().mapToDouble(i -> i.getDouble("amount")).sum();
            double sum = map.values().stream().mapToDouble(i -> i).sum() + sum1;
            double userTotal = (sum + rent) / users.size() - map.getOrDefault(user, 0d) - utilitiesPerUser.getOrDefault(user, 0d) - add + remove;
            mapTotals.put(user, (double) Math.round(userTotal));
        }

        double total = 0d;
        for (String user : users) {
            total += mapTotals.get(user);
        }

        mapTotals.put("Utilities", sum1);
        mapTotals.put("Total", total);
        return mapTotals;
    }

    private static String convertToJavascriptTable(List<Entry> list) {
        String entryTable = "[";
        for (Entry aList : list) {
            entryTable += "{\"Date\":\"" + aList.getDate().toString() + "\",\"User\":\"";
            entryTable += aList.getUser() + "\",\"Category\":\"";
            entryTable += aList.getCategory() + "\",\"Description\":\"";
            entryTable += aList.getDescription() + "\",\"Amount\":\"";
            entryTable += aList.getAmount().toString() + "\"},";
        }
        entryTable = entryTable.substring(0, entryTable.length() - 1);
        entryTable += "]";
        return entryTable;
    }

    private static void addNewEntry(Entry e) {
        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("entry");
        BasicDBObject document = new BasicDBObject();
        document.put("user", e.getUser());
        document.put("date", e.getDate());
        document.put("category", e.getCategory());
        document.put("amount", e.getAmount());
        document.put("description", e.getDescription());
        table.insert(document);
    }

    private static void updateSettings(List<Setting> settings, Date date) {

        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("setting");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", date);

        DBCursor cursor = table.find(searchQuery);

        if (cursor.size() == 0) {
            BasicDBObject document = new BasicDBObject();
            document.put("date", date);
            document.put("settings", settings);
            table.insert(document);
        }
    }

    private static List<Setting> loadSettings(Date parse) {
        List<Setting> res = new ArrayList<>();
        DB db = RentCalculator.getDBConnection();
        DBCollection table = db.getCollection("setting");

        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.put("date", parse);

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

        return res;
    }

    public static void calculateSummariesForEachMonth() {
        //Starting last month, until an entry is found.
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        boolean noEntryFound = true;
        int i = 0;
        while (noEntryFound && i < 10) {
            DB db = RentCalculator.getDBConnection();
            DBCollection table = db.getCollection("summary");
            DBCursor date = table.find(new BasicDBObject("date", cal.getTime()));
            if (date.size() > 0) {
                noEntryFound = false;
            } else {
                int indexYear = cal.get(Calendar.YEAR);
                int indexMonth = cal.get(Calendar.MONTH);
                Map<String, Double> map = calculateSummaryForMonth(indexYear, indexMonth);
                BasicDBObject document = new BasicDBObject();
                document.put("date", cal.getTime());
                document.put("users", map);
                table.insert(document);
            }
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
            i++;
        }
    }

    private static Map<String, Double> calculateSummaryForMonth(int indexYear, int indexMonth) {
        try {
            val = BasicDBObjectBuilder.start("$gte", DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01")).add("$lt", DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 2) + "-01")).get();
            return getTotalsPerUser(indexMonth, indexYear, new HashMap<>());
        } catch (ParseException e) {
            LoggerFactory.getLogger(IndexController.class).error("Hello", e);
        }
        return null;
    }

}
