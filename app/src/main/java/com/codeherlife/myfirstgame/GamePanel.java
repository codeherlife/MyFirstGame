package com.codeherlife.myfirstgame;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by melanie on 4/16/18.
 */

public class GamePanel extends SurfaceView implements SurfaceHolder.Callback
{
    // the constructor. When you create the object the constructor is called.
    public GamePanel(Context context)
    {
        super(context);

        //add the callback to the surface holder to intercept events
        getHolder().addCallback(this);

        //LEFT OFF HERE AT VIDEO 2 5:00!!!!!!
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder){}

    @Override
    public void surfaceCreated(SurfaceHolder holder){}

}
