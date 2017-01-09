package lollygaggingsimulator;

import java.awt.Graphics;

public class Projectile extends Entity {

    private byte speed;//the speed of witch Projectile travels,negtive values for revesed direction
    private boolean active; //this turns to false if it is out of bounds

    public Projectile(int x, int y, byte speed) {
        super(x, y, 10, 5);
        this.speed = speed;
        active = false;
    }

    public void update() {
        if (active) {
            increeseX(speed);
            super.update();
            
            if ( getX() > Program.xSize | getX() < 0) { //if the projectile is out of bounds it gets deactivated
                active = false;

            }

        }
    }

    public void draw(Graphics g) {
        if (active) {
            super.draw(g);
        }
    }

    public boolean getActive() {
        return active;
    }

    public void activate(int x , int y , byte speed) {
        active = true;
        setX(x);
        setY(y);
        this.speed = speed;
    }
}
