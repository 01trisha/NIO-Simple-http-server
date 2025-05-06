package http;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Server {
    private AsynchronousServerSocketChannel server;

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

            AsynchronousSocketChannel clientChannel = future.get(60, TimeUnit.SECONDS);
            while (true){
                //тут сделать чтение итд
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

