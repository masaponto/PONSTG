package com.masaponto.android.ponstg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

class Explotion{

    private Bitmap explotionImage;

    public int exSwitch = 0;

    private int displayX;
    public int count;


    Rect[] srcs = new Rect[10];
    Rect dst;


    Explotion(Bitmap explotionImage, int X, int Y, int count, int displayX){
        this.explotionImage = explotionImage;
        this.displayX = displayX;

        int h = explotionImage.getHeight();
        int w = explotionImage.getWidth() / 10;

        this.count = count;

        for(int i = 0; i < 10; i++){
            srcs[i] = new Rect(i * w, 0, w + i * w, h);
        }

        dst = new Rect(X - displayX/6, Y - displayX/6, X + displayX/6, Y + displayX/6);

    }

    public void drawMove(Canvas c){
        c.drawBitmap(explotionImage, srcs[exSwitch], dst, new Paint());
    }

}