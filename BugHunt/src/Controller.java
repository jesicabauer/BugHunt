import javax.swing.JFrame; // for JFrame
import javax.swing.JOptionPane;
import java.awt.*; // for graphics & MouseListener 
import java.awt.event.*; // need for events and MouseListener
import java.util.TimerTask; // need for Timer and TimerTask classes

////////////////////////////////////
/////////////////////////////////////
///////// controller class 
////////////////////////////////////
////////////////////////////////////


// WHY DON'T BUGS ERASE ON RESET
// ERASE BUG COMPLETELY AFTER LEVEL "2"
// WIN WHEN ALL BUGS ARE DELETED

class Controller extends TimerTask implements MouseListener  {
    public static final int SLOW_BUG = 0; // these are in order
    public static final int FAST_BUG = 1;
    public static final int BOLT_BUG = 2; 
    public static final int BUG_DONE = 3; // this should be last
 
    public static final int TIME_TO_MOVE_BUGS_IN_MILLISECONDS = 70; // 80 milliseconds on timer
    public static final int NUMBER_OF_BUG_TYPES = BUG_DONE;// to match the number of game levels slow + fast = 2
    public static final int MAX_NUMBER_OF_BUGS = 4; // cheap short cut for array sizing
    public static final int MAX_NUMBER_OF_MISSES = 5; //
    public static final int TIME_TO_WAIT = 6000; 
    public static final int TIME_TO_CLICK = TIME_TO_WAIT*MAX_NUMBER_OF_BUGS; // six seconds

    public JFrame gameJFrame;
    public Container gameContentPane;
    private int bugLevel[] = new int[MAX_NUMBER_OF_BUGS]; // make this an array, probably
    private boolean gameIsReady = false;
    private Bug gameBug[][] = new Bug[NUMBER_OF_BUG_TYPES][MAX_NUMBER_OF_BUGS]; // make this a multi-dimensional array
    private java.util.Timer gameTimer = new java.util.Timer();
    private int xMouseOffsetToContentPaneFromJFrame = 0;
    private int yMouseOffsetToContentPaneFromJFrame = 0;
    private int currentMisses = 1; 
    private int countDown = TIME_TO_CLICK; 
    
    public Controller(String passedInWindowTitle, 
        int gameWindowX, int gameWindowY, int gameWindowWidth, int gameWindowHeight){
        gameJFrame = new JFrame(passedInWindowTitle);
        gameJFrame.setSize(gameWindowWidth, gameWindowHeight);
        gameJFrame.setLocation(gameWindowX, gameWindowY);
        gameJFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameContentPane = gameJFrame.getContentPane();
        gameContentPane.setLayout(null); // not need layout, will use absolute system
        gameContentPane.setBackground(Color.white);
        gameJFrame.setVisible(true);
        // Event mouse position is given inside JFrame, where bug's image in JLabel is given inside ContentPane,
        //  so adjust for the border and frame
        int borderWidth = (gameWindowWidth - gameContentPane.getWidth())/2;  // 2 since border on either side
        xMouseOffsetToContentPaneFromJFrame = borderWidth;
        yMouseOffsetToContentPaneFromJFrame = gameWindowHeight - gameContentPane.getHeight()-borderWidth; // assume side border = bottom border; ignore title bar

        // create the bugs, now that JFrame has been initialized
        for (int i = 0; i < MAX_NUMBER_OF_BUGS; i++){
	        gameBug[SLOW_BUG][i] = new SlowBug(gameJFrame,1,10);// JFrame, hits required,% change direction
	        gameBug[FAST_BUG][i] = new FastBug(gameJFrame,1,25);// JFrame, hits required,% change direction
	        gameBug[BOLT_BUG][i] = new BoltBug(gameJFrame,1,10);
        }

        resetGame(SLOW_BUG);
        gameTimer.schedule(this, 0, TIME_TO_MOVE_BUGS_IN_MILLISECONDS);
 
        // register this class as a mouse event listener for the JFrame
        gameJFrame.addMouseListener(this);
        
        
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage("swatter.png");
        Cursor c = toolkit.createCustomCursor(image , new Point(gameContentPane.getX(), 
        		gameContentPane.getY()), "img");
        gameContentPane.setCursor (c);
        
        
        
    }   
    
    public void resetGame(int startingBugLevel){
        gameIsReady = false;
        for (int i = 0; i < MAX_NUMBER_OF_BUGS; i++) {
        	bugLevel[i] = startingBugLevel;
        	currentBug(i).create();
        }
        gameIsReady = true;
        
    }
    
