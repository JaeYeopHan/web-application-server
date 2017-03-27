package model;

/**
 * Created by Jbee on 2017. 3. 26..
 */
public class DataOfGet implements Data {
    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String CSS_CONTENT_TYPE = "text/css";
    private static final String JAVASCRIPT_CONTENT_TYPE = "text/javascript";

    private int statusCode;
    private String api;
    private byte[] bodyData;

    public DataOfGet() {}

    public DataOfGet(int statusCode, String api) {
        this.statusCode = statusCode;
        this.api = api;
    }

    public DataOfGet(int statusCode, String api, byte[] bodyData) {
        this.statusCode = statusCode;
        this.api = api;
        this.bodyData = bodyData;
    }

    public byte[] getByte() {
        return this.bodyData;
    }

    public int bodyLength() {
        return this.bodyData.length;
    }

    public String api() {
        return this.api;
    }

    public String contentType() {
        if (this.api.contains("html")) {
            return HTML_CONTENT_TYPE;
        }

        if (this.api.contains("css")) {
            return CSS_CONTENT_TYPE;
        }

        if (this.api.contains("js")) {
            return JAVASCRIPT_CONTENT_TYPE;
        }

        return "";
    }

    @Override
    public String httpMethod() {
        return "GET";
    }

    public boolean is200Response() {
        return this.statusCode == 200;
    }

    public boolean is302Response() {
        return this.statusCode == 302;
    }

    public boolean isBodyDataNull() {
        return this.bodyData == null;
    }
}
