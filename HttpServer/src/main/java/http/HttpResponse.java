package http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse{
    private final static String SERVER_DIR = "server_dir";
    private final HttpRequest request;
    private final static String HEADER = """
            HTTP/1.1 %d %s\r
            Server: simple-http\r
            Content-Type: %s\r
            Content-Length: %d\r
            Connection: %s\r
            \r
            """;


    public HttpResponse(HttpRequest request){
        this.request = request;
    }

    public String getResponse() throws IOException {
        String method = request.getMethod();
        String path = request.getPath();
        String responseBody;
        int statusCode;
        String statusText;
        String contentType;
        String connectionStatus = request.getHeader("connection");

        try{
            if ("GET".equalsIgnoreCase(method)){
                if ("/".equals(path)){
                    responseBody = "Hello world!";
                    statusCode = 200;
                    statusText = "OK";
                    contentType = "text/plain";
                }else{
                    File file = new File(SERVER_DIR + path);
                    if (file.exists() && file.isFile()){
                        byte[] fileContent = Files.readAllBytes(file.toPath());
                        responseBody = new String(fileContent);
                        statusCode = 200;
                        statusText = "OK";
                        contentType = getMimeType(path);
                    }else{
                        responseBody = "404 Not Found";
                        statusCode = 404;
                        statusText = "Not Found";
                        contentType = "text/plain";
                    }
                }
            }else if ("POST".equalsIgnoreCase(method)){
                String body = request.getBody();
                String filename = "post_data_" + System.currentTimeMillis() + ".txt";
                File file = new File(filename);
                try(FileOutputStream out = new FileOutputStream(file)){
                    out.write(body.getBytes());
                }
                responseBody = "Data saved!";
                statusCode = 200;
                statusText = "OK";
                contentType = "text/plain";
            }else{
                responseBody = "405 Method Not Allowed";
                statusCode = 405;
                statusText = "Method Not Allowed";
                contentType = "text/plain";
            }
        }catch(IOException e){
            throw e;
        }

        String header = HEADER.formatted(statusCode, statusText, contentType, responseBody.getBytes().length, connectionStatus);

        return header + responseBody;
    }

    private String getMimeType(String path) throws IOException {
        try {
            File file = new File(SERVER_DIR + path);
            if (!path.contains(".")){
                return "application/octet-stream";
            }else{
                if (file.exists()) {
                    String mimeType = Files.probeContentType(file.toPath());
                    if (mimeType != null) {
                        return mimeType;
                    }
                }
            }
        } catch (IOException e) {
            throw e;
        }
        return "application/octet-stream";
    }
}
