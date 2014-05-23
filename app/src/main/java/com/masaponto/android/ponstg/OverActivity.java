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
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.lang.reflect.Method;

public class OverActivity extends Activity{

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        overrideGetSize(display, size);
        int displayX = size.x;
        int displayY = size.y;

        super.onCreate(savedInstanceState);
        String score = "" + getIntent().getIntExtra("score", 0);

        OverSurfaceView mSurfaceView = new OverSurfaceView(this, displayX, displayY, score);
        setContentView(mSurfaceView);
    }


    void overrideGetSize(Display display, Point outSize){
        try{
            // test for new method to trigger exception
            Class pointClass = Class.forName("android.graphics.Point");
            Method newGetSize = Display.class.getMethod("getSize", new Class[]{pointClass});
            // no exception, so new method is available, just use it
            newGetSize.invoke(display, outSize);
        }catch(Exception ex){
            // new method is not available, use the old ones
            outSize.x = display.getWidth();
            outSize.y = display.getHeight();
        }
    }

    public void onPause(){
        super.onPause();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    // 終了していいか、ダイアログで確認
                    showDialog(OverActivity.this, "Quit game", "Are you sure you want to quit?");
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    //ダイアログ
    private void showDialog(final Context context, String title, String text){
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        Intent title = new Intent(context, TitleActivity.class);
                        title.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        context.startActivity(title);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                    }
                })

                .show();
    }

}

class OverSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable{

    int displayX, displayY;
    String score;
    int nowScore;
    int highScore = 0;

    private boolean isRunning = true;
    private Thread thread;

    private Context mContext;

    Resources res = getResources();

    Bitmap homeImage = BitmapFactory.decodeResource(res, R.drawable.home);
    final Rect homeSrc = new Rect(0, 0, homeImage.getWidth(), homeImage.getHeight());
    int homeX1, homeX2, homeY1, homeY2;
    Rect homeDst, homeDst2;

    Bitmap retryImage = BitmapFactory.decodeResource(res, R.drawable.retry);
    final Rect retrySrc = new Rect(0, 0, retryImage.getWidth(), retryImage.getHeight());
    int retryX1, retryX2, retryY1, retryY2;
    Rect retryDst, retryDst2;

    Bitmap twitImage = BitmapFactory.decodeResource(res, R.drawable.twit);
    final Rect twitSrc = new Rect(0, 0, twitImage.getWidth(), twitImage.getHeight());
    int twitX1, twitX2, twitY1, twitY2;
    Rect twitDst, twitDst2;

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

        homeX1 = displayX / 16;
        homeY1 = displayY * 2 / 3;
        homeX2 = displayX * 5 / 16;
        homeY2 = displayY * 2 / 3 + displayX * 1 / 4;
        homeDst = new Rect(homeX1, homeY1, homeX2, homeY2);
        homeDst2 = new Rect(homeX1, homeY1 + displayX / 100, homeX2, homeY2 + displayX / 100);

        retryX1 = displayX * 6 / 16;
        retryY1 = displayY * 2 / 3;
        retryX2 = displayX * 10 / 16;
        retryY2 = displayY * 2 / 3 + displayX * 1 / 4;
        retryDst = new Rect(retryX1, retryY1, retryX2, retryY2);
        retryDst2 = new Rect(retryX1, retryY1 + displayX / 100, retryX2, retryY2 + displayX / 100);

        twitX1 = displayX * 11 / 16;
        twitY1 = displayY * 2 / 3;
        twitX2 = displayX * 15 / 16;
        twitY2 = displayY * 2 / 3 + displayX * 1 / 4;
        twitDst = new Rect(twitX1, twitY1, twitX2, twitY2);
        twitDst2 = new Rect(twitX1, twitY1 + displayX / 100, twitX2, twitY2 + displayX / 100);

        //  typeface = Typeface.createFromAsset(getContext().getAssets(), "Pigmo-00_pilot.ttf");

        setFocusable(true);
        getHolder().addCallback(this);
        calcHighScore(context, score);

    }


    public void calcHighScore(Context context, String score){
        nowScore = Integer.parseInt(score);
        highScore = loadHighScore(context);

        if(nowScore > highScore){
            saveHighScore(context, nowScore);
        }

        highScore = loadHighScore(context);
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

        }

        if(event.getAction() == MotionEvent.ACTION_UP){

            homePushFlag = false;
            retryPushFlag = false;
            twitPushFlag = false;

            if(homeX1 < x && x < homeX2 && homeY1 < y && y < homeY2){
                Intent title = new Intent(getContext(), TitleActivity.class);
                isRunning = false;
                title.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(title);
            }

            if(retryX1 < x && x < retryX2 && retryY1 < y && y < retryY2){
                Intent mainIntent = new Intent(getContext(), MainActivity.class);
                isRunning = false;
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(mainIntent);
            }

            if(twitX1 < x && x < twitX2 && twitY1 < y && y < twitY2){
                try{
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_TEXT, "PLAYED PON-SHOOTING!\nSCORE:" + score + " #PON_SHOOTING");
                    intent.setType("text/plain");
                    mContext.startActivity(intent);

                }catch(Exception e){
                    Uri uri = Uri.parse("market://search?q=com.twitter.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
                    mContext.startActivity(marketIntent);
                }
            }
        }


        return true;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height){
    }

    public void surfaceCreated(SurfaceHolder holder){

        if(thread == null || thread.getState() == Thread.State.TERMINATED){
            thread = new Thread(this);
            thread.start();
        }else{
            thread.start();
        }
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

        while(isRunning){
            Canvas canvas = getHolder().lockCanvas();
            draw(canvas, mPaint);
            getHolder().unlockCanvasAndPost(canvas);
        }

        try{
            Thread.sleep(10);//お決まり
        }catch(Exception e){
        }

    }

    public void draw(Canvas canvas, Paint mPaint){

        canvas.drawColor(Color.WHITE);
        mPaint.setColor(Color.WHITE);
        // mPaint.setTypeface(typeface);
        mPaint.setColor(Color.BLACK);

        String overText = "GameOver";
        mPaint.setTextSize(60 * scale);
        float textWidth = mPaint.measureText(overText);
        float dispX = displayX / 2 - textWidth/2;
        canvas.drawText(overText, dispX, displayY / 4, mPaint);

        mPaint.setTextSize(40 * scale);
        canvas.drawText("SCORE:" + score, dispX, displayY / 3 + (45 * scale), mPaint);
        canvas.drawText("HIGHSCORE:" + highScore, dispX, displayY / 3 + (90 * scale), mPaint);

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
        mPaint.setColor(Color.BLACK);

    }

    public void saveHighScore(Context mcontext, int score){

        SharedPreferences pref = mcontext.getSharedPreferences("HighScore", Context.MODE_PRIVATE);

        // プリファレンスに書き込むためのEditorオブジェクト取得 //
        SharedPreferences.Editor editor = pref.edit();

        // "user_name" というキーで名前を登録
        editor.putInt("HighScore", score);

        // 書き込みの確定（実際にファイルに書き込む）
        editor.commit();

    }


    public int loadHighScore(Context context){
        // プリファレンスの準備 //
        SharedPreferences pref = context.getSharedPreferences("HighScore", Context.MODE_PRIVATE);

        return pref.getInt("HighScore", 0);
    }


}
