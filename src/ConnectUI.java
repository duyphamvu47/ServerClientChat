import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.LineBorder;


public class ConnectUI extends JFrame implements ActionListener, KeyListener{

    public JTextField IP;
    public JButton confirmButton;

    public void actionPerformed(ActionEvent ae){
        if (ae.getSource() == confirmButton){
            callLogIn();
        }
    }

    public ConnectUI(){
        this.setTitle("ConnectUI");
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(3);
            }
        });

        JLabel IP_lb = new JLabel("Server address:");

        IP = new JTextField(12);
        IP.addKeyListener(this);

        confirmButton = new JButton("Connect");
        confirmButton.addActionListener(this);


        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(IP_lb, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(IP, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(confirmButton, gbc);

        this.add(mainPanel);
        this.setSize(350,200);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            this.callLogIn();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }


    private void callLogIn(){
        String ip = IP.getText();
        if (IP != null){
            if (!ip.isEmpty()){
                setVisible(false);
                LogIn logIn_menu = new LogIn(ip);
            }
        }
    }

}