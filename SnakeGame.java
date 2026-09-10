package game;

import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener{
    int boardwidth;
    int boardheigth;
    int[][] grid;

    int TileSize = 25;  // size of one tile (25x25 pixels)

    int score = 0;
    
    SnakeBody snakeBody;   // Represents the snake's body
    
    Random random;       // random number generator for food placement
    
    Timer gameLoop;      
    int velocityC; 
    int velocityR;       // movement velocity of the snake

    boolean gameOver;


    SnakeGame(int boardwidth, int boardheigth) {
        this.boardheigth = boardheigth;
        this.boardwidth = boardwidth;
        

        grid = new int[boardwidth / TileSize][boardheigth / TileSize]; // 600/25=24
        
        setPreferredSize(new Dimension(this.boardwidth, this.boardheigth)); // for the size of panel
        setBackground(Color.BLACK);
        addKeyListener(this);             // this is refering current object of a class GameSnake 
        setFocusable(true);     // listen the key press 

        snakeBody = new SnakeBody();
        snakeBody.addSegment(5, 5);  // Start with the snake's head at (5,5)

        random = new  Random();
        placeFood(); // Place the first food at random position
        
        velocityC = 1;
        velocityR = 0; // snake moves right because velocityC = 1
        gameLoop = new Timer(100,this);
        gameLoop.start(); // actionperformed call

        gameOver = false;
        
    }

    // used as a node
    public class Tile {
        int r; // Row
        int c; // Column
        Tile next;
    
        Tile(int r, int c) {
            this.r = r;
            this.c = c;
            this.next=null;
        }
    }

    public class SnakeBody {
        // (tail) | | -> | | -> null (head)
        // implementation of linked list using queue
        Tile head;
        Tile tail;
    
        SnakeBody() {
            this.head = null;
            this.tail = null;
        }
    
        // enqueue a segment at the head(rear)
        public void addSegment(int r, int c) {
            Tile newHead = new Tile(r, c);
            if(head==null && tail==null){
                head = tail = newHead;

                grid[head.r][head.c] = 1;
            }
            // insertion at the end
            else if(head.next==null){
                head.next=newHead;
                head=newHead;
                grid[head.r][head.c] = 1;
        }
        }
    
        // Remove the tail segment from front
        public void removeTail() {
            if (tail != null) {
                if (tail == head) {
                    // If there is only one segment reset both head and tail
                    grid[tail.r][tail.c] = 0;
                    head = tail = null;
                } else {
                    // Move the tail pointer to the next segment
                    grid[tail.r][tail.c] = 0; // Clear the grid cell of the current tail
                    tail = tail.next;        // Update the tail to the next segment
                }
            }
        }
        
    }


    public void paintComponent(Graphics g){
        super.paintComponent(g);  // Clears the previous frame
        draw(g);
    }

    public void draw(Graphics g) {

        if(gameOver){
            
            String message = "GAME OVER";
            Font font = new Font("Arial", Font.BOLD, 30);
            g.setFont(font);
            g.setColor(Color.WHITE);
            FontMetrics metrics = g.getFontMetrics(font); //placement of gameOver
            int x = (boardwidth - metrics.stringWidth(message)) / 2;
            int y = (boardheigth - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(message, x, y);
            
            String message2 = "PRESS R (RESTART:)";
            int y2 = y + metrics.getHeight();  // Move the y position down by the height of the first message
            int x2 = (boardwidth - metrics.stringWidth(message2)) / 2;
            g.drawString(message2, x2, y2); 
            return;
        }
        // Draw grid
        for (int i = 0; i < boardwidth / TileSize; i++) {
            // Draw vertical lines
            // x1,y1,x2,y2
            g.drawLine(i * TileSize, 0, i * TileSize, boardheigth);
            // Draw horizontal lines
            g.drawLine(0, i * TileSize, boardwidth, i * TileSize);
        }
    
        // Draw food
        g.setColor(Color.RED);
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[0].length; c++) {
                if (grid[r][c] == 2) {        // Food position in grid
                    g.fillRect(c * TileSize, r * TileSize, TileSize, TileSize);
                }
            }
        }
    
        // Draw snake
        g.setColor(Color.PINK);
        // Node current = front;
        Tile current = snakeBody.tail; // Start from the tail
        while (current != null) {
            g.fillOval(current.c * TileSize, current.r * TileSize, TileSize, TileSize); // Draw each segment
            current = current.next; // Move to the next segment
        }

        //score
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String scoreText = "SCORE: " + score;
        g.drawString(scoreText, boardwidth - 100, 30);
    }
            
    public void placeFood(){
        int r = random.nextInt(boardheigth / TileSize); //24
        int c = random.nextInt(boardwidth / TileSize);
        
        // Check and generate new coordinates until an empty cell is found
        while (grid[r][c] != 0) {
            r = random.nextInt(boardwidth / TileSize);
            c = random.nextInt(boardheigth / TileSize);
        }
        
        // Place food at the valid position
        grid[r][c] = 2;

    } 

    public void move() {
        // Calculate new head position
        int newR = snakeBody.head.r + velocityR; // Update row based on vertical velocity      //5+0=5
        int newC = snakeBody.head.c + velocityC; // Update column based on horizontal velocity //5+1=6
    
        // Check for collisions
        // newR TopBorder,  newC LeftBorder ,BottomBorder,RightBorder
        if (newR < 0 || newC < 0 || newR >= boardheigth / TileSize || newC >= boardwidth / TileSize || grid[newR][newC] == 1) {
            gameOver();
            gameLoop.stop(); 
            return;
        }
    
        if (grid[newR][newC] == 2) {

            score++;

            placeFood();
    
            // Add a new segment to the snake's body
            snakeBody.addSegment(newR, newC);
        }
        else {
            // If no food is eaten, remove the tail to keep the length the same
            snakeBody.removeTail();
            // Add a new segment at the new head position
            snakeBody.addSegment(newR, newC);
        }
    }

    public void gameOver(){
        gameOver = true;
        repaint();
    }

    public void restartGame() {
        // Close the current JFrame
        SwingUtilities.getWindowAncestor(this).dispose();
    
        // Restart the game by calling the main method again
        main(null);
    }

    
    // for timer
    @Override
    public void actionPerformed(ActionEvent e){
        move();
        repaint();
    }


    // for key control
    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver && e.getKeyCode() == KeyEvent.VK_R) {
            restartGame(); // Call the method to restart the game
        }
     
        if (!gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_UP && velocityR != 1) {
                velocityC = 0;
                velocityR = -1;
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityR != -1) {
                velocityC = 0;
                velocityR = 1;
            } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityC != 1) {
                velocityC = -1;
                velocityR = 0;
            } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityC != -1) {
                velocityC = 1;
                velocityR = 0;
            }
        }
     }


    // do not need to use them just to find them in code
    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    public static void main(String[] args) {
        int boardwidth = 600;
        int boardheigth = 600;

        JFrame frame = new JFrame("Viper's Voyage");
        frame.setVisible(true);
        frame.setSize(boardwidth, boardheigth);
        frame.setLocationRelativeTo(null);   // open up the window at the center of our screen  
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  // terminate when click on x

        SnakeGame SG = new SnakeGame(boardwidth, boardheigth);
        frame.add(SG);  // Add the game panel
        frame.pack();   // for full screen 600x600 by not adding the title
        SG.requestFocus(); // listen the key press 
    }  
}