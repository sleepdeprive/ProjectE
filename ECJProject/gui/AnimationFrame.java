import java.awt.*;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.MouseMotionAdapter;

/*
 * This class represents the 'graphical user interface' or 'presentation' layer or 'frame'. Its job is to continuously 
 * read input from the user (i.e. keyboard, mouse) and to render (draw) a universe or 'logical' layer. Also, it
 * continuously prompts the logical layer to update itself based on the number of milliseconds that have elapsed.
 * 
 * Consider the presentation layer to be 'dumb'... it generally does not try to affect the logical layer; most information
 * passes from the logical layer to the presentation layer.
 */

public class AnimationFrame extends JFrame {

	final public static int FRAMES_PER_SECOND = 60;
	final public static int SCREEN_HEIGHT = 600;
	final public static int SCREEN_WIDTH = 800;
	final private static boolean SHOW_GRID = false;

	private int screenCenterX = SCREEN_WIDTH / 2;
	private int screenCenterY = SCREEN_HEIGHT / 2;

	//scale at which to render the universe. When 1, each logical unit represents 1 pixel in both x and y dimension
	private double scale = 1;
	//point in universe on which the screen will center
	private double logicalCenterX = 0;		
	private double logicalCenterY = 0;

	private JPanel panel = null;
	private JButton btnPauseRun;
    private JButton btnExitRun;
	private JLabel lblTop;
	private JLabel lblBottom;

	private static boolean stop = false;

	private long current_time = 0;								//MILLISECONDS
	private long next_refresh_time = 0;							//MILLISECONDS
	private long last_refresh_time = 0;
	private long minimum_delta_time = 1000 / FRAMES_PER_SECOND;	//MILLISECONDS
	private long actual_delta_time = 0;							//MILLISECONDS
	private long elapsed_time = 0;
	boolean isPaused = false;

	private KeyboardInput keyboard = new KeyboardInput();
	private Universe universe = null;

	//local (and direct references to various objects in universe ... should reduce lag by avoiding dynamic lookup
	private Animation animation = null;
	private DisplayableSprite player1 = null;
	private ArrayList<DisplayableSprite> sprites = null;
	private ArrayList<Background> backgrounds = null;
	private Background background = null;
	int universeLevel = 0;
	
	/*
	 * Much of the following constructor uses a library called Swing to create various graphical controls. You do not need
	 * to modify this code to create an animation, but certainly many custom controls could be added.
	 */
	
