package http;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Server{
    private AsynchronousServerSocketChannel server;
    private final static int BUFFER_SIZE = 256;

//    private final static String HEADER = """
//                    HTTP/1.1 %d %s
//                    Server: simple-http
//                    Content-Type: %s
//                    Content-Length: %s
//                    Connection: keep-alive
//
//                    """;

    public void processing(){
        try{
            server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", 8080));

            while (true){
                Future<AsynchronousSocketChannel> future = server.accept();
                handleClient(future);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void handleClient(Future<AsynchronousSocketChannel> future) throws ExecutionException, InterruptedException, TimeoutException, IOException{

        AsynchronousSocketChannel clientChannel = future.get(60, TimeUnit.SECONDS);

        if (clientChannel == null || !clientChannel.isOpen()){
            return;
        }

        boolean keepAlive = true;

        while (keepAlive && clientChannel.isOpen()){
            String rawRequest = readFullRequest(clientChannel);
            if (rawRequest == null || rawRequest.isEmpty()){
                break;
            }

            System.out.println(rawRequest);

            HttpRequest req = new HttpRequest(rawRequest);

            String connectionHeader = req.getHeader("connection");
            keepAlive = connectionHeader == null || !connectionHeader.equalsIgnoreCase("close");

            HttpResponse res = new HttpResponse(req);

//            String response = HEADER.formatted(
//                    200, "OK",
//                    "text/plain",
//                    body.length()
//            ) + body;
//

            String response = res.getResponse();
            ByteBuffer responseBuffer = ByteBuffer.wrap(response.getBytes(StandardCharsets.UTF_8));
            clientChannel.write(responseBuffer).get();
            
            System.out.println(response);
        }

        clientChannel.close();
    }

    private String readFullRequest(AsynchronousSocketChannel clientChannel) throws ExecutionException, InterruptedException, TimeoutException{
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        StringBuilder requestBuilder = new StringBuilder();
        int contentLength = 0;
        boolean headersParsed = false;

        while (true){
            int bytesRead = clientChannel.read(buffer).get();
            if (bytesRead <= 0){
                break;
            }

            buffer.flip();
            requestBuilder.append(StandardCharsets.UTF_8.decode(buffer));
            buffer.clear();

            if (!headersParsed){
                int headerEnd = requestBuilder.indexOf("\r\n\r\n");
                if (headerEnd >= 0){
                    headersParsed = true;

                    String headers = requestBuilder.substring(0, headerEnd);
                    for (String line : headers.split("\r\n")){
                        if (line.toLowerCase().startsWith("content-length:")){
                            try{
                                contentLength = Integer.parseInt(line.split(":")[1].trim());
                            }catch(NumberFormatException e){
                                contentLength = 0;
                            }
                        }
                    }

                    int bodyStart = headerEnd + 4;
                    int currentBodyLength = requestBuilder.length() - bodyStart;
                    if (currentBodyLength >= contentLength){
                        break;
                    }
                }
            }else{
                int headerEnd = requestBuilder.indexOf("\r\n\r\n");
                int bodyStart = headerEnd + 4;
                int currentBodyLength = requestBuilder.length() - bodyStart;
                if (currentBodyLength >= contentLength){
                    break;
                }
            }
        }
        return requestBuilder.toString();
    }
}

