package com.masaponto.android.ponstg;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by masato on 5/21/14.
 */

class Explotion{

    private Bitmap explotionImage;

    public int exSwitch = 0;

    int dispX, dispY;
    int count;

    Rect[] srcs = new Rect[10];
    Rect dst;


    Explotion(Bitmap explotionImage, int X, int Y, int count){
        this.explotionImage = explotionImage;
        int h = explotionImage.getHeight();
        int w = explotionImage.getWidth() / 10;

        dispX = X - w + 20;
        dispY = Y - h;

        this.count = count;

        for(int i = 0; i < 10; i++){
            srcs[i] = new Rect(i * w, 0, w + i * w, h);
        }

        dst = new Rect(dispX, dispY, dispX + w * 3 / 2, dispY + h * 3 / 2);

    }

    public void drawMove(Canvas c){
        c.drawBitmap(explotionImage, srcs[exSwitch], dst, new Paint());
    }

}