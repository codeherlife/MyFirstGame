package com.codeherlife.myfirstgame;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by melanie on 4/21/18.
 */

public class MainThread extends Thread {

    private int FPS = 30;
    private double averageFPS;
    private SurfaceHolder surfaceHolder;
    private GamePanel gamePanel;
    private boolean running;
    public static Canvas canvas;

    //the constructor, call the constructor with the super class and override the run method inside class thread
    public MainThread(SurfaceHolder surfaceHolder, GamePanel gamePanel){

        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }
    @Override
    public void run(){
        long startTime;
        long timeMillis;
        long waitTime;
        long totalTime = 0;
        int frameCount = 0;
        long targetTime = 1000/FPS;
    }
}
