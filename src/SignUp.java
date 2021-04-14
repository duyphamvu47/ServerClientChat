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



public class SignUp extends JFrame implements ActionListener, KeyListener {

    JTextField username;
    JPasswordField password;
    JButton confirmButton;
    Socket s;
    OutputStream os;
    BufferedWriter bw;
    BufferedReader br;
    JLabel signIn_res;


    public void actionPerformed(ActionEvent ae){
        if(ae.getSource() == confirmButton){
            signup();
        }
    }
    private void signup(){
        String ID = username.getText();
        String pass = password.getText();
        int checkID = checkID(ID, pass);
        switch (checkID){
            case 0:
                try {
                    bw.write("8|" + ID + "|" + pass);
                    bw.newLine();
                    bw.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    this.exit();
                }
                break;

            case 1:
                signIn_res.setText("Username can't contain special characters");
                break;

            case 2:
                signIn_res.setText("Please don't use special username like: Client, Server,..");
                break;

            case 3:
                signIn_res.setText("Password can't contain special characters");
                break;

            case 4:
                signIn_res.setText("Password can't contain special name like: Client, Server,..");
                break;
        }
    }


    public SignUp(String IP){

        this.setTitle("Sign Up");
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                close();
            }
        });

        JLabel username_lb = new JLabel("Username: ");
        JLabel password_lb = new JLabel("Password");
        signIn_res = new JLabel();

        username = new JTextField(12);
        password = new JPasswordField(12);
        password.addKeyListener(this);
        username.addKeyListener(this);


        confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(this);

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
        gridbag_panel.add(confirmButton, gbc);


        JPanel result_panel = new JPanel();
        result_panel.add(signIn_res);




        try{
            s = new Socket(IP,3200);

            os=s.getOutputStream();
            bw = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            InputStream is=s.getInputStream();
            br=new BufferedReader(new InputStreamReader(is, "UTF-8"));
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }



        main_panel.add(gridbag_panel);
        main_panel.add(result_panel);
        this.add(main_panel);
        this.setSize(350,200);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        System.out.println("Constructor");
        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("SignIn");
                try{
                    while(true){

                        String receiveMessage = br.readLine();

                        if(receiveMessage != null && !receiveMessage.equals("Client has disconnected")){
                            System.out.println(receiveMessage);
                            String[] signinRes = receiveMessage.split("\\|");
                            if(signinRes[1].equals("true")){
                                break;
                            }
                            else{
                                signIn_res.setText("Username already exist");
                            }
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    exit();
                }
                close();
            }
        }).start();
    }

    // Handle when lost connection to server
    public void exit(){
        JOptionPane.showMessageDialog(this, "Can't connect to server",
                "ERROR: Server not found", JOptionPane.ERROR_MESSAGE);
        System.exit(3);
    }


    private boolean checkSpecialChar(String str){
        Pattern pattern = Pattern.compile("[a-zA-Z0-9]*");

        Matcher matcher = pattern.matcher(str);

        if (!matcher.matches()) {
            return false;
        } else {
            return true;
        }
    }




    public int checkID(String id, String pass){
        String[] notAllowedID = {"client", "server", "fail", "true"};
        if(!checkSpecialChar(id)){
            return 1;
        }
        for(String temp: notAllowedID){
            if(id.toLowerCase().equals(temp)){
                return 2;
            }
        }
        if(!checkSpecialChar(pass)){
            return 3;
        }
        for(String temp: notAllowedID){
            if(pass.toLowerCase().equals(temp)){
                return 4;
            }
        }
        return 0;
    }





    public void close(){
        try {
            bw.write("0|Unloged client" );
            bw.newLine();
            bw.flush();
            this.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            this.exit();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            signup();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}