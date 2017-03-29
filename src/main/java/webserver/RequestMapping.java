package webserver;

import controller.Controller;
import controller.CreateUserController;
import controller.ListUserController;
import controller.LoginController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestMapping {

    private RequestMapping() {}

    private static Map<String, Controller> controllers = new ConcurrentHashMap<>();

    static {
        controllers.put("/user/create", new CreateUserController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/list", new ListUserController());
    }

    public static Controller getController(String requestUrl) {
        return controllers.get(requestUrl);
    }
}
