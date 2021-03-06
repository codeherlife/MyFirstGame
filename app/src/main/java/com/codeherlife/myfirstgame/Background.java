package com.codeherlife.myfirstgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by melanie on 4/23/18.
 */

public class Background {

    private Bitmap image;
    private int x, y, dx;

    //the constructor:
    public Background(Bitmap res)
    {
        image = res;
        dx = GamePanel.MOVESPEED;
    }

    public void update()
    {
        x+=dx;
        if(x <-GamePanel.WIDTH){
            x=0;
        }
    }

    public void draw(Canvas canvas)
    {
        canvas.drawBitmap(image, x, y, null);
        if(x<0)
        {
            canvas.drawBitmap(image, x+GamePanel.WIDTH, y, null);
        }
    }


}
