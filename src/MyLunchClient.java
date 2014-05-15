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
    private JButton orderButton;

    private JButton lunchSelectButton;

    private JPanel panel1;

    private JPanel panel2;

    private JPanel panel3;

    private JLabel label1;

    private JTextField addressTextField;

    private JLabel imgLabel;

    private Socket socket;

    private DataInputStream in;

    private DataOutputStream out;

    String orderText = "음식을 주문합니다.";

    String lunchSelectText = "점심을 골라보아요!!";

    public void initUI(int width, int height) throws IOException {
        addressTextField = new JTextField("주소를 적어주세요.");
        addressTextField.setFont(new Font("SansSerif", Font.BOLD, 12));

        panel1 = new JPanel();
        panel2 = new JPanel();
        panel3 = new JPanel();

        orderButton = new JButton();
        lunchSelectButton = new JButton();

        label1 = new JLabel();

        panel1.setLayout(new BorderLayout(10, 10));
        panel1.add(addressTextField, BorderLayout.NORTH);
        add(panel1, BorderLayout.NORTH);



        BufferedImage img = ImageIO.read(new File("./assets/abc.png"));
        ImageIcon icon = new ImageIcon(img);
        imgLabel = new JLabel(icon);

        panel2.add(imgLabel, BorderLayout.NORTH);
        add(panel2, BorderLayout.CENTER);

        lunchSelectButton.setText(lunchSelectText);
        lunchSelectButton.addActionListener(this);

        orderButton.setText(orderText);
        orderButton.addActionListener(this);

        panel3.setLayout(new BorderLayout());
        panel3.add(lunchSelectButton, BorderLayout.NORTH);
        panel3.add(orderButton, BorderLayout.CENTER);

        label1.setBackground(new Color(153, 153, 255));
        label1.setText("점심을 골라주세요 :)");

        panel3.add(label1, BorderLayout.SOUTH);
        add(panel3, BorderLayout.SOUTH);
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
        if(event.getActionCommand().equals(orderText)) {
            label1.setText("주문 접수중 ...");
            sendOrder();
        } else if(event.getActionCommand().equals(lunchSelectText)) {
            BufferedImage img = null;
            try {
                img = ImageIO.read(new File("./assets/def.png"));
                ImageIcon thisIcon = new ImageIcon(img);
                imgLabel.setIcon(thisIcon);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOrder() {
        try {
            List<String> order = new ArrayList<String>();
            order.add(addressTextField.getText());

            out.writeUTF(StringUtils.join(order, "\t"));
            label1.setText("Thank you!");
            out.flush();
        } catch (Exception e) {
            label1.setText("Error : " + e.toString());
        }
    }


    public void stop() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            label1.setText("Error : " + e.toString());
        }
    }
}


