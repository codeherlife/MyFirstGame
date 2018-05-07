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

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    // the constructor. When you create the object the constructor is called.
    public GamePanel(Context context)
    {
        super(context);

        //add the callback to the surface holder to intercept events
        getHolder().addCallback(this);

        thread = new MainThread(getHolder(), this);

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

        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if(event.getAction()==MotionEvent.ACTION_DOWN)
        {
            if(!player.getPlaying())
            {
                player.setPlaying(true);
            }
            else
            {
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

            bg.update();
            player.update();

            //calculate the threshold of height the border can have, based on the score.
            //max and min border height are updated, and the border switched direction when either max or min is met.

            maxBorderHeight = 30+player.getScore()/progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 of the screen
            if(maxBorderHeight > HEIGHT/4) maxBorderHeight = HEIGHT/4;
            minBorderHeight = 5+player.getScore()/progressDenom;

            //update top border
            this.updateTopBorder();

            //update bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missilesElapsed = (System.nanoTime()- missileStartTime)/ 1000000;
            if(missilesElapsed > (2000 - player.getScore()/4)){

                System.out.println("making missile");
                //first missile always go down the middle
                if(missiles.size()==0)
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile), WIDTH + 10, HEIGHT/2, 45, 15, player.getScore(), 13));
                }
                else
                {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(),R.drawable.missile),
                            WIDTH+10, (int)(rand.nextDouble()*(HEIGHT)), 45,15, player.getScore(), 13));

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
            for (Missile m : missiles) {
                m.draw(canvas);
            }
            canvas.restoreToCount(savedState);
        }

    }

    public void updateBottomBorder()
    {
        //every 50 points, insert randomly placed top blocks that break the pattern
        if(player.getScore()%50==0)
        {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),R.drawable.brick),
                    topborder.get(topborder.size()-1).getX()+20, 0, (int)((rand.nextDouble()*(maxBorderHeight
            ))+1)));
        }
    }
    public void updateTopBorder()
    {
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if(player.getScore()%40 == 0)
        {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size()-1).getX()+20, (int)((rand.nextDouble()
                    *maxBorderHeight)+ (HEIGHT-maxBorderHeight))));
        }

    }

}
