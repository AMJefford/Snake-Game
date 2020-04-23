import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.util.*;
import java.util.concurrent.TimeUnit;

//SCORE BOARD
//LIGHTNING FEATURE JUMPS YOU ANYWHERE ON THE BOARD


public class SnakeBoard extends JPanel implements ActionListener
{

  private final int WIDTH = 800;
  private final int HEIGHT = 800;
  private final int DOT_SIZE = 40; //size of apple and snake
  private final int MAX_DOTS = 400; //max num of possible dots on screen
  private final int MAX_HEALTH = 5;
  private final int HEALTHBAR_SIZE = 80;

  // private final int RANDOM_POS = 29; //calc random pos for apple
  private final int START_SNAKE = 2; //length of starting snake

  //store the coordinates of all dots in snake
  private final int x[] = new int[MAX_DOTS];
  private final int y[] = new int[MAX_DOTS];

  private Image gold, head, body, gameOverIcon, poison, lightning;
  private int bodyLength;

  //random negative not same
  private int yApple, xApple = -4;
  private int xPoison, yPoison = -3;
  private int xLightning, yLightning = -1;
  private Random rn = new Random();


  private boolean gameOver = false;
  //checks if apple or poison eaten
  //asumes poison, only needs one boolean
  private boolean appleBool, poisonBool, lightningBool, lightningMove, perform;// = true;

  //max health = 5
  private int health;// = 3;


  // LEFT: 37, UP: 38, RIGHT: 39, DOWN: 40
  private int currentDir;
  // private char holdMove = 'L';
  Timer timer;

  private boolean checkHit = true;
  public static boolean press = false;


  public SnakeBoard()
  {
    initBoard();
  }

  private void initBoard()
  {
    timer = new Timer(200, this);
    setFocusable(true);
    setBackground(Color.black);
    setPreferredSize(new Dimension(WIDTH, HEIGHT));
    loadImages();
    initGame();
    addKeyListener(new Keys());
  }


  private void loadImages()
  {
      ImageIcon iib = new ImageIcon("body.png");
      body = iib.getImage();

      ImageIcon iia = new ImageIcon("gold.png");
      gold = iia.getImage();

      ImageIcon iih = new ImageIcon("head.png");
      head = iih.getImage();

      ImageIcon iigo = new ImageIcon("gameOver.png");
      gameOverIcon = iigo.getImage();

      ImageIcon iip = new ImageIcon("poison.png");
      poison = iip.getImage();

      ImageIcon iil = new ImageIcon("lightning.png");
      lightning = iil.getImage();

  }

  private void initGame()
  {
    //starting direction = right
    currentDir = 39;
    appleBool = true;
    poisonBool = true;
    lightningBool = true;
    lightningMove = false;
    perform = false;
    health = 4;

    bodyLength = 2;
    for (int i = 0; i < START_SNAKE; i++)
    {
      x[i] = HEIGHT/2 - (i * DOT_SIZE);
      //start in the middle of the page, add body i * 10 to the right (i * 10)
      //where i is the number of body pieces needed, initialised at 1, head and one body
      y[i] = WIDTH/2;
    }
    placeItem();

    //repaint here as if just after game over needs to repaint to original start screen
    repaint();

    timer.start();
  }

  //checks if snake has hit itself or the edge
  private void checkCollision()
  {
    //check collision with edge
    if (x[0] < 0 || x[0] == WIDTH || y[0] < 0 || y[0] == HEIGHT - HEALTHBAR_SIZE - DOT_SIZE)
    {
      gameOver = true;
    }

    //check collision with itself
    for (int i = 0; i < bodyLength; i++)
    {
      for (int j = 0; j < i; j++)
      {
        if (x[i] == x[j] && y[i] == y[j])
        {
          gameOver = true;
        }
      }
    }
  }

