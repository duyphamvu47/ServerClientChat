import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import javax.swing.*;
import javax.swing.border.LineBorder;

public class TCPClient extends Thread implements ActionListener
{
    JFrame frame;
    JPanel panel1;
    JPanel mainPanel;
    JTextArea chat_frame;
    JTextArea input;
    JButton submit, sendFile;
    Socket s;
    OutputStream os;
    BufferedWriter bw;
    BufferedReader br;
    boolean finish = false;
    private boolean isLogIn = false;
    String username = "";
    private DefaultListModel<String> model = new DefaultListModel<>();
    JList clientList;


    public void actionPerformed(ActionEvent ae){
        if(ae.getSource() == submit){
            String message = input.getText();
            if(clientList.getSelectedValue().equals("Public") || clientList.getSelectedValue() == null){
                String sendMessage = "2|" + message;
                chat_frame.append(username + ": " + message + "\n");
                sendMesg(sendMessage);
            }
            else if (!clientList.getSelectedValue().equals(this.username)){
                String sendMessage = "4|" + clientList.getSelectedValue() + "|" + message;
                chat_frame.append("(Private)  To " + clientList.getSelectedValue() + ": " + message + "\n");
                sendMesg(sendMessage);
            }

            input.setText("");
        }
        else if(ae.getSource() == sendFile){
            JFileChooser j = new JFileChooser();

            File workingDirectory = new File(System.getProperty("user.dir"));
            j.setCurrentDirectory(workingDirectory);
            j.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);


            int r = j.showOpenDialog(null);
            if (r == JFileChooser.APPROVE_OPTION) {

                String dir = j.getSelectedFile().getAbsolutePath();
                if(checkFile(dir)){
                    sendFile(dir);
                }
            }
            else
                System.out.println("User cancel operation");
        }
    }


    //Send everything to server
    public void sendMesg(String msg){
        try{
            bw.write(msg);
            bw.newLine();
            bw.flush();
        }
        catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Can't connect to server",
                    "ERROR: Server not found", JOptionPane.ERROR_MESSAGE);
            System.exit(3);
        }
    }

    //Check file's existence and size
    public boolean checkFile(String dir){
        if (Files.exists(Paths.get(dir))){
            File fin = new File(dir);

            if(fin.length() < 10*1024*1024){
                return true;
            }
            else{
                chat_frame.append("ERROR: Selected file must under 10MBs\n");
            }
        }
        else{
            chat_frame.append("Can't find file: " + dir + "\n");
        }
        return false;
    }

    //Read file and create send message
    public void sendFile(String dir) {
        File fin = new File(dir);
        byte[] buff = new byte[(int) fin.length()];

        try{
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fin));
            bis.read(buff, 0, buff.length);
            String data = Base64.getEncoder().encodeToString(buff);
            String message = "";
            if(clientList.getSelectedValue().equals("Public")){
                message = "3|" + fin.getName() + "|" + data;
            }
            else if (!clientList.getSelectedValue().equals(this.username)){
                message = "7|" + clientList.getSelectedValue() + "|" + fin.getName() + "|" + data;
            }
