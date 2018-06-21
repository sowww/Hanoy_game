import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Random;

import javax.naming.InitialContext;
import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.basic.BasicScrollPaneUI.HSBChangeListener;


public class HanoyGame extends JFrame implements Runnable, KeyListener, MouseInputListener, MouseMotionListener {

    //HANOY_COLUMN CLASS STARTS
    public class HanoyColumn {
        private static final int SIDE_INDENT = 140;
        private int index;
        private int middleX, bottomY;
        private int 
            WIDTH = 20, 
            HEIGHT = 10 + (HANOY_RECTS_COUNT * HANOY_RECTS_HEIGHT);
        private int[] stack = new int[HANOY_RECTS_COUNT];
        private int stackTop;
        
        public HanoyColumn() {}
        public HanoyColumn(int INDEX) {
            for (int i = 0; i <= HANOY_RECTS_COUNT - 1; i++) { stack[i] = -1; }
            stackTop = -1;
            index = INDEX;
            setXAccordingToFrame();
            setYAccordingToFrame();
        }

        private void setXAccordingToFrame() {
            int frameWidth     = HanoyGame.this.getWidth();
            int columnInterval = Math.round((frameWidth - 2*SIDE_INDENT) / (COLUMNS_COUNT - 1));
            int newX           = SIDE_INDENT + (index * columnInterval);
            middleX            = newX;
        }
        
        private void setYAccordingToFrame() {
            int frameHeight = HanoyGame.this.getHeight();
            int newY = frameHeight - 2;
            bottomY = newY;
        }
        
        public void addRectToStack(int SIZE_INDEX) {
            stackTop++;
            stack[stackTop] = SIZE_INDEX;
        }
        public void delRectFromStack() { 
            stack[stackTop] = -1;
            stackTop--;
        }
        
        public int getTopSize() {
            if (stackTop != -1) {
                return stack[stackTop];
            } else return 999;
        }
        
        public int getMiddleX() { return middleX; }
        public int getBottomY() { return bottomY; }
        public int getWidth() { return WIDTH; }
        public int getHeight() { return HEIGHT; }
        
        public int getRealY() {
            int realY = bottomY - HEIGHT + 5;
            return realY;
        }
    }
    //HANOY_COLUMN CLASS ENDS
    
    
    //HANOY_RECT CLASS STARTS
    public class HanoyRect {
        
        int sizeIndex;
        int width;
        final int   
            MIN_WIDTH = 40,
            WIDTH_STEP = 20,
            HEIGHT = HANOY_RECTS_HEIGHT;
        int x, y;
        int 
            A = 1,
            vX = 0,
            vY = 0;
        Color color;
        int column, level;
        
        Status status;
        
        public HanoyRect() {}
        public HanoyRect(int COLUMN, int SIZE_INDEX, int LEVEL) {
            column    = COLUMN;
            level     = LEVEL;
            sizeIndex = SIZE_INDEX;
            setWidth();
            color     = randomColor();
            x         = hanoyColumns[column].getMiddleX() - width / 2;
            y         = hanoyColumns[column].getBottomY() - HEIGHT*LEVEL;
            status    = Status.NOT_MOVING;
        }
        private void setWidth() {
            int newWidth = MIN_WIDTH + sizeIndex * WIDTH_STEP;
            width = newWidth;
        }
        
        private Color randomColor() {
            Random random = new Random();
            int r, g, b;
            r = random.nextInt(255);
            g = random.nextInt(255);
            b = random.nextInt(255);
            Color randColor = new Color(r, g, b);
            return randColor;
        }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return HEIGHT; }
        
        public Status getStatus() { return status; }
        public void setStatus(Status STATUS) { status = STATUS; }
                
        private void stepXToMouse() {
            int target_mx = mx - width / 2;  
            int stepX = sign(x - target_mx)*Math.abs(x - target_mx)/5;
            if (Math.abs(target_mx - x) > 1 ) {
                x  = (int)Math.round(x - stepX);
            }
        }
        
