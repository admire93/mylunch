import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class MyLunchServer {
    public static void main(String args[]) throws IOException {
        if(args.length == 0) {
            System.out.println("서버를 시작하는데 필요한 argument가 존재하지않습니다");
            return;
        }

        try {
            ServerSocket server = new ServerSocket(9000);
            System.out.println("내 점심 서버가 시작되었습니다.");
            while(true) {
                Socket client = server.accept();
                MyLunchWorker worker = new MyLunchWorker(client);
                worker.start();
            }
        } catch(IOException e) {
            System.out.println("서버가 정상적으로 시작되지않았습니다.");
            e.printStackTrace();
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
                String[] m = message.split("\n");
                System.out.println("주문 내역");
                for(int i = 0; i < m.length; i++) {
                    String[] r = m[i].split("\t");
                    System.out.println("----------------");
                    System.out.println(String.format("주소: %s", r[0]));
                    System.out.println(String.format("식당: %s", r[1]));
                    System.out.println(String.format("메뉴: %s", r[2]));
                    System.out.println(String.format("갯수: %s", r[3]));
                }
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