//            String message = "3|" + fin.getName() + "|" + data ;
            bis.close();
            sendMesg(message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        chat_frame.append(username + " send a file: " + fin.getName() + "\n");

    }

    //Download file
    public void receiveFile(String fileName, String data){
        try{
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
            byte[] buff = Base64.getDecoder().decode(data);
            bos.write(buff, 0, buff.length);
            bos.close();
            this.openFile(fileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void openFile(String dir){
        try{
            if (Files.exists(Paths.get(dir))){
                File fin = new File(dir);
                int option = JOptionPane.showConfirmDialog(frame, "Open: " + fin.getName() + " ?", "Receive: " + fin.getName(), JOptionPane.YES_OPTION, JOptionPane.NO_OPTION);
                if(option == JOptionPane.YES_OPTION){
                    if(Desktop.isDesktopSupported()){
                        Desktop desktop = Desktop.getDesktop();
                        desktop.open(fin);
                    }
                    else{
                        chat_frame.append("Open file is not support in this platform\n");
                    }
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

    }



    //Constructor
    public TCPClient(String username, String IP)
    {
        // Send text when press "Enter"
        KeyListener keyListener = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    String message = input.getText();
                    if(clientList.getSelectedValue().equals("Public") || clientList.getSelectedValue() == null){
                        String sendMessage = "2|" + message;
                        chat_frame.append(username + ": " + message + "\n");
                        sendMesg(sendMessage);
                    }
                    else if (!clientList.getSelectedValue().equals(username)){
                        String sendMessage = "4|" + clientList.getSelectedValue() + "|" + message;
                        chat_frame.append("(Private)  To " + clientList.getSelectedValue() + ": " + message + "\n");
                        sendMesg(sendMessage);
                    }
                    e.consume();
                    input.setText("");
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        };

        this.username = username;

        frame = new JFrame(username);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                sendMesg("0|" + username);
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        model.addElement(this.username);
        clientList = new JList(model);
        clientList.setFixedCellWidth(100);
        clientList.setBorder(new LineBorder(Color.BLACK));
        clientList.setSelectedIndex(0);

        JLabel clientList_lb = new JLabel("Client list");
        clientList_lb.setLabelFor(clientList);



        mainPanel = new JPanel();
        mainPanel.setSize(750, 550);
        mainPanel.setLayout(new FlowLayout());

        panel1 = new JPanel();
        panel1.setSize(750, 550);

        GridBagLayout layout = new GridBagLayout();
        panel1.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        chat_frame = new JTextArea(24, 32);
        chat_frame.setEditable(false);
        chat_frame.setLineWrap(true);
        chat_frame.setWrapStyleWord(true);
        chat_frame.setBorder(new LineBorder(Color.BLACK));


        input = new JTextArea(4, 48);
        input.setBorder(new LineBorder(Color.BLACK));
        input.addKeyListener(keyListener);

        submit = new JButton("Send");
        submit.addActionListener(this);

        frame.getRootPane().setDefaultButton(submit);


        sendFile = new JButton("Send file");
        sendFile.addActionListener(this);


        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 2;
        gbc.insets = new Insets(5, 0, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel1.add(chat_frame, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 10, 5, 3);
        panel1.add(clientList_lb, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 10, 5, 3);
        panel1.add(clientList, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 0, 5, 10);
        panel1.add(input, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 10, 5, 0);
        panel1.add(sendFile, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(0, 10, 5, 0);
        panel1.add(submit, gbc);

        mainPanel.add(panel1);

        frame.add(mainPanel);

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


        sendMesg("1|" + this.username);
    }


    private synchronized void updateClientList(String src){
        String[] list_client = src.split("\\,");
        model.clear();
        for(String temp: list_client){
            model.addElement(temp);
        }
        clientList.setSelectedIndex(0);
    }

    //Process message from server
    public void messageProcess(String msg){
        String[] preprocess = msg.split("\\|");
        String identifier = preprocess[0];
        if (preprocess.length == 1){
            chat_frame.append(msg + "\n");
            return;
        }

        switch (identifier){
            case "3":

            case "7":
                chat_frame.append(preprocess[1] + " send a file: " + preprocess[2] + "\n");
                this.receiveFile(preprocess[2], preprocess[3]);
                break;

            case "1":
                chat_frame.append(preprocess[1] + "\n");
                break;

            case "4":
                chat_frame.append("(Private)  " + preprocess[1] + ": " + preprocess[2] + "\n");
                break;

            case "5":
                this.updateClientList(preprocess[1]);
                break;
        }
    }




    public void run(){
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(800, 600);
        frame.setVisible(true);
        frame.setResizable(false);


        String receivedMessage;

        new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    while (!finish)
                    {
                        String receivedMessage;
                        System.out.println("Listening");
                        receivedMessage=br.readLine();
                        System.out.println(receivedMessage);
                        if (receivedMessage == username + " has disconnected" || receivedMessage == null){
                            exit();
                        }
                        else{
                            messageProcess(receivedMessage);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Can't connect to server",
                            "ERROR: Server not found", JOptionPane.ERROR_MESSAGE);
                    System.exit(3);
                }

            }
        }).start();
    }

    private void exit() throws IOException {
        finish = true;
        br.close();
        bw.close();
        s.close();
    }


    public static void main(String[] args) {
        ConnectUI connectUI = new ConnectUI();
    }


}
