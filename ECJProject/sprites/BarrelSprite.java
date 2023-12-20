import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BarrelSprite implements DisplayableSprite {

    private static Image image;
    private boolean visible = true;
    private double centerX = 0;
    private double centerY = 0;
    private double width = 50;
    private double height = 65;
    private boolean dispose = false;
    
    private double velocityX = 2;
    private double velocityY = -3.75;
    private double ACCCELERATION_Y = 0.05;
    private double cooldown = 3500;
    
    public BarrelSprite(double centerX, double centerY, boolean visible) {
        
        if (image == null && visible) {
            try {
                image = ImageIO.read(new File("res/Eric/barrel.png"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }       
        }
        
        this.centerX = centerX;
        this.centerY = centerY;
        this.visible = visible;
        
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
        
        cooldown -= actual_delta_time;
        if(cooldown <= 0) {
            setDispose(true); 
            ExplosionSprite explosion = new ExplosionSprite(this.centerX, this.centerY, true);
            universe.getSprites().add(explosion);
        }
        
        boolean onGround = isOnGround(universe);
        
        if(!onGround) {
            this.velocityY += ACCCELERATION_Y;
            this.centerY += this.velocityY;
            this.centerX += this.velocityX;
        }
        
        System.out.println(this.centerY);
    }

    private boolean isOnGround(Universe universe) {
        boolean onGround = false;
        for (DisplayableSprite sprite: universe.getSprites()) {
            if (sprite instanceof BarrierSprite) {

                boolean bottomColiding = Math.abs(this.getMaxY() - sprite.getMinY()) < 5;

                boolean toRight = this.getMinX() > sprite.getMaxX();
                boolean toLeft = this.getMaxX() < sprite.getMinX();
                boolean withinRange = (toRight == false) && (toLeft == false);
                
                if (bottomColiding && withinRange) {
                    onGround = true;
                    break;
                }
            }
        }
        return onGround;
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
