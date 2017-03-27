package handler;

import model.Data;
import model.DataOfGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * Created by Jbee on 2017. 3. 24..
 */
public class HttpRequestHandler {
    private static final Logger log = LoggerFactory.getLogger(HttpRequestHandler.class);

    private BufferedReader br;

    public HttpRequestHandler(InputStream in) throws UnsupportedEncodingException {
        this.br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    }

    public Data handle() throws IOException {
        String line = br.readLine();
        String[] headerUrl = getParedHeader(line);
        String httpMethod = headerUrl[0];
        String api = headerUrl[1];

        if (isGetReq(httpMethod)) {
            new HttpGetRequestHandler(br).handle(api);
        }

        if (isPostReq(httpMethod)) {
            new HttpPostRequestHandler(br).handle(api);
        }
        return new DataOfGet();
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
