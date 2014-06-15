import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * 서버를 시작하는 `main` 함수를 가지고있는 클래스입니다.
 * 포트 번호를 받아서 서버를 실행하고 `MyLunchWorker` 쓰레드를 작동하는 등의 일을 처리합니다.
 */
public class MyLunchServer {
    /**
     *
     * @param args 첫번째 인자로 서버의 포트를 받습니다.
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        if(args.length == 0) {
            System.out.println("서버를 시작하는데 필요한 argument가 존재하지않습니다");
            return;
        }
        int port = 9000;

        // 첫번째 인자로 정수값이 아닌 다른 값이 들어오면 무시하고 9000번으로 설정합니다.
        try {
            port = Integer.parseInt(args[0]);
        } catch(Exception e) {
        }

        try {
            // 서버 소켓을 시작합니다.
            ServerSocket server = new ServerSocket(port);
            System.out.println("내 점심 서버가 시작되었습니다.");

            // 소켓을 받아서 워커 쓰레드를 시작합니다.
            while(true) {
                Socket client = server.accept();
                MyLunchWorker worker = new MyLunchWorker(client);
                worker.start();
            }
        } catch(IOException e) {
            // 정상적으로 서버가 시작하지못했을 경우 예외처리
            System.out.println("서버가 정상적으로 시작되지않았습니다.");
            e.printStackTrace();
        }
    }
}


/**
 *  `Runnable` 인터페이스를 구현해서 쓰레드 환경을 구성합니다.
 *  `run` 함수 내부에선 주문을 입력 처리를 합니다.
 */
class MyLunchWorker implements Runnable {

    // `MyLunchServer`에서 넘겨주는 소켓을 저장하는 변수
    private Socket socket;

    // `socket`의 input stream을 저장합니다.
    private DataInputStream in;

    // `socket`의 output stream을 저장합니다.
    private DataOutputStream out;

    // 주문 입력 날짜를 저장합니다.
    private Date date;

    // 클라이언트의 ip를 저장합니다.
    private String addr;

    // `MyLunchWorker`를 `Thread`로 시작합니다.
    private Thread t;


    /**
     *
     * @param s `MyLunchServer`가 넘겨준 `Socket`
     * @throws IOException
     */
    public MyLunchWorker(Socket s) throws IOException {
        socket = s;
        addr = socket.getInetAddress().toString();

        // 클라이언트의 소켓을 이용해서 in, out에 스트림을 저장하고 ip를 이용해서 입장 문장을 출력합니다.
        System.out.println(String.format("손님 '%s' 입장.", addr));
        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    /**
     * 쓰레드를 시작하면 실제로 수행되는 메소드입니다.
     * `in` 변수에서 유저의 입력을 읽고 형식에 맞게 파싱을 한후 주문내역을 출력합니다.
     */
    public void run() {
        try {
            // 쓰레드를 `interrupt` 메소드로 중단시키기위해서 사용된 메소드
            while(!Thread.currentThread().isInterrupted()) {

                // 유저 입력을 읽습니다.
                String message = in.readUTF();

                /* 유저 입력은 다음과 같은 csv 포맷으로 저장되어있습니다.

                서울과학기술대학교 미래관 320호      치즈밥     갈비 치즈밥     2
                서울과학기술대학교 미래관 320호      치즈밥     그냥 치즈밥     1

                따라서 '\n'으로 먼저 줄단위로 나눈 '\t'으로 상세 주문 내역을 분리합니다.
                 */
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
            // socket이 종료되게되면 손님이 나간것이므로 그것에 대한 처리.
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

    /**
     * `Runnable`만 구현되있는 `MyLunchWorker`를 실제 쓰레드로 만들고 시작합니다.
     */
    public void start() {
        t = new Thread(this);
        t.start();
    }
}
