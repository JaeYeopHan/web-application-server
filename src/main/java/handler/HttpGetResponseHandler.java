package handler;

import model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Jbee on 2017. 3. 26..
 */
public class HttpGetResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpGetResponseHandler.class);

    private DataOutputStream dos;

    public HttpGetResponseHandler(DataOutputStream dos) {
        this.dos = dos;
    }

    public void response(Data data) {
        if (data.is200Response()) {
            response200Header(dos, data.bodyLength(), data.contentType());
            if (!data.isBodyDataNull()) {
                responseBody(dos, data.getByte());
            }
        }
        if (data.is302Response()) {
            response302Header(dos, data.api());
        }
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

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
