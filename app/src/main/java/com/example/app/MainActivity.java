package com.example.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MainActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //画面の大きさ取得
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int displayX = display.getWidth();
        int displayY = display.getHeight();

        super.onCreate(savedInstanceState);
        MySurfaceView mSurfaceView = new MySurfaceView(this, displayX, displayY);
        setContentView(mSurfaceView);
    }

    public void onPause(){
        super.onPause();
        finish();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // 終了していいか、ダイアログで確認
                    showDialog(MainActivity.this,"Quit game","Are you sure you want to quit?");
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    //ダイアログ
    private void showDialog(Context context,String title,String text) {
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish(); //終了
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                })

                .show();
    }


}

class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable
{

    //画面の大きさ
    int displayX;
    int displayY;

    int score = 0;

    //自機オブジェクト
    private Chara chara;

    //ビームの数
    final int N = 5;

    //ビーム発射ループ用カウンタ
    int beamCount = 0;

    //ビーム用配列
    private CharaBeam[] charaBeam;

    //敵用リスト
    private List<Enemy> enemys = new ArrayList<Enemy>();

    //敵ビーム用リスト
    private List<EnemyBeam> enemyBeams = new ArrayList<EnemyBeam>();

    //爆発用リスト
    private List<Explotion> explotions = new ArrayList<Explotion>();

    //private SurfaceHolder holder;

    private boolean isRunning;
    private Thread thread;
    private Context mContext;

    //charaが敵にやられたかの判定
    boolean hitFlag = false;

    //pause
    boolean pauseFlag = false;

    //touchされた場所
    int touchX, touchX2;
    int touchY, touchY2;

    //charaの中心座標
    int charaCenterX;
    int charaCenterY;

    Resources res = this.getContext().getResources();

    //画像
    Bitmap charaImage = BitmapFactory.decodeResource(res, R.drawable.chara);
    Bitmap enemyImage = BitmapFactory.decodeResource(res,R.drawable.enemy);

    Bitmap explotionImage = BitmapFactory.decodeResource(res,R.drawable.explotion);

    Bitmap pauseImage = BitmapFactory.decodeResource(res,R.drawable.pause);
    final Rect pauseSrc = new Rect(0,0,pauseImage.getWidth(),pauseImage.getHeight());
    int pauseX1,pauseX2,pauseY1,pauseY2;
    Rect pauseDst,pauseDst2;
    boolean pausePushFlag = false;

    Bitmap playImage = BitmapFactory.decodeResource(res, R.drawable.play);
    final Rect playSrc = new Rect(0,0,playImage.getWidth(),playImage.getHeight());
    boolean playPushFlag = false;


    SoundPool sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
    int beamSoundId, hitSoundId;

    int scale;

    //Typeface typeface;

