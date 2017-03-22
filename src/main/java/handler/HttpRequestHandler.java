package handler;

import db.DataBase;
import model.Data;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;
import util.IOUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Jbee on 2017. 3. 24..
 */
public class HttpRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String CSS_CONTENT_TYPE = "text/css";
    private static final String JAVASCRIPT_CONTENT_TYPE = "text/javascript";

    private static final String STATIC_FOLDER_PATH = "./webapp";
    private static final String USER_CREATE_API = "/user/create";
    private static final String LOGIN_API = "/user/login";
    private static final String MAIN_PAGE = "/index.html";
    private static final String LOGIN_FAILED_PAGE = "/user/login_failed.html";
    private static final String USER_LIST_PAGE = "/user/list.html";
    private static final String LOGIN_PAGE = "/user/login.html";
    private BufferedReader br;

    public HttpRequestHandler(InputStream in) throws UnsupportedEncodingException {
        this.br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    }

    public Data handle() {
        try {
            String line = br.readLine();
            String[] headerUrl = getParedHeader(line);
            if (isGetReq(headerUrl[0])) {
                String api = headerUrl[1];
                log.debug("api: {}", api);
                if (api.equals(USER_LIST_PAGE)) {
                    if (!isLoginStatus(br)) {
                        return new Data(302, LOGIN_PAGE, null);
                    }
                    return new Data(200, HTML_CONTENT_TYPE, getUserListHTML().getBytes());
                }
                byte[] repBody = Files.readAllBytes(new File(STATIC_FOLDER_PATH + api).toPath());
                return new Data(200, getContentType(api), repBody);
            }

            if (isPostReq(headerUrl[0])) {
                String api = headerUrl[1];
                if (api.equals(USER_CREATE_API)) {
                    return handleCreateUserRequest();
                }

                if (api.equals(LOGIN_API)) {
                    return handleLoginRequest();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Data handleLoginRequest() throws IOException {
        String reqBody = IOUtils.readData(br, getContentLength(br));
        log.debug("body: {}", reqBody);
        Map<String, String> loginData = HttpRequestUtils.parseQueryString(reqBody);

        if (isLoginSuccess(loginData)) {
            log.debug("login success: {}", loginData);
            return new Data(302, MAIN_PAGE, null);
        }
        log.debug("login failed: {}", loginData);
        byte[] repBody = Files.readAllBytes(new File(STATIC_FOLDER_PATH + LOGIN_FAILED_PAGE).toPath());
        return new Data(200, getContentType(LOGIN_FAILED_PAGE) ,repBody);
    }

    private Data handleCreateUserRequest() throws IOException {
        String reqBody = IOUtils.readData(br, getContentLength(br));
        log.debug("reqBody: {}", reqBody);
        Map<String, String> userData = HttpRequestUtils.parseQueryString(reqBody);
        User user = new User(userData);
        log.debug("user: {}", user);
        DataBase.addUser(user);
        return new Data(302, MAIN_PAGE, null);
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

    private String getContentType(String url) {
        String contentType = null;
        if (url.contains("html")) {
            contentType = HTML_CONTENT_TYPE;
        }

        if (url.contains("css")) {
            contentType = CSS_CONTENT_TYPE;
        }

        if (url.contains("js")) {
            contentType = JAVASCRIPT_CONTENT_TYPE;
        }

        return contentType;
    }

    private String[] getParedHeader(String line) {
        return line.split(" ");
    }

    private boolean isPostReq(String httpMethod) {
        return httpMethod.equals("POST");
    }

    private boolean isGetReq(String httpMethod) {
        return httpMethod.equals("GET");
    }

}