	public AnimationFrame(Animation animation)
	{
		super("");
		
		this.animation = animation;
		this.setVisible(true);		
		this.setFocusable(true);
		this.setSize(SCREEN_WIDTH + 20, SCREEN_HEIGHT + 36);

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				this_windowClosing(e);
			}
		});

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent arg0) {
				keyboard.keyPressed(arg0);
			}
			@Override
			public void keyReleased(KeyEvent arg0) {
				keyboard.keyReleased(arg0);
			}
		});
		getContentPane().addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				contentPane_mouseMoved(e);
			}
		});
		
		Container cp = getContentPane();
		cp.setBackground(Color.BLACK);
		cp.setLayout(null);

		panel = new DrawPanel();
		panel.setLayout(null);
		panel.setSize(SCREEN_WIDTH, SCREEN_HEIGHT);
		getContentPane().add(panel, BorderLayout.CENTER);

		btnPauseRun = new JButton("||");
		btnPauseRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				btnPauseRun_mouseClicked(arg0);
			}
		});

		btnPauseRun.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnPauseRun.setBounds(SCREEN_WIDTH - 64, 20, 48, 32);
		btnPauseRun.setFocusable(false);
		getContentPane().add(btnPauseRun);
		getContentPane().setComponentZOrder(btnPauseRun, 0);

		lblTop = new JLabel("Health: ");
		lblTop.setForeground(Color.WHITE);
		lblTop.setFont(new Font("Consolas", Font.BOLD, 20));
		lblTop.setBounds(16, 22, SCREEN_WIDTH - 16, 30);
		getContentPane().add(lblTop);
		getContentPane().setComponentZOrder(lblTop, 0);

		lblBottom = new JLabel("Status");
		lblBottom.setForeground(Color.WHITE);
		lblBottom.setFont(new Font("Consolas", Font.BOLD, 30));
		lblBottom.setBounds(16, SCREEN_HEIGHT - 30 - 16, SCREEN_WIDTH - 16, 36);
		lblBottom.setHorizontalAlignment(SwingConstants.CENTER);
		getContentPane().add(lblBottom);
		getContentPane().setComponentZOrder(lblBottom, 0);

	}

	/* 
	 * Consider this the entry point into an Animation. The gui and the universe should run on separate
	 * threads. This allows the gui to remain responsive to user input while the universe is updating
	 * its state. The universe (a.k.a. logical) thread is created below.
	 */
	public void start()
	{
		Thread thread = new Thread()
		{
			public void run()
			{
    				animationLoop();
				System.out.println("run() complete");
			}
		};

		thread.start();
		System.out.println("main() complete");

	}	
	
	/*
	 * The animationLoop runs on the logical thread, and is only active when the universe needs to be
	 * updated. There are actually two loops here. The outer loop cycles through all universes as provided
	 * by the animation. Whenever a universe is 'complete', the animation is asked for the next universe;
	 * if there is none, then the loop exits and this method terminates
	 * 
	 * The inner loop attempts to update the universe regularly, whenever enough milliseconds have
	 * elapsed to move to the next 'frame' (i.e. the refresh rate). Once the universe has updated itself,
	 * the code then moves to a rendering phase where the universe is rendered to the gui and the
	 * controls updated. These two steps may take several milliseconds, but hopefully no more than the refresh rate.
	 * When the refresh has finished, the loop (and thus the thread) goes to sleep until the next
	 * refresh time. 
	 */
	private void animationLoop() {

		universe = animation.getNextUniverse();
		universeLevel++;

		while (stop == false && universe != null) {

			sprites = universe.getSprites();
			player1 = universe.getPlayer1();
			backgrounds = universe.getBackgrounds();
			this.scale = universe.getScale();

			// main game loop
			while (stop == false && universe.isComplete() == false) {

				//adapted from http://www.java-gaming.org/index.php?topic=24220.0
				last_refresh_time = System.currentTimeMillis();
				next_refresh_time = current_time + minimum_delta_time;

				//sleep until the next refresh time
				while (current_time < next_refresh_time)
				{
					//allow other threads (i.e. the Swing thread) to do its work
					Thread.yield();

					try {
						Thread.sleep(1);
					}
					catch(Exception e) {    					
					} 

					//track current time
					current_time = System.currentTimeMillis();
				}

				//read input
				keyboard.poll();
				handleKeyboardInput();

				//UPDATE STATE
				updateTime();				
				universe.update(universe, keyboard, actual_delta_time);
				
				//align animation frame with logical universe
				this.logicalCenterX = universe.getXCenter();
				this.logicalCenterY = universe.getYCenter();

				//REFRESH
				updateControls();
				this.repaint();
				
				System.out.println("running");
			}
			
			handleUniverseComplete();

			universe = animation.getNextUniverse();
			keyboard.poll();

		}

		AudioPlayer.setStopAll(true);
		dispose();	

	}

	private void handleUniverseComplete() {
					
		if (false ) {
			JOptionPane.showMessageDialog(this,
					"Proceed to next level!");
			//get the next level
			universe = animation.getNextUniverse();
		}
		else {
			int choice = JOptionPane.showOptionDialog(this,
					"You died. Play again?",
							"Game Over",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							null,
							null,
							null);
			
			if (choice == 0) {
				// reset the level
				universe = animation.getNextUniverse();
				keyboard.poll();					
			}
			else {
				//end the game
				universe = null;
			}
		}				
	}			
	
	
	private void updateControls() {
		
		this.lblTop.setText(String.format("Health: %d                                           Time: %-1.3f", player1.getHealth(), elapsed_time / 1000.0));
		this.lblBottom.setText(Integer.toString(universeLevel));
		if (universe != null) {
			this.lblBottom.setText(universe.toString());
		}

	}

	private void updateTime() {

		current_time = System.currentTimeMillis();
		actual_delta_time = (isPaused ? 0 : current_time - last_refresh_time);
		last_refresh_time = current_time;
		elapsed_time += actual_delta_time;

	}
	
	protected void btnPauseRun_mouseClicked(MouseEvent arg0) {
		if (isPaused) {
			this.btnPauseRun.setText("||");
			   for (DisplayableSprite sprite : universe.getSprites()) {
			       if (sprite instanceof PauseSprite) {
                       sprite.setDispose(true);
			       }
	            }
			   for (DisplayableSprite sprite : universe.getSprites()) {
			   if (sprite instanceof ExitSprite) {
                   sprite.setDispose(true);
                  }
		}
	              isPaused = false;

			}
		else {
			this.btnPauseRun.setText(">");
			 PauseSprite pause = new PauseSprite(player1.getCenterX(), -335);
	         universe.getSprites().add(pause);
			isPaused = true;
		}
	}
	
	Icon icon = new ImageIcon("res/exit.png");
	JButton btnExit = new JButton(icon);
	
	protected void btnExitRun_mouseClicked(MouseEvent arg0) {
	    
        }
	private void handleKeyboardInput() {
		
		if (keyboard.keyDown(80) && ! isPaused) {
			btnPauseRun_mouseClicked(null);	
		}
		else if (keyboard.keyDown(79) && isPaused ) {
			btnPauseRun_mouseClicked(null);
		}
		
	}

	/*
	 * This method will run whenever the universe needs to be rendered. The animation loop calls it
	 * by invoking the repaint() method.
	 * 
	 * The work is reasonably simple. First, all backgrounds are rendered from "furthest" to "closest"
	 * Then, all sprites are rendered in order. Observe that the logical coordinates are continuously
	 * being translated to screen coordinates. Thus, how the universe is rendered is determined by
	 * the gui, but what is being rendered is determined by the universe.
	 */
	class DrawPanel extends JPanel {

		public void paintComponent(Graphics g)
		{	
			if (universe == null) {
				return;
			}

			if (backgrounds != null) {
				for (Background background: backgrounds) {
					paintBackground(g, background);
				}
			}

			if (sprites != null) {
				for (DisplayableSprite activeSprite : sprites) {
					DisplayableSprite sprite = activeSprite;
					if (sprite.getVisible()) {
						if (sprite.getImage() != null) {
							g.drawImage(sprite.getImage(), translateToScreenX(sprite.getMinX()), translateToScreenY(sprite.getMinY()), scaleLogicalX(sprite.getWidth()), scaleLogicalY(sprite.getHeight()), null);
						}
						else {
							g.setColor(Color.BLUE);
							g.fillRect(translateToScreenX(sprite.getMinX()), translateToScreenY(sprite.getMinY()), scaleLogicalX(sprite.getWidth()), scaleLogicalY(sprite.getHeight()));
						}
					}
				}				
			}
			
			if (SHOW_GRID) {
				for (int x = 0; x <= SCREEN_WIDTH; x+=50) {
					if (x % 100 == 0) {
						g.setColor(Color.GRAY);						
					} else {
						g.setColor(Color.DARK_GRAY);						
					}					
					g.drawLine(x, 0, x, SCREEN_HEIGHT);
				}
				for (int y = 0; y <= SCREEN_HEIGHT; y+= 50) {
					if (y % 100 == 0) {
						g.setColor(Color.GRAY);						
					} else {
						g.setColor(Color.DARK_GRAY);						
					}
					g.drawLine(0, y, SCREEN_WIDTH, y);
				}
			}
		}
		
		/*
		 * The algorithm for rendering a background may appear complex, but you can think of it as
		 * 'tiling' the screen from top left to bottom right. Each time, the gui determines a screen coordinate
		 * that has not yet been covered. It then asks the background (which is part of the universe) for the tile
		 * that would cover the equivalent logical coordinate. This tile has height and width, which allows
		 * the gui to draw the tile and to then move to the screen coordinate just to the top right of this tile.
		 * Again, the background is asked for the tile that would cover this coordinate.
		 * When eventually this coordinate is off the right hand edge of the screen, then move to the left of the screen
		 * but at the bottom of the previously drawn tile. 
		 */
		private void paintBackground(Graphics g, Background background) {
			
			if ((g == null) || (background == null)) {
				return;
			}
			
			double shiftLogicalX = background.getShiftX();
			double shiftLogicalY = background.getShiftY();			
						
			//what tile covers the top-left corner?
			double logicalLeft = ( logicalCenterX  - (screenCenterX / scale) - shiftLogicalX);
			double logicalTop =  (logicalCenterY - (screenCenterY / scale) - shiftLogicalY) ;
			
			int row = background.getRow((int)(logicalTop));
			int col = background.getCol((int)(logicalLeft));
			Tile tile = background.getTile(col, row);
			
			boolean rowDrawn = false;
			boolean screenDrawn = false;
			while (screenDrawn == false) {
				while (rowDrawn == false) {
					tile = background.getTile(col, row);
										
					if (tile.getWidth() <= 0 || tile.getHeight() <= 0) {
						//no increase in width; will cause an infinite loop, so consider this screen to be done
						g.setColor(Color.GRAY);
						g.fillRect(0,0, SCREEN_WIDTH, SCREEN_HEIGHT);					
						rowDrawn = true;
						screenDrawn = true;		
					}
					else {
						Tile nextTile = background.getTile(col+1, row+1);
						int width = translateToScreenX(nextTile.getMinX()) - translateToScreenX(tile.getMinX());
						int height = translateToScreenY(nextTile.getMinY()) - translateToScreenY(tile.getMinY());
						g.drawImage(tile.getImage(), translateToScreenX(tile.getMinX() + shiftLogicalX), translateToScreenY(tile.getMinY() + shiftLogicalY), width + 1, height + 1, null);
						
					}					
					//does the RHE of this tile extend past the RHE of the visible area?
					if (translateToScreenX(tile.getMinX() + shiftLogicalX + tile.getWidth()) > SCREEN_WIDTH || tile.isOutOfBounds()) {
						rowDrawn = true;
					}
					else {
						col++;
					}
				}
				//does the bottom edge of this tile extend past the bottom edge of the visible area?
				if (translateToScreenY(tile.getMinY() + shiftLogicalY + tile.getHeight()) > SCREEN_HEIGHT || tile.isOutOfBounds()) {
					screenDrawn = true;
				}
				else {
					//TODO - should be passing in a double, as this represents a universe coordinate
					col = background.getCol((int)logicalLeft);
					row++;
					rowDrawn = false;
				}
			}
		}			
		
	}

	private int translateToScreenX(double logicalX) {
		return screenCenterX + scaleLogicalX(logicalX - logicalCenterX);
	}		
	private int scaleLogicalX(double logicalX) {
		return (int) Math.round(scale * logicalX);
	}
	private int translateToScreenY(double logicalY) {
		return screenCenterY + scaleLogicalY(logicalY - logicalCenterY);
	}		
	private int scaleLogicalY(double logicalY) {
		return (int) Math.round(scale * logicalY);
	}

	private double translateToLogicalX(int screenX) {
		int offset = screenX - screenCenterX;
		return offset / scale;
	}
	private double translateToLogicalY(int screenY) {
		int offset = screenY - screenCenterY;
		return offset / scale;			
	}
	
	protected void contentPane_mouseMoved(MouseEvent e) {
		MouseInput.screenX = e.getX();
		MouseInput.screenY = e.getY();
		MouseInput.logicalX = translateToLogicalX(MouseInput.screenX);
		MouseInput.logicalY = translateToLogicalY(MouseInput.screenY);
	}

	protected void this_windowClosing(WindowEvent e) {
		System.out.println("windowClosing()");
		stop = true;
		dispose();	
	}
}
