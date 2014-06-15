import com.sun.deploy.util.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.rmi.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * UI를 초기화하는 `LunchClient`에 적절한 인자를 넘겨서 소켓 연결 합니다.
 */
public class MyLunchClient {
    /**
     *
     * @param args 인자로 연결할 서버 주소와 포트를 받습니다.
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 9000;

        // 인자에 올바르지못한 값이 들어오면 무시하고 `localhost`와 9000번 포트로 시작합니다.
        if(args.length == 2) {
            try {
                host = args[0];
                port = Integer.parseInt(args[1]);
            } catch(Exception e) {
            }
        }


        try {
            final LunchClient myLunch = new LunchClient(600, 400, host, port);
        } catch (IOException e) {
            System.out.println("프로그램이 정상적으로 실행되지못했습니다.");
            e.printStackTrace();
        }
    }
}

/**
 * swing을 이용하여 UI를 초기화합니다.
 */
class LunchClient extends JFrame implements ActionListener {

    // 그림이 랜덤으로 돌아갈때 약간씩 멈춰서 화면이 전환되는것을 보여줍니다.
    private Timer timer = new Timer(200, this);

    // 그림 전환 효과가 시작 상태를 담고있는 변수
    private boolean running = false;

    // 그림 전환이 몇번 됬는지 세고 있는 함수
    int count = 0;

    // 주문 버튼
    private JButton orderButton;

    // 다른 점심을 랜덤으로 선택할수있게하는 버튼
    private JButton lunchSelectButton;

    // 맨 위 패널로 주소정보를 담고있습니다.
    private JPanel panel1;

    // 중간 패널로 간판 이미지, 주문표, 메뉴를 담고있습니다.
    private JPanel panel2;

    // 맨 밑 패널로 모든 버튼들을 담고있습니다.
    private JPanel panel3;

    // 주소를 적는 텍스트 필드
    private JTextField addressTextField;

    // 간판 이미지를 담는 라벨
    private JLabel imgLabel;

    // 메뉴를 선택하는 라디오버튼의 추상화 그룹
    private ButtonGroup jbg1;

    // 첫번째 음식 메뉴 버튼
    private JRadioButton radio1;

    // 두번째 음식 메뉴 버튼
    private JRadioButton radio2;

    // 세번째 음식 메뉴 버튼
    private JRadioButton radio3;

    // 서버에 연결하는 소켓
    private Socket socket;

    // `socket`의 input stream
    private DataInputStream in;

    // `socket`의 output stream
    private DataOutputStream out;

    // 주문표 테이블
    private JTable table;

    // 주문을 추가하는 버튼
    private JButton menuAddButton;

    // 주문을 초기화하는 버튼
    private JButton menuClearButton;

    // 그림 전환 효과 할때 선택된 메뉴가 어떤건지 담고있는 변수
    private int currentIndex = 0;

    // 테이블이 몇줄인지 저장하는 변수
    private int rowOfMenu = 0;

    // 간판 그림의 경로 `assets/lunchImgPath[i].png`로 저장합니다.
    private List<String> lunchImgPath = Arrays.asList(
            "cheese",
            "soondae",
            "bongus",
            "pizza"
    );

    // 식당 이름
    private List<String> lunchText = Arrays.asList(
            "치즈밥",
            "순대국",
            "봉구스",
            "피자천국"
    );

    // 식당 메뉴를 정의하는 변수
    private List<List<String>> lunchMenuText = Arrays.asList(
            Arrays.asList(
                    "갈비 치즈밥",
                    "김치 치즈밥",
                    "걍 치즈밥"
            ),
            Arrays.asList(
                    "고기만 순대국",
                    "내장만 순대국",
                    "혼합 순대국"
            ),
            Arrays.asList(
                    "봉구스 밥버거",
                    "햄 밥버거",
                    "치즈 밥버거"
            ),
            Arrays.asList(
                    "치즈 피자",
                    "콤비네이션 피자",
                    "불고기 피자"
            )
    );

