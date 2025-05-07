package http;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest{
    private final String method;
    private final String path;
    private final Map<String, String> headers = new HashMap<>();
    private final String body;

    public HttpRequest(String raw){
        String[] parts = raw.split("\r\n\r\n", 2);
        String head = parts[0];
        this.body = parts.length > 1 ? parts[1] : "";

        String[] lines = head.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        this.method = requestLine[0];
        this.path = requestLine[1];

        for (int i = 1; i < lines.length; i++){
            String[] hd = lines[i].split(":", 2);
            if (hd.length == 2){
                headers.put(hd[0].trim().toLowerCase(), hd[1].trim());
            }
        }
    }

    public String getMethod(){
        return method;
    }

    public String getPath(){
        return path;
    }

    public String getHeader(String name){
        return headers.get(name.toLowerCase());
    }

    public String getBody(){
        return body;
    }
}
