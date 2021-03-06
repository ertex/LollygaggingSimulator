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
    private JLabel localIp, localPort;
    String serverIP;
    int port;
    private JPanel networkPanel;
    private JButton connectButton;
    private Thread t;
    private boolean running, connected;

    private long pingSent, pingRecived; //this is used to get the ping to remote
    private int ping; //the current ping
    private int foobar = 0;//This is used in pingRemote, you might want to look the other way?

    public NetworkHandler(ActionListener actionHandler) {
        running = true;
        connected = false; //wether or not local is connected to remote
        port = 33678; //the port that will be used to connect to the server
        t = new Thread(this, "NetworkHandler");
        createGUI(actionHandler);
        t.start();//also calls for the run
    }

    public void run() { //This is the core of the network, it makes sure that everything is executed in the right order

        try {
            serverSocket = new ServerSocket(port, 100); //creates the server socket that the remote will connect to
        } catch (IOException ex) {

        }
        while (running) {

            if (socket == null) { //checks if there is a estblished connection, if not: check for incoming connections
                try {
                    waitForConnect();
                } catch (IOException e) {

                }
            } else {

                try {
                    setupStreams(); //creates the sockets and connects them
                    whileConnected();//The program stays here as long as the progrm is running
                    closeStreams(); //closes all the sokets nice and easy so nothing breaks
                } catch (IOException ex) {

                }

            }
        }

    }

    public void setupStreams() throws IOException {//creates the streams 
        connected = true;
        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(socket.getInputStream());

    }

    public void waitForConnect() throws IOException { //tries to establish a connection every 1 second with a incoming connection
        System.out.println("Waiting for sombody to connect...");
        serverSocket.setSoTimeout(1000);
        socket = serverSocket.accept();

    }

    public void whileConnected() throws IOException { //this method is the main core of the class, it recives messages
        Byte message = (byte) 0;//sends a 0 in confirmation that it is connected
        sendMessage(message);

        do {
            try {
                message = (byte) input.readObject();
                Program.lastRecived = message; //saves the last recived message/input in a static variable, this might not be the safest approach but it works for this application

            } catch (ClassNotFoundException n) {
                System.out.println("Could not read this");
            }

        } while (true);

    }

    public void pingRemote() {//sends a mesge that bounces on remote as "43" nd time gets recorded, see Program.run() "case 42" & "case 43"
        if (connected) {//won't ping unless remote is connected
            foobar++;
            if (foobar >= 120) {//this is a way that makes it only ping every 60 ittertions, hence not drawing stupid ammounts of power
                foobar = 0;
                //this solotion is horrible, if I forget to ask you how to do this in  different way, take contact
                ping = (int) (pingRecived - pingSent);//this calculates the ping by taking the diference in time between reciving and sending a message
                //this means it lags behind by one tick, but that is close enogh
                sendMessage((byte) 42);//sends the ping
                pingSent = System.currentTimeMillis(); //saves the time it was sent
            }
        }
    }

    public void closeStreams() throws IOException { //yep, this turns of the streams, seems like it's a good thing to do
        
        output.close();
        input.close();
        socket.close();
        socket = null;
    }

    public void sendMessage(Byte message) { //sends a message to remote

        try {
            output.writeObject(message);
            output.flush();
        } catch (IOException e) {
            System.out.println("Could not send that message");

        }

    }

    public void connectToServer() throws IOException {//tries to send a connection to another client

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

        localIp = new JLabel("Enter a Ip adress:");
        localPort = new JLabel("your Port:"); //To make it clearer that the textfeild is the server port

        ipFeild = new JTextField("IP");
        ipFeild.setPreferredSize(new Dimension(100, 20));
        ipFeild.setVisible(true);

        portFeild = new JTextField("Port");
        portFeild.setPreferredSize(new Dimension(50, 20));
        portFeild.setVisible(true);

        openPortFeild = new JTextField("Port");
        openPortFeild.setText(port + "");
        openPortFeild.setPreferredSize(new Dimension(50, 20));
        openPortFeild.setVisible(true);
        openPortFeild.addActionListener(actionHandler);//This actionListner won't work

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

    public int getPing() {
        return ping;

    }

    public void setPingRecived(long time) {
        pingRecived = time;
    }

}
