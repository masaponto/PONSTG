package com.masaponto.android.ponstg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by masato on 5/21/14.
 */
class Enemy{

    private Bitmap enemyImage;
    private Rect enemySrc, enemyDst;
    private Point p;
    private Paint paint;

    private int enemyCenterX;
    private int enemyCenterY;
    private int displayX, displayY;
    private int sizeX, sizeY;
    private int scale;
    private int moveSpeed;

    double hitDistance;

    public boolean deathFlag = false;

    //add
    Matrix matrix1 = new Matrix();
    Matrix matrix2 = new Matrix();
    Matrix matrix3 = new Matrix();
    Matrix matrix4 = new Matrix();
    Bitmap enemy1, enemy2, enemy3, enemy4;
    private int count;

    Enemy(Bitmap enemyImage, int w, int h, int displayX, int displayY, int sizeX, int sizeY, int scale, int moveSpeed){
        this.enemyImage = enemyImage;
        p = new Point();
        paint = new Paint();
        p.x = w;
        p.y = h;
        this.scale = scale;
        this.displayX = displayX;
        this.displayY = displayY;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.moveSpeed = moveSpeed;

        enemyCenterX = p.x + (sizeX) / 2;
        enemyCenterY = p.y + (sizeY) / 2;

        matrix1.postScale(0.1F * scale, 0.1F * scale);
        matrix2.postRotate(22.5F);
        matrix2.postScale(0.1F * scale, 0.1F * scale);
        matrix3.postRotate(45F);
        matrix3.postScale(0.1F * scale, 0.1F * scale);
        matrix4.postRotate(67.5F);
        matrix4.postScale(0.1F * scale, 0.1F * scale);
        enemy1 = Bitmap.createBitmap(enemyImage, 0, 0, enemyImage.getWidth(), enemyImage.getHeight(), matrix1, true);
        enemy2 = Bitmap.createBitmap(enemyImage, 0, 0, enemyImage.getWidth(), enemyImage.getHeight(), matrix2, true);
        enemy3 = Bitmap.createBitmap(enemyImage, 0, 0, enemyImage.getWidth(), enemyImage.getHeight(), matrix3, true);
        enemy4 = Bitmap.createBitmap(enemyImage, 0, 0, enemyImage.getWidth(), enemyImage.getHeight(), matrix4, true);

        count = 0;
    }

    public void move(){
        p.y += moveSpeed * scale;
        enemyCenterX = p.x + (sizeX) / 2;
        enemyCenterY = p.y + (sizeY) / 2;
    }

    public void drawMove(Canvas c){

        count++;

        if(count < 5){
            c.drawBitmap(enemy1, p.x, p.y, paint);
        }else if(5 <= count && count < 10){
            c.drawBitmap(enemy2, p.x, p.y, paint);
        }else if(10 <= count && count < 15){
            c.drawBitmap(enemy3, p.x, p.y, paint);
        }else{
            c.drawBitmap(enemy4, p.x, p.y, paint);
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
