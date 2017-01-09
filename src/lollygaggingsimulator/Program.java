package lollygaggingsimulator;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class Program extends JFrame implements ActionListener {

    private Canvas canvas;
    Graphics g;
    private ArrayList<Projectile> projectiles;
    static final int xSize = 1200; // size of the window
    static final int ySize = 600;
    static byte lastRecived;
    // static final int floor = 400; //The height of the arena floor

    private Character localPlayer, remotePlayer;

    private JPanel defencePanel;
    private JPanel attackPanel;
    private JPanel guiPanel;

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
        generateProjectiles();
        run();
    }

    public void generateProjectiles() {
        for (int i = 0; i < 50; i++) {
            projectiles.add(new Projectile(0, 0, (byte) 0));
        }
    }

    public void run() {
        while (true) {
            if (System.currentTimeMillis() > (lastUpdateTick + (1000 / TPS))) { //checks if enoth time has passed for another itteration
                lastUpdateTick = System.currentTimeMillis();//saves the time when it went into the while loop
                localPlayer.update();
                remotePlayer.update();
                for (Projectile o : projectiles) {
                    o.update();

                }

                paintComponents();
                collitionCheck();

            }
            if (lastRecived > 0) {
                if (lastRecived == 1) {//The following checks what action the remoteplayer did
                    remotePlayer.duck();//remotePlayer dicks

                } else if (lastRecived == 2) {
                    remotePlayer.jump(); //remotePlayer jumps

                } else if (lastRecived == 3) {//checks what ctionw as perormed and sees if another shot was fired not long ago
                    shoot(700, 297, (byte) -2);//remotePlayer shoots a low attack with negative direction

                } else if (lastRecived == 4) {//checks what ctionw as perormed and sees if another shot was fired not long ago
                    shoot(700, 201, (byte) -2);//remotePlayer shoots a high attack with negative direction      

                }
                lastRecived = 0;
            }

        }
    }

    public void collitionCheck() {

    }

    public void paintComponents() {
        g = canvas.getGraphics();
        g.clearRect(0, 0, xSize, ySize);
        localPlayer.draw(g);
        remotePlayer.draw(g);
        for (Projectile o : projectiles) {
            o.draw(g);
        }

    }

    public void createAndShowGUI() {
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

        guiPanel.setBackground(Color.blue);
        guiPanel.setSize(xSize, 100);
        frame.add(guiPanel, BorderLayout.NORTH);
        guiPanel.add(defencePanel, BorderLayout.WEST);
        guiPanel.add(attackPanel, BorderLayout.EAST);
        guiPanel.add(networkHandler.getNetworkPanel(), BorderLayout.EAST);
        defencePanel.setLayout(new GridLayout(2, 0));
        attackPanel.setLayout(new GridLayout(2, 3));

        button1 = new JButton("Jump");//the panel for Jumping
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
        canvas.setBackground(Color.red);
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

    private class ActionHandler implements ActionListener//this listens if a action is performed
    {

        public void actionPerformed(ActionEvent e) //TODO: Make methods instead of doing everything within cases
        {
            //if (networkHandler.connected()) {
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
                        if (networkHandler.connected() & localPlayer.getLastShot() + 2500 < System.currentTimeMillis()) {//cheks if the progrm is connected to remote and if the previous shot was fired at a sufficent interval
                            shoot(300, 201, (byte) 2);
                            localPlayer.setLastShot(System.currentTimeMillis());
                            networkHandler.sendMessage((byte) 4);
                        }
                        break;

                    case "Low attack":
                        if (networkHandler.connected() & localPlayer.getLastShot() + 2500 < System.currentTimeMillis()) {//cheks if the progrm is connected to remote and if the previous shot was fired at a sufficent interval
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
