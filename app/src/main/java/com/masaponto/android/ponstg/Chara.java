package com.masaponto.android.ponstg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

class Chara{

    private Bitmap charaImage;
    private Point p;
    private Paint paint;

    public int hitDistance1, hitDistance2;

    private int displayX, displayY;

    Chara(Bitmap charaImage, int displayX, int displayY, int scale){
        this.charaImage = charaImage;
        this.displayX = displayX;
        this.displayY = displayY;

        p = new Point();
        paint = new Paint();

        init(displayX, displayY);
    }

    public void init(int displayX, int displayY){
        p.x = displayX / 2 - (charaImage.getWidth()) / 2;
        p.y = displayY * 2 / 3 - (charaImage.getHeight()) * 3;
    }

    public void move(int x, int y){
        p.x = x - (charaImage.getWidth()) / 2;
        p.y = y - (charaImage.getHeight()) / 2;

        //画面外のでないように処理
        if(p.x < 0){
            p.x = 0;
        }
        if(p.x + (charaImage.getWidth()) > displayX){
            p.x = displayX - (charaImage.getWidth());
        }
        if(p.y < 0){
            p.y = 0;
        }
        if(p.y + (charaImage.getHeight()) > displayY * 3/4){
            p.y = displayY * 3/4 - charaImage.getHeight();
        }

    }

    public int getCenterX(){
        return p.x + (charaImage.getWidth()) / 2;
    }

    public int getCenterY(){
        return p.y + (charaImage.getHeight()) / 2;
    }

    public void drawMove(Canvas c){
        c.drawBitmap(charaImage, p.x, p.y, paint);
    }

}
