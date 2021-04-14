import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class LogIn extends Frame implements ActionListener, KeyListener {
    JTextField username;
    JPasswordField password;
    JButton loginButton, registerButton;
    Socket s;
    OutputStream os;
    BufferedWriter bw;
    BufferedReader br;
    String IP;
    public boolean isLogIn = false;
    public String ID = "";

    public void actionPerformed(ActionEvent ae){
        if(ae.getSource() == loginButton){
            login();
        }
        else if(ae.getSource() == registerButton){
            SignUp signUp = new SignUp(IP);
        }
    }

    private void login(){
        String ID = username.getText();
        String pass = password.getText();
        if (ID.isEmpty()){
            JOptionPane.showMessageDialog(this, "Invalid username",
                    "Please enter username", JOptionPane.ERROR_MESSAGE);
        }
        else if (pass.isEmpty()){
            JOptionPane.showMessageDialog(this, "Invalid password",
                    "Please enter password", JOptionPane.ERROR_MESSAGE);
        }
        else{
            this.ID = ID;
            try {
                bw.write("6|" + ID + "|" + pass);
                bw.newLine();
                bw.flush();
                this.listen();
            } catch (IOException e) {
                e.printStackTrace();
                lostConnect();
            }
        }
    }

    private void close(){
        try {
            bw.write("0|Unloged client" );
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
            lostConnect();
        }
    }

    public LogIn(String IP){
        this.IP = IP;

        this.setTitle("Log In");
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.out.println("Closing");
                close();
                System.exit(3);
            }
        });

        JLabel username_lb = new JLabel("Username: ");
        JLabel password_lb = new JLabel("Password");

        username = new JTextField(12);
        password = new JPasswordField(12);

        password.addKeyListener(this);
        username.addKeyListener(this);

        loginButton = new JButton("Log In");
        loginButton.addActionListener(this);

        registerButton = new JButton("Sign up");
        registerButton.addActionListener(this);

        JPanel button_panel = new JPanel();
        button_panel.add(loginButton);
        button_panel.add(registerButton);

        JPanel main_panel = new JPanel();
        main_panel.setLayout(new FlowLayout());


        JPanel gridbag_panel = new JPanel();
        gridbag_panel.setSize(450, 300);
        gridbag_panel.setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gridbag_panel.add(username_lb, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gridbag_panel.add(username, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gridbag_panel.add(password_lb, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridheight = 1;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(5, 5, 5, 5);
        gridbag_panel.add(password, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridheight = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 5, 5, 5);
        gridbag_panel.add(button_panel, gbc);


        try{
            s = new Socket(IP,3200);

        } catch (IOException e) {
            e.printStackTrace();
            lostConnect();
        }


        try{
            os=s.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            InputStream is=s.getInputStream();
            br=new BufferedReader(new InputStreamReader(is, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.exit(3);
        }


        main_panel.add(gridbag_panel);
        this.add(main_panel);
        this.setResizable(false);
        this.setSize(450,300);
        this.setLocationRelativeTo(null);
        this.setVisible(true);

    }

    public void lostConnect(){
        JOptionPane.showMessageDialog(this, "Can't connect to server",
                "ERROR: Server not found", JOptionPane.ERROR_MESSAGE);
        System.exit(3);
    }




    private void listen(){
        try{
                String receiveMessage = br.readLine();

                if(receiveMessage != null && !receiveMessage.equals("Client has disconnected")){
                    System.out.println(receiveMessage);
                    String[] loginRes = receiveMessage.split("\\|");
                    System.out.println(loginRes[1]);
                    if(loginRes[1].equals("true")){
                        close();
                        this.setVisible(false);
                        TCPClient client = new TCPClient(this.ID, IP);
                        client.start();
                    }
                    else if(loginRes[1].equals("exist")){
                        JOptionPane.showMessageDialog(this, "You already log in",
                                "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                    else if(loginRes[1].equals("false")){
                        JOptionPane.showMessageDialog(this, "Invalid username or password",
                                "ERROR", JOptionPane.ERROR_MESSAGE);
                    }
                }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            login();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}