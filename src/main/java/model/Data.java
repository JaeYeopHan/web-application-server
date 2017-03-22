package model;

/**
 * Created by Jbee on 2017. 3. 24..
 */
public class Data {
    int statusCode;
    String page;
    byte[] bodyData;

    public Data(int statusCode, String page, byte[] bodyData) {
        this.statusCode = statusCode;
        this.page = page;
        this.bodyData = bodyData;
    }
}
