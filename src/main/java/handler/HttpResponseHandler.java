package handler;

import model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Jbee on 2017. 3. 24..
 */
public class HttpResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);

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

    private DataOutputStream dos;

    public HttpResponseHandler(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void response(Data data) {

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
}
