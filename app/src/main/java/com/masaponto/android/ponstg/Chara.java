package com.masaponto.android.ponstg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by masato on 5/21/14.
 */

class Chara{

    private Bitmap charaImage;
    private Rect charaSrc, charaDst;
    private Point p;
    private Paint paint;

    public int hitDistance1, hitDistance2;

    private int displayX, displayY;

    private int scale;
    private int sizeX, sizeY;

    Chara(Bitmap charaImage, int displayX, int displayY, int sizeX, int sizeY, int scale){
        this.charaImage = charaImage;
        this.displayX = displayX;
        this.displayY = displayY;
        this.scale = scale;
        this.sizeX = sizeX;
        this.sizeY = sizeY;

        p = new Point();
        paint = new Paint();
        charaSrc = new Rect(0, 0, charaImage.getWidth(), charaImage.getHeight());

        init(displayX, displayY);
    }

    public void init(int displayX, int displayY){
        p.x = displayX / 2 - (sizeX) / 2;
        p.y = displayY * 2 / 3 - (sizeY) * 3;
        charaDst = new Rect(p.x, p.y, p.x + sizeX, p.y + sizeY);
    }

    public void move(int x, int y){
        p.x = x - (sizeX) / 2;
        p.y = y - (sizeY) / 2;

        //画面外のでないように処理
        if(p.x < 0){
            p.x = 0;
        }
        if(p.x + (sizeX) > displayX){
            p.x = displayX - (sizeX);
        }
        if(p.y < 0){
            p.y = 0;
        }
        if(p.y + (sizeY) > displayY * 3/4){
            p.y = displayY * 3/4 - sizeY;
        }

        charaDst = new Rect(p.x, p.y, p.x + sizeX, p.y + sizeY);
    }

    public int getCenterX(){
        return p.x + (sizeX) / 2;
    }

    public int getCenterY(){
        return p.y + (sizeY) / 2;
    }

    public void drawMove(Canvas c){
        c.drawBitmap(charaImage, charaSrc, charaDst, paint);
    }

}
