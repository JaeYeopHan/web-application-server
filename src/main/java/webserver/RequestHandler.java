package webserver;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;

import db.DataBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String CSS_CONTENT_TYPE = "text/css";
    private static final String JAVASCRIPT_CONTENT_TYPE = "text/javascript";

    private static final String STATIC_FOLDER_PATH = "./webapp";
    private static final String USER_CREATE_API = "/user/create";
    private static final String LOGIN_API = "/user/login";
    private static final String USER_LIST_PAGE = "/user/list.html";
    private static final String MAIN_PAGE = "/index.html";
    private static final String LOGIN_FAILED_PAGE = "/user/login_failed.html";
    private static final String LOGIN_PAGE = "/user/login.html";

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line = br.readLine();
            String[] headerUrl = getParedHeader(line);

            if (isGetReq(headerUrl[0])) {
                String api = headerUrl[1];
                log.debug("api: {}", api);
                if (api.equals(USER_LIST_PAGE)) {
                    handleGetUserListPage(out, br);
                    return;
                }
                DataOutputStream dos = new DataOutputStream(out);
                byte[] body = Files.readAllBytes(new File(STATIC_FOLDER_PATH + api).toPath());
                response200Header(dos, body.length, getContentType(api));
                responseBody(dos, body);
                return ;
            }

            if (isPostReq(headerUrl[0])) {
                String api = headerUrl[1];
                if (api.equals(USER_CREATE_API)) {
                    handleCreateUserRequest(out, br);
                    return;
                }

                if (api.equals(LOGIN_API)) {
                    handleLoginRequest(out, br);
                    return;
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void handleGetUserListPage(OutputStream out, BufferedReader br) throws IOException {
        DataOutputStream dos = new DataOutputStream(out);
        if (!isLoginStatus(br)) {
            response302Header(new DataOutputStream(out), LOGIN_PAGE);
            return ;
        }
        String userListHTML = getUserListHTML();
        byte[] body = userListHTML.getBytes();
        response200Header(dos, body.length, HTML_CONTENT_TYPE);
        responseBody(dos, body);
        return ;
    }

    private void handleLoginRequest(OutputStream out, BufferedReader br) throws IOException {
        String reqBody = IOUtils.readData(br, getContentLength(br));
        DataOutputStream dos = new DataOutputStream(out);
        log.debug("body: {}", reqBody);
        Map<String, String> loginData = HttpRequestUtils.parseQueryString(reqBody);

        if (isLoginSuccess(loginData)) {
            log.debug("login success: {}", loginData);
            response302LoginSuccessHeader(dos);
            return ;
        }
        log.debug("login failed: {}", loginData);
        byte[] repBody = Files.readAllBytes(new File(STATIC_FOLDER_PATH + LOGIN_FAILED_PAGE).toPath());
        response200LoginFailedHeader(dos);
        responseBody(dos, repBody);
        return ;
    }

    private void handleCreateUserRequest(OutputStream out, BufferedReader br) throws IOException {
        String reqBody = IOUtils.readData(br, getContentLength(br));
        DataOutputStream dos = new DataOutputStream(out);
        log.debug("reqBody: {}", reqBody);
        Map<String, String> userData = HttpRequestUtils.parseQueryString(reqBody);
        User user = new User(userData);
        log.debug("user: {}", user);
        DataBase.addUser(user);
        response302Header(dos, MAIN_PAGE);
        return ;
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
            sb.append("<tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
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

    private void response302Header(DataOutputStream dos, String path) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: http://localhost:8080" + path + " \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: http://localhost:8080" + MAIN_PAGE + " \r\n");
            dos.writeBytes("Set-Cookie: logined=true" + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200LoginFailedHeader(DataOutputStream dos) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + HTML_CONTENT_TYPE + ";charset=utf-8\r\n");
            dos.writeBytes("Set-Cookie: logined=false" + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
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
