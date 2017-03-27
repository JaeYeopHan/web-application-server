package handler;

import model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Jbee on 2017. 3. 26..
 */
public class HttpPostResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);

    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String MAIN_PAGE = "/index.html";
    private static final String LOGIN_FAILED_PAGE = "/user/login_failed.html";

    private DataOutputStream dos;

    public HttpPostResponseHandler(DataOutputStream dos) {
        this.dos = dos;
    }

    public void response(Data data) {
        if (data.api().equals(MAIN_PAGE)) {
            response302LoginSuccessHeader(dos, data.api());
        }
        if (data.api().equals(LOGIN_FAILED_PAGE)) {
            response200LoginFailedHeader(dos);
        }
    }

    private void response302LoginSuccessHeader(DataOutputStream dos, String api) {
        try {
            dos.writeBytes("HTTP/1.1 302 OK \r\n");
            dos.writeBytes("Location: http://localhost:8080" + api + " \r\n");
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
}
