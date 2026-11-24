package com.mycompany.smartorder.client;

import java.io.Serializable;

/**
 *
 * @author joshar
 */
public class Response implements Serializable {
    private String requestId;
    private boolean success;
    private String message;
    private Object data;
    
    public Response(){}

    public Response(String requestId, boolean success, String message, Object data) {
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    public String getRequestId() { return requestId; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Object getData() { return data; }    
}
