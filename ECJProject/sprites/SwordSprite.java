import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class SwordSprite implements DisplayableSprite{
	
	private static Image image;
	private boolean visible = true;
	private double centerX;
	private double centerY;
	private double width = 30;
	private double height = 45;
	private boolean dispose = false;
	private PlayerSprite holder;
	
	    public SwordSprite(PlayerSprite player1, boolean visible) {
	
		if (image == null && visible) {
			try {
				image = ImageIO.read(new File("res/Philip/Sword.png"));
			}
			catch (IOException e) {
				e.printStackTrace();
			}		
		}
		this.holder = player1;
		this.centerX = player1.getCenterX();
		this.centerY = player1.getCenterY();
		this.visible = visible;
	}
	
	@Override
	public int getHealth() {
		return 1;
	}

	@Override
	public int getArmor() {
		return 30;
	}

	@Override
	public Image getImage() {
		return image;
	}

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
		if(Math.abs(holder.getVelocityX()) / holder.getVelocityX() < 0) {
			this.centerX = holder.getCenterX() - 35;
			this.centerY = holder.getCenterY() - 20;
			}else if(Math.abs(holder.getVelocityX()) / holder.getVelocityX() > 0){
				this.centerX = holder.getCenterX() + 35;
				this.centerY = holder.getCenterY() - 20;
			}

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