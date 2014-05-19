package com.masaponto.android.ponstg;

import android.app.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.view.Display;

public class TitleActivity extends Activity{

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int displayX = display.getWidth();
        int displayY = display.getHeight();

        super.onCreate(savedInstanceState);
        TitleSurfaceView mSurfaceView = new TitleSurfaceView(this, displayX, displayY);
        setContentView(mSurfaceView);
    }

    public void onPause(){
        super.onPause();
        finish();
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){

            if(event.getKeyCode() == KeyEvent.KEYCODE_BACK){
                // 終了していいか、ダイアログで確認
                showDialog(TitleActivity.this, "Quit game", "Are you sure you want to quit?");
                return true;
            }

            if(event.getKeyCode() == KeyEvent.KEYCODE_HOME){
                // 終了していいか、ダイアログで確認
                showDialog(TitleActivity.this, "Quit game", "Are you sure you want to quit?");
                return true;
            }


        }
        return super.dispatchKeyEvent(event);
    }


    //ダイアログ
    private void showDialog(Context context, String title, String text){
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        finish(); //終了
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                    }
                })

                .show();
    }

}

class TitleSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    int displayX, displayY;

    private boolean isRunning = true;
    private Thread thread;

    private Context mContext;
    Resources res = getResources();

    Bitmap playImage = BitmapFactory.decodeResource(res, R.drawable.play);
    final Rect playSrc = new Rect(0, 0, playImage.getWidth(), playImage.getHeight());
    int playX1, playX2, playY1, playY2;
    Rect playDst, playDst2;

    boolean playPushFlag = false;

    //Typeface typeface;

    int highScore;

    int scale;

    public TitleSurfaceView(Context context, int x, int y){
        super(context);

        mContext = context;

        this.displayX = x;
        this.displayY = y;

        scale = displayX / 480;

        playX1 = displayX / 2 - displayX/5;
        playY1 = displayY * 1 / 2;
        playX2 = displayX / 2 + displayX/5;
        playY2 = displayY * 1 / 2 + displayX * 2 / 5;
        playDst = new Rect(playX1, playY1, playX2, playY2);
        playDst2 = new Rect(playX1, playY1 + displayX / 100, playX2, playY2 + displayX / 100);

        //typeface = Typeface.createFromAsset(getContext().getAssets(), "Pigmo-00_pilot.ttf");

        highScore = loadHighScore(context);

        setFocusable(true);
        getHolder().addCallback(this);
    }

    public boolean onTouchEvent(MotionEvent event){
        super.onTouchEvent(event);

        int x = (int) event.getX();
        int y = (int) event.getY();

        if(event.getAction() == MotionEvent.ACTION_DOWN){

            if(playX1 < x && x < playX2 && playY1 < y && y < playY2){
                playPushFlag = true;
            }

        }

        if(event.getAction() == MotionEvent.ACTION_UP){

            playPushFlag = false;

            if(playX1 < x && x < playX2 && playY1 < y && y < playY2){
                Intent mainIntent = new Intent(getContext(), MainActivity.class);
                isRunning = false;
                mContext.startActivity(mainIntent);
            }
        }

        return true;
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    }

    public void surfaceCreated(SurfaceHolder holder){
        isRunning = true;
        thread = new Thread(this);
        thread.start();
    }

    public void surfaceDestroyed(SurfaceHolder holder){
        isRunning = false;
        try{
            thread.join();
        }catch(InterruptedException ex){
        }
        thread = null;
    }

    public void run(){
        Paint mPaint = new Paint();

        int count = 50;

        while(isRunning){

            Canvas canvas = getHolder().lockCanvas();
            count++;

            draw(canvas, mPaint, count);
            getHolder().unlockCanvasAndPost(canvas);

        }

        try{
            Thread.sleep(10);//お決まり
        }catch(Exception e){
        }

    }

    public void draw(Canvas canvas, Paint mPaint, int count){

        canvas.drawColor(Color.WHITE);
        mPaint.setTextSize(30 * scale);
        mPaint.setColor(Color.BLACK);

        //mPaint.setTypeface(typeface);

        if(playPushFlag){
            canvas.drawBitmap(playImage, playSrc, playDst2, mPaint);
        }else{
            canvas.drawBitmap(playImage, playSrc, playDst, mPaint);
        }

        mPaint.setTextSize(50 * scale);
        String titleText = "SHOOTING";
        float textWidth = mPaint.measureText(titleText);
        float dispX = displayX / 2 - textWidth/2;
        canvas.drawText("PON", dispX, displayY / 3 - (52 * scale), mPaint);
        canvas.drawText("SHOOTING", dispX, displayY / 3 , mPaint);
        mPaint.setTextSize(30 * scale);
        canvas.drawText("HIGHSCORE:" + highScore, dispX, displayY / 3 + (35 * scale), mPaint);

        canvas.drawText("developed by masaponto", 0, displayY - (50 * scale), mPaint);
    }

    public int loadHighScore(Context context){
        // プリファレンスの準備 //
        SharedPreferences pref = context.getSharedPreferences("HighScore", Context.MODE_WORLD_READABLE);

        return pref.getInt("HighScore", 0);
    }

}