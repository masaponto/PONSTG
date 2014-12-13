package com.masaponto.android.ponstg;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import java.lang.reflect.Method;


public class MainActivity extends Activity{

    private MySurfaceView mSurfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        overrideGetSize(display, size);
        int displayX = size.x;
        int displayY = size.y;

        mSurfaceView = new MySurfaceView(this, displayX, displayY);
        setContentView(mSurfaceView);
    }


    //画面の大きさ取得
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
        if(!mSurfaceView.hitFlag){
            mSurfaceView.pauseFlag = true;
        }
    }

}

