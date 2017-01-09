package lollygaggingsimulator;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NetworkHandler implements Runnable {

    ServerSocket serverSocket;
    Socket socket;
    ObjectOutputStream output;
    ObjectInputStream input;

    private JTextField ipFeild, portFeild, openPortFeild;
    private JLabel localIp,localPort;
    String serverIP;
    int port;
    private JPanel networkPanel;
    private JButton connectButton;
    private Thread t;
    private boolean running, connected;

    public NetworkHandler(ActionListener actionHandler) {
        running = true;
        connected = false;
        port = 33678;
        t = new Thread(this, "NetworkHandler");
        createGUI(actionHandler);
        t.start();
    }

    public void run() {

        try {
            serverSocket = new ServerSocket(port, 100);
        } catch (IOException ex) {

        }
        while (running) {

            if (socket == null) {
                try {
                    waitForConnect();
                } catch (IOException e) {

                }
            } else {

                try {
                    setupStreams();
                    whileConnected();
                    closeStreams();
                } catch (IOException ex) {

                }

            }
        }

    }

    public void setupStreams() throws IOException {
        connected = true;
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

    }

    public void waitForConnect() throws IOException {
        System.out.println("Waiting for sombody to connect...");
        serverSocket.setSoTimeout(1000);
        socket = serverSocket.accept();

    }

    public void whileConnected() throws IOException {
        Byte message = (byte) 0;//sends a 0 in confirmation that it is connected
        sendMessage(message);

        do {
            try {
                message = (byte) input.readObject();
                System.out.println(socket.getInetAddress().getHostAddress() + ": " + message);
                Program.lastRecived = message;

            } catch (ClassNotFoundException n) {
                System.out.println("Could not read this");
            }

        } while (true);

    }

    public void closeStreams() throws IOException {
        output.close();
        input.close();
        socket.close();
        socket = null;
    }

    public void sendMessage(Byte message) {

        try {
            output.writeObject(message);
            output.flush();
            System.out.println(message + " was sent");
        } catch (IOException e) {
            System.out.println("Could not send that message");

        }

    }

    public void connectToServer() throws IOException {

        System.out.println("Connecting to ..." + ipFeild.getText() + " : " + portFeild.getText());
        try {
            socket = new Socket(InetAddress.getByName(ipFeild.getText()), Integer.parseInt(portFeild.getText()));

            System.out.println("Connected!!!! to: " + socket.getInetAddress().getHostName());
        } catch (java.net.UnknownHostException e) {
            System.out.println("Unknown adress");

        }
    }

    public void createGUI(ActionListener actionHandler) {

        networkPanel = new JPanel();
        networkPanel.setLayout(new FlowLayout());

        localIp = new JLabel();
        localPort = new JLabel("your Port:"); //To make it clearer that the textfeild is the server port
        try {
            localIp.setText(InetAddress.getLocalHost().getHostAddress()); //This is your LOCAL ip
        } catch (UnknownHostException ex) {

        }
        ipFeild = new JTextField("IP");
        ipFeild.setPreferredSize(new Dimension(100, 20));
        ipFeild.setVisible(true);

        portFeild = new JTextField("Port");
        portFeild.setPreferredSize(new Dimension(50, 20));
        portFeild.setVisible(true);
        
        openPortFeild = new JTextField("Port");
        openPortFeild.setText(port+"");
        openPortFeild.setPreferredSize(new Dimension(50, 20));
        openPortFeild.setVisible(true);
        openPortFeild.addActionListener(actionHandler);

        connectButton = new JButton("Connect");
        connectButton.setVisible(true);
        connectButton.setText("Connect");
        connectButton.addActionListener(actionHandler);

        networkPanel.add(localIp); //adding all the Jcomponents in the right order
        networkPanel.add(ipFeild);
        networkPanel.add(portFeild);
        networkPanel.add(connectButton);
        networkPanel.add(localPort);
        networkPanel.add(openPortFeild);

    }

    public JPanel getNetworkPanel() { //This returns the networkpanel so it can be used in the main GUI
        return networkPanel;
    }

    public boolean connected() {
        return connected;
    }
}