

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;


public class clientThread extends Thread{
    private ArrayList<clientThread> clientList;
    private Socket socket;
    private BufferedWriter bw;
    private BufferedReader br;
    private String username = "";
    boolean finish = false;
    private JTextArea chat_frame;
    private DefaultListModel<String> model;

    // Constructor
    public clientThread(Socket sk, ArrayList<clientThread> list, JTextArea history, DefaultListModel<String> model){
        this.socket = sk;
        clientList = list;
        clientList.add(this);
        try {
            bw  = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream(), "UTF-8"));
            br = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        // Set default is "Client". "Client" is the name for connection with log in and sign up menu
        username = "Client";
        chat_frame = history;
        this.model = model;
        System.out.println(socket.getPort());
    }

    private synchronized void updateUserList(){
        this.model.clear();
        model.addElement("Server");
        for(clientThread client: clientList){
            if(!client.username.equals("Client")){
                model.addElement(client.username);
            }
        }
    }


    // Send public message
    private synchronized void Announce(String msg){
        try{
            for(clientThread client: clientList){
                if(client != this && !client.username.equals("Client"))
                {
                    System.out.println(msg);
                    client.bw.write(msg);
                    client.bw.newLine();
                    client.bw.flush();
                }

            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        chat_frame.append(msg + "\n");
    }

    // Send client list to every client
    private synchronized void sendClientList(){
        String msg = "5|" + "Public";
        for(clientThread client: clientList){
            if(!client.username.equals("Client"))
                msg += "," + client.username;

        }

        try{
            System.out.println(msg);
            for(clientThread client: clientList){
                if(!client.username.equals("Client")) {
                    client.bw.write(msg);
                    client.bw.newLine();
                    client.bw.flush();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        chat_frame.append("Send client list" + "\n");
    }

    private synchronized void newcomerAnnouce(){
        String msg = "Welcome " + username;
        Announce("1|" + msg);
    }

    private synchronized void leaveAnnounce(){
        String msg = username + " has disconnected";
        if (!this.username.equals("Client")){
            Announce(msg);
        }
        else{
            try{
                bw.write(msg);
                bw.newLine();
                bw.flush();

            }
            catch (Exception e){
                e.printStackTrace();
            }
            chat_frame.append(msg + "\n");
        }

    }



    private synchronized void logIn(String ID, String pass){
        try{
            for(clientThread client: clientList){
                if(client.username.equals(ID)){
                    this.bw.write("6|exist");
                    this.bw.newLine();
                    this.bw.flush();
                    return;
                }
            }
            BufferedReader fin = new BufferedReader(new FileReader(new File("account.txt")));
            String line = "";
            while((line = fin.readLine()) != null){
                String[] info = line.split("\\|");
                if(ID.equals(info[0]) && pass.equals(info[1])){
                    this.bw.write("6|true");
                    this.bw.newLine();
                    this.bw.flush();
                    return;
                }
            }

            this.bw.write("6|false");
            this.bw.newLine();
            this.bw.flush();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private synchronized void messageProcess(String msg){
        String[] preprocessed = msg.split("\\|");
        String identifier = preprocessed[0];
        switch (identifier){
            case "2":
                publicMessage(preprocessed[1]);
                break;

            case "0":
                this.leaveAnnounce();
                this.finish = true;
                break;

            case "1":
                this.username = preprocessed[1];
                this.newcomerAnnouce();
                this.sendClientList();
                this.updateUserList();
                break;

            case "3":
                this.chat_frame.append("Receiving: " + preprocessed[1] + "\n");
                this.broadcastFile(preprocessed[1], preprocessed[2]);
                break;

            case "4":
                privateMessage(preprocessed[1], preprocessed[2]);
                break;

            case "6":
                chat_frame.append("Log in request: " + preprocessed[1] + "|" + preprocessed[2] + "\n");
                this.logIn(preprocessed[1], preprocessed[2]);
                break;

            case "7":
                this.privateFile(preprocessed[1], preprocessed[2], preprocessed[3]);
                break;

            case "8":
                chat_frame.append("Sign up: " + preprocessed[1] + "|" + preprocessed[2] + "\n");
                this.checkID(preprocessed[1], preprocessed[2]);
                break;

        }
    }


    private synchronized void checkID(String ID, String pass){
        try{
            BufferedReader fin = new BufferedReader(new FileReader(new File("account.txt")));
            String line = "";
            while((line = fin.readLine()) != null){
                String[] split = line.split("\\|");
                System.out.println(split[0]  + ", " + ID);
                if (split[0].equals(ID)){
                    bw.write("8|false");
                    bw.newLine();
                    bw.flush();
                    chat_frame.append("Sign up fail\n");
                    return;
                }
            }

            this.addNewAccount(ID, pass);
            bw.write("8|true");
            bw.newLine();
            bw.flush();
            chat_frame.append("Sign up success\n");
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }


    private synchronized void addNewAccount(String ID, String pass){
        try{
            BufferedWriter fout = new BufferedWriter(new FileWriter(new File("account.txt"), true));
            fout.write(ID + "|" + pass + "\n");
            fout.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }



    private synchronized void privateFile(String receiver, String fileName, String data){
        String sendMessage = "7|" + this.username + "|" + fileName + "|" + data;
        try{
            for (clientThread client: clientList){
                if(client.username.equals(receiver)){
                    client.bw.write(sendMessage);
                    client.bw.newLine();
                    client.bw.flush();
                    chat_frame.append(this.username + "->" + receiver + ": " + fileName + "\n");
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        chat_frame.append(this.username + " send a file: " + fileName + "\n");
    }


    private synchronized void broadcastFile(String fileName, String data){
        String sendMessage = "3|" + this.username + "|" + fileName + "|" + data;
        try{
            System.out.println("Sending: " + fileName);
            for (clientThread client: clientList){
                if(client != this){
                    client.bw.write(sendMessage);
                    client.bw.newLine();
                    client.bw.flush();
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        chat_frame.append(this.username + " send a file: " + fileName + "\n");
    }


    private synchronized void publicMessage(String msg){
        String sendMessage = this.username + ": " +  msg;
        Announce(sendMessage);
    }

    private synchronized void privateMessage(String receiver, String message){
        try{
            for(clientThread client: clientList){
                if(client.username.equals(receiver)){
                    client.bw.write("4|" + this.username + "|" + message);
                    client.bw.newLine();
                    client.bw.flush();
                    chat_frame.append(this.username + "->" + receiver + ": " + message + "\n");
                }
            }

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }



    public void run(){
        try{
            while(!finish){
                String receivedMessage = br.readLine();
                System.out.println("Receive: " + receivedMessage);
                messageProcess(receivedMessage);
            }
            clientList.remove(this);
            if(!this.username.equals("Client")){
                this.sendClientList();
                this.updateUserList();
            }

            br.close();
            bw.close();
            socket.close();

        }
        catch(Exception e){
            e.printStackTrace();
        }

        System.out.println("ClientThread closing");
    }

}


