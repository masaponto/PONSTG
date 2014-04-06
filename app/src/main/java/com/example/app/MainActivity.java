package com.example.app;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //画面の大きさ取得
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int displayX = display.getWidth();
        int displayY = display.getHeight();

        super.onCreate(savedInstanceState);
        MySurfaceView mSurfaceView = new MySurfaceView( this, displayX, displayY );
        setContentView(mSurfaceView);
    }

}

class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    //画面の大きさ
    int displayX;
    int displayY;

    //自機オブジェクト
    private Chara chara;

    //ビームの数
    int N = 10;

    //ループ用
    int j = 0;

    //ビーム用配列
    private CharaBeam[] charaBeam;

    //敵オブジェクト
    private Enemy enemy;

    private List<Enemy> enemys = new ArrayList<Enemy>();

    private SurfaceHolder holder;

    private boolean isRunning = true;
    private Thread thread;

    Resources res = this.getContext().getResources();

    //それぞれの画像
    Bitmap charaImage = BitmapFactory.decodeResource(res, R.drawable.chara);
    Bitmap charaBeamImage = BitmapFactory.decodeResource(res, R.drawable.beam);
    Bitmap enemyImage = BitmapFactory.decodeResource(res,R.drawable.enemy);
    Bitmap explotionImage = BitmapFactory.decodeResource(res,R.drawable.explotion);

    //ビーム音
    MediaPlayer beamSound;

    //ヒット音
    MediaPlayer hitSound;

    public MySurfaceView(Context context,int x,int y){
        super(context);

        beamSound = MediaPlayer.create(context, R.raw.beamsound);
        hitSound = MediaPlayer.create(context,R.raw.hitsound);

        this.displayX = x;
        this.displayY = y;

        setFocusable(true);
        getHolder().addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height ){
    }

    public void surfaceCreated(SurfaceHolder holder){

        chara = new Chara(charaImage,displayX,displayY);
        enemy = new Enemy(enemyImage,displayX,displayY);

        charaBeam = new CharaBeam[N];
        for(int i = 0; i < N; i++){
            charaBeam[i] = new CharaBeam(charaBeamImage);
        }

        for(int i = 0; i < N; i++){
            enemys.add(new Enemy(enemyImage,displayX,displayY));
        }


        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        isRunning = false;
        try {
            thread.join();
        } catch(InterruptedException ex) {
        }
        thread = null;

    }

    public boolean onTouchEvent(MotionEvent event){

        super.onTouchEvent(event);

        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();

        if(action == MotionEvent.ACTION_DOWN){

            int centerX = chara.getX() + charaImage.getWidth()/2;
            int centerY = chara.getY() + charaImage.getHeight()/2;

            double distance = Math.sqrt(Math.pow((centerX - x),2) + Math.pow((centerY - y), 2));

            int charaR = charaImage.getWidth()/2;

            if(distance < 3 * charaR){
                beamSound.seekTo(0);
                beamSound.start();
                charaBeam[j].CharaBeamFlag = true;
                j = (j + 1) % N;
            }

        }

        if(action == MotionEvent.ACTION_MOVE){
            chara.move(x, y);
        }

        try {
            hitSound.prepare();
            beamSound.prepare();
        } catch ( IllegalStateException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }

        return true;
    }

    public void run(){

        Paint mPaint = new Paint();
        Random rnd = new Random();

        int enemyCenterX;
        int enemyCenterY;

        int enemyR = enemyImage.getWidth()/2;

        int count = 0;


        while(isRunning){

            count++;

            Canvas canvas = getHolder().lockCanvas();

            if(canvas != null){
                canvas.drawColor(Color.WHITE);
                mPaint.setColor(Color.WHITE);
                chara.drawMove(canvas);

                int ran = rnd.nextInt(displayX);

                enemy.drawMove(canvas, ran);
                enemy.move();

                enemyCenterX = enemy.getX() + enemyImage.getWidth()/2;
                enemyCenterY = enemy.getY() + enemyImage.getHeight()/2;


                for(int k = 0; k < N; k++){
                    if(charaBeam[k].CharaBeamFlag){
                        charaBeam[k].drawMove(canvas, chara.getX() + charaImage.getWidth() / 2, chara.getY());
                        charaBeam[k].isDead = false;

                        charaBeam[k].move();

                        int beamCenterX = charaBeam[k].getX() + charaBeamImage.getWidth()/2;
                        int beamCenterY = charaBeam[k].getY() + charaBeamImage.getHeight()/2;

                        double hitDistance = Math.sqrt(Math.pow((enemyCenterX - beamCenterX),2)
                                + Math.pow((enemyCenterY - beamCenterY), 2));

                        if(hitDistance < enemyR ){
                            charaBeam[k].CharaBeamFlag = false;
                            canvas.drawBitmap(explotionImage, enemy.getX() + enemyImage.getWidth() / 2, enemy.getX() + enemyImage.getHeight(), new Paint());

                            enemy.isDead = true;
                            hitSound.seekTo(0);
                            hitSound.start();

                            try {
                                hitSound.prepare();
                            } catch ( IllegalStateException e ) {
                                e.printStackTrace();
                            } catch ( IOException e ) {
                                e.printStackTrace();
                            }
                        }

                    }
                }

                getHolder().unlockCanvasAndPost(canvas);
            }

            try {
                Thread.sleep(10);//お決まり
            } catch (Exception e){}
        }
    }

}


