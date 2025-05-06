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

public class Server {
    private AsynchronousServerSocketChannel server;
    private final static int BUFFER_SIZE = 256;

    private final static String HEADER = """
                    HTTP/1.1 %d %s
                    Server: simple-http
                    Content-Type: %s
                    Content-Length: %s
                    Connection: close
                    
                    """;

    public void processing(){
        try {
            server = AsynchronousServerSocketChannel.open().bind(new InetSocketAddress("127.0.0.1", 8080));

            Future<AsynchronousSocketChannel> future = server.accept();

            while (true){
                handleClient(future);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    private void handleClient(Future<AsynchronousSocketChannel> future) throws ExecutionException, InterruptedException, TimeoutException, IOException {

        AsynchronousSocketChannel clientChannel = future.get(60, TimeUnit.SECONDS);

        while(clientChannel != null && clientChannel.isOpen()){
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            StringBuilder request = new StringBuilder();

            while (true) {
                int bytesRead = clientChannel.read(buffer).get();
//                clientChannel.write(buffer);
                if (bytesRead <= 0){
                    break;
                }

                buffer.flip();
                request.append(new String(buffer.array(), 0, bytesRead));
                buffer.clear();

                if (request.toString().contains("\r\n\r\n")){
                    break;
                }
//                clientChannel.write(buffer);
            }
//            buffer.clear();

            buffer.put((new String("Hello World\n")).getBytes(StandardCharsets.UTF_8));
            buffer.flip();
            clientChannel.write(buffer).get();
            clientChannel.close();
        }

    }
}