        private void stepYToMouse() {
            int target_my = my - HEIGHT / 2;
            int stepY = sign(y - target_my)*Math.abs(y - target_my)/5;
            if (Math.abs(target_my - y) > 1 ) {
                y  = (int)Math.round(y - stepY);
            }
        }
        
        private void stepToMouse() {
            try {
                stepXToMouse();
                stepYToMouse();
                Thread.sleep(1000/200);
            } catch (InterruptedException e) {}
            
        }
        
        
        public void drag(int diffX, int diffY) {
            if ( y + diffY < hanoyColumns[column].getRealY() - HEIGHT ) {
                x += diffX;
                stepToMouse();
            } else {
                x = hanoyColumns[column].getMiddleX() - width / 2;
                stepYToMouse();
            }
            
            if (x + width/2 < frontier_0) {
                if (column != 0 && sizeIndex < hanoyColumns[0].getTopSize()) {
                    hanoyColumns[column].delRectFromStack(); 
                    column = 0;
                    hanoyColumns[0].addRectToStack(sizeIndex);
                }
            } else if ((frontier_0 < x + width/2) && ( x + width/2 < frontier_1)) {
                if (column != 1 && sizeIndex < hanoyColumns[1].getTopSize()) {
                    hanoyColumns[column].delRectFromStack();
                    column = 1;
                    hanoyColumns[1].addRectToStack(sizeIndex);
                }
            } else {
                if (column != 2 && sizeIndex < hanoyColumns[2].getTopSize()) {
                    hanoyColumns[column].delRectFromStack();
                    column = 2;
                    hanoyColumns[2].addRectToStack(sizeIndex);
                }
            }
            int maxY = FRAME_HEIGHT - 2 - HEIGHT * (hanoyColumns[column].stackTop + 1);
            if (y + diffY < maxY ) {
                y = y + diffY;
            } else {
                y = maxY;
            }
        }
        
