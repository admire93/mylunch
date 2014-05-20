import com.sun.deploy.util.StringUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
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

public class MyLunchClient {
    public static void main(String[] args) {
        try {
            final LunchClient myLunch = new LunchClient(600, 400, "127.0.0.1");
        } catch (IOException e) {
            System.out.println("프로그램이 정상적으로 실행되지못했습니다.");
            e.printStackTrace();
        }
    }
}

class LunchClient extends JFrame implements ActionListener {
    private Timer timer = new Timer(200, this);

    private boolean running = false;

    int count = 0;

    private JButton orderButton;

    private JButton lunchSelectButton;

    private JPanel panel1;

    private JPanel panel2;

    private JPanel panel3;

    private JLabel label1;

    private JTextField addressTextField;

    private JLabel imgLabel;

    private ButtonGroup jbg1;

    private JRadioButton radio1;

    private JRadioButton radio2;

    private JRadioButton radio3;

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    private int currentIndex = 0;

    private List<String> lunchImgPath = Arrays.asList(
            "cheese",
            "soondae",
            "bongus",
            "pizza"
    );

    private List<String> lunchText = Arrays.asList(
            "치즈밥",
            "순대국",
            "봉구스",
            "피자천국"
    );

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

    String orderText = "음식을 주문합니다.";

    String lunchSelectText = "점심을 골라보아요!!";

    String menuName;

    public void initPanelNorth(JPanel panel) {
        orderButton = new JButton();
        lunchSelectButton = new JButton();

        label1 = new JLabel();

        panel.setLayout(new BorderLayout(10, 10));
        panel.add(addressTextField, BorderLayout.NORTH);
        add(panel, BorderLayout.NORTH);
    }

    public void initPanelMiddle(JPanel panel) throws IOException {
        BufferedImage img = ImageIO.read(new File("./assets/lunch.png"));
        ImageIcon icon = new ImageIcon(img);
        imgLabel = new JLabel(icon);

        radio1 = new JRadioButton("메뉴", false);
        radio2 = new JRadioButton("정해", false);
        radio3 = new JRadioButton("주세요", false);

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

        jbg1 = new ButtonGroup();

        jbg1.add(radio1);
        jbg1.add(radio2);
        jbg1.add(radio3);

        panel.add(imgLabel, BorderLayout.NORTH);
        panel.add(radio1);
        panel.add(radio2);
        panel.add(radio3);
        add(panel, BorderLayout.CENTER);
    }

    public void initPanelSouth(JPanel panel) {
        lunchSelectButton.setText(lunchSelectText);
        lunchSelectButton.addActionListener(this);

        orderButton.setText(orderText);
        orderButton.addActionListener(this);

        panel.setLayout(new BorderLayout());
        panel.add(lunchSelectButton, BorderLayout.LINE_START);
        panel.add(orderButton, BorderLayout.LINE_END);

        label1.setBackground(new Color(153, 153, 255));
        label1.setText("점심을 골라주세요 :)");

        panel.add(label1, BorderLayout.SOUTH);
        add(panel, BorderLayout.SOUTH);
    }

    public void initUI(int width, int height) throws IOException {
        addressTextField = new JTextField("주소를 적어주세요.");
        addressTextField.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();

        initPanelNorth(panel1);
        initPanelMiddle(panel2);
        initPanelSouth(panel3);
    }

    public LunchClient(int width, int height, String host) throws IOException {
        initUI(width, height);

        final LunchClient that = this;
        this.setTitle("내 점심 :]");
        this.setSize(width, height);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                that.stop();
                super.windowClosing(e);
            }
        });

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);

        connect(host);
    }

    void connect(String host) {
        try {
            socket = new Socket(host, 9000);
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (UnknownHostException e) {
            stop();
        } catch (IOException ie) {
            stop();
        }
    }

    public void actionPerformed(ActionEvent event) {
        if(event.getActionCommand() != null) {
            if(event.getActionCommand().equals(orderText)) {
                label1.setText("주문 접수중 ...");
                sendOrder();
            } else if(event.getActionCommand().equals(lunchSelectText)) {
                if (!running) {
                    timer.start();
                    running = true;
                }
                selectLunch();
            }
        } else {
            selectLunch();
            count++;
            if (count == 20) {
                timer.stop();
                count = 0;
                running = false;
            }
        }
    }

    public void sendOrder() {
        try {
            List<String> order = new ArrayList<String>();
            order.add(addressTextField.getText());
            if(out != null) {
                out.writeUTF(StringUtils.join(order, "\t"));
                out.flush();
                label1.setText("주문되었습니다.");
            } else {
                label1.setText("인터넷 연결을 확인해주세요.");
            }
        } catch (Exception e) {
            label1.setText("Error : " + e.toString());
        }
    }

    public void selectLunch() {
        int randomIndex = (int)(Math.random() * (lunchImgPath.size()));

        String basePath = "./assets/%s.png";

        BufferedImage img = null;

        try {
            img = ImageIO.read(new File(String.format(basePath, lunchImgPath.get(randomIndex))));
            ImageIcon thisIcon = new ImageIcon(img);
            imgLabel.setIcon(thisIcon);
        } catch (IOException e) {
            e.printStackTrace();
        }

        currentIndex = randomIndex;
        radio1.setText(lunchMenuText.get(currentIndex).get(0));
        radio2.setText(lunchMenuText.get(currentIndex).get(1));
        radio3.setText(lunchMenuText.get(currentIndex).get(2));
    }


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
            label1.setText("Error : " + e.toString());
        }
    }
}


