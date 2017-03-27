package handler;

import db.DataBase;
import model.Data;
import model.DataOfGet;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

/**
 * Created by Jbee on 2017. 3. 26..
 */
public class HttpGetRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpGetRequestHandler.class);
    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String USER_LIST_PAGE = "/user/list.html";
    private static final String LOGIN_PAGE = "/user/login.html";
    private static final String STATIC_FOLDER_PATH = "./webapp";

    private BufferedReader br;

    public HttpGetRequestHandler(BufferedReader br) {
        this.br = br;
    }

    public Data handle(String api) throws IOException {
        log.debug("api: {}", api);
        if (api.equals(USER_LIST_PAGE)) {
            return getUserListPage();
        }
        byte[] repBody = Files.readAllBytes(new File(STATIC_FOLDER_PATH + api).toPath());
        return new DataOfGet(200, api, repBody);
    }

    private Data getUserListPage() throws IOException {
        if (!isLoginStatus(br)) {
            return new DataOfGet(302, LOGIN_PAGE);
        }
        //TODO api => other parameter instead of HTML_CONTENT_TYPE
        return new DataOfGet(200, HTML_CONTENT_TYPE, getUserListHTML().getBytes());
    }

    private boolean isLoginStatus(BufferedReader br) throws IOException {
        String line = br.readLine();
        while (!line.equals("")) {
            line = br.readLine();
            String cookieValue = getCookie(line);
            if (cookieValue != null && cookieValue.contains("true")) {
                return true;
            }
        }
        return false;
    }

    //util
    private String getUserListHTML() {
        Collection<User> users = DataBase.findAll();
        log.debug("users: {}", users);
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        sb.append("<thead>");
        sb.append("<tr>");
        sb.append("<th>#</th> <th>사용자 아이디</th> <th>이름</th> <th>이메일</th><th></th>");
        sb.append("</tr>");
        sb.append("</thead>");
        sb.append("<tbody>");
        for (User user : users) {
            sb.append("<tr>");
            sb.append("<th scope=\"row\">1</th> <td>" + user.getUserId()+ "</td>");
            sb.append("<td>" + user.getName() + "</td>");
            sb.append("<td>" + user.getEmail() + "</td>");
            sb.append("<td><a href=\"#\" class=\"btn btn-success\" role=\"button\">수정</a></td>");
            sb.append("</tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private String getCookie(String line) {
        String cookieValue = null;
        if (line.contains("Cookie")) {
            String[] parsed = line.split(":");
            String temp = parsed[1].trim();
            String[] parsedCookie = temp.split("=");
            cookieValue = parsedCookie[1].trim();
            log.debug("cookieValue: {}", cookieValue);
        }
        return cookieValue;
    }
}
