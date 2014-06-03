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

    private JTable table;

    private JScrollPane scrollPane;

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
        addressTextField = new JTextField("주소를 적어주세요.");
        addressTextField.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(addressTextField);
        add(panel, BorderLayout.NORTH);
    }

    public void initPanelMiddle(JPanel panel) throws IOException {
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

        BufferedImage img = ImageIO.read(new File("./assets/lunch.png"));
        ImageIcon icon = new ImageIcon(img);
        imgLabel = new JLabel(icon);
        imgLabel.setBorder(new EmptyBorder(0, 0, 10, 0));


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

        String rowData[][] = {
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
                { "Row1-Column1", "Row1-Column2", "Row1-Column3"},
        };

        String columnNames[] = { "Column One", "Column Two", "Column Three"};

        table = new JTable(rowData, columnNames);
        table.setEnabled(false);

        JPanel imgPane = new JPanel();
        imgPane.setLayout(new BoxLayout(imgPane, BoxLayout.PAGE_AXIS));
        imgPane.add(imgLabel);
        imgPane.add(table);

        JPanel radioPane = new JPanel();
        radio3.setBorder(new EmptyBorder(0, 0, 0, 50));
        radioPane.setLayout(new BoxLayout(radioPane, BoxLayout.LINE_AXIS));
        radioPane.add(radio1);
        radioPane.add(radio2);
        radioPane.add(radio3);

        panel.add(imgPane);
        panel.add(radioPane);

        add(panel, BorderLayout.CENTER);
    }

    public void initPanelSouth(JPanel panel) {
        orderButton = new JButton();
        lunchSelectButton = new JButton();

        lunchSelectButton.setText(lunchSelectText);
        lunchSelectButton.addActionListener(this);

        orderButton.setText(orderText);
        orderButton.addActionListener(this);

        JButton a = new JButton();
        a.setText("add");

        JButton b = new JButton();
        b.setText("readd");

        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(lunchSelectButton);
        panel.add(a);
        panel.add(b);
        panel.add(orderButton);

        add(panel, BorderLayout.SOUTH);
    }

    public void initUI(int width, int height) throws IOException {
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
        this.setResizable(false);

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
            order.add(lunchText.get(currentIndex));
            order.add(menuName);
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