  //perform lightning move depending on whether lightning move triggered from previous move
  private void decidePerform()
  {
    if (perform)
    {
      perform = false;
      try
      {
        Thread.sleep(2000);
        lightningMove = false;
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
    }

    if (lightningMove)
    {
      lightningMove();
      perform = true;
    }
  }


  //checks if item hit
  private void checkItem()
  {

    //perform lightning move depending on whether lightning move triggered from previous move
    decidePerform();

    //only check health after health updated, saves function call every time
    //x[0] and y[0] are head coordinates, if head hits apple
    if (x[0] == xApple && y[0] == yApple)
    {
      appleBool = true;

      //updates bodyLength and repaints first so new snake is painted
      //prevents placement of apple being places under tail of snake
      health++;
      checkHealth();

      //stops bar passing max health, and perfroming an iteration every graphic update
      if (health > MAX_HEALTH)
      {
        health = MAX_HEALTH;
      }
      //repaint before placing new apple, meangs new item ! be placed under tail of snake
      bodyLength++;
      repaint();
      placeItem();

    }

    //x[0] and y[0] are head coordinates, if head hits apple
    else if (x[0] == xPoison && y[0] == yPoison)
    {
      health--;
      poisonBool = true;
      checkHealth();
      placeItem();
    }

    else if (x[0] == xLightning && y[0] == yLightning)
    {

      lightningBool = true;
      // moves body
      lightningMove = true;
      repaint();
      placeItem();


    }
  }




  //move snake anywhere on board
  public void lightningMove()
  {

      checkHit = true;
      while (checkHit)
      {
        checkHit = false;
        int tmpX = x[0];
        int tmpY = y[0];

        x[0] = getXCo();
        y[0] = getYCo();

        //gives room for user to turn quickly without immediate game over
        while (x[0] < 2 * DOT_SIZE || x[0] > WIDTH - (2 * DOT_SIZE) || y[0] < (2 * DOT_SIZE) || y[0] > HEIGHT - (2 * DOT_SIZE) - HEALTHBAR_SIZE)
        {
          x[0] = getXCo();
          y[0] = getYCo();
        }

        int holdX;
        int holdY;

        for (int i = 1; i < bodyLength; i++)
        {
          holdX = tmpX;
          holdY = tmpY;
          tmpX = x[i];
          tmpY = y[i];

          //determines next body piece direction, changes updated position of snake to same position

          if (x[i] == holdX + DOT_SIZE)
          {
            x[i] = x[i - 1] + DOT_SIZE;
            y[i] = y[i - 1];
          }

          else if (x[i] == holdX - DOT_SIZE)
          {
            x[i] = x[i - 1] - DOT_SIZE;
            y[i] = y[i - 1];

          }

          else if (y[i] == holdY + DOT_SIZE)
          {
            y[i] = y[i - 1] + DOT_SIZE;
            x[i] = x[i - 1];

          }

          else if (y[i] == holdY - DOT_SIZE)
          {
            y[i] = y[i - 1] - DOT_SIZE;
            x[i] = x[i - 1];

          }
        }


      //checks if out of bounds
      for (int i = 0; i < bodyLength; i++)
      {
        if (x[i] < 2 * DOT_SIZE || x[i] > WIDTH - (2 * DOT_SIZE) || y[i] < (2 * DOT_SIZE) || y[i] > HEIGHT - (2 * DOT_SIZE) - HEALTHBAR_SIZE)
        {
          checkHit = true;
        }
      }
    }
  }


  public int getXCo()
  {
    int xVar = -1;

    while (xVar % DOT_SIZE != 0 || xVar > WIDTH - DOT_SIZE || xVar < 0 )// || tmpValY % DOT_SIZE != 0)
    {
      //random int between 0 and 800 (height or width), subtract 40 for size of icon so not off edge
      //subtract 80 (from size of health bar) + 40 = 120
      xVar = ((1 + rn.nextInt(8)) * 100);
    }
    // System.out.println("X :" + xVar);

    return xVar;
  }

  private int getYCo()
  {
    int yVar = -1;

    while (yVar % DOT_SIZE != 0 || yVar > HEIGHT - DOT_SIZE - HEALTHBAR_SIZE || yVar < 0 )
    {
      yVar = ((1 + rn.nextInt(8)) * 100);
    }
      //if random number less than 40, then subtract 40, set to 0
    if (yVar < 0)
    {
      yVar = 0;
    }
    return yVar;
  }

  private boolean checkHit(int xVar, int yVar)
  {
    boolean hit = false;

    //checks collision with snake
    for (int i = 0; i < bodyLength; i++)
    {
      //checks if hit snake
      if (xVar == x[i] && yVar == y[i])
      {
        hit = true;
      }
    }

    //checking collision with all items
    if ((xVar == xPoison && yVar == yPoison) || (xVar == xLightning && yVar == yLightning) ||
    (xVar == xApple && yVar == yApple))
    {
      hit = true;
    }

    return hit;
  }

  //place apple on the screen
  private void placeItem()
  {
    while (appleBool || poisonBool || lightningBool)
    {

      boolean hit = true;
      int xVar = -1;
      int yVar = -1;

      //create new coordinates, check if hitting snake, else update graphics
      while (hit)
      {
        xVar = getXCo();
        yVar = getYCo();
        hit = checkHit(xVar, yVar);
      }

      if (appleBool)
      {
        // System.out.print("place");
        xApple = xVar;
        yApple = yVar;
        System.out.println("APPLE PLACED X : " + xApple + "  Y :" + yApple);


        // System.out.println("X :" + xApple + "    Y : " + yApple);
        appleBool = false;
      }

      else if (poisonBool)
      {
        poisonBool = false;
        xPoison = xVar;
        yPoison = yVar;
        // System.out.println("X :" + xPoison + "    Y : " + yPoison + "kk");

      }
      else if (lightningBool)
      {
        lightningBool = false;
        xLightning = xVar;
        yLightning = yVar;
      }
    }
  }

  @Override
  public void paintComponent(Graphics g)
  {
    super.paintComponent(g);
    getGraphics(g);
  }

  private void getGraphics(Graphics g)
  {
    if (!gameOver)
    {
      //locating health bar at bottom of screen
      g.setColor(Color.white);
      g.fillRect(WIDTH/4, HEIGHT - 80, 400, 40);

      //width of red rect should be enemy width * (enemyhealth/snakemaxhealth)
      g.setColor(Color.red);
      g.fillRect(WIDTH/4,HEIGHT - 80, (400 * health)/5, 40);

      //draw items and snake
      g.drawImage(gold, xApple, yApple, this);
      g.drawImage(poison, xPoison, yPoison, this);
      g.drawImage(lightning, xLightning, yLightning, this);


      for (int i = 0; i < bodyLength; i++)
      {
        //head of snake
        if (i == 0)
        {
          g.drawImage(head, x[i], y[i], this);
        }
        else
        {
          g.drawImage(body, x[i], y[i], this);
        }
      }
      Toolkit.getDefaultToolkit().sync();

    }

    else
    {
      g.drawImage(gameOverIcon, 0, 0, this);
      timer.stop();
    }
  }

  @Override
  public void actionPerformed(ActionEvent e)
  {
    move(currentDir);
  }

  public void checkHealth()
  {
    if (health < 1)
    {
      gameOver = true;
    }
  }

  //called when change in direction, or time has elapsed from timer
  public void move(int moveDir)
  {
    for (int i = bodyLength; i > 0; i--)
    {
      //moving tail to next pos along of body until body is in head position
      x[i] = x[(i-1)];
      y[i] = y[(i-1)];
    }

    switch (moveDir)
    {
      //depending on direction, move head pos up down left right
      case 37:
        x[0] -= DOT_SIZE;
        break;
      case 39:
        x[0] += DOT_SIZE;
        break;
      case 38:
        y[0] -= DOT_SIZE;
        break;
      case 40:
        y[0] += DOT_SIZE;
        break;
    }

    checkItem();
    checkCollision();
    repaint();

    //update current direction
    currentDir = moveDir;

    //timer starts evenly from new direction, prevents delay in turning
    timer.stop();
    timer.start();
  }


  // managing key input
  private class Keys extends KeyAdapter
  {
    // Snake snake = new SnakeBoard();
    // snake.press = true;
    @Override
    public void keyPressed(KeyEvent e)
    {
      //32 = spacebar
      if (e.getKeyCode() == 32)
      {
         if (gameOver)
         {
           gameOver = false;
           initGame();
         }
      }

      //if key pressed is not same as previous = new direction
      else if (e.getKeyCode() != currentDir)
      {
        move(e.getKeyCode());
      }
    }
  }



}