    private void bugGotHit(int i){
	        currentBug(i).gotHit();
//	        System.out.println(i + " is passed to bugGotHit");
	        if (currentBug(i).isDying()){
	            currentBug(i).kill();
	            bugLevel[i]++;
//	            System.out.println("bug " + i + " is level " + bugLevel[i]);
	            if (bugLevel[i] < BUG_DONE) { // not done, go to next level of bug
	                currentBug(i).create();
//	                System.out.println("currentBug returns " + bugLevel[i]);
//	                System.out.println("bug " + i + " was created at level " + bugLevel[i]); 
	                
	            }
	        }
    }
    
    private boolean didIWin(){
    	boolean winner = true;  
//    	System.out.println("Did I Win is runnning");
    	for (int i = 0; i < MAX_NUMBER_OF_BUGS; i++) {
	    	if (bugLevel[i] < BUG_DONE) {
	    		winner = false; 
//	    		System.out.println("you did not win");
	    	} 
    	}
    	return winner; 
    }
    
    private Bug currentBug(int i){
    		
    	if (bugLevel[i] < BUG_DONE) {
    		return gameBug[bugLevel[i]][i];
    	} else {
    		return null;
    	}
    	
    }
    
    //run() to override run() in java.util.TimerTask
    // this is run when timer expires
    public void run() {
        if (gameIsReady){
        	for (int i = 0; i < MAX_NUMBER_OF_BUGS; i++) {
        		if (currentBug(i) != null) {
            currentBug(i).move();
        		}
            // when count down has exceeded 6 seconds
            if (countDown <= 0 ) {
            	// if you haven't lost yet
            	if (currentMisses < MAX_NUMBER_OF_MISSES) {
            		JOptionPane.showMessageDialog(gameJFrame,"You Missed! (" + currentMisses + "/" + MAX_NUMBER_OF_MISSES +")");
            		currentMisses++; 
            		countDown = TIME_TO_CLICK;
            	// else, you lose!
            	} else {
            		JOptionPane.showMessageDialog(gameJFrame,"You Missed! (" + currentMisses + "/" + MAX_NUMBER_OF_MISSES +"). Game Over!");
                    currentBug(i).kill();
                    currentMisses = 1; 
                    countDown = TIME_TO_CLICK;
                    resetGame(SLOW_BUG);
            	}
            // if the count-down hasn't exceeded 6 seconds, then decrement it every time the bug moves
            } else {
            	countDown -= TIME_TO_MOVE_BUGS_IN_MILLISECONDS; 
            }
        	}
        }
    }

    public void mousePressed(MouseEvent event){
    	countDown = TIME_TO_CLICK;
        // make sure game is in progress
        if (gameIsReady){
        	boolean didIHit = false; 
        	for (int i = 0; i < MAX_NUMBER_OF_BUGS; i++) {
        		if (currentBug(i) != null) {
	            if (currentBug(i).isBugHit(event.getX()-xMouseOffsetToContentPaneFromJFrame, event.getY()-yMouseOffsetToContentPaneFromJFrame)){
//	            	System.out.println("bug " + i + " got hit");
	            	bugGotHit(i);
	                didIHit = true; 
	                if (didIWin()){   // did they win?
	                   gameIsReady = false; 
	                   JOptionPane.showMessageDialog(gameJFrame,"You WON!");
	                   JOptionPane.showMessageDialog(gameJFrame,"Let's play again!");
	                   resetGame(SLOW_BUG);
	                 }
	            } 
        		}
        	}
	            
	        if (!didIHit) {
		        if(currentMisses < MAX_NUMBER_OF_MISSES) { // they missed so game is over
		                JOptionPane.showMessageDialog(gameJFrame,"You Missed! (" + currentMisses + "/" + MAX_NUMBER_OF_MISSES +")");
		                currentMisses++; 
		        } else { // they missed so game is over
		            	JOptionPane.showMessageDialog(gameJFrame,"You Missed! (" + currentMisses + "/" + MAX_NUMBER_OF_MISSES +"). Game Over!");
		                
		            	for (int i = 0; i < MAX_NUMBER_OF_BUGS; i++) {
		            		if (currentBug(i) != null) {
		            	currentBug(i).kill();
		            		}
		                }
		                
		                    currentMisses = 1; 
		                    resetGame(SLOW_BUG);
		        }
	        }
	
        }
    }
    
    public void mouseEntered(MouseEvent event) {    
        ;
    }
    public void mouseExited(MouseEvent event) {
        ;
    }
    public void mouseClicked( MouseEvent event) {
        ;
    }
    public void mouseReleased( MouseEvent event) {
        ;
    }

    public static void main( String args[]){
        Controller myController = new Controller("Bug Game", 50,50, 800, 600);// window title, int gameWindowX, int gameWindowY, int gameWindowWidth, int gameWindowHeight){
    }
    
}