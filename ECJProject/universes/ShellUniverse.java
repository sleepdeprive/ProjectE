import java.util.ArrayList;

public class ShellUniverse implements Universe {

	private static final int GROUND_MINY = 250;
	private boolean complete = false;	
	private Background secondaryBackground = null;	
	private Background primaryBackground = null;	
	private Background redSkyBackground = null;	
	private ArrayList<Background> backgrounds = null;
    private DisplayableSprite sword1 = null;
    private boolean swordVisible = false;
	private PlayerSprite player1 = null;
	private ArrayList<DisplayableSprite> sprites = new ArrayList<DisplayableSprite>();
    private ArrayList<DisplayableSprite> disposedSprites = new ArrayList<DisplayableSprite>();

    private DisplayableSprite adam1 = null;
    private DisplayableSprite adam2 = null;
    private DisplayableSprite adam3 = null;
    private DisplayableSprite bob1 = null;
    private DisplayableSprite bob2 = null;

    private DisplayableSprite Adam1 = null;
    private DisplayableSprite Adam2 = null;
    private DisplayableSprite Adam3 = null;
    private DisplayableSprite Bob1 = null;
    private DisplayableSprite Bob2 = null;
    
	private int winCooldown = 10000;
	private boolean gameWon = false;

	public ShellUniverse () {	    
		redSkyBackground = new RedSkyBackground();
		secondaryBackground = new SecondaryTerrainBackground();
		primaryBackground = new PrimaryTerrainBackground();

		backgrounds = new ArrayList<Background>();
		backgrounds.add(redSkyBackground);
		backgrounds.add(secondaryBackground);
		backgrounds.add(primaryBackground);

		this.setXCenter(0);
		this.setYCenter(0);
		player1 = new PlayerSprite(100, -50);
		if(player1.getCharacter() == "Philip") {
		    swordVisible = true;
		}
		sword1 = new SwordSprite(player1, swordVisible);

		sprites.add(new BarrierSprite(-1000000, 0, 1000000, 500, false));
	    sprites.add(new BarrierSprite(-360, -250, -300, 10, true));


		adam1 = new AdamEnemy(900, -50);
		adam2 = new AdamEnemy(800, -50);
		adam3 = new AdamEnemy(200, -50);
		bob1 = new BobEnemy(600, -50);
		bob2 = new BobEnemy(500, -50);

		sprites.add(player1);
		sprites.add(sword1);
		sprites.add(adam1);
		sprites.add(adam2);
		sprites.add(adam3);
		sprites.add(bob1);
		sprites.add(bob2);
	}
	
	   public void gameWin(Universe universe) {
	        if(bob1.isDead() && bob2.isDead() && adam1.isDead() && adam2.isDead() && adam3.isDead()) {
	        	gameWon  = true;
	        	WinSprite win = new WinSprite(player1.getCenterX(),-335);
	        	player1.getWin(true);
	            universe.getSprites().add(win);
	            if(winCooldown <= 0) {
	            	System.exit(0);
	            }
	        }
	    }

	public double getScale() {
		return 1;
	}

	public double getXCenter() {
		return this.player1.getCenterX();
	}

	public double getYCenter() {
		return -GROUND_MINY;
	}

	public void setXCenter(double xCenter) {
	}

	public void setYCenter(double yCenter) {
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public DisplayableSprite getPlayer1() {
		return player1;
	}

	public ArrayList<DisplayableSprite> getSprites() {
		return sprites;
	}

	public boolean centerOnPlayer() {
		return false;
	}		

	@Override
	public ArrayList<Background> getBackgrounds() {
		return backgrounds;
	}	
	
	public void update(Universe universe, KeyboardInput keyboard, long actual_delta_time) {
				
		gameWin(universe);
		
		for (int i = 0; i < sprites.size(); i++) {
			DisplayableSprite sprite = sprites.get(i);
			sprite.update(this, keyboard, actual_delta_time);
    	}

		this.redSkyBackground.setShiftX((player1.getCenterX() * 1));
		this.secondaryBackground.setShiftX((player1.getCenterX() * 0.85));
		this.primaryBackground.setShiftX((player1.getCenterX() * 0.5));

//		System.out.println(String.format("player1X: %5.2f; sky: %5.2f; mountain: %5.2f; forest: %5.2f", player1.getCenterX(), redSkyBackground.getShiftX(), secondaryBackground.getShiftX(), primaryBackground.getShiftX()));
		disposeSprites();
		if(gameWon) {
		winCooldown  -= actual_delta_time;
		}
	}

    protected void disposeSprites() {
        
        
        for (int i = 0; i < sprites.size(); i++) {
            DisplayableSprite sprite = sprites.get(i);
            if (sprite.getDispose() == true) {
                disposedSprites.add(sprite);
            }
        }
        for (int i = 0; i < disposedSprites.size(); i++) {
            DisplayableSprite sprite = disposedSprites.get(i);
            sprites.remove(sprite);
        }
        if (disposedSprites.size() > 0) {
            disposedSprites.clear();
        }
    }	
	public String toString() {
		return "";
	}

}
