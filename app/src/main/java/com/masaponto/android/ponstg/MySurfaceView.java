package com.masaponto.android.ponstg;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Masato on 2014/12/13.
 */

class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    //画面の大きさ
    int displayX, displayY;

    //スコア
    private int score = 0;

    //自機オブジェクト
    private Chara chara;

    //ビームの数
    private final int N = 20;

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

    private boolean isRunning;
    private Thread thread;
    private Context mContext;

    //charaが敵にやられたかの判定
    protected boolean hitFlag = false;

    //pause
    protected boolean pauseFlag = false;

    //touchされたか
    boolean touchFlag = false;

    private int count;
    private int overTime;

    //touchされた場所
    private int touchX, touchX2;
    private int touchY, touchY2;

    //charaの中心座標
    int charaCenterX;
    int charaCenterY;

    Resources res = this.getContext().getResources();

    //画像
    Bitmap charaImage = BitmapFactory.decodeResource(res, R.drawable.chara);

    Bitmap enemyImage = BitmapFactory.decodeResource(res, R.drawable.enemy);
    Bitmap[] enemyImageArray;
    Matrix enemyMatrix;

    Bitmap explotionImage = BitmapFactory.decodeResource(res, R.drawable.explotion);

    Bitmap pauseImage = BitmapFactory.decodeResource(res, R.drawable.pause);
    final Rect pauseSrc = new Rect(0, 0, pauseImage.getWidth(), pauseImage.getHeight());
    int pauseX1, pauseX2, pauseY1, pauseY2;
    Rect pauseDst, pauseDst2;
    boolean pausePushFlag = false;

    Bitmap playImage = BitmapFactory.decodeResource(res, R.drawable.play);
    final Rect playSrc = new Rect(0, 0, playImage.getWidth(), playImage.getHeight());
    boolean playPushFlag = false;

    //音
    SoundPool sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
    int beamSoundId, hitSoundId;

    //画面の比率
    int scale;

    static final long FPS = 60;
    static final long FRAME_TIME = 1000 / FPS;

    public MySurfaceView(Context context, int x, int y){
        super(context);
        mContext = context;

        // for key event
        this.requestFocus();
        this.setFocusableInTouchMode(true);

        setFocusable(true);
        getHolder().addCallback(this);

        //音
        beamSoundId = sp.load(context, R.raw.beamsound, 1);
        hitSoundId = sp.load(context, R.raw.hitsound, 1);

        //画面サイズ
        displayX = x;
        displayY = y;

        //ポーズボタンの画像の描画サイズ
        pauseX1 = displayX - displayX / 5;
        pauseY1 = 0;
        pauseX2 = displayX;
        pauseY2 = displayX / 5;
        pauseDst = new Rect(pauseX1, pauseY1, pauseX2, pauseY2);
        pauseDst2 = new Rect(pauseX1, pauseY1 + displayX / 100, pauseX2, pauseY2 + displayX / 100);

        //画面の割合を横480を基準にする
        scale = displayX / 480;

        charaImage = Bitmap.createScaledBitmap(charaImage, displayX/11, displayY/11, true);
        enemyImage = Bitmap.createScaledBitmap(enemyImage, displayX/11, displayX/11, true);

        enemyImageArray = new Bitmap[4];
        enemyMatrix = new Matrix();
        for(int i = 0; i < 4; i++){
            enemyMatrix.setRotate(22.5F * (float) i, enemyImage.getWidth() / 2, enemyImage.getHeight() / 2);
            enemyImageArray[i] = Bitmap.createBitmap(enemyImage, 0, 0, enemyImage.getWidth(), enemyImage.getHeight(), enemyMatrix, true);
        }


    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    }

    public void surfaceCreated(SurfaceHolder holder){

        chara = new Chara(charaImage, displayX, displayY);

        charaBeam = new CharaBeam[N];

        for (int i = 0; i < N; i++){
            charaBeam[i] = new CharaBeam(displayX, scale);
        }

        isRunning = true;

        if (thread == null || thread.getState() == Thread.State.TERMINATED) {
            thread = new Thread(this);
            thread.start();
        } else {
            thread.start();
        }


    }

    public void surfaceDestroyed(SurfaceHolder holder){

        sp.release();
        isRunning = false;
        try{
            thread.join();
        }catch(InterruptedException ex){}
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

                if(pauseX1 < touchX && touchX < pauseX2 && pauseY1 < touchY && touchY < pauseY2){
                    pausePushFlag = true;
                    playPushFlag = true;
                }

                else{

                    if(!pauseFlag){
                        touchFlag = true;
                        sp.play(beamSoundId, 1.0F, 1.0F, 0, 0, 1.0F);
                    }

                }

            }


            if(action == MotionEvent.ACTION_MOVE){

                touchX2 = (int) event.getX();
                touchY2 = (int) event.getY();

                if(!pauseFlag){
                    chara.move(charaCenterX - (touchX - touchX2), charaCenterY - (touchY - touchY2));
                }

            }

            if(action == MotionEvent.ACTION_UP){

                pausePushFlag = false;
                playPushFlag = false;
                touchFlag = false;

                if (pauseFlag) {
                    if (pauseX1 < touchX2 && touchX2 < pauseX2 && pauseY1 < touchY2 && touchY2 < pauseY2) {
                        pauseFlag = false;
                    }
                }
                else {
                    if (pauseX1 < touchX2 && touchX2 < pauseX2 && pauseY1 < touchY2 && touchY2 < pauseY2) {
                        pauseFlag = true;
                        showDialog("Paused");
                    }
                }

            }

        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        super.onKeyDown(keyCode, event);
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                pauseFlag = true;
                showDialog("Paused");
                break;
        }
        return true;
    }

    //ダイアログ
    public void showDialog(String title){

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle(title);

        alertDialog.setPositiveButton("Resume", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                // Resumeボタン押下時の処理
                Log.d("AlertDialog", "Neutral which :" + which);
                pauseFlag = false;
            }
        });


        alertDialog.setNegativeButton("Exit", new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialog, int which){
                // Exitボタン押下時の処理
                Log.d("AlertDialog", "Negative which :" + which);
                Intent titleIntent = new Intent(getContext(), TitleActivity.class);
                titleIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                isRunning = false;
                mContext.startActivity(titleIntent);
            }
        });

        alertDialog.show();

    }


    public void run(){

        Paint mPaint = new Paint();
        mPaint.setFilterBitmap(true);

        count = 50 * scale;
        overTime = 0;

        long loopCount = 0;
        long waitTime = 0;
        long startTime = System.currentTimeMillis();

        int enemyMoveSpeed, enemyIncidence;
        int enemyBeamMoveSpeed, enemyBeamIncidence;

        final int explotionSpeed = 5;

        final int enemyR = displayX / 11;
        final int charaR = displayX / 20;

        boolean overFlag = false;


        while(isRunning){

            Canvas canvas = getHolder().lockCanvas();

            if(canvas != null){

                if(!pauseFlag){

                    count++;

                    if(!hitFlag){

                        enemyMoveSpeed = ((score / 15) + 4) * scale;
                        enemyIncidence = ( 60 - (score / 15) * 9);
                        if(enemyIncidence < 10){
                            enemyIncidence = 10;
                        }

                        addEnemy(count, enemyMoveSpeed, enemyIncidence);

                        enemyBeamMoveSpeed = ( (score / 15) + 4) * scale;
                        enemyBeamIncidence = ( 70 - (score / 15) * 7 ) * scale;
                        if(enemyBeamIncidence < 10){
                            enemyBeamIncidence = 10;
                        }

                        addEnemyBeam(count, enemyBeamIncidence, enemyBeamMoveSpeed);

                    }

                    addBeam(count);

                    overTime = moveEnemy(charaR, overTime, count);
                    overTime = moveEnemyBeam(charaR, overTime, count);
                    moveCharaBeam(enemyR, count);

                    removeEnemy();
                    removeEnemyBeam();

                    doExplotion(explotionSpeed, count);

                }

                drawGame(canvas, mPaint);

                getHolder().unlockCanvasAndPost(canvas);

                if(overTime != 0 && overTime + 100 < count){
                    overFlag = true;
                }

                if(overFlag){
                    isRunning = false;
                    gameOver();
                }

            }

            waitTime = (loopCount * FRAME_TIME) - (System.currentTimeMillis() - startTime);

            try{
                if(waitTime > 0){
                    Thread.sleep(waitTime);
                }
            }
            catch (Exception e) {
            }

        }

    }


    private void addBeam(int count){
        //beamを出す
        if(touchFlag && count % 15 == 0){
            charaBeam[beamCount].CharaBeamFlag = true;
            beamCount = (beamCount + 1) % N;
        }
    }


    private void addEnemy(int count, int moveSpeed, int incidence){
        if(count % (incidence / scale) == 0){
            Random rnd = new Random();
            int ran = rnd.nextInt(displayX - (displayX / 11));
            enemys.add(new Enemy(enemyImageArray, ran, 0, displayX, displayY, scale, moveSpeed));
        }
    }


    private void addEnemyBeam(int count, int incidence, int moveSpeed){
        if(count % incidence == 0 && !enemys.isEmpty()){
            for(int i = 0; i < enemys.size(); i++){
                enemyBeams.add(new EnemyBeam(enemys.get(i).getCenterX()
                        , enemys.get(i).getCenterY(), chara.getCenterX(), chara.getCenterY(), displayX, scale, moveSpeed));
            }
        }
    }


    private void removeEnemy(){
        for(int i = 0; i < enemys.size(); i++){
            if(enemys.get(i).deathFlag == true){
                enemys.remove(i);
            }
        }
    }


    private int moveEnemy(int charaR, int overTime, int count){

        for(int i = 0; i < enemys.size(); i++){

            if(!hitFlag){
                enemys.get(i).move();
            }

            chara.hitDistance1 = (int) Math.sqrt(Math.pow(chara.getCenterX() - enemys.get(i).getCenterX(), 2)
                    + Math.pow(chara.getCenterY() - enemys.get(i).getCenterY(), 2));

            if(chara.hitDistance1 < charaR){

                hitFlag = true;

                if(overTime == 0){
                    overTime = count;
                }

                explotions.add(new Explotion(explotionImage, chara.getCenterX()
                        , chara.getCenterY(), count, displayX));

                explotions.add(new Explotion(explotionImage, enemys.get(i).getCenterX()
                        , enemys.get(i).getCenterY(), count, displayX));

                enemys.get(i).deathFlag = true;

                sp.play(hitSoundId, 1.0F, 1.0F, 0, 0, 1.0F);

            }

            //画面外に出たらリストから消す
            if(enemys.get(i).getY() > displayY * 3/4 || enemys.get(i).getCenterX() > displayX || enemys.get(i).getCenterX() < 0){
                enemys.get(i).deathFlag = true;
            }

        }

        return overTime;

    }


    private int moveEnemyBeam(int charaR, int overTime, int count){

        for(int i = 0; i < enemyBeams.size(); i++){

            if(!hitFlag){
                enemyBeams.get(i).move();
            }

            chara.hitDistance2 = (int) Math.sqrt(Math.pow(chara.getCenterX() - enemyBeams.get(i).getCenterX(), 2)
                    + Math.pow(chara.getCenterY() - enemyBeams.get(i).getCenterY(), 2));

            if(enemyBeams.get(i).getY() > displayY * 3/4 || enemyBeams.get(i).getY() < 0
                    || enemyBeams.get(i).getX() < 0 || enemyBeams.get(i).getX() > displayX){
                enemyBeams.get(i).deathFlag = true;
            }

            if(chara.hitDistance2 < charaR){

                hitFlag = true;

                if(overTime == 0){
                    overTime = count;
                }

                explotions.add(new Explotion(explotionImage, chara.getCenterX()
                        , chara.getCenterY(), count, displayX));

                enemyBeams.get(i).deathFlag = true;

                sp.play(hitSoundId, 1.0F, 1.0F, 0, 0, 1.0F);

            }

        }

        return overTime;

    }


    private void removeEnemyBeam(){
        for(int i = 0; i < enemyBeams.size(); i++){
            if(enemyBeams.get(i).deathFlag){
                enemyBeams.remove(i);
            }
        }
    }


    public void moveCharaBeam(int enemyR, int count){

        for(int i = 0; i < N; i++){

            if(charaBeam[i].CharaBeamFlag){
                charaBeam[i].isDead = false;

                if(!hitFlag){
                    charaBeam[i].move();
                }

                for(int j = 0; j < enemys.size(); j++){

                    enemys.get(j).hitDistance = Math.sqrt(Math.pow((enemys.get(j).getCenterX() - charaBeam[i].getCenterX()), 2)
                            + Math.pow((enemys.get(j).getCenterY() - charaBeam[i].getCenterY()), 2));

                    //ビームの当たり判定
                    if(enemys.get(j).hitDistance < enemyR){

                        score++;

                        explotions.add(new Explotion(explotionImage, enemys.get(j).getCenterX()
                                , enemys.get(j).getCenterY(), count, displayX));

                        charaBeam[i].CharaBeamFlag = false;
                        charaBeam[i].isDead = true;

                        enemys.get(j).deathFlag = true;

                        sp.play(hitSoundId, 1.0F, 1.0F, 0, 0, 1.0F);

                    }

                }

            }

        }

    }


    //爆発処理
    public void doExplotion(int explotionSpeed, int count){

        for(int i = 0; i < explotions.size(); i++){

            for(int j = 1; j < 10; j++){
                if(explotions.get(i).count + explotionSpeed * j < count){
                    explotions.get(i).exSwitch = j;
                }
            }

            if(explotions.get(i).count + explotionSpeed * 10 < count){
                explotions.remove(i);
            }

        }

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

        if(!hitFlag){

            for(int i = 0; i < N; i++){
                charaBeam[i].drawMove(canvas, chara.getCenterX()
                        , chara.getCenterY() - (displayX / 11) / 2 + charaBeam[i].getRadius());
            }

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
        canvas.drawText("SCORE:" + score, 0, 50 * scale, mPaint);

        canvas.drawRect(0, displayY * 3/4, displayX, displayY, mPaint);

    }

    private void gameOver(){
        //GameOver画面へ
        Intent gameOver = new Intent(getContext(), OverActivity.class);
        gameOver.putExtra("score", score);
        gameOver.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(gameOver);

    }

}
