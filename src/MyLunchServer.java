import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MyLunchServer {
    public static void main(String args[]) throws IOException {
        if(args.length == 0) {
        }

        try {
            LunchServer server = new LunchServer(9000);
            server.run();
        } catch(IOException e) {
            System.out.println("서버가 정상적으로 시작되지않았습니다.");
            e.printStackTrace();
        }
    }
}

class LunchServer {

    ServerSocket server;

    public LunchServer(int port) throws IOException {
        server = new ServerSocket(port);
    }

    void run() throws IOException {
        System.out.println("내 점심 서버가 시작되었습니다.");
        while(true) {
            Socket client = server.accept();
            MyLunchWorker worker = new MyLunchWorker(client);
            worker.start();
        }

    }
}


class MyLunchWorker implements Runnable {

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    private Date date;

    private String addr;

    private Thread t;


    public MyLunchWorker(Socket s) throws IOException {
        socket = s;
        addr = socket.getInetAddress().toString();
        System.out.println(String.format("손님 '%s' 입장.", addr));
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    public void run() {
        try {
            while(!Thread.currentThread().isInterrupted()) {
                String message = in.readUTF();
                System.out.println(message);
            }
        } catch(EOFException e) {
            System.out.println(String.format("손님 '%s' 종료.", socket.getInetAddress()));
        } catch(IOException e) {
            try {
                in.close();
                out.close();
                socket.close();
            } catch (IOException ig) {
            }
            t.interrupt();
            e.printStackTrace();
        }
    }

    public void start() {
        t = new Thread(this);
        t.start();
    }
}
