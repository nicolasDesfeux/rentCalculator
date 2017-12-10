import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nicolas on 2017-08-09.
 */


public class LoginController {

    public static ModelAndView serveLoginPage(Request request, Response response) {
        Map<String, Object> model = new HashMap<>();
        model.put("loggedOut", RequestUtil.removeSessionAttrLoggedOut(request));
        model.put("loginRedirect", RequestUtil.removeSessionAttrLoginRedirect(request));
        return ViewUtil.render(request, model, "templates/login.vm");
    }

    ;


    public static Route handleLoginPost = (Request request, Response response) -> {
        Map<String, Object> model = new HashMap<>();
        if (!UserController.authenticate(RequestUtil.getQueryUsername(request), RequestUtil.getQueryPassword(request))) {
            model.put("authenticationFailed", true);
            return ViewUtil.render(request, model, "templates/login.vm");
        }
        model.put("authenticationSucceeded", true);
        request.session().attribute("currentUser", RequestUtil.getQueryUsername(request));
        if (RequestUtil.getQueryLoginRedirect(request) != null) {
            response.redirect(RequestUtil.getQueryLoginRedirect(request));
        }
        return ViewUtil.render(request, model, "templates/login.vm");
    };


    public static Route handleLogoutPost = (Request request, Response response) -> {
        request.session().removeAttribute("currentUser");
        request.session().attribute("loggedOut", true);
        response.redirect(Path.Web.LOGIN);
        return null;
    };


    // The origin of the request (request.pathInfo()) is saved in the session so
    // the user can be redirected back after login
    public static void ensureUserIsLoggedIn(Request request, Response response) {
        if (request.session().attribute("currentUser") == null) {
            request.session().attribute("loginRedirect", request.pathInfo());
            response.redirect(Path.Web.LOGIN);
        }
    }

    ;

}
