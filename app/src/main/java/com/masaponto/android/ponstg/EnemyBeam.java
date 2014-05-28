package com.masaponto.android.ponstg;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

class EnemyBeam{

    private Point p;
    private Paint paint;
    private int beamCenterX;
    private int beamCenterY;
    private double angle;
    private int scale, radius;
    private int moveSpeed;
    public boolean deathFlag = false;

    EnemyBeam(int w, int h, int charaX, int charaY, int displayX, int scale, int moveSpeed){
        p = new Point();
        p.x = w;
        p.y = h;

        paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setColor(Color.rgb(196,15,24));

        angle = Math.atan2(charaY - h, charaX - w);
        this.scale = scale;
        radius = displayX / 80;
        this.moveSpeed = moveSpeed;
    }

    public void move(){
        p.x += Math.cos(angle) * moveSpeed * scale;
        p.y += Math.sin(angle) * moveSpeed * scale;
        beamCenterX = p.x + radius / 2;
        beamCenterY = p.y + radius / 2;
    }

    public void drawMove(Canvas c){
        c.drawCircle(p.x, p.y, radius, paint);
    }

    public int getX(){
        return p.x;
    }

    public int getY(){
        return p.y;
    }

    public int getCenterX(){
        return beamCenterX;
    }

    public int getCenterY(){
        return beamCenterY;
    }

}
