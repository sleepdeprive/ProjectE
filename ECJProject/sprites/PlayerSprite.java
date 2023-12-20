import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class PlayerSprite implements DisplayableSprite {

    private final double ACCCELERATION_X = 300;
    private final double ACCCELERATION_Y = 600;
    private final double MAX_VELOCITY_X = 350;
    private double DEACCELERATION_X = 1000;
    private final double MINIMUM_X_VELOCITY = 20;
    private final double INITIAL_JUMP_VELOCITY = 130;

    private CollisionDetection collisionDetection;
    private VirtualSprite virtual = new VirtualSprite();

    private static Image image;
    private double centerX = 0;
    private double centerY = 0;
    private double width = 75;
    private double height = 75;
    private boolean dispose = false;	
    private double velocityX = 0;
    private double velocityY = 0;    
    private String direction = "r";
    private String steps = "stepsA";
    private String filename = "res/Eric/stepsA/r.PNG";
    private int stepCounter = 0;

    private static String character = "Eric";
    private int health = 19;
    private long cooldownZ = 0;
    private long cooldownX = 0;
    private int damageCooldown = 1;
	private boolean isDead = false;
	private boolean win;
    private static int armor = -1;

    public PlayerSprite(double centerX, double centerY) {

        super();
        this.centerX = centerX;
        this.centerY = centerY;		

        collisionDetection = new CollisionDetection();

        collisionDetection.setBounceFactorX(0);
        collisionDetection.setBounceFactorY(0);

    }

    public int getHealth() {
        return health;
    }

    public int getArmor() {
        if(character == "Louie") {
            armor = 2;
        }else if(character == "Philip") {
            armor = 5;
        }else if(character == "Eric") {
            armor = 8;
        }else {
            armor  = -1;
        }
        return armor;
    }

    public String getCharacter() {
        return character;
    }

    @Override
    public Image getImage() {
        if(velocityX > 0) {
            direction = "r";
        }

        else if(velocityX < 0) {
            direction = "l";
        }

        filename = "res/" + character + "/" + steps + "/" + direction + ".png";

        try {
            image = ImageIO.read(new File(filename));
        }
        catch (IOException e) {
            System.err.println(e.toString());
        };
        return image;
    }



    public boolean getVisible() {
        return true;
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

    public double getVelocityX() {
        return velocityX;
    }

    public boolean getDispose() {
        return dispose;
    }

    public void setDispose(boolean dispose) {
        this.dispose = dispose;
    }

    public void attackZ(Universe universe) {
        if(character == "Eric") {
            BloccSprite blocc = new BloccSprite(this.centerX + (this.velocityX >= 0 ? 100 : -100), this.centerY + 20, true);
            universe.getSprites().add(blocc);
        }else if(character == "Philip") {

        }
    }

    public void attackX(Universe universe) {
        if(character == "Eric") {
            BarrelSprite barrel = new BarrelSprite(centerX, centerY - 10, true);
            universe.getSprites().add(barrel);
        }else if(character == "Philip") {

        }
    }

    public void attackC() {

    }

    public void update(Universe universe, KeyboardInput keyboard, long actual_delta_time) {
    	if(win){
    		steps = "Win";

    	}else {
    		
            if(health <= 0) {
                setDispose(true);
                isDead = true;
                universe.setComplete(true);
                //TODO.... need to exit universe properly, not use the system.exit                
//                System.exit(0);
            }

    		if(stepCounter % 45 <= 15) {
    			steps = "stepsA";
    		}else if(stepCounter % 45 <= 30) {
    			steps = "stepsB";
    		}else {
    			steps = "stepsC";
    		}

    		if(keyboard.keyDown(88) && cooldownX <= 0) {
    			cooldownX = 5000;
    			attackX(universe);
    		}

    		if(keyboard.keyDown(90) && cooldownZ <= 0) {
    			cooldownZ = 9999;
    			attackZ(universe);
    		}
    	}

    	cooldownZ -= actual_delta_time;
    	cooldownX  -= actual_delta_time;
    	damageCooldown -= actual_delta_time;

    	boolean onGround = isOnGround(universe);




    	if (onGround) {


    		if (keyboard.keyDown(32)) {
    			this.velocityY -= INITIAL_JUMP_VELOCITY;
    			onGround = false;
    		}
    		if (keyboard.keyDown(39)) {

    			velocityX += actual_delta_time * 0.001 * ACCCELERATION_X;
    			if (velocityX > MAX_VELOCITY_X) {
    				velocityX = MAX_VELOCITY_X;
    			}
    			stepCounter ++;
    		}

    		else if (keyboard.keyDown(37)) {
    			velocityX -= actual_delta_time * 0.001 * ACCCELERATION_X;
    			if (velocityX < - MAX_VELOCITY_X) {
    				velocityX = - MAX_VELOCITY_X;

    			}          
    			stepCounter ++;
    		}
    		else {

    			if (Math.abs(this.velocityX) > MINIMUM_X_VELOCITY) {
    				this.velocityX -= actual_delta_time * 0.001 *  DEACCELERATION_X * Math.signum(this.velocityX);
    			}
    			else {
    				this.velocityX = 0;
    			}
    		}
    	}

    	collisionDetection.calculate2DBounce(virtual, this, universe.getSprites(), velocityX, velocityY, actual_delta_time);
    	this.velocityX = virtual.getVelocityX();
    	this.velocityY = virtual.getVelocityY();

    	double deltaX = actual_delta_time * 0.001 * velocityX;
    	double deltaY = actual_delta_time * 0.001 * velocityY;

    	boolean collidingBarrierX = checkCollisionWithBarrier(universe.getSprites(), deltaX, 0);
    	boolean collidingBarrierY = checkCollisionWithBarrier(universe.getSprites(), 0, deltaY);

    	if (onGround == true) {
    		this.velocityY = 0;
    	} else {
    		this.velocityY = this.velocityY + ACCCELERATION_Y * 0.001 * actual_delta_time;
    	}

    	if(collidingBarrierX == false) {
    		this.centerX += deltaX;
    	}
    	if(collidingBarrierY == false) {
    		this.centerY += deltaY;
    	}

    	boolean collidingWithEnemy = checkCollisionWithEnemy(universe.getSprites(), deltaX, deltaY);

    	if(collidingWithEnemy == true && damageCooldown <= 0) {
    		damageCooldown = 1000;
    		attackDamage(10);
    	}
    }

    private boolean checkCollisionWithBarrier(ArrayList<DisplayableSprite> sprites, double deltaX, double deltaY) {

        boolean colliding = false;

        for (DisplayableSprite sprite : sprites) {
            if (sprite instanceof BarrierSprite || sprite instanceof BloccSprite || sprite instanceof BarrelSprite || sprite.getClass().toString().contains("Enemy")) {
                if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, this.getMaxX()  + deltaX, this.getMaxY() + deltaY, 
                        sprite.getMinX(),sprite.getMinY(), 
                        sprite.getMaxX(), sprite.getMaxY()) == true) {
                    colliding = true;                 
                }
            }
        }
        return colliding;
    }  

    private boolean isOnGround(Universe universe) {
        boolean onGround = false;
        for (DisplayableSprite sprite: universe.getSprites()) {
            if (sprite instanceof BarrierSprite || sprite instanceof BloccSprite || sprite instanceof BarrelSprite) {

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

    private boolean checkCollisionWithEnemy(ArrayList<DisplayableSprite> sprites, double deltaX, double deltaY) {

        boolean colliding = false;

        for (DisplayableSprite sprite : sprites) {
            if (sprite instanceof BobEnemy || sprite instanceof AdamEnemy) {
                if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, this.getMaxX()  + deltaX, this.getMaxY() + deltaY,
                        sprite.getMinX(),sprite.getMinY(),
                        sprite.getMaxX(), sprite.getMaxY()) == true) {
                    colliding = true;                
                }
            }
        }
        return colliding;
    }  

    @Override
    public void attackDamage(int damage) {
        this.health -= damage;
    }

    public void dead() {
        if(health <= 0) {
            setDispose(true);
            isDead = true;
            //TODO.... need to exit universe properly, not use the system.exit
            
            System.exit(0);
        }
    }

	@Override
	public boolean isDead() {
		return isDead;
	}

	public void getWin(boolean win) {
		this.win = win;	}

}