    public MySurfaceView(Context context,int x,int y){
        super(context);
        mContext = context;

        beamSoundId = sp.load(context, R.raw.beamsound, 1);
        hitSoundId = sp.load(context, R.raw.hitsound, 1);

        this.displayX = x;
        this.displayY = y;

        pauseX1 = displayX - displayX/5;
        pauseY1 = 0;
        pauseX2 = displayX;
        pauseY2 = displayX/5;
        pauseDst = new Rect( pauseX1, pauseY1, pauseX2, pauseY2);
        pauseDst2 = new Rect( pauseX1, pauseY1 + displayX/100, pauseX2, pauseY2 + displayX/100);

        scale = displayX / 480;

        //typeface = Typeface.createFromAsset(getContext().getAssets(), "Pigmo-00_pilot.ttf");

        setFocusable(true);
        getHolder().addCallback(this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height ){
    }

    public void surfaceCreated(SurfaceHolder holder){

        chara = new Chara(charaImage, displayX, displayY, displayX/11, displayY/11, scale);

        charaBeam = new CharaBeam[N];
        for(int i = 0; i < N; i++){
            charaBeam[i] = new CharaBeam(displayX, scale);
        }

        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder){

        sp.release();
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

        if(!hitFlag){

            if(action == MotionEvent.ACTION_DOWN){

                touchX = (int) event.getX();
                touchY = (int) event.getY();

                charaCenterX = chara.getCenterX();
                charaCenterY = chara.getCenterY();

                if(pauseX1 < touchX && touchX < pauseX2 && pauseY1< touchY && touchY < pauseY2){
                    pausePushFlag = true;
                    playPushFlag = true;
                }
                else{
                    if(!pauseFlag){
                        //beamを出す
                        if (!charaBeam[beamCount].CharaBeamFlag) {
                            sp.play(beamSoundId, 1.0F, 1.0F, 0, 0, 1.0F);
                        }
                        charaBeam[beamCount].CharaBeamFlag = true;
                        beamCount = (beamCount + 1) % N;
                    }

                }
            }


            if(action == MotionEvent.ACTION_MOVE){
                touchX2 = (int) event.getX();
                touchY2 = (int) event.getY();

                if(!pauseFlag) {
                    chara.move(charaCenterX - (touchX - touchX2), charaCenterY - (touchY - touchY2));
                }
            }

            if(action == MotionEvent.ACTION_UP){
                pausePushFlag = false;
                playPushFlag = false;

                if(pauseFlag){
                    if (pauseX1 < touchX2 && touchX2 < pauseX2 && pauseY1 < touchY2 && touchY2 < pauseY2) {
                        pauseFlag = false;
                    }
                }
                else {
                    if (pauseX1 < touchX2 && touchX2 < pauseX2 && pauseY1 < touchY2 && touchY2 < pauseY2) {
                        pauseFlag = true;
                        showDialog(mContext,"Paused","");
                    }
                }

            }
        }

        return true;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    // 終了していいか、ダイアログで確認
                    showDialog(mContext,"Quit game","Are you sure you want to quit?");
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    //ダイアログ
    private void showDialog(Context context,String title,String text) {
        AlertDialog.Builder alertDialog=new AlertDialog.Builder(mContext);

        // ダイアログの設定
        alertDialog.setTitle(title);      //タイトル設定

        if(text!=""){
            alertDialog.setMessage(text);
        }

        // OK(肯定的な)ボタンの設定
        alertDialog.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // OKボタン押下時の処理
                Log.d("AlertDialog", "Positive which :" + which);
                Intent title = new Intent(getContext(),TitleActivity.class);
                title.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                title.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                isRunning = false;
                //thread = null;
                mContext.startActivity(title);
            }
        });

        // SKIP(中立的な)ボタンの設定
        alertDialog.setNeutralButton("Resume", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // SKIPボタン押下時の処理
                Log.d("AlertDialog", "Neutral which :" + which);
                pauseFlag = false;
            }
        });

        // NG(否定的な)ボタンの設定
        alertDialog.setNegativeButton("Restart", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // NGボタン押下時の処理
                Log.d("AlertDialog", "Negative which :" + which);
                Intent mainIntent = new Intent(getContext(),MainActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                isRunning = false;
                //thread = null;
                mContext.startActivity(mainIntent);
            }
        });

        // ダイアログの作成と描画
