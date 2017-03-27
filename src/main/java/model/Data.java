package model;

/**
 * Created by Jbee on 2017. 3. 24..
 */
public interface Data {
    boolean is200Response();
    boolean is302Response();
    boolean isBodyDataNull();
    byte[] getByte();
    int bodyLength();
    String api();
    String contentType();
    String httpMethod();
}
