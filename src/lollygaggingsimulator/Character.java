package lollygaggingsimulator;

import java.awt.Graphics;

public class Character extends Entity {
    int originX,originY; //were it was first paresed
    int moveScale; //how big the movements re
    long lastShot;

    public Character(int x, int y, int width, int height) {
        super(x, y, width, height);
        lastShot = System.currentTimeMillis();
        originX = x; 
        originY = y;
        moveScale = 150;
        
    }

    public void jump() {
        if (getY() == originY) {//checks if character is on the origin
            setY(originY-moveScale);//chnges the position of the character to a higher pos
        }
    }

    public void duck() {
        if (getY() == originY) {//checks if character is on the origin
            setY(originY+moveScale);//chnges the position of the character to a lower pos
            System.out.println("ducking...");
        }
    }

    public void update() {//this method applies a normalising effect to the chracter that drags it towards the origin
        
        if (getY() != originY) {//checks if the character is at the origin, if not it will check if it's lower or higher
            if (getY() > originY) {
                increeseY(-1);
                super.update();
            }
            if (getY() < originY) {
                increeseY(1);
                super.update();
            }
        }
        
        
    }

    public void draw(Graphics g) {
        super.draw(g);

    }
    
    public long getLastShot(){
    return lastShot;
    }
    
    public void setLastShot(long lastShot){
    this.lastShot = lastShot;
    }

}
