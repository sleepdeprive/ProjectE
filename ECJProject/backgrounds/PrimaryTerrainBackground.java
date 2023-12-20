import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class PrimaryTerrainBackground implements Background {

    private Image forestImage;
    private Image blackImage;
    private int backgroundWidth = 0;
    private int backgroundHeight = 0;
    private double shiftX = 0;
    private double shiftY = 0;

    public PrimaryTerrainBackground() {
    	try {
    		this.forestImage = ImageIO.read(new File("res/backgrounds/sack_terrain.png"));
    		this.blackImage = ImageIO.read(new File("res/backgrounds/black.png"));
    		backgroundWidth = forestImage.getWidth(null);
    		backgroundHeight = forestImage.getHeight(null);
    		
    	}
    	catch (IOException e) {
    		//System.out.println(e.toString());
    	}		
    }
	
	public Tile getTile(int col, int row) {
		//row is an index of tiles, with 0 being the at the origin
		//col is an index of tiles, with 0 being the at the origin
		int x = (col * backgroundWidth);
		int y = (row * backgroundHeight);
		Tile newTile = null;
		
		if (row == -1) {
			newTile = new Tile(forestImage, x, y, backgroundWidth, backgroundHeight, false);
		} else if (row >= 0) {			
			newTile = new Tile(blackImage, x, y, backgroundWidth, backgroundHeight, false);
		}
		else {
			newTile = new Tile(null, x, y, backgroundWidth, backgroundHeight, false);
		}
			
		
		
		return newTile;
	}
	
	public int getCol(double x) {
		//which col is x sitting at?
		int col = 0;
		if (backgroundWidth != 0) {
			col = (int) (x / backgroundWidth);
			if (x < 0) {
				return col - 1;
			}
			else {
				return col;
			}
		}
		else {
			return 0;
		}
	}
	
	public int getRow(double y) {
		//which row is y sitting at?
		int row = 0;
		
		if (backgroundHeight != 0) {
			row = (int) (y / backgroundHeight);
			if (y < 0) {
				return row - 1;
			}
			else {
				return row;
			}
		}
		else {
			return 0;
		}
	}

	@Override
	public double getShiftX() {
		return shiftX;
	}

	@Override
	public double getShiftY() {
		return shiftY;
	}

	@Override
	public void setShiftX(double shiftX) {
		this.shiftX = shiftX;		
	}

	@Override
	public void setShiftY(double shiftY) {
		this.shiftY = shiftY;		
	}
	
}


