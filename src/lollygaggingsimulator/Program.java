package lollygaggingsimulator;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Program extends JFrame implements ActionListener {

    private Canvas canvas;
    Graphics g;
    private ArrayList<Projectile> projectiles;
    static final int xSize = 1200; // size of the window
    static final int ySize = 600;
    static byte lastRecived;

    private Character localPlayer, remotePlayer;

    private JPanel defencePanel;
    private JPanel attackPanel;
    private JPanel guiPanel;

    private BufferStrategy bs;

    private MouseEvent mousePressed;
    private ActionHandler actionHandler = new ActionHandler();
    private NetworkHandler networkHandler = new NetworkHandler(actionHandler);

    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;

    private long lastUpdateTick; //Used for updateing 
    private final int TPS = 60;//USed for the update speed

    public Program() {
        System.out.println("started");

        projectiles = new ArrayList();
        localPlayer = new Character(200, 200, 100, 100); //the local character is created here, to move it change the values
        remotePlayer = new Character(700, 200, 100, 100);
        lastUpdateTick = System.currentTimeMillis();
        createAndShowGUI();
        canvas.createBufferStrategy(2);//creates double buffering in canvas object
        bs = canvas.getBufferStrategy();
        generateProjectiles(); //creates the projectiles that will be used 
        run();
    }

    public void generateProjectiles() {
        for (int i = 0; i < 10; i++) {
            projectiles.add(new Projectile(0, 0, (byte) 0));
        }
    }

    public void run() {
        while (true) {
            if (System.currentTimeMillis() > (lastUpdateTick + (1000 / TPS))) { //checks if enoth time has passed for another itteration
                lastUpdateTick = System.currentTimeMillis();//saves the time when it went into the while loop
                localPlayer.update();
                remotePlayer.update();
                networkHandler.pingRemote();
                for (Projectile o : projectiles) {
                    o.update();

                }

                paintComponents();
                collitionCheck();

            }
            if (lastRecived > 0) { //This is were all the magic happens, all of the incoming messages are sorted here
                //and the number decrypted to an action
                switch (lastRecived) { //a switch with what the last message was, hence making something understandable from it
                    case 1:
                        //The following checks what action the remoteplayer did
                        remotePlayer.duck();//remotePlayer dicks
                        break;
                    case 2:
                        remotePlayer.jump(); //remotePlayer jumps
                        break;
                    case 3:
                        //checks what ctionw as perormed and sees if another shot was fired not long ago
                        shoot(700, 297, (byte) -2);//remotePlayer shoots a low attack with negative direction
                        break;
                    case 4:
                        //checks what ctionw as perormed and sees if another shot was fired not long ago
                        shoot(700, 201, (byte) -2);//remotePlayer shoots a high attack with negative direction      
                        break;
                    case (byte) 37:  //checks to see if your opponent sent a message that remote were hit.
                        resetProjectiles();
                        JOptionPane.showMessageDialog(Program.this, "YOU HIT THE OPONENT!, you re winer!"); //popup with you hit your opponent message
                        resetProjectiles();
                        break;

                    case (byte) 42:
                        networkHandler.sendMessage((byte) 43);//bounces back a signal, this is to get the time between the sent and recived ping
                        break;

                    case (byte) 43:
                        networkHandler.setPingRecived(System.currentTimeMillis());
                        break;

                    default:
                        break;

                }
                lastRecived = 0;
            }

        }
    }

    public void resetProjectiles() {

        for (Projectile o : projectiles) {
            o.setActive(false);
        }
    }

    public void collitionCheck() {//checks if any of the players have a projectile intersecting one of them.
        for (Projectile o : projectiles) {
            if (o.getActive()) { // only checks the ones that is cirrently being used 
                if (localPlayer.getHitbox().intersects(o.getHitbox())) { //checks the collition of localPlayer, it is only nessecary to check the localPlayer, since both clients are checking
                    //the main ereson for this is increeced performance and correct sync, else there might be a conflict between what the result is. 
                    //it might cause some questionable mechanics, i.e one seeing the opponet getting hit while the other one didn't
                    networkHandler.sendMessage((byte) 37);//sends the code of victory to the other player
                    resetProjectiles();
                    JOptionPane.showMessageDialog(Program.this, "YOU WERE HIT, take this as a defeat.");//popup with you lost, hence game over, but this game got infinite replay value!
                    //So it's not game over, just GAME ON!
                    //TODO add score counter
                }

            }
        }
    }

    public void paintComponents() {
        g = (Graphics2D) bs.getDrawGraphics();
        g.clearRect(0, 0, xSize, ySize); //clears the canvas
        g.drawString("ping: " +networkHandler.getPing(), 0, 10);//Prints out the current ping
        
        localPlayer.draw(g); //draws the locl player
        remotePlayer.draw(g); //draws the remote player
        for (Projectile o : projectiles) { //itterates though all the projectiles nd calls the method draw
            o.draw(g);//o.draw only executes correctly if the projectile bool "active" is true
        }
        if (!bs.contentsLost()) {
            bs.show();
        }
    }

    public void createAndShowGUI() { //To be honest, I'm not going to bother commenting this... sorry...
        JFrame frame = new JFrame("LollygaggingSimulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        canvas = new Canvas();
        canvas.setSize(xSize, ySize);
        canvas.setVisible(true);
        frame.setVisible(true);
        frame.setSize(xSize, ySize);

        defencePanel = new JPanel();
        attackPanel = new JPanel();
        guiPanel = new JPanel();

        guiPanel.setSize(xSize, 100);
        frame.add(guiPanel, BorderLayout.NORTH);
        guiPanel.add(defencePanel, BorderLayout.WEST);
        guiPanel.add(attackPanel, BorderLayout.EAST);
        guiPanel.add(networkHandler.getNetworkPanel(), BorderLayout.EAST);
        defencePanel.setLayout(new GridLayout(2, 0));
        attackPanel.setLayout(new GridLayout(2, 3));

        button1 = new JButton("Jump");//the button for Jumping
        button1.setVisible(true);
        button1.setText("Jump");
        button1.addActionListener(actionHandler);
        defencePanel.add(button1);

        button2 = new JButton("Duck"); //the button for ducking
        button2.setVisible(true);
        button2.setText("Duck");
        button2.addActionListener(actionHandler);
        defencePanel.add(button2);

        button3 = new JButton("High attack"); // the button for high attacks
        button3.setVisible(true);
        button3.setText("High attack");
        button3.addActionListener(actionHandler);
        attackPanel.add(button3);

        button4 = new JButton("Low attack"); // The button for low attacks
        button4.setVisible(true);
        button4.setText("Low attack");
        button4.addActionListener(actionHandler);
        attackPanel.add(button4);

        frame.add(canvas);
        frame.pack();

    }

    public void shoot(int x, int y, byte speed) { // a method for shooting a projectile, In hindsight this method should have been in Chracter
        for (Projectile o : projectiles) { //finds a unactivated projectile and uses it instead of making a new one
            if (!o.getActive()) {
                o.activate(x, y, speed);
                break;
            }

        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    private class ActionHandler implements ActionListener//this listens if a action is performed and exceutes the linked action 
    {

        public void actionPerformed(ActionEvent e) {

            try {

                String cmd = e.getActionCommand();
                switch (cmd) {

                    case "Duck":
                        if (networkHandler.connected()) {//cheks if the progrm is connected to remote
                            localPlayer.duck();
                            networkHandler.sendMessage((byte) 1);
                        }
                        break;

                    case "Jump":
                        if (networkHandler.connected()) {//cheks if the progrm is connected to remote
                            localPlayer.jump();
                            networkHandler.sendMessage((byte) 2);
                        }
                        break;

                    case "High attack":
                        if (networkHandler.connected() & localPlayer.getLastShot() + 3500 < System.currentTimeMillis()) {//cheks if the progrm is connected to remote and if the previous shot was fired at a sufficent interval
                            shoot(300, 201, (byte) 2);
                            localPlayer.setLastShot(System.currentTimeMillis());
                            networkHandler.sendMessage((byte) 4);
                        }
                        break;

                    case "Low attack":
                        if (networkHandler.connected() & localPlayer.getLastShot() + 3500 < System.currentTimeMillis()) {//cheks if the progrm is connected to remote and if the previous shot was fired at a sufficent interval
                            shoot(300, 297, (byte) 2);
                            localPlayer.setLastShot(System.currentTimeMillis());
                            networkHandler.sendMessage((byte) 3);
                        }
                        break;

                    case "Connect"://this is hit from within NetworkHandler and will connect to the entered IP adress
                        System.out.println("Connect");
                        networkHandler.connectToServer();
                        break;

                    case "Porta":
                        System.out.println("PORTED");
                        break;

                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //   }
        }
    }
}
