import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ExitSprite implements DisplayableSprite{

    private static Image image;
    private boolean visible = true;
    private double centerX = 0;
    private double centerY = 0;
    private double width = 200;
    private double height = 200;
    private boolean dispose = false;
    
    public ExitSprite(double minX, double minY, double maxX, double maxY) {
        
        if (image == null && visible) {
            try {
                image = ImageIO.read(new File("res/Pause_Board.png"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }       
        }
        
        this.centerX = (minX + maxX) / 2;
        this.centerY = (minY + maxY) / 2;
        this.width = maxX - minX;
        this.height = maxY - minY;
        
    }

    @Override
    public int getArmor() {
        return 30;
    }


    @Override
    public int getHealth() {
        return 1;
    }

    public Image getImage() {
        return image;
    }
    
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    //DISPLAYABLE
    
    public boolean getVisible() {
        return this.visible;
    }
    
    public double getMinX() {
        return centerX - (width / 2);
    }

    public double getMaxX() {
        return centerX + (width / 2);
    }

    public double getMinY() {
        return centerY - (height / 2);
    }

    public double getMaxY() {
        return centerY + (height / 2);
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    public double getCenterX() {
        return centerX;
    };

    public double getCenterY() {
        return centerY;
    };
    
    
    public boolean getDispose() {
        return dispose;
    }

    public void setDispose(boolean dispose) {
        this.dispose = dispose;
    }


    public void update(Universe universe, KeyboardInput keyboard, long actual_delta_time) {
            
    }

    @Override
    public void attackDamage(int damage) {
        //leave blank this is not hittable
    }

	@Override
	public boolean isDead() {
		// TODO Auto-generated method stub
		return false;
	}

}

