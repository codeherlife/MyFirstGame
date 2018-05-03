package com.codeherlife.myfirstgame;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by melanie on 5/2/18.
 */

public class Smokepuff extends GameObject {
    //variable for radius below:
    public int r;
    //contructor of the smokepuff: setting the x and the y in the super class,
    // GameObject to the x and y being passed into the constructor.
    public Smokepuff(int x, int y)
    {
        r = 5;
        super.x = x;
        super.y = y;
    }
    public void update()
    {
        //making the snow puff move to the speed of negative 10.
        x-=10;

    }
    public void draw(Canvas canvas)
    {
        Paint paint = new Paint();
        paint.setColor(Color.GRAY);
        paint.setStyle(Paint.Style.FILL);

        canvas.drawCircle(x-r, y-r, r, paint);
        canvas.drawCircle(x-r+2, y-r-2, r, paint);
        canvas.drawCircle(x-r+4, y-r+1, r, paint);
    }
}
