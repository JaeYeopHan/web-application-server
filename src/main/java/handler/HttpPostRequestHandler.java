package handler;

import db.DataBase;
import model.Data;
import model.DataOfGet;
import model.DataOfPost;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * Created by Jbee on 2017. 3. 26..
 */
public class HttpPostRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpPostRequestHandler.class);
    private static final String STATIC_FOLDER_PATH = "./webapp";
    private static final String USER_CREATE_API = "/user/create";
    private static final String LOGIN_API = "/user/login";
    private static final String MAIN_PAGE = "/index.html";
    private static final String LOGIN_FAILED_PAGE = "/user/login_failed.html";
    private BufferedReader br;

    public HttpPostRequestHandler(BufferedReader br) {
        this.br = br;
    }

    public Data handle(String api) throws IOException {
        if (api.equals(USER_CREATE_API)) {
            return handleCreateUserRequest();
        }

        if (api.equals(LOGIN_API)) {
            return handleLoginRequest();
        }
        return new DataOfGet();
    }

    private Data handleLoginRequest() throws IOException {
        String reqBody = IOUtils.readData(br, getContentLength(br));
        log.debug("body: {}", reqBody);
        Map<String, String> loginData = HttpRequestUtils.parseQueryString(reqBody);

        if (isLoginSuccess(loginData)) {
            log.debug("login success: {}", loginData);
            return new DataOfPost(302, MAIN_PAGE);
        }
        log.debug("login failed: {}", loginData);
        byte[] repBody = Files.readAllBytes(new File(STATIC_FOLDER_PATH + LOGIN_FAILED_PAGE).toPath());
        return new DataOfPost(200, LOGIN_FAILED_PAGE, repBody);
    }

    private Data handleCreateUserRequest() throws IOException {
        String reqBody = IOUtils.readData(br, getContentLength(br));
        log.debug("reqBody: {}", reqBody);
        Map<String, String> userData = HttpRequestUtils.parseQueryString(reqBody);
        User user = new User(userData);
        log.debug("user: {}", user);
        DataBase.addUser(user);
        return new DataOfPost(302, MAIN_PAGE);
    }

    private boolean isLoginSuccess(Map<String, String> loginData) {
        User user = DataBase.findUserById(loginData.get("userId"));
        if (user == null) {
            return false;
        }

        if (!user.isCorrect(loginData.get("password"))) {
            return false;
        }

        return true;
    }

    private int getContentLength(BufferedReader br) throws IOException {
        int contentLength = 0;
        String line = br.readLine();
        while (!line.equals("")) {
            log.debug("HTTP Header: {}", line);
            line = br.readLine();
            if (line.contains("Content-Length")) {
                String[] parsed = line.split(":");
                contentLength = Integer.parseInt(parsed[1].trim());
            }
        }
        return contentLength;
    }
}