class Chara{

    private Bitmap charaImage;
    private Point p;

    public Chara(Bitmap charaImage, int w, int h){
        this.charaImage = charaImage;
        p = new Point();
        init(w,h);
    }

    private void init(int w, int h){
        p.x = w/2 - charaImage.getWidth()/2;
        p.y = h - charaImage.getHeight() * 2;
    }

    public void move(int x, int y){
        if(x < p.x + charaImage.getWidth() / 2){
            p.x-=20;
        }
        if(x > p.x + charaImage.getWidth() / 2){
            p.x+=20;
        }

        if(y < p.y + charaImage.getHeight() + 30){
            p.y-=20;
        }
        if(y > p.y + charaImage.getHeight() + 30){
            p.y+=20;
        }
    }

    public int getX(){
        return p.x;
    }

    public int getY(){
        return p.y;
    }

    public void drawMove(Canvas c){
        c.drawBitmap(charaImage, p.x, p.y, new Paint());
    }

}


class CharaBeam{

    private Bitmap charaBeamImage;
    private Point p;

    boolean CharaBeamFlag = false;
    boolean isDead = true;

    public CharaBeam(Bitmap charaBeamImage){
        this.charaBeamImage = charaBeamImage;
        p = new Point();
    }

    private void init(int w, int h){
        p.x = w;
        p.y = h;
    }

    public void move(){
        p.y -= 10;
        if(p.y < 0){
            isDead = true;
            CharaBeamFlag = false;
        }
    }

    public void drawMove(Canvas c, int w, int h){
        if(isDead){
            init(w,h);
        }
        c.drawBitmap(charaBeamImage, p.x, p.y, new Paint());
    }

    public int getX(){
        return p.x;
    }

    public int getY(){
        return p.y;
    }

}


class Enemy{

    private Bitmap enemyImage;
    private Point p;
    int dispX, dispY;

    boolean isDead = true;

    public Enemy(Bitmap enemyImage, int dispX, int dispY){
        this.enemyImage = enemyImage;

        this.dispX = dispX;
        this.dispY = dispY;

        p = new Point();
    }

    public void init(int w){
        p.x = w;
        p.y = 0;
        isDead = false;
    }

    public void move(){
        p.y += 3;

        if(p.y > dispY){
            isDead = true;
        }
    }

    public void drawMove(Canvas c, int w){
        if(isDead){
            init(w);
        }
        c.drawBitmap(enemyImage, p.x, p.y, new Paint());
    }

    public int getX(){
        return p.x;
    }

    public int getY(){
        return p.y;
    }
}