package lollygaggingsimulator;

import java.awt.Graphics;
import java.awt.Rectangle;

public class Entity {

    private Rectangle hitbox;
    private int x, y, height, width;

    public Entity(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.height = height;
        this.width = width;
        hitbox = new Rectangle(x,y,width,height);

    }
    
    public void update(){
    hitbox.setBounds(x, y, width, height);//this moves hitbox with the enity
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public void increeseY(int y) {
        this.y = this.y + y;

    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void increeseX(int x) {
        this.x = this.x + x;

    }
    
    public Rectangle getHitbox(){ // It hurts me that it's not getRect()
    
    return hitbox;
    }

    public void draw(Graphics g) {
        g.fillRect(x, y, width, height);
    }
}
