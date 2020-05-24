import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class RentCalculator {


    public static void main(String[] args) {
        new RentCalculator();

        staticFileLocation("/public");
        // Set up before-filters (called before each get/post)
        before("*", Filters.addTrailingSlashes);
        before("*", Filters.handleLocaleChange);

        get("/expenses/:user/", "text/html",(request, response) -> "Hello");
        get("/monthlyView/", IndexController::serverMonthlyViewPage, new VelocityTemplateEngine());
        get("/summaryView/", IndexController::serverSummaryViewPage, new VelocityTemplateEngine());
        get("/settings/", IndexController::serverSettingsViewPage, new VelocityTemplateEngine());

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
        post("/getUsers/", IndexController::getUsers);
        post("/updateDate/", IndexController::updateDate);


        get(Path.Web.LOGIN, LoginController::serveLoginPage, new VelocityTemplateEngine());
        post(Path.Web.LOGIN, LoginController.handleLoginPost);

        //Set up after-filters (called after each get/post)
        after("*", Filters.addGzipHeader);
    }
}