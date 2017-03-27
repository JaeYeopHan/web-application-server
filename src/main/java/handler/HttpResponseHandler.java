package handler;

import model.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.OutputStream;

/**
 * Created by Jbee on 2017. 3. 24..
 */
public class HttpResponseHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpResponseHandler.class);

    private DataOutputStream dos;

    public HttpResponseHandler(OutputStream out) {
        this.dos = new DataOutputStream(out);
    }

    public void response(Data data) {
        if (data.httpMethod().equals("POST")) {
            new HttpPostResponseHandler(dos).response(data);
        }
        new HttpGetResponseHandler(dos).response(data);
    }
}
