package com.mycompany.smartorder.client;

import java.io.Serializable;
import java.util.Map;

/**
 *
 * @author joshar
 */
public class Request implements Serializable {    
    private String requestId;
    private String action;
    private Map<String, Object> payload;
    
    public Request(){};

    public Request(String requestId, String action, Map<String, Object> payload) {
        this.requestId = requestId;
        this.action = action;
        this.payload = payload;
    }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