    // 주문 버튼 텍스트
    private String orderText = "음식을 주문합니다.";

    // 점심 선택 텍스트
    private String lunchSelectText = "식당 무작위 선택";

    // 음식 추가 버튼 텍스트
    private String menuAddButtonText = "음식 추가";

    // 주문 초기화 버튼 텍스트
    private String menuClearButtonText = "주문 내역 초기화";

    // 선택된 메뉴이름을 담는 변수
    private String menuName;

    // 주문표의 사이즈를 결정하는 변수
    private int sizeOfMenuTable = 8;

    // 점심 메뉴가 몇개인지 알려주는 변수
    private int menuNumber = 1;

    /**
     * panel1을 받아서 panel1에 들어갈 내용을 초기화합니다.
     * 주소입력 부분을 초기화합니다.
     *
     * @param panel panel1 을 받습니다.
     */
    public void initPanelNorth(JPanel panel) {
        addressTextField = new JTextField("주소를 적어주세요.");
        // 폰트는 SansSerif로 설정하고 두껍게, 12포인트
        addressTextField.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(addressTextField);
        add(panel, BorderLayout.NORTH);
    }

    /**
     * 간판 이미지와 라디오버튼, 주문표를 초기화합니다.
     *
     * @param panel panel2에 받습니다.
     * @throws IOException
     */
    public void initPanelMiddle(JPanel panel) throws IOException {
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        // 처음 보이는 기본 그림을 로딩합니다.
        BufferedImage img = ImageIO.read(new File("./assets/lunch.png"));
        ImageIcon icon = new ImageIcon(img);
        imgLabel = new JLabel(icon);
        // 약간의 공백을 만들기위해서 EmptyBorder를 추가
        imgLabel.setBorder(new EmptyBorder(0, 0, 10, 0));


        // 라디오 버튼을 초기화합니다.
        radio1 = new JRadioButton("메뉴", false);
        radio2 = new JRadioButton("정해", false);
        radio3 = new JRadioButton("주세요", false);

        // 라디오 버튼이 선택된다면 `menuName`에 메뉴의 이름을 집어넣습니다.
        radio1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuName = radio1.getText();
            }
        });

        radio2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuName = radio2.getText();
            }
        });

        radio3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuName = radio3.getText();
            }
        });

        // 라디오 버튼을 버튼 그룹으로 묶습니다.
        jbg1 = new ButtonGroup();

        jbg1.add(radio1);
        jbg1.add(radio2);
        jbg1.add(radio3);

        // 테이블 초기화 값입니다.
        String rowData[][] = {
                { "", "", ""},
                { "", "", ""},
                { "", "", ""},
                { "", "", ""},
                { "", "", ""},
                { "", "", ""},
                { "", "", ""},
                { "", "", ""}
        };

        String columnNames[] = { "가게이름", "메뉴", "갯수"};

        // 테이블을 500x100 사이즈로 초기화하고 고칠 수 없도록 설정합니다.
        table = new JTable(rowData, columnNames);
        table.setEnabled(false);
        table.setSize(new Dimension(500, 100));

        // 이미지 부분과 주문표 부분을 패널에 추가합니다.
        JPanel imgPane = new JPanel();
        imgPane.setLayout(new BoxLayout(imgPane, BoxLayout.PAGE_AXIS));
        imgPane.add(imgLabel);
        imgPane.add(table);

        // 라디오 버튼 부분을 패널에 추가합니다.
        JPanel radioPane = new JPanel();
        radio3.setBorder(new EmptyBorder(0, 0, 0, 50));
        radioPane.setLayout(new BoxLayout(radioPane, BoxLayout.LINE_AXIS));
        radioPane.add(radio1);
        radioPane.add(radio2);
        radioPane.add(radio3);

        panel.add(imgPane);
        panel.add(radioPane);

        // panel2를 프레임에 추가합니다.
        add(panel, BorderLayout.CENTER);
    }

    /**
     * 각종 버튼들을 초기화합니다. 주문과 관련된 모든 버튼들이 모여있는 패널입니다.
     * @param panel panel3을 받습니다.
     */
    public void initPanelSouth(JPanel panel) {
        // 주문버튼, 점심선택 버튼, 점심 추가버튼, 주문표 초기화버튼을 초기화합니다.
        orderButton = new JButton();
        lunchSelectButton = new JButton();

        lunchSelectButton.setText(lunchSelectText);
        lunchSelectButton.addActionListener(this);

        orderButton.setText(orderText);
        orderButton.addActionListener(this);

        menuAddButton = new JButton();
        menuAddButton.setText(menuAddButtonText);
        menuAddButton.addActionListener(this);

        menuClearButton = new JButton();
        menuClearButton.setText(menuClearButtonText);
        menuClearButton.addActionListener(this);

        // 갯수를 선택하는 JComboBox를 초기화합니다.
        String[] num = {"1개", "2개", "3개", "4개", "5개"};
        final JComboBox menuNumberCombo = new JComboBox(num);

        // 콤보박스가 선택되면 `menuNumber`를 설정하여 메뉴를 몇개시켰는지 저장합니다.
        menuNumberCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                menuNumber = menuNumberCombo.getSelectedIndex() + 1;
            }
        });


        // 패널에 버튼들을 추가합니다
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(lunchSelectButton);
        panel.add(menuClearButton);
        panel.add(menuNumberCombo);
        panel.add(menuAddButton);
        panel.add(orderButton);

        // 프레임에 panel3을 추가합니다.
        add(panel, BorderLayout.SOUTH);
    }

    /**
     * UI요소들을 초기화합니다.
     * @param width 프레임의 너비
     * @param height 프레임의 높이
     * @throws IOException
     */
    public void initUI(int width, int height) throws IOException {
        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();

        // 상,중,하 패널을 초기화합니다.
        initPanelNorth(panel1);
        initPanelMiddle(panel2);
        initPanelSouth(panel3);
    }

    /**
     *
     * @param width 프레임의 너비
     * @param height 프레임의 높이
     * @param host 연결할 서버 주소
     * @param port 연결할 서버의 포트
     * @throws IOException
     */
    public LunchClient(int width, int height, String host, int port) throws IOException {
        // UI를 초기화합니다.
        initUI(width, height);

        // callback 메소드 안에서 `this`를 사용하기위해 final로 `that`을 저장합니다.
        final LunchClient that = this;
        // 타이틀을 설정합니다
        this.setTitle("내 점심 :]");
        this.setSize(width, height);

        // 윈도우 닫을때 `stop` 메소드를 실행합니다.
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                that.stop();
                super.windowClosing(e);
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        // 프레임의 크기를 재조정하지못하게합니다.
        this.setResizable(false);

        // 소켓을 연결합니다.
        connect(host, port);
    }

    /**
     * 소켓을 연결합니다.
     * @param host 연결할 서버의 주소
     * @param port 연결할 서버의 포트
     */
    void connect(String host, int port) {
        try {
            // 소켓을 연결하고 문제가없다면 스트림을 저장합니다.
            socket = new Socket(host, port);
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (UnknownHostException e) {
            stop();
        } catch (IOException ie) {
            stop();
        }
    }

    /**
     * 버튼들의 이벤트를 받아서 처리합니다.
     * @param event 이벤트리스너의 이벤트를 받는 `ActionEvent`
     */
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if(command != null) {
            if(command.equals(orderText)) {
                // 주문 버튼이 눌리면 주문을 시작합니다
                sendOrder();
            } else if(command.equals(lunchSelectText)) {
                // 점심 선택 버튼이 눌리면 그림 전환효과를 시작합니다
                if (!running) {
                    timer.start();
                    running = true;
                }
                selectLunch();
            } else if(command.equals(menuAddButtonText)) {
                // 주문 추가 버튼이 눌리면 주문을 추가합니다.
                addMenu();
            } else if(command.equals(menuClearButtonText)) {
                // 주문 초기화 버튼이 눌리면 주문을 초기화합니다.
                clearMenu();
            }
        } else {
            // 그림 전환 효과가 시작되면 버튼이 눌리지않은 채로 이벤트가 발생합니다 따라서 command가 없을시에
            // 그림 전환 효과 중인것으로 간주하고 그림 전환 효과를 count가 20보다 작을때까지 실행합니다.
            selectLunch();
            count++;
            if (count == 20) {
                timer.stop();
                count = 0;
                running = false;
            }
        }
    }

    /**
     * 주문표에있는 주문을 서버로 전송합니다.
     * csv 포맷으로 만들어서 전송합니다.
     */
    public void sendOrder() {
        try {
            List<String> order = new ArrayList<String>();
            String address = addressTextField.getText();
            // 주문표의 주문수대로 주문을 보냅니다
            for(int i = 0; i < sizeOfMenuTable; i++) {
                if(!table.getValueAt(i, 0).equals("")) {
                    // 주문 내용은 \t으로 구분합니다.
                    order.add(String.format("%s\t%s\t%s\t%s",
                            address, table.getValueAt(i, 0), table.getValueAt(i, 1), table.getValueAt(0, 2)));
                }
            }
            if(out != null) {
                // 다른 주문내용은 \n으로 구분합니다. 알맞은 포맷으로 만든뒤에 서버로 전송합니다.
                out.writeUTF(StringUtils.join(order, "\n"));
                out.flush();
            }
        } catch (Exception e) {
        }
        clearMenu();
    }

    /**
     * 랜덤으로 점심 메뉴를 선택하고 이미지 라벨에 이미지를 교체합니다.
     */
    public void selectLunch() {
        // 랜덤으로 점심메뉴를 선택합니다.
        int randomIndex = (int)(Math.random() * (lunchImgPath.size()));

        String basePath = "./assets/%s.png";

        BufferedImage img = null;

        try {
            // 점심 식당의 이미지를 불러와서 교체합니다.
            img = ImageIO.read(new File(String.format(basePath, lunchImgPath.get(randomIndex))));
            ImageIcon thisIcon = new ImageIcon(img);
            imgLabel.setIcon(thisIcon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 알맞은 식당 메뉴로 라디오 버튼의 텍스트를 바꿔줍니다.
        currentIndex = randomIndex;
        radio1.setText(lunchMenuText.get(currentIndex).get(0));
        radio2.setText(lunchMenuText.get(currentIndex).get(1));
        radio3.setText(lunchMenuText.get(currentIndex).get(2));
    }


    /**
     * 예상치 못한 예외가 발생하였을때 소켓 스트림을 닫고 소켓을 닫습니다.
     */
    public void stop() {
        try {
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
            if(socket != null) {
                socket.close();
            }
        } catch (IOException e) {
        }
    }

    /**
     * 주문표에 음식을 추가합니다.
     */
    public void addMenu() {
        // 최대 주문량보다 작을때만 주문을 추가합니다.
        if(rowOfMenu < sizeOfMenuTable) {
            //현재 선택된 점심을 불러와서 주문합니다.
            table.setValueAt(lunchText.get(currentIndex), rowOfMenu, 0);
            table.setValueAt(menuName, rowOfMenu, 1);
            table.setValueAt(String.format("%d개", menuNumber), rowOfMenu, 2);

            //주문을 추가했다면 주문량을 증가합니다.
            rowOfMenu++;
        }
    }

    /**
     * 모든 주문을 초기화합니다.
     */
    public void clearMenu() {
        for(int i = 0; i < sizeOfMenuTable; i++) {
            table.setValueAt("", i, 0);
            table.setValueAt("", i, 1);
            table.setValueAt("", i, 2);
        }
    }
}