//        alertDialog.create();
        alertDialog.show();
    }



    public void run(){

        Paint mPaint = new Paint();

        int count = 50;
        int overTime = 0;

        final int explotionSpeed = 5;

        final int enemyR = displayX/11;
        final int charaR = displayX/20;

        boolean overFlag = false;


        while(isRunning){

            Canvas canvas = getHolder().lockCanvas();

            if(canvas != null) {

                if(!pauseFlag) {
                    count++;

                    if(!hitFlag) {


                        if(count % (60 / scale) == 0) {
                            Random rnd = new Random();
                            //int ran = rnd.nextInt(displayX - 2 * (displayX/11)) + (displayX/11) / 2;
                            int ran = rnd.nextInt(displayX - (displayX/11)) + displayX/11;
                            //int ran = rnd.nextInt(displayX - (displayX/2));
                            enemys.add(new Enemy(enemyImage, ran, 0, displayX, displayY, displayX/11, displayX/11, scale));
                        }

                        if(count % 100 == 0 && !enemys.isEmpty()) {
                            for(int i = 0; i < enemys.size(); i++) {
                                enemyBeams.add(new EnemyBeam(enemys.get(i).getCenterX()
                                        , enemys.get(i).getCenterY(), chara.getCenterX(), chara.getCenterY(), displayX, scale));
                            }
                        }

                    }

                    for(int i = 0; i < enemys.size(); i++){

                        if(!hitFlag) {
                            enemys.get(i).move();
                        }

                        //画面外に出たらリストから消す
                        if(enemys.get(i).getY() > displayY * 2/3){
                            enemys.remove(i);
                        }

                        chara.hitDistance1 = (int) Math.sqrt(Math.pow(chara.getCenterX() - enemys.get(i).getCenterX(), 2)
                                + Math.pow(chara.getCenterY() - enemys.get(i).getCenterY(), 2));

                        if(chara.hitDistance1 < charaR){

                            hitFlag = true;

                            if (overTime == 0) {
                                overTime = count;
                            }

                            explotions.add(new Explotion(explotionImage, chara.getCenterX()
                                    , chara.getCenterY(), count));

                            explotions.add(new Explotion(explotionImage, enemys.get(i).getCenterX()
                                    , enemys.get(i).getCenterY(), count));

                            enemys.remove(i);

                            sp.play(hitSoundId, 1.0F, 1.0F, 0, 0, 1.0F);

                        }
                    }


                    for (int i = 0; i < enemyBeams.size(); i++) {
                        if (!hitFlag) {
                            enemyBeams.get(i).move();
                        }

                        chara.hitDistance2 = (int) Math.sqrt(Math.pow(chara.getCenterX() - enemyBeams.get(i).getCenterX(), 2)
                                + Math.pow(chara.getCenterY() - enemyBeams.get(i).getCenterY(), 2));

                        if (enemyBeams.get(i).getY() > displayY * 2 / 3 || enemyBeams.get(i).getY() < 0
                                || enemyBeams.get(i).getX() < 0 || enemyBeams.get(i).getX() > displayX) {
                            enemyBeams.remove(i);
                        }


                        if (chara.hitDistance2 < charaR) {
                            hitFlag = true;

                            if (overTime == 0) {
                                overTime = count;
                            }

                            explotions.add(new Explotion(explotionImage, chara.getCenterX()
                                    , chara.getCenterY(), count));

                            enemyBeams.remove(i);

                            sp.play(hitSoundId, 1.0F, 1.0F, 0, 0, 1.0F);

                        }

                    }

                    for (int i = 0; i < N; i++) {
                        if (charaBeam[i].CharaBeamFlag) {
                            charaBeam[i].isDead = false;

                            if (!hitFlag) {
                                charaBeam[i].move();
                            }

                            for (int j = 0; j < enemys.size(); j++) {
                                enemys.get(j).hitDistance = Math.sqrt(Math.pow((enemys.get(j).getCenterX() - charaBeam[i].getCenterX()), 2)
                                        + Math.pow((enemys.get(j).getCenterY() - charaBeam[i].getCenterY()), 2));

                                //ビームの当たり判定
                                if (enemys.get(j).hitDistance < enemyR) {

                                    score++;

                                    explotions.add(new Explotion(explotionImage, enemys.get(j).getCenterX()
                                            , enemys.get(j).getCenterY(), count));

                                    charaBeam[i].CharaBeamFlag = false;
                                    charaBeam[i].isDead = true;

                                    enemys.remove(j);

                                    sp.play(hitSoundId, 1.0F, 1.0F, 0, 0, 1.0F);

                                }

                            }

                        }
                    }


                    //爆発処理
                    for (int i = 0; i < explotions.size(); i++) {

                        for (int j = 1; j < 10; j++) {
                            if (explotions.get(i).count + explotionSpeed * j < count) {
                                explotions.get(i).exSwitch = j;
                            }
                        }

                        if (explotions.get(i).count + explotionSpeed * 10 < count) {
                            explotions.remove(i);
                        }

                    }


                }

                drawGame(canvas, mPaint);

                getHolder().unlockCanvasAndPost(canvas);

                if (overTime != 0 && overTime + 100 < count) {
                    overFlag = true;
                }

                if (overFlag) {
                    isRunning = false;
                    gameOver();
                }

            }
        }


        try {
            Thread.sleep(10);//お決まり
        } catch (Exception e){}
    }


    private void drawGame(Canvas canvas, Paint mPaint){

        canvas.drawColor(Color.WHITE);
        mPaint.setColor(Color.WHITE);

        for(int i = 0; i < enemyBeams.size(); i++){
            enemyBeams.get(i).drawMove(canvas);
        }

        for(int i = 0; i < enemys.size(); i++){
            enemys.get(i).drawMove(canvas);
        }

        for(int i = 0; i < N; i++){
            charaBeam[i].drawMove(canvas, chara.getCenterX() - charaBeam[i].getRadius() / 2
                    , chara.getCenterY() - (displayX/11)/2 + charaBeam[i].getRadius());
        }

        if(!hitFlag){
            chara.drawMove(canvas);
        }

        for(int i = 0; i < explotions.size(); i++){
            explotions.get(i).drawMove(canvas);
        }

        if(pauseFlag){
            if(playPushFlag){
                canvas.drawBitmap(playImage, pauseSrc, pauseDst2, mPaint);
            }else{
                canvas.drawBitmap(playImage, pauseSrc, pauseDst, mPaint);
            }
        }else{
            if(pausePushFlag){
                canvas.drawBitmap(pauseImage, playSrc, pauseDst2, mPaint);
            }else{
                canvas.drawBitmap(pauseImage, playSrc, pauseDst, mPaint);
            }
        }

        mPaint.setTextSize(50 * scale);
        mPaint.setColor(Color.BLACK);
        canvas.drawText("SCORE:" + score,0,50 * scale, mPaint);

        canvas.drawRect(0,displayY*2/3,displayX,displayY,mPaint);

    }

    private void gameOver(){
        //GameOver画面へ
        Intent gameOver = new Intent(getContext(),OverActivity.class);
        gameOver.putExtra("score", score);
        mContext.startActivity(gameOver);
    }

}