        public void stepFalling() {
            int maxY = FRAME_HEIGHT - 2 - HEIGHT * (hanoyColumns[column].stackTop + 1);
            int targetX = hanoyColumns[column].getMiddleX() - width / 2;
            if (Math.abs(x - targetX) > 10) {
                x = (int)Math.round(x - sign(x - targetX)* (Math.abs(x - targetX)/5));
            } else {
                x = targetX;
            }
            vY = vY + A;
            if (y + vY < maxY) {
                y = y + vY;
            } else { 
                y = maxY; 
                if (x == targetX) {status = Status.NOT_MOVING;}
            }
        }
        
        
    }
    //HANOY_RECT CLASS ENDS
    
    
    //HANOY_GAME VARIABLES
    Thread runner;
    
    Image bufferImage = null;   // bufferImage for paint()
    Graphics2D g2Buffer;        // Component that draws into the buffer
    
    enum Status {NOT_MOVING, DRAGING_ON_COLUMN, DRAGING_OFF_COLUMN, FALLING}
    
    int HANOY_RECTS_COUNT = 5;      // Rings count
    int HANOY_RECTS_HEIGHT = 30;    // Ring height
    int COLUMNS_COUNT = 3;          // Bars count
    
    int draggingHanoyRect = -1;     // Which ring is dragging (-1 for no ring)
    
    int frontier_0, frontier_1;     // Center between 1 and 2 bars, center between 2 and 3 bars
    
    int mx, my;                     // Mouse coords
    
    boolean gameOver;
    
    HanoyColumn[] hanoyColumns = new HanoyColumn[COLUMNS_COUNT];
    HanoyRect[] hanoyRects = new HanoyRect[20];
    
    int 
        FRAME_WIDTH = 800,      // Ширина окна
        FRAME_HEIGHT = 400;     // Высота окна
    
    public HanoyGame() {
        super("Hanoy game");
    }
    
    public void init() {
        // Setting up window
        this.setResizable(false);
        this.setBounds(30, 30, FRAME_WIDTH, FRAME_HEIGHT);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setBackground(Color.WHITE);
        this.setFocusable(true);
        
        gameOver = false;
        
        // Create bars
        for (int i = 0; i <= COLUMNS_COUNT - 1; i++) {  
            hanoyColumns[i] = new HanoyColumn(i);
        }
        
        // Calculate frontiers
        frontier_0 = (hanoyColumns[0].getMiddleX()+hanoyColumns[1].getMiddleX())/2;
        frontier_1 = (hanoyColumns[1].getMiddleX()+hanoyColumns[2].getMiddleX())/2;

        // Create rings
        for (int i = 0; i <= HANOY_RECTS_COUNT - 1; i++) {
            hanoyRects[i] = new HanoyRect(0, HANOY_RECTS_COUNT - i - 1, i+1);
            hanoyColumns[0].addRectToStack(HANOY_RECTS_COUNT - i - 1);
        }
        
        bufferImage = createImage(FRAME_WIDTH, FRAME_HEIGHT);   // Setting up buffer size
        g2Buffer = (Graphics2D)bufferImage.getGraphics();       // Connecting graphics to this buffer
        
        // Filling background
        g2Buffer.setColor(Color.WHITE);
        g2Buffer.fillRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        
        // Setting up text rendering
        g2Buffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Starting a thread
        this.start();
    }
    
    public static void main(String[] args) {

        // Creating a game
        HanoyGame hanoyGameFrame = new HanoyGame();
        
        // Adding listeners
        hanoyGameFrame.addKeyListener(hanoyGameFrame);
        hanoyGameFrame.addMouseListener(hanoyGameFrame);
        hanoyGameFrame.addMouseMotionListener(hanoyGameFrame);
        
        // Initialization
        hanoyGameFrame.init();
    }
    
    
    // Sing calculation function
    public int sign(int i) {
        if (i < 0) {
            return -1;
        } else if ( i == 0 ) {
            return 0;
        } else {
            return 1;
        }
    }
    
    // Cleaning the buffer
    private void clearBuffer() {
        g2Buffer.clearRect(0, 0, FRAME_WIDTH, FRAME_HEIGHT);
        
        Color color = new Color(245, 245, 245);
        g2Buffer.setColor(color);
        g2Buffer.fillRect(frontier_0, 0, frontier_1-frontier_0, FRAME_HEIGHT);
        
        g2Buffer.setColor(new Color(220, 220, 220));
        g2Buffer.drawLine(frontier_0, 0, frontier_0, FRAME_HEIGHT);
        g2Buffer.drawLine(frontier_1, 0, frontier_1, FRAME_HEIGHT);
    }
    
    // Drawing bars
    private void fillColumns() {
        g2Buffer.setColor(Color.DARK_GRAY);     // Setting up colors
        int X, Y, WIDTH, HEIGHT;                //
        int ARC_RADIUS = 12;                    // Corners radius
        for (HanoyColumn hanoyColumn : hanoyColumns) {              // for each bar
            X = hanoyColumn.getMiddleX() - hanoyColumn.WIDTH/2;     // Calculating coords and size
            Y = hanoyColumn.getBottomY() - hanoyColumn.HEIGHT;      // 
            WIDTH = hanoyColumn.getWidth();                         // 
            HEIGHT = hanoyColumn.getHeight();                       //
            g2Buffer.fillRoundRect(X, Y+5, WIDTH, HEIGHT+5, ARC_RADIUS, ARC_RADIUS); // Draw to the buffer
        }
    }
    
    // Drawing rings
    private void fillRects() {
        int X, Y, WIDTH, HEIGHT;    //
        int ARC_RADIUS = 15;        // Corners radius
        
        for (int i = 0 ; i <= HANOY_RECTS_COUNT - 1; i++) { // for each ring

            // Задаем
            X = hanoyRects[i].getX();               // X  
            Y = hanoyRects[i].getY();               // Y
            WIDTH = hanoyRects[i].getWidth();       // Width
            HEIGHT = hanoyRects[i].getHeight();     // Height
            g2Buffer.setColor(hanoyRects[i].color); // Color
            
            g2Buffer.fillRoundRect(X, Y, WIDTH, HEIGHT, ARC_RADIUS, ARC_RADIUS); // Draw to the buffer
        }
    }
    
    private void drawGameOver() {
        g2Buffer.setColor(Color.RED);
        Font winFont = new Font("Segoe UI", Font.BOLD, 70);
        g2Buffer.setFont(winFont);
        g2Buffer.drawString("WIN!", FRAME_WIDTH / 2 - 80, FRAME_HEIGHT / 2 - 100);
    }
    
    // Функция отрисовки
    public void paint(Graphics g) {
        clearBuffer();  // Clear buffer
        fillColumns();  // Draw bars
        fillRects();    // Draw rings
        if (gameOver) { drawGameOver(); }
        g.drawImage(bufferImage, 0, 0, null); // Show buffer
    }
    
    public void update(Graphics g) {
        paint(g); 
    }

    
    //// Threads part
    //
    // Starting a thread
    public void start() {
        if (runner == null) {
            runner = new Thread(this);
            runner.start();
        }
    }
    
    // Stoping a thread
    public void stop() {
        if (runner != null) {
            runner.stop();
            runner = null;
        }
    }
    
    // Thread body
    @Override
    public void run() {
        while (true) {
            for (int i = 0; i <= HANOY_RECTS_COUNT - 1; i++) {
                if (hanoyRects[i].status == Status.FALLING) {hanoyRects[i].stepFalling();}
            }
            
            for (int i = 1; i <= 3 - 1; i++) {
                if (hanoyColumns[i].stackTop == HANOY_RECTS_COUNT - 1) { gameOver = true; }
            }
            
            try {
                repaint();
                Thread.sleep(1000/60);
            } catch(InterruptedException e) {}
        }
    }
    //
    ////

    
    //// Keyboard
    //
    // Key pressed event
    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {   //
        case 82:                    // "R" pressed:
            init();                 // Reinitialize a game
            break;
        case 84:                    // "T" pressed:
            HANOY_RECTS_COUNT++;    // Increase rings count
            init();                 // Reinitialize a game
            break;
        case 89:                    // "Y" pressed:
            HANOY_RECTS_COUNT--;    // Decrease rings count
            init();                 // Reinitialize a game
            break;
        default:
            break;
        }
        
        System.out.println(e.getKeyCode()); // Key print
        e.consume();
    }
    
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}
    
    
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        mx = e.getX();
        my = e.getY();
        draggingHanoyRect = chooseRect( e.getX(), e.getY() );
        if ( draggingHanoyRect != -1 ) {
            hanoyRects[draggingHanoyRect].setStatus(Status.DRAGING_ON_COLUMN);
        }
    }
    public void mouseReleased(MouseEvent e) {
        if ( draggingHanoyRect != -1 ) {
            hanoyRects[draggingHanoyRect].setStatus(Status.FALLING);
            hanoyRects[draggingHanoyRect].vY = 0;
        }
        draggingHanoyRect = -1;
    }
    
    public void mouseDragged(MouseEvent e) {
        int new_mx = e.getX();
        int new_my = e.getY();
        
        if ( draggingHanoyRect != -1 ) {
            hanoyRects[draggingHanoyRect].drag(new_mx - mx, new_my - my);
        }
        
        mx = new_mx;
        my = new_my;
        repaint();
    }
    public void mouseMoved(MouseEvent e) {}
    
    private int chooseRect(int X, int Y) {
        int rect = -1;
        int zone = 1;
        if (X < frontier_0) {
            zone = 0; 
        } else if ((frontier_0 < X) && ( X < frontier_1)) {
            zone = 1;
        } else {
            zone = 2;
        }
        if (hanoyColumns[zone].stackTop != -1) {
            rect = HANOY_RECTS_COUNT-1-hanoyColumns[zone].stack[hanoyColumns[zone].stackTop];
        }
        return rect;
    }
}
