/**
 * Created by Nicolas on 2017-01-01.
 */

import org.apache.commons.lang.time.DateUtils;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import java.math.BigDecimal;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class IndexController {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DISPLAY_MONTH_FORMAT = new SimpleDateFormat("MMMM yyyy");
    private static Date val;

    public static ModelAndView serverSettingsViewPage(Request re, Response res) throws ParseException, UnknownHostException {
        HashMap<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "templates/settingsView.vm");
    }

    public static ModelAndView serverSummaryViewPage(Request re, Response res) throws ParseException, UnknownHostException {
        //Model to fill up.
        HashMap<String, Object> model = new HashMap<>();
        String columnList = "[";
        Collection<User> users = HibernateUtil.getAllUsers();
        columnList += "{name: \"" + "Date" + "\", type: \"text\"},";
        for (User user : users) {
            columnList += "{name: \"" + (user != null ? user.getFulName() : "Other") + "\", type: \"number\", itemTemplate: function(value) {\n" +
                    "                        return \"$\" + value;\n" +
                    "                    }},";
        }
        columnList += "{name: \"" + "Utilities" + "\", type: \"number\", itemTemplate: function(value) {\n" +
                "                        return \"$\" + value;\n" +
                "                    }},";
        columnList += "{name: \"" + "Total" + "\", type: \"number\", itemTemplate: function(value) {\n" +
                "                        return \"$\" + value;\n" +
                "                    }},";
        columnList = columnList.substring(0, columnList.length() - 1) + "]";
        model.put("columnList", columnList);

        return new ModelAndView(model, "templates/summaryView.vm");
    }

    public static String getSummaryPerUser(Request re, Response res) throws ParseException {
        Date oldest = HibernateUtil.getOldestEntry(EntryType.ONETIME).getDate();

        String totalAmountPerUser = "[";
        Date date = new Date();
        while (date.after(oldest)) {
            totalAmountPerUser += getTotalAmountforMonth(date);
            date = DateUtils.addMonths(date, -1);
        }
        totalAmountPerUser = totalAmountPerUser.substring(0, totalAmountPerUser.length() - 1) + "]";
        return totalAmountPerUser;
    }

    public static String getUsers(Request re, Response res) throws ParseException {
        Collection<User> allUsers = HibernateUtil.getAllUsers();
        String users = "[";
        for (User user : allUsers) {
            users += "{\"_id\":\""+user.getKey()+"\",\"firstName\":\"" + user.getFirstName() + "\",\"lastName\":\"" + user.getLastName() + "\"},";
        }
        return users.substring(0, users.length() - 1)+"]";
    }


    public static ModelAndView serverMonthlyViewPage(Request re, Response res) throws ParseException, UnknownHostException {

        //Set up the date on the controller.
        Date date = new Date();
        String source = re.queryParams("date");
        if (source != null) {
            date = DATE_FORMAT.parse(source);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        val = cal.getTime();
        //Date set up complete.

        //Model to fill up.
        HashMap<String, Object> model = new HashMap<>();

        String columnList = "[";
        String userListSelect = "[{},";
        Collection<User> users = HibernateUtil.getAllUsers();
        for (User user : users) {
            columnList += "{name: \"" + (user != null ? user.getFulName() : "Other") + "\", type: \"number\", itemTemplate: function(value) {\n" +
                    "                        return \"$\" + value;\n" +
                    "                    }},";
            userListSelect += "{\"Name\":\"" + user.getFulName() + "\", \"Id\":\"" + user.getKey() + "\"},";
        }
        columnList += "{name: \"" + "Utilities" + "\", type: \"number\", itemTemplate: function(value) {\n" +
                "                        return \"$\" + value;\n" +
                "                    }},";
        columnList += "{name: \"" + "Total" + "\", type: \"number\", itemTemplate: function(value) {\n" +
                "                        return \"$\" + value;\n" +
                "                    }},";
        columnList = columnList.substring(0, columnList.length() - 1) + "]";
        model.put("columnList", columnList);
        userListSelect = userListSelect.substring(0, userListSelect.length() - 1) + "]";
        model.put("userListSelect", userListSelect);

        model.put("date", DATE_FORMAT.format(DATE_FORMAT.parse(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 1) + "-01")));
        model.put("currentMonth", DISPLAY_MONTH_FORMAT.format(cal.getTime()));
        model.put("previousMonth", DATE_FORMAT.format(DATE_FORMAT.parse(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH)) + "-01")));
        model.put("nextMonth", DATE_FORMAT.format(DATE_FORMAT.parse(cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH) + 2) + "-01")));


        return new ModelAndView(model, "templates/monthlyView.vm");
    }

    private static Map<User, BigDecimal> getTotalsPerUser(int indexMonth, int indexYear) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, indexMonth);
        cal.set(Calendar.YEAR, indexYear);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Collection<Entry> list = HibernateUtil.getEntriesForMonth(cal.getTime(), EntryType.ONETIME);

        Map<User, BigDecimal> totalEntriesPerUser = new HashMap<>();

        for (Entry entry : list) {
            if (totalEntriesPerUser.containsKey(entry.getUser())) {
                totalEntriesPerUser.put(entry.getUser(), totalEntriesPerUser.get(entry.getUser()).add(entry.getAmount()));
            } else {
                totalEntriesPerUser.put(entry.getUser(), entry.getAmount());
            }
        }

        if (!totalEntriesPerUser.isEmpty()) {
            final String[] entryTable = {"["};
            totalEntriesPerUser.forEach((user, aDouble) -> {
                entryTable[0] += "{\"User\":\"" + user.getFirstName() + "\",\"Amount\":\"";
                entryTable[0] += aDouble + "\"},";
            });

            entryTable[0] = entryTable[0].substring(0, entryTable[0].length() - 1);
            entryTable[0] += "]";
        }

        Collection<Entry> settings = loadSettings(DATE_FORMAT.parse(indexYear + "-" + (indexMonth + 1) + "-01"));

        Map<User, BigDecimal> utilitiesPerUser = new HashMap<>();
        BigDecimal rent = BigDecimal.ZERO;
        Collection<User> users = HibernateUtil.getAllUsers();
        for (Entry setting : settings) {
            if (setting.getAmount() != null && !"Rent".equals(setting.getDescription()) && !users.contains(HibernateUtil.findUserById(setting.getPayingTo()))) {
                utilitiesPerUser.put(setting.getUser(), utilitiesPerUser.getOrDefault(setting.getUser(), BigDecimal.ZERO).add(setting.getAmount()));
            } else if ("Rent".equals(setting.getDescription())) {
                rent = rent.add(setting.getAmount());
            }
        }

        BigDecimal utilitiesTotal = BigDecimal.valueOf(utilitiesPerUser.values().stream().mapToDouble(BigDecimal::doubleValue).sum());

        Map<User, BigDecimal> mapTotals = new LinkedHashMap<>();
        for (User user : users) {

            double add = settings.stream().filter(entry -> users.contains(entry.getUser()) && !"Rent".equals(entry.getDescription()) && user.equals(HibernateUtil.findUserById(entry.getPayingTo()))).mapToDouble((entry) -> entry.getAmount().doubleValue()).sum();
            double remove = settings.stream().filter(entry -> users.contains(HibernateUtil.findUserById(entry.getPayingTo())) && !"Rent".equals(entry.getDescription()) && user.equals(entry.getUser())).mapToDouble((entry) -> entry.getAmount().doubleValue()).sum();
            double sum = totalEntriesPerUser.values().stream().mapToDouble(BigDecimal::doubleValue).sum() + utilitiesTotal.doubleValue();
            double userTotal = (sum + rent.doubleValue()) / users.size() - totalEntriesPerUser.getOrDefault(user, BigDecimal.ZERO).doubleValue() - utilitiesPerUser.getOrDefault(user, BigDecimal.ZERO).doubleValue() + remove - add;
            mapTotals.put(user, BigDecimal.valueOf(Math.round(userTotal)));
        }

        BigDecimal total = BigDecimal.ZERO;
        for (User user : users) {
            total = total.add(mapTotals.get(user));
        }


        mapTotals.put(new User(null, "Utilities", "utilities"), utilitiesTotal);
        mapTotals.put(new User(null, "Total", "total"), total);
        return mapTotals;
    }

    private static String convertToJavascriptTable(Collection<Entry> list) {
        if (!list.isEmpty()) {
            String entryTable = "[";
            for (Entry aList : list) {
                entryTable += "{\"Date\":\"" + aList.getDate().toString() + "\",\"user\":";
                entryTable += "\"" + aList.getUser().getKey() + "\",\"category\":\"";
                entryTable += aList.getCategory() + "\",\"description\":\"";
                entryTable += aList.getDescription() + "\",\"_id\":\"";
                entryTable += aList.getKey() + "\",\"amount\":\"";
                entryTable += aList.getAmount().toString() + "\"},";
            }
            entryTable = entryTable.substring(0, entryTable.length() - 1);
            entryTable += "]";
            return entryTable;
        }
        return "[]";
    }

    private static Collection<Entry> loadSettings(Date parse) {
        Collection<Entry> entriesForMonth = HibernateUtil.getEntriesForMonth(parse, EntryType.RECURRING);
        if (entriesForMonth.size() == 0) {
            entriesForMonth = HibernateUtil.getEntriesForMonth(new Date(0), EntryType.RECURRING);
        }

        return entriesForMonth;
    }

    public static Object getEntriesForMonth(Request re, Response res) {
        Collection<Entry> list = HibernateUtil.getEntriesForMonth(val, EntryType.ONETIME);
        return convertToJavascriptTable(list);
    }

    public static Object getTableSettings(Request re, Response res) {
        Collection<Entry> settings = loadSettings(val);

        String tableSettings = "[";
        for (Entry setting : settings) {
            tableSettings += "{\"date\" :\"" + DATE_FORMAT.format(val) + "\",\"_id\" :\"" + setting.getKey() + "\",\"userId\" :\"" + setting.getUser().getKey() + "\",\"description\":\"" + setting.getDescription() + "\",\"amount\":\"";
            tableSettings += setting.getAmount() + "\",\"user\":\"";
            tableSettings += setting.getUser().getKey() + "\",\"payingTo\":\"";
            tableSettings += setting.getPayingTo() + "\"},";
        }
        tableSettings = tableSettings.substring(0, tableSettings.length() - 1);
        tableSettings += "]";

        return tableSettings;
    }

    public static Object getTotalAmountPerUser(Request re, Response res) throws Exception {
        String totalAmountPerUser = "[";
        totalAmountPerUser += getTotalAmountforMonth(val);
        totalAmountPerUser = totalAmountPerUser.substring(0, totalAmountPerUser.length() - 1) + "]";
        return totalAmountPerUser;
    }

    private static String getTotalAmountforMonth(Date val) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTime(val);
        Map<User, BigDecimal> mapTotals = getTotalsPerUser(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR));
        String totalAmountPerUser = "{\"Date\" : \"<a href='/monthlyView?date="+DATE_FORMAT.format(cal.getTime())+"'>" + DISPLAY_MONTH_FORMAT.format(cal.getTime()) + "</a>\",";
        for (User user : mapTotals.keySet()) {
            totalAmountPerUser += "\"" + (user != null ? user.getFulName() : "Other") + "\":\"" + mapTotals.get(user) + "\",";
        }
        return totalAmountPerUser.substring(0, totalAmountPerUser.length() - 1) + "},";
    }

    public static Object getExpensesPerUser(Request re, Response res) {
        Collection<Entry> list = HibernateUtil.getEntriesForMonth(val, EntryType.ONETIME);

        Map<User, BigDecimal> totalEntriesPerUser = new HashMap<>();

        for (Entry entry : list) {
            if (totalEntriesPerUser.containsKey(entry.getUser())) {
                totalEntriesPerUser.put(entry.getUser(), totalEntriesPerUser.get(entry.getUser()).add(entry.getAmount()));
            } else {
                totalEntriesPerUser.put(entry.getUser(), entry.getAmount());
            }
        }

        if (!totalEntriesPerUser.isEmpty()) {
            final String[] entryTable = {"["};
            totalEntriesPerUser.forEach((user, aDouble) -> {
                entryTable[0] += "{\"User\":\"" + user.getFirstName() + "\",\"Amount\":\"";
                entryTable[0] += aDouble + "\"},";
            });

            entryTable[0] = entryTable[0].substring(0, entryTable[0].length() - 1);
            entryTable[0] += "]";
            return entryTable[0];
        } else {
            return "[]";
        }
    }


}
