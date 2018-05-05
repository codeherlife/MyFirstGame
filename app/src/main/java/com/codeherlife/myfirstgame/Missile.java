package com.codeherlife.myfirstgame;

import java.util.Random;

/**
 * Created by melanie on 5/5/18.
 */

public class Missile extends GameObject {
    private int score;
    private int speed;
    private Random rand = new Random();




    public Missile (Bitmap res, int x, int y, int w, int h, int s, int numFrames)
    {

        super.x = x;
        super.y = y;

        width = w;

        height = h;

    }


}
