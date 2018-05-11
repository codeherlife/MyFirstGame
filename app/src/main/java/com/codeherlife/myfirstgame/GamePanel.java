package com.codeherlife.myfirstgame;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by melanie on 4/16/18.
 * Game obective- (difficulty)- based on max and min border height- as player scores go up the borders start to
 * take up more and more of the screen.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long smokeStartTime;
    private long missileStartTime;

    private MainThread thread;
    private Background bg;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    //creating these variables to reset the player back to the original position, when he dies.
    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;



    // the constructor. When you create the object the constructor is called.
    public GamePanel(Context context)
    {
        super(context);

        //add the callback to the surface holder to intercept events
        getHolder().addCallback(this);


        //make game panel focusable so it can handle events
        setFocusable(true);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){
        boolean retry = true;
        int counter = 0;
        while(retry && counter<1000)
        {
            counter++;
            try{thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            }catch (InterruptedException e){e.printStackTrace();}

        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder){

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.grassbg1));
        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.helicopter), 65, 25, 3);
        smoke = new ArrayList<Smokepuff>();
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();

        //going to make little smoke puffs come out one at a time instead of constant stream of puffs.
        smokeStartTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
        {
            if(!player.getPlaying() && newGameCreated && reset)
            {
                player.setPlaying(true);
                player.setUp(true);
            }
            if(player.getPlaying())
            {
                if(!started) started= true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if(event.getAction()==MotionEvent.ACTION_UP)
        {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update ()
    {
        if(player.getPlaying()) {

            if(botborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }
            if(topborder.isEmpty())
            {
                player.setPlaying(false);
                return;
            }

            bg.update();
            player.update();

            //calculate the threshold of height the border can have, based on the score.
            //max and min border height are updated, and the border switched direction when either max or min is met.

            maxBorderHeight = 30+player.getScore()/progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 of the screen
            if(maxBorderHeight > HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;

            //check bottom border collision
            for(int i=0; i<botborder.size(); i++){
                if(collision(botborder.get(i), player))
                    player.setPlaying(false);

            }

            //check top border collision
            for(int i=0; i<topborder.size(); i++)
            {
                //if there's a collision between the top border and the player
                if(collision(topborder.get(i), player))
                    player.setPlaying(false);

            }

            //update top border
            this.updateTopBorder();

            //update bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missilesElapsed = (System.nanoTime()- missileStartTime)/ 1000000;
            if(missilesElapsed > (2000 - player.getScore()/4)){


                //first missile always go down the middle
                if(missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }
                else
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT - (maxBorderHeight * 2))+ maxBorderHeight), 45,15, player.getScore(), 13));

                }
                //reset timer
                missileStartTime = System.nanoTime();
            }

            //loop through every missile
            for(int i=0; i<missiles.size(); i++){

                //update missile
                missiles.get(i).update();
                if(collision(missiles.get(i), player))
                {
                    missiles.remove(i);
                    player.setPlaying(false);
                    break;
                }

                //remove missile if it is way off the screen
                if(missiles.get(i).getX()<-100)
                {
                    missiles.remove(i);
                    break;
                }
            }

            //add smokepuffs on timer
            long elapsed = (System.nanoTime() - smokeStartTime) / 1000000;

            //if elapsed time is greater than 120 then add a new smoke puff.
            if(elapsed > 120)
            {
                smoke.add(new Smokepuff(player.getX(), player.getY()+10));
                smokeStartTime = System.nanoTime();
            }

            //update smoke puffs and remove them accordingly
            for(int i=0; i<smoke.size(); i++)
            {
                smoke.get(i).update();
                if(smoke.get(i).getX()<-10)
                {
                    smoke.remove(i);
                }
            }
        }
        else {
            //if the player is not playing, reset DY. (Disappear is the var that determines if we're going to draw
            //the helicopter or not. helicopter will disappear if reset hasn't been completed yet but the
            //player is not playing.
            player.resetDY();
            if(!reset)
            {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(),R.drawable.explosion), player.getX(),
                        player.getY()-30, 100, 100, 25);
            }

            explosion.update();

            //reset elapsed is how long you want to wait before resetting the player.
            long resetElapsed = (System.nanoTime()-startReset)/1000000;

            if(!newGameCreated) {
                newGame();
            }
        }
    }

    public boolean collision(GameObject a, GameObject b)
    {
        if(Rect.intersects(a.getRectangle(), b.getRectangle()))
        {
            return true;
        }
        return false;
    }

    @Override
    public void draw(Canvas canvas) {

        //android studio recommended i add the following line of code although wasn't indicated in tutorial
        super.draw(canvas);

        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            player.draw(canvas);

            //draw smokepuffs
            for (Smokepuff sp : smoke) {
                sp.draw(canvas);
            }

            //draw missiles
            for (Missile m: missiles) {
                m.draw(canvas);
            }
            canvas.restoreToCount(savedState);

            //draw topborder
            for(TopBorder tb: topborder)
            {
                tb.draw(canvas);
            }

            //draw botborder
            for(BotBorder bb: botborder)
            {
                bb.draw(canvas);
            }

        }

    }

    public void updateTopBorder()
    {
        //every 50 points, insert randomly placed top blocks that break the pattern
        if(player.getScore()%50==0)
        {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    topborder.get(topborder.size()-1).getX()+20, 0, (int)((rand.nextDouble()*(maxBorderHeight
            ))+1)));
        }
        for(int i=0; i<topborder.size(); i++)
        {
            topborder.get(i).update();
            if(topborder.get(i).getX()<-20)
            {
                topborder.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if(topborder.get(topborder.size()-1).getHeight()>=maxBorderHeight)
                {
                    topDown = false;
                }
                //(retrieve the last element in the arraylist)
                if(topborder.get(topborder.size()-1).getHeight()<=minBorderHeight)
                {
                    topDown = true;
                }
                //new border added will have larger height
                if(topDown)
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()+1));
                }
                //new border added will have smaller height
                else
                {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size()-1).getX()+20,
                            0, topborder.get(topborder.size()-1).getHeight()-1));
                }
            }
        }
    }
    public void updateBottomBorder()
    {
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if(player.getScore()%40 == 0)
        {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size()-1).getX()+20, (int)((rand.nextDouble()
                    *maxBorderHeight)+ (HEIGHT-maxBorderHeight))));
        }

        //update bottom border
        for(int i=0; i<botborder.size(); i++)
        {
            botborder.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one.
            if(botborder.get(i).getX()<-20) {
                botborder.remove(i);


                //determine if border will be moving up or down
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT- maxBorderHeight) {
                    botDown = true;
                }

                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT- minBorderHeight) {
                    botDown = false;
                }

                //adding new borders- So if botDown we'll be adding borders that is 1 less than the last in the array.
                //ELSe, we will be adding a border that is one more than the last element in the array.

                if (botDown)
                //x position is last element in the array plus 20, because width is 20.
                {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() - 1));
                    // - 1 because it is moving upwards.
                }
            }

        }

    }
    //create the initial borders and the new game method. This will be called every time the player dies
    // and we want to reset the game.
    public void newGame()
    {
        botborder.clear();
        topborder.clear();
        missiles.clear();
        smoke.clear();

        //reset min border height
        minBorderHeight = 5;
        maxBorderHeight = 30;
        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT/2);

        //create initial borders- this loop will create borders until they are width+40 off the screen.
        //will create enough borders so they go off the screen slightly.

        //initial top border
        for(int i=0; i*20 < WIDTH+40; i++)
        {
            //first top border created
            if(i==0)
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i*20,0,10));
            }
            else
            {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i*20,0,topborder.get(i-1).getHeight()+1));
            }
        }
        //initial bottom border
        for(int i=0; i*20 < WIDTH+40; i++)
        {
            //first border ever created
            if(i==0)
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick)
                ,i*20, HEIGHT- minBorderHeight));
            }

            //adding borders until the initial screen is filled.
            else
            {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i*20, botborder.get(i-1).getY()-1));
            }
        }

        // we want new game created to be called anytime the player dies.
        newGameCreated = true;
    }

}
