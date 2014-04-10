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
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;

import java.util.ArrayList;
import java.util.List;

public class OverActivity extends Activity{

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int displayX = display.getWidth();
        int displayY = display.getHeight();

        super.onCreate(savedInstanceState);
        String score = "" + getIntent().getIntExtra("score", 0);
        OverSurfaceView mSurfaceView = new OverSurfaceView( this, displayX, displayY, score);
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
                    showDialog(OverActivity.this,"Quit game","Are you sure you want to quit?");
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

class OverSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    int displayX, displayY;
    String score;

    private boolean isRunning = true;
    private Thread thread;

    private Context mContext;

    Resources res = getResources();

    Bitmap homeImage = BitmapFactory.decodeResource(res,R.drawable.home);
    final Rect homeSrc = new Rect(0,0,homeImage.getWidth(),homeImage.getHeight());
    int homeX1,homeX2,homeY1,homeY2;
    Rect homeDst,homeDst2;

    Bitmap retryImage = BitmapFactory.decodeResource(res,R.drawable.retry);
    final Rect retrySrc = new Rect(0,0,retryImage.getWidth(),retryImage.getHeight());
    int retryX1,retryX2,retryY1,retryY2;
    Rect retryDst,retryDst2;

    Bitmap twitImage = BitmapFactory.decodeResource(res,R.drawable.twit);
    final Rect twitSrc = new Rect(0,0,twitImage.getWidth(),twitImage.getHeight());
    int twitX1,twitX2,twitY1,twitY2;
    Rect twitDst,twitDst2;

    boolean homePushFlag = false;
    boolean retryPushFlag = false;
    boolean twitPushFlag = false;

    int scale;

    Typeface typeface;

    public OverSurfaceView(Context context, int x, int y, String score){
        super(context);
        mContext = context;

        this.displayX = x;
        this.displayY = y;
        this.score = score;

        scale = displayX / 480;

        homeX1 = displayX/16;
        homeY1 = displayY*2/3;
        homeX2 = displayX*5/16;
        homeY2 = displayY*2/3 + displayX*1/4;
        homeDst = new Rect( homeX1, homeY1, homeX2, homeY2);
        homeDst2 = new Rect( homeX1, homeY1 + displayX/100, homeX2, homeY2 + displayX/100);

        retryX1 = displayX*6/16;
        retryY1 = displayY*2/3;
        retryX2 = displayX*10/16;
        retryY2 = displayY*2/3 + displayX*1/4;
        retryDst = new Rect( retryX1, retryY1, retryX2, retryY2);
        retryDst2 = new Rect( retryX1, retryY1 + displayX/100, retryX2, retryY2 + displayX/100);

        twitX1 = displayX*11/16;
        twitY1 = displayY*2/3;
        twitX2 = displayX*15/16;
        twitY2 = displayY*2/3 + displayX*1/4;
        twitDst = new Rect( twitX1, twitY1, twitX2, twitY2);
        twitDst2 = new Rect( twitX1, twitY1 + displayX/100, twitX2, twitY2 + displayX/100);

      //  typeface = Typeface.createFromAsset(getContext().getAssets(), "Pigmo-00_pilot.ttf");

        setFocusable(true);
        getHolder().addCallback(this);
    }

    public boolean onTouchEvent(MotionEvent event){
        super.onTouchEvent(event);

        int x = (int) event.getX();
        int y = (int) event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN){

            if(homeX1 < x && x < homeX2 && homeY1 < y && y < homeY2){
                homePushFlag = true;
            }

            if(retryX1 < x && x < retryX2 && retryY1 < y && y < retryY2){
                retryPushFlag = true;
            }

            if(twitX1 < x && x < twitX2 && twitY1 < y && y < twitY2){
                twitPushFlag = true;
            }


            //Log.v("tag", "y:" + y + "x:" + x + " homeR:" + homeR + " distance:" + homeDistance);

        }

        if(event.getAction() == MotionEvent.ACTION_UP){

            homePushFlag = false;
            retryPushFlag = false;
            twitPushFlag = false;

            if(homeX1 < x && x < homeX2 && homeY1< y && y < homeY2){
                Intent title = new Intent(getContext(),TitleActivity.class);
                isRunning = false;
                //thread = null;
                mContext.startActivity(title);
            }

            if(retryX1 < x && x < retryX2 && retryY1< y && y < retryY2){
                Intent mainIntent = new Intent(getContext(),MainActivity.class);
                isRunning = false;
                //thread = null;
                mContext.startActivity(mainIntent);
            }

            if(twitX1 < x && x < twitX2 && twitY1< y && y < twitY2){

            }
        }


        return true;
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height ){
    }

    public void surfaceCreated(SurfaceHolder holder){
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

    public void run(){
        Paint mPaint = new Paint();

        while(isRunning){
            Canvas canvas = getHolder().lockCanvas();
            draw(canvas, mPaint);
            getHolder().unlockCanvasAndPost(canvas);
        }

        try {
            Thread.sleep(10);//お決まり
        } catch (Exception e){}

    }

    public void draw(Canvas canvas, Paint mPaint){

        canvas.drawColor(Color.WHITE);
        mPaint.setColor(Color.WHITE);
       // mPaint.setTypeface(typeface);
        mPaint.setColor(Color .BLACK);

        mPaint.setTextSize(60 * scale);
        canvas.drawText("GameOver", displayX/4, displayY/2 - 70 * scale, mPaint);

        mPaint.setTextSize(40 * scale);
        canvas.drawText("SCORE:" + score, displayX/4 , displayY/2, mPaint);

        if(homePushFlag){
            canvas.drawBitmap(homeImage, homeSrc, homeDst2, mPaint);
        }else{
            canvas.drawBitmap(homeImage, homeSrc, homeDst, mPaint);
        }

        if(retryPushFlag){
            canvas.drawBitmap(retryImage, retrySrc, retryDst2, mPaint);
        }else{
            canvas.drawBitmap(retryImage, retrySrc, retryDst, mPaint);
        }

        if(twitPushFlag){
            canvas.drawBitmap(twitImage, twitSrc, twitDst2, mPaint);
        }else{
            canvas.drawBitmap(twitImage, twitSrc, twitDst, mPaint);
        }


        mPaint.setTextSize(30 * scale);
        mPaint.setColor(Color .BLACK);

    }

}
