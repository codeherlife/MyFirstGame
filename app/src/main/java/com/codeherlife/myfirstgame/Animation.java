package com.codeherlife.myfirstgame;

import android.graphics.Bitmap;

/**
 * Created by melanie on 4/30/18.
 */

public class Animation {
    private Bitmap[] frames;
    private int currentFrame;
    private long startTime;
    private long delay;
    private boolean playedOnce;

    public void setFrames(Bitmap[] frames){

        this.frames = frames;
        currentFrame = 0;
        startTime = System.nanoTime();
    }
}
public void setDelay