class Chara
{

    private Bitmap charaImage;
    private Rect charaSrc, charaDst;
    private Point p;
    private Paint paint;

    public int hitDistance1, hitDistance2;

    private int displayX,displayY;

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
        charaSrc = new Rect(0,0,charaImage.getWidth(),charaImage.getHeight());

        init(displayX,displayY);
    }

    private void init(int displayX, int displayY){
        p.x = displayX/2 - (sizeX)/2;
        p.y = displayY*2/3 - (sizeY)*3;
        charaDst = new Rect(p.x,p.y,p.x+sizeX,p.y+sizeY);
    }

    public void move(int x, int y){
        p.x = x - (sizeX)/2;
        p.y = y - (sizeY)/2;

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
        if(p.y + (sizeY) > displayY*2/3){
            p.y = displayY*2/3 - sizeY;
        }

        charaDst = new Rect(p.x,p.y,p.x+sizeX,p.y+sizeY);
    }

    public int getCenterX(){
        return p.x + (sizeX)/2;
    }

    public int getCenterY(){
        return p.y + (sizeY)/2;
    }

    public void drawMove(Canvas c){
        c.drawBitmap(charaImage, charaSrc, charaDst, paint);
    }

}


class CharaBeam
{

    private Point p;
    private int beamCenterX;
    private int beamCenterY;
    private Paint paint;

    public boolean CharaBeamFlag = false;
    public boolean isDead = true;

