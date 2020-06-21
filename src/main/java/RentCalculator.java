import dao.EntryDao;
import dao.UserDao;
import spark.Filters;
import spark.template.velocity.VelocityTemplateEngine;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static spark.Spark.*;

public class RentCalculator {

    private Date currentMonth;
    private UserDao userDao;
    private EntryDao entryDao;

    public Date getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(Date currentMonth) {
        this.currentMonth = currentMonth;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public EntryDao getEntryDao() {
        return entryDao;
    }

    public void setEntryDao(EntryDao entryDao) {
        this.entryDao = entryDao;
    }

    public RentCalculator() {
        Calendar c = new GregorianCalendar();
        c.setTime(new Date());
        c.set(Calendar.DAY_OF_MONTH, 1);
        //c.add(Calendar.MONTH, -10);
        c.set(Calendar.HOUR, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 0);
        this.currentMonth = c.getTime();
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("apacheDerby");
        this.userDao = new UserDao(entityManagerFactory);
        this.entryDao = new EntryDao(entityManagerFactory);
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        RentCalculator rc = new RentCalculator();
        boolean isInitialized = rc.getPropValue("initialized") != null && rc.getPropValue("initialized").toLowerCase().equals("yes");

        if (!isInitialized) {
            runScript("importRecords.sql");
        }

        staticFileLocation("/public");
        // Set up before-filters (called before each get/post)
        before("*", Filters.addTrailingSlashes);
        before("*", Filters.handleLocaleChange);
        //Set up after-filters (called after each get/post)
        after("*", Filters.addGzipHeader);

        get("/users/", "text/html", (request, response) -> rc.userDao.toJson(rc.userDao.findAll()));
        get("/entries/", "text/html", (request, response) -> rc.entryDao.toJson(rc.entryDao.findAll()));
        get("/conf/", IndexController::confPage, new VelocityTemplateEngine());

        get("/monthlyView/", (re, res) -> IndexController.serverMonthlyViewPage(re, res, rc), new VelocityTemplateEngine());
        get("/summaryView/", (re, res) -> IndexController.serverSummaryViewPage(re, res, rc), new VelocityTemplateEngine());
        /*get("/settings/", IndexController::serverSettingsViewPage, new VelocityTemplateEngine());

        get("/import/new/", ImportController::serveHomePage, new VelocityTemplateEngine());
        post("/import/new/", ImportController::serveHomePage, new VelocityTemplateEngine()) ;

        post("/updateItem/", EntryController::updateEntry);
        post("/updateRecurringEntry/", EntryController::updateEntry);
        post("/insertItem/", EntryController::insertEntry);
        post("/insertRecurringItem/", EntryController::insertEntry);

        post("/deleteItem/", EntryController::deleteEntry);
        post("/getEntries/", IndexController::getEntriesForMonth);
        post("/getTableSettings/", IndexController::getTableSettings);
        post("/getTotalAmounts/", IndexController::getTotalAmountPerUser);
        post("/getExpensesPerUser/", IndexController::getExpensesPerUser);
        post("/getSummary/", IndexController::getSummaryPerUser);
        post("/getUsers/", IndexController::getUsers);*/
        post("/updateDate/", (re, res) -> rc.addCurrentMonth(re.queryParams("increment")));
        get("/getExpensesPerUser/", (re, res) -> IndexController.getExpensesPerUser(re, res, rc));
        get("/getSummaryPerUser/", (re, res) -> IndexController.getSummaryPerUser(re, res, rc));
        get("/currentMonthEntry/", "text/html", (request, response) -> rc.entryDao.toJson(rc.entryDao.getAllEntries(rc.getCurrentMonth())));
        get("/getSummary/", (re, res) -> IndexController.getSummary(re, res, rc));
    }

    private boolean addCurrentMonth(String increment) {
        Calendar c= new GregorianCalendar();
        c.setTime(this.getCurrentMonth());
        c.add(Calendar.MONTH,Integer.parseInt(increment));
        this.setCurrentMonth(c.getTime());
        return true;
    }

    private static void runScript(String pathname) throws FileNotFoundException, SQLException, ClassNotFoundException {

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection("jdbc:derby:myEmbeddedDB");

        File f = new File("src/main/resources/script/" + pathname);
        Scanner sc = new Scanner(f);
        StringBuilder query = new StringBuilder();

        while (sc.hasNextLine()) {
            String str = sc.nextLine();
            query.append(str).append("\n");
            if (str.contains(";")) {
                try {
                    //System.out.println(query.toString().replace(";",""));
                    Statement st = conn.createStatement();
                    st.execute(query.toString().replace(";", ""));
                } catch (SQLException throwables) {
                    System.out.println(query.toString().replace(";", ""));
                    System.out.println(throwables.getSQLState());
                    throwables.printStackTrace();
                }
                query = new StringBuilder();
            }
        }
        sc.close();

    }

    public String getPropValue(String property) throws IOException {

        InputStream inputStream = null;
        String result = null;
        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            // get the property value and print it out
            result = prop.getProperty(property);

        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            assert inputStream != null;
            inputStream.close();
        }
        return result;
    }

}

