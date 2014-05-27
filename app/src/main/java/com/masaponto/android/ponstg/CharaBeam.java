package com.masaponto.android.ponstg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;



class CharaBeam{

    private Point p;
    private int beamCenterX;
    private int beamCenterY;
    private Paint paint;

    public boolean CharaBeamFlag = false;
    public boolean isDead = true;

    private int scale;
    private int charaBeamSpeed;
    private int radius, displayX;

    CharaBeam(int displayX, int scale){
        this.displayX = displayX;
        this.scale = scale;
        p = new Point();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setFilterBitmap(true);
        radius = displayX / 80;
        charaBeamSpeed = 5 * scale;
    }

    private void init(int w, int h){
        p.x = w;
        p.y = h;
    }

    public void move(){
        p.y -= charaBeamSpeed;
        beamCenterX = p.x + radius / 2;
        beamCenterY = p.y + radius / 2;

        if(p.y < 0){
            isDead = true;
            CharaBeamFlag = false;
        }
    }

    public void drawMove(Canvas c, int w, int h){
        if(isDead){
            init(w, h);
        }
        c.drawCircle(p.x, p.y, radius, paint);
    }

    public int getCenterX(){
        return beamCenterX;
    }

    public int getCenterY(){
        return beamCenterY;
    }

    public int getRadius(){
        return radius;
    }


}
