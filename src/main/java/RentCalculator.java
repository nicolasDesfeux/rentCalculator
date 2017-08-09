import spark.template.velocity.VelocityTemplateEngine;

import static spark.Spark.*;

public class RentCalculator {


    public static void main(String[] args) {
        new RentCalculator();

        staticFileLocation("/public");

        get("/monthlyView", IndexController::serverMonthlyViewPage, new VelocityTemplateEngine());
        get("/summaryView", IndexController::serverSummaryViewPage, new VelocityTemplateEngine());
        get("/settings", IndexController::serverSettingsViewPage, new VelocityTemplateEngine());

        get("/import/new", ImportController::serveHomePage, new VelocityTemplateEngine());
        post("/import/new", ImportController::serveHomePage, new VelocityTemplateEngine());

        post("/updateItem", EntryController::updateEntry);
        post("/updateRecurringEntry", EntryController::updateEntry);
        post("/insertItem", EntryController::insertEntry);
        post("/deleteItem", EntryController::deleteEntry);
        post("/getEntries", IndexController::getEntriesForMonth);
        post("/getTableSettings", IndexController::getTableSettings);
        post("/getTotalAmounts", IndexController::getTotalAmountPerUser);
        post("/getExpensesPerUser", IndexController::getExpensesPerUser);
        post("/getSummary", IndexController::getSummaryPerUser);
        post("/getUsers", IndexController::getUsers);
    }
}