import javax.swing.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;

public class TCPServer extends Thread
{

    JFrame frame;
    JPanel panel1;
    JPanel panel2;
    JPanel mainPanel;
    JTextArea chat_frame;
    ServerSocket s;
    Socket ss;
    OutputStream os;
    BufferedWriter bw;
    private ArrayList<clientThread> clientList;
    public DefaultListModel<String> model = new DefaultListModel<>();
    private JList userList;


    public TCPServer()
    {
        //Client list
        model.addElement("Server");
        userList = new JList(model);
        userList.setFixedCellWidth(100);
        userList.setBorder(new LineBorder(Color.BLACK));

        //Frame
        frame = new JFrame("Server");
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        //Panel
        mainPanel = new JPanel();
        mainPanel.setSize(750, 550);
        mainPanel.setLayout(new FlowLayout());

        panel1 = new JPanel();
        panel1.setSize(750, 600);

        GridBagLayout layout = new GridBagLayout();
        panel1.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();


        chat_frame = new JTextArea(30, 30);
        chat_frame.setEditable(false);
        chat_frame.setLineWrap(true);
        chat_frame.setWrapStyleWord(true);
        chat_frame.setBorder(new LineBorder(Color.BLACK));
        JScrollPane chatFrame_scroll = new JScrollPane(chat_frame);
        chatFrame_scroll.setVerticalScrollBarPolicy(22);

        JLabel chatFrame_lb = new JLabel("Server log");
        JLabel userList_lb = new JLabel("User list");

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 3, 5, 3);
        panel1.add(chatFrame_lb, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 3, 5, 10);
        panel1.add(chatFrame_scroll, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 10, 5, 3);
        panel1.add(userList_lb, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.insets = new Insets(5, 10, 5, 3);
        gbc.fill = GridBagConstraints.VERTICAL;
        panel1.add(userList, gbc);

        mainPanel.add(panel1);

        frame.add(mainPanel);
        frame.setResizable(false);


        try{
            s = new ServerSocket(3200, 10, InetAddress.getLocalHost());
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        clientList = new ArrayList<>();
        frame.setTitle("Server: " + s.getInetAddress());
    }

    private synchronized void checkFile(){
        if (!Files.exists(Paths.get("account.txt"))){
            try{
                FileOutputStream fout = new FileOutputStream("account.txt");
                fout.close();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void run(){
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setSize(800, 600);
        frame.setVisible(true);
        JOptionPane.showMessageDialog(frame, "IP: " + s.getInetAddress(),
                "Server address", JOptionPane.NO_OPTION);
        this.checkFile();
        try
        {

            while(clientList.size() < 10)
            {
                chat_frame.append("Waiting for a Client\n");
                ss=s.accept();
                new clientThread(ss, clientList, chat_frame, model).start();
            }

        }
        catch(IOException e)
        {
            System.out.println("There're some error");
        }
    }

    public static void main(String[] args) {
        TCPServer sever = new TCPServer();
        sever.start();
    }

}
