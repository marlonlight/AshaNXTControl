
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author mluz
 */
public class MyCanvas extends Canvas{
    
    public boolean isPressed = false;
    AshaNXTControl midlet;
    
    public MyCanvas(){
        setFullScreenMode(true);
    }

    protected void paint(Graphics g) {
        g.setColor(0, 128, 0);
        g.fillRect(0, 0, getWidth(), getHeight());
    }
    
    protected void pointerPressed(int x, int y){
        isPressed = true;
    }

    protected void pointerReleased(int x, int y){
        isPressed = false;
    }
    
}
