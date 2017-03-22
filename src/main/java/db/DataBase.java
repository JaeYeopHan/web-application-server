package db;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import model.User;

public class DataBase {
    //prevent from creating DataBase Instance
    private DataBase() {}

    private static Map<String, User> users = Maps.newHashMap();

    private static Map<String, User> concurrentUsers = Maps.newConcurrentMap();//multi-threading environment

    public static void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    public static User findUserById(String userId) {
        return users.get(userId);
    }

    public static Collection<User> findAll() {
        return users.values();
    }
}
