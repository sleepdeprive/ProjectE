import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class AdamEnemy implements DisplayableSprite {

    private final double ACCCELERATION_X = 300;
    private final double ACCCELERATION_Y = 600;
    private final double MAX_VELOCITY_X = 350;
    private double DEACCELERATION_X = 1500;
    private final double MINIMUM_X_VELOCITY = 20;
    private final double INITIAL_JUMP_VELOCITY = 200;
    private PlayerSprite holder;

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
    private String direction = "L";
    private String filename = "res/Enemies/Adam.L.PNG";

    private int health = 8;
    private int armor = 10;
    public boolean isDead = false;

    public AdamEnemy(double centerX, double centerY) {

        super();
        this.centerX = centerX;
        this.centerY = centerY;     

        collisionDetection = new CollisionDetection();

        collisionDetection.setBounceFactorX(0.5);
        collisionDetection.setBounceFactorY(0);
    }
    @Override
    public int getHealth() {
        return health;
    }

    @Override
    public int getArmor() {
        return armor;
    }

    @Override
    public Image getImage() {
        if(velocityX > 0) {
            direction = "r";
        }

        else if(velocityX < 0) {
            direction = "l";
        }

        filename = "res/Enemies/Adam." + direction + ".png";

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


    public boolean getDispose() {
        return dispose;
    }

    public void setDispose(boolean dispose) {
        this.dispose = dispose;
    }



    public void update(Universe universe, KeyboardInput keyboard, long actual_delta_time) {
        dead();
        
        boolean onGround = isOnGround(universe);


        double deltaX = actual_delta_time * 0.001 * velocityX;
        double deltaY = actual_delta_time * 0.001 * velocityY;

        boolean collidingBarrierX = checkCollisionWithBarrier(universe.getSprites(), deltaX, 0);
        boolean collidingBarrierY = checkCollisionWithBarrier(universe.getSprites(), 0, deltaY);

        collisionDetection.calculate2DBounce(virtual, this, universe.getSprites(), velocityX, velocityY, actual_delta_time);
        this.centerX = virtual.getCenterX();
        this.centerY = virtual.getCenterY();
        this.velocityX = virtual.getVelocityX();
        this.velocityY = virtual.getVelocityY();

        if (onGround == true) {
            this.velocityY = 0;
        } else {
            this.velocityY = this.velocityY + ACCCELERATION_Y * 0.001 * actual_delta_time;
        }
        if (checkSight(universe.getSprites(), deltaX, deltaY) == 1){
            this.velocityX = 5;

        }else if(checkSight(universe.getSprites(), deltaX, deltaY) == 2){
            this.velocityX = -10;

        }else {
            this.velocityX = -20;
        }

        if(collidingBarrierX == false) {
            this.centerX += deltaX;
        }
        if(collidingBarrierY == false) {
            this.centerY += deltaY;
        }
        boolean collidingWithExplosion = checkCollisionWithExplosion(universe.getSprites(), deltaX, deltaY);
        
        if(collidingWithExplosion == true) {
            attackDamage(10);
        }
    }

    private boolean checkCollisionWithBarrier(ArrayList<DisplayableSprite> sprites, double deltaX, double deltaY) {

        boolean colliding = false;

        for (DisplayableSprite sprite : sprites) {
            if (sprite instanceof BarrierSprite || sprite instanceof BloccSprite || sprite instanceof BarrelSprite || sprite instanceof PlayerSprite || (sprite instanceof BobEnemy && sprite != this)) {
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

    private boolean checkCollisionWithExplosion(ArrayList<DisplayableSprite> sprites, double deltaX, double deltaY) {

        boolean colliding = false;

        for (DisplayableSprite sprite : sprites) {
            if (sprite instanceof ExplosionSprite){
                if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, this.getMaxX()  + deltaX, this.getMaxY() + deltaY,
                        sprite.getMinX(),sprite.getMinY(),
                        sprite.getMaxX(), sprite.getMaxY()) == true) {
                    colliding = true;                
                }
            }
        }
        return colliding;
        }  
    private int checkSight(ArrayList<DisplayableSprite> sprites, double deltaX, double deltaY) {

        int colliding = 0;

        for (DisplayableSprite sprite : sprites) {
            if (sprite instanceof PlayerSprite || sprite instanceof BloccSprite|| sprite instanceof BobEnemy  ||  (sprite instanceof AdamEnemy && sprite != this)) {
                if(Math.abs(velocityX) / velocityX < 10) {
                    if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, 
                            this.getMaxX()  + deltaX, this.getMaxY() + deltaY, 
                            sprite.getMinX(),sprite.getMinY(), 
                            sprite.getMaxX(), sprite.getMaxY()) == true) {
                        colliding = 1;                 
                    }else {
                        if (CollisionDetection.overlaps(this.getMinX() + deltaX, this.getMinY() + deltaY, 
                                this.getMaxX()  + deltaX, this.getMaxY() + deltaY, 
                                sprite.getMinX(),sprite.getMinY(), 
                                sprite.getMaxX(), sprite.getMaxY()) == true) {
                            colliding = 2;                 

                        }
                    }
                }
            }
        }
        return colliding;
    }  


    public void attackDamage(int damage) {
        this.health -= damage;       
    }
    
    public void dead() {
        if(health <= 0) {
            setDispose(true);
            isDead = true;
        }
    }
    
    public boolean isDead() {
        return isDead;
    }

}