    private int scale;
    private int charaBeamSpeed;
    private int radius, displayX;

    CharaBeam(int displayX,int scale){
        this.displayX = displayX;
        this.scale = scale;
        p = new Point();
        paint = new Paint();
        paint.setColor(Color.BLACK);
        radius = displayX/80;
        charaBeamSpeed = 5*scale;
    }

    private void init(int w, int h){
        p.x = w;
        p.y = h;
    }

    public void move(){
        p.y -= charaBeamSpeed;
        beamCenterX = p.x + radius/2;
        beamCenterY = p.y + radius/2;

        if(p.y < 0){
            isDead = true;
            CharaBeamFlag = false;
        }
    }

    public void drawMove(Canvas c, int w, int h){
        if(isDead){
            init(w,h);
        }
        c.drawCircle(p.x, p.y, radius, paint);
    }

    public int getCenterX(){
        return beamCenterX;
    }
    public int getCenterY(){
        return beamCenterY;
    }
    public int getRadius() {return radius;}


}


class Enemy
{

    private Bitmap enemyImage;
    private Rect enemySrc, enemyDst;
    private Point p;
    private Paint paint;

    private int enemyCenterX;
    private int enemyCenterY;
    private int displayX, displayY;
    private int sizeX, sizeY;
    private int scale;

    double hitDistance;

    Enemy(Bitmap enemyImage, int w, int h, int displayX, int displayY, int sizeX, int sizeY, int scale){
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

        enemySrc = new Rect(0,0,enemyImage.getWidth(),enemyImage.getHeight());
        enemyDst = new Rect(p.x,p.y,p.x+sizeX,p.y+sizeY);
    }

    public void move(){
        //displayX 480 displayY800
        p.y += 4 * scale;
        enemyCenterX = p.x + (sizeX)/2;
        enemyCenterY = p.y + (sizeY)/2;
        enemyDst = new Rect(p.x,p.y,p.x+sizeX,p.y+sizeY);
    }

    public void drawMove(Canvas c){
        c.drawBitmap(enemyImage, enemySrc, enemyDst, paint);
    }

    public int getX(){
        return p.x;
    }

    public int getY(){
        return p.y;
    }

    public int getCenterX() {
        return  enemyCenterX;
    }

    public int getCenterY() { return  enemyCenterY; }

}


class EnemyBeam
{

    private Point p;
    private Paint paint;
    private int beamCenterX;
    private int beamCenterY;
    private double angle;
    private int scale, radius;


    EnemyBeam(int w, int h, int charaX, int charaY, int displayX, int scale){
        p = new Point();
        p.x = w;
        p.y = h;

        paint = new Paint();
        paint.setColor(Color.BLACK);

        angle = Math.atan2(charaY - h, charaX - w);
        this.scale = scale;
        radius = displayX/80;
    }

    public void move(){
        p.x += Math.cos(angle) * 4 * scale;
        p.y += Math.sin(angle) * 4 * scale;
        beamCenterX = p.x + radius/2;
        beamCenterY = p.y + radius/2;
    }

    public void drawMove(Canvas c) {
        c.drawCircle(p.x, p.y,radius, paint);
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


class Explotion
{

    private Bitmap explotionImage;

    public int exSwitch = 0;

    int dispX, dispY;
    int count;

    Rect[] srcs = new Rect[10];
    Rect dst;


    Explotion(Bitmap explotionImage,int X, int Y, int count){
        this.explotionImage = explotionImage;
        int h = explotionImage.getHeight();
        int w = explotionImage.getWidth() / 10;

        dispX = X - w + 20;
        dispY = Y - h;

        this.count = count;

        for(int i = 0; i < 10; i++){
            srcs[i] = new Rect(i * w, 0, w + i*w, h);
        }

        dst = new Rect(dispX, dispY, dispX + w*3/2, dispY + h*3/2);

    }

    public void drawMove(Canvas c){
        c.drawBitmap(explotionImage, srcs[exSwitch], dst, new Paint());
    }

}