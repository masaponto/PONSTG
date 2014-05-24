package com.masaponto.android.ponstg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;


class Enemy{

    private Bitmap[] enemyImage;
    private Point p;
    private Paint paint;

    private int enemyCenterX;
    private int enemyCenterY;
    private int displayX, displayY;

    private int scale;
    private int moveSpeed;

    public double hitDistance;

    public boolean deathFlag = false;

    private int count;

    Enemy(Bitmap[] enemyImage, int w, int h, int displayX, int displayY, int scale, int moveSpeed){
        this.enemyImage = enemyImage;
        p = new Point();
        paint = new Paint();
        p.x = w;
        p.y = h;
        this.scale = scale;
        this.displayX = displayX;
        this.displayY = displayY;

        this.moveSpeed = moveSpeed;

        enemyCenterX = p.x + (enemyImage[0].getWidth()) / 2;
        enemyCenterY = p.y + (enemyImage[0].getHeight()) / 2;

        count = 0;
    }

    public void move(){
        p.y += moveSpeed * scale;
        enemyCenterX = p.x + (enemyImage[0].getWidth()) / 2;
        enemyCenterY = p.y + (enemyImage[0].getHeight()) / 2;
    }

    public void drawMove(Canvas c){

        count++;

        if(count < 5){
            c.drawBitmap(enemyImage[0], p.x, p.y, paint);
        }else if(5 <= count && count < 10){
            c.drawBitmap(enemyImage[1], p.x, p.y, paint);
        }else if(10 <= count && count < 15){
            c.drawBitmap(enemyImage[2], p.x, p.y, paint);
        }else{
            c.drawBitmap(enemyImage[3], p.x, p.y, paint);
        }

        if(count >= 20){
            count = 0;
        }
    }

    public int getX(){
        return p.x;
    }

    public int getY(){
        return p.y;
    }

    public int getCenterX(){
        return enemyCenterX;
    }

    public int getCenterY(){
        return enemyCenterY;
    }

}
