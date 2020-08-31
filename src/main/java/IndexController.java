import dao.EntryDao;
import domain.Entry;
import domain.EntryType;
import domain.StagingEntry;
import domain.User;
import spark.ModelAndView;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class IndexController {

    public static ModelAndView confPage(Request re, Response res) {
        HashMap<String, Object> model = new HashMap<>();
        return new ModelAndView(model, "templates/users.vm");
    }

    public static ModelAndView serverImportPage(Request re, Response res, RentCalculator rc) {
        HashMap<String, Object> model = new HashMap<>();
        StringBuilder list = new StringBuilder();
        for (User user : rc.getUserDao().findAll()) {
            list.append("<option value=\"").append(user.getId()).append("\">").append(user.getFullName()).append("</option>\n");
        }
        model.put("userList", list);
        list = new StringBuilder();
        Collection<Entry> entries = rc.getEntryDao().findAll();

        Set<String> categoriesSet = new HashSet<>();
        for (Entry entry : entries) {
            categoriesSet.add("\"" + entry.getCategory() + "\"");
        }
        List<String> categories = new ArrayList<>(categoriesSet);
        categories.sort(String::compareTo);
        model.put("categoryList", "[\"\"," + String.join(",", categories) + "]");

        return new ModelAndView(model, "templates/import.vm");
    }

    public static ModelAndView importFile(Request re, Response res, RentCalculator rc) throws IOException, ServletException, ParseException {
        HashMap<String, Object> model = new HashMap<>();
        re.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement(""));
        Part filePart = re.raw().getPart("file");
        try (InputStream inputStream = filePart.getInputStream()) {
            int userId = Integer.parseInt(re.raw().getParameter("user"));
            String s = new String(inputStream.readAllBytes());
            EntryDao entryDao = rc.getEntryDao();
            boolean firstLine = true;
            for (String line : s.split("\n")) {
                if (!firstLine) {
                    String[] elements = line.split(",");
                    String description = elements[4];
                    description = description.trim().replaceAll("^\"+|\"+$", "");
                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                    Date date = sdf.parse(elements[2]);
                    StagingEntry e = new StagingEntry(rc.getUserDao().findOne(userId), null, description, Double.parseDouble(elements[6]), date, EntryType.ONETIME);
                    entryDao.create(e);
                } else {
                    firstLine = false;
                }
            }
        } catch(Exception e){
            res.raw().setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
        }
        return serverImportPage(re, res, rc);
    }

    public static ModelAndView serverMonthlyViewPage(Request re, Response res, RentCalculator rc) throws ParseException, UnknownHostException {
        HashMap<String, Object> model = new HashMap<>();
        SimpleDateFormat sf = new SimpleDateFormat("MMMM yyyy");
        model.put("currentMonth", sf.format(rc.getCurrentMonth()));
        String fieldsList = "[" +
                "                        {" +
                "                            \"name\": \"category\"," +
                "                            \"title\": \"Category\"," +
                "                            \"type\": \"text\"" +
                "                        },";
        List<String> l = new ArrayList<>();
        for (User user : rc.getUserDao().findAll()) {
            l.add("{" +
                    "                            \"name\": \"user" + (user.getId()) + "\"," +
                    "                            \"title\": \"" + user.getFullName() + "\"," +
                    "                            \"type\": \"number\"," +
                    "                            \"itemTemplate\": function (value) {return '$' + (value==undefined?'0':value);}" +
                    "                        }");
        }
        fieldsList += String.join(",", l) + "]";
        model.put("listFields", fieldsList);
        model.put("listFieldsSummary", fieldsList.replace("Category", "Operation on Joint account"));
        return new ModelAndView(model, "templates/monthlyView.vm");
    }

    public static String getSummaryPerUser(Request re, Response res, RentCalculator rc) throws Exception {
        Collection<Entry> list = rc.getEntryDao().getAllEntries(rc.getCurrentMonth());

        Map<User, Double> map = list.stream().collect(Collectors.toMap(Entry::getUser, Entry::getAmount, Double::sum));
        OptionalDouble average = map.values().stream().mapToDouble(a -> a).average();
        if (!average.isPresent()) {
            throw new Exception("Cannot calculate the monthly summary!");

        }
        double averageValue = average.getAsDouble();
        StringBuilder json = new StringBuilder("[");

        List<String> lDeposit = new ArrayList<>();
        List<String> lWithdraw = new ArrayList<>();
        map.forEach((user, total) -> {
            if ((total - averageValue) > 0) {
                lWithdraw.add("\"user" + user.getId() + "\":\"" + Math.abs(total - averageValue) + "\"");
            } else {
                lDeposit.add("\"user" + user.getId() + "\":\"" + Math.abs(total - averageValue) + "\"");
            }
        });
        json.append("{\"category\":\"").append("Deposit").append("\",");
        json.append(String.join(",", lDeposit)).append("}");
        json.append(",{\"category\":\"").append("Withdraw").append("\",");
        json.append(String.join(",", lWithdraw)).append("}");
        return json.toString() + "]";
    }

    public static String getExpensesPerUser(Request re, Response res, RentCalculator rc) {
        Collection<Entry> list = rc.getEntryDao().getAllEntries(rc.getCurrentMonth());

        List<Entry> listByCategoryAndUser = new ArrayList<>();
        List<String> categories = new ArrayList<>();
        for (Entry entry : list) {
            //Check for total
            List<Entry> existingTotalEntry = listByCategoryAndUser.stream().filter(a -> a.getUser().equals(entry.getUser()) && "Total".equals(a.getCategory())).collect(Collectors.toList());
            if (existingTotalEntry.size() > 0) {
                Entry totalEntry = existingTotalEntry.get(0);
                totalEntry.setAmount(totalEntry.getAmount() + entry.getAmount());
            } else {
                listByCategoryAndUser.add(new Entry(entry.getUser(), "Total", "", entry.getAmount(), new Date(), EntryType.ONETIME, ""));
            }
            //Check for category
            List<Entry> existingEntry = listByCategoryAndUser.stream().filter(a -> a.getUser().equals(entry.getUser()) && Objects.equals(entry.getCategory(), a.getCategory())).collect(Collectors.toList());
            if (existingEntry.size() > 0) {
                Entry totalEntry = existingEntry.get(0);
                totalEntry.setAmount(totalEntry.getAmount() + entry.getAmount());
            } else {
                if (!categories.contains(entry.getCategory())) {
                    categories.add(entry.getCategory());
                }
                listByCategoryAndUser.add(new Entry(entry.getUser(), entry.getCategory(), "", entry.getAmount(), new Date(), EntryType.ONETIME, ""));
            }
        }
        categories.add(0, "Total");
        StringBuilder json = new StringBuilder("[");
        for (String category : categories) {
            json.append("{\"category\":\"").append(category == null ? "Undefined" : category).append("\",");
            List<String> l = new ArrayList<>();
            List<Entry> totalEntriesPerUser = listByCategoryAndUser.stream().filter(a -> Objects.equals(a.getCategory(), category)).collect(Collectors.toList());
            for (Entry entry : totalEntriesPerUser) {
                l.add("\"user" + entry.getUser().getId() + "\":\"" + entry.getAmount() + "\"");
            }
            json.append(String.join(",", l)).append("},");
        }
        String jsonResult = json.toString().substring(0, json.length() - 1) + "]";
        System.out.println(jsonResult);
        return jsonResult;
    }

    public static String getSummary(Request re, Response res, RentCalculator rc) {
        Collection<Entry> list = rc.getEntryDao().getAllEntries(rc.getCurrentMonth(), -12);
        List<String> dates = new ArrayList<>();
        List<Entry> listByDateAndUser = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy");
        for (Entry entry : list) {
            //Check for total
            List<Entry> existingTotalEntry = listByDateAndUser.stream().filter(a -> a.getUser().equals(entry.getUser()) && "Total".equals(a.getCategory())).collect(Collectors.toList());
            if (existingTotalEntry.size() > 0) {
                Entry totalEntry = existingTotalEntry.get(0);
                totalEntry.setAmount(totalEntry.getAmount() + entry.getAmount());
            } else {
                listByDateAndUser.add(new Entry(entry.getUser(), "Total", "", entry.getAmount(), new Date(), EntryType.ONETIME, ""));
            }

            //Check for date
            List<Entry> existingEntry = listByDateAndUser.stream().filter(a -> sdf.format(a.getDate()).equals(sdf.format(entry.getDate())) && Objects.equals(entry.getCategory(), a.getCategory())).collect(Collectors.toList());
            if (existingEntry.size() > 0) {
                Entry totalEntry = existingEntry.get(0);
                totalEntry.setAmount(totalEntry.getAmount() + entry.getAmount());
            } else {
                if (!dates.contains(sdf.format(entry.getDate()))) {
                    dates.add(sdf.format(entry.getDate()));
                }
                listByDateAndUser.add(new Entry(entry.getUser(), entry.getCategory(), "", entry.getAmount(), entry.getDate(), EntryType.ONETIME, ""));
            }
            System.out.println(entry);
            System.out.println(listByDateAndUser);
        }
        StringBuilder json = new StringBuilder("[");
        dates.add(0, "Total");
        for (String date : dates) {
            json.append("{\"date\":\"").append(date == null ? "Undefined" : date).append("\",");
            List<String> l = new ArrayList<>();
            List<Entry> totalEntriesPerUser = listByDateAndUser.stream().filter(a -> Objects.equals(sdf.format(a.getDate()), date)).collect(Collectors.toList());
            if (date != null && date.equals("Total")) {
                totalEntriesPerUser = listByDateAndUser.stream().filter(a -> Objects.equals(a.getCategory(), date)).collect(Collectors.toList());
            }
            for (Entry entry : totalEntriesPerUser) {
                l.add("\"user" + entry.getUser().getId() + "\":\"" + entry.getAmount() + "\"");
            }
            json.append(String.join(",", l)).append("},");
        }
        String jsonResult = json.toString().substring(0, json.length() - 1) + "]";
        System.out.println(jsonResult);
        return jsonResult;
    }

    public static ModelAndView serverSummaryViewPage(Request re, Response res, RentCalculator rc) {
        HashMap<String, Object> model = new HashMap<>();
        SimpleDateFormat sf = new SimpleDateFormat("MMMM yyyy");

        model.put("currentMonth", sf.format(rc.getCurrentMonth()));
        String fieldsList = "[" +
                "                        {" +
                "                            \"name\": \"date\"," +
                "                            \"title\": \"Date\"," +
                "                            \"type\": \"text\"" +
                "                        },";
        List<String> l = new ArrayList<>();
        for (User user : rc.getUserDao().findAll()) {
            l.add("{" +
                    "                            \"name\": \"user" + (user.getId()) + "\"," +
                    "                            \"title\": \"" + user.getFullName() + "\"," +
                    "                            \"type\": \"number\"," +
                    "                            \"itemTemplate\": function (value) {return '$' + (value==undefined?'0':value);}" +
                    "                        }");
        }
        fieldsList += String.join(",", l) + "]";
        model.put("listFields", fieldsList);
        return new ModelAndView(model, "templates/summaryView.vm");
    }

    public static String deleteStagingEntry(Request re, Response res, RentCalculator rc) {
        rc.getStagingEntryDao().deleteById(Long.parseLong(re.params("id")));
        return "Success";
    }
}
