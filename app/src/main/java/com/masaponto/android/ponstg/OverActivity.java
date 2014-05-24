package com.masaponto.android.ponstg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;


public class OverActivity extends Activity{

    private TextView highScoreText, scoreText;
    private ImageButton mHomeButton, mRetryButton, mTwitButton;
    private String score;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.over);

        score = "" + getIntent().getIntExtra("score", 0);

        scoreText = (TextView)this.findViewById(R.id.score);
        scoreText.setText(""+score);

        calcHighScore(this, score);

        highScoreText = (TextView)this.findViewById(R.id.over_hightscore);
        highScoreText.setText(""+loadHighScore(this));

        mHomeButton = (ImageButton)this.findViewById(R.id.over_home_button);
        mHomeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent titleIntent = new Intent(v.getContext(), TitleActivity.class);
                titleIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(titleIntent);
            }
        });


        mRetryButton = (ImageButton)this.findViewById(R.id.over_retry_Button);
        mRetryButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent mainIntent = new Intent(v.getContext(), MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(mainIntent);
            }
        });


        mTwitButton = (ImageButton)this.findViewById(R.id.over_twit_button);
        mTwitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                try{
                    Intent twitIntent = new Intent(Intent.ACTION_SEND);
                    twitIntent.putExtra(Intent.EXTRA_TEXT, "PLAYED PON-SHOOTING!\nSCORE:" + score + " #PON_SHOOTING");
                    twitIntent.setType("text/plain");
                    startActivity(twitIntent);

                }catch(Exception e){
                    Uri uri = Uri.parse("market://search?q=com.twitter.android");
                    Intent marketIntent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(marketIntent);
                }
            }
        });

    }

    public void saveHighScore(Context mcontext, int score){
        SharedPreferences pref = mcontext.getSharedPreferences("HighScore", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("HighScore", score);
        editor.commit();
    }

    protected int loadHighScore(Context context){
        SharedPreferences pref = context.getSharedPreferences("HighScore", Context.MODE_PRIVATE);
        return pref.getInt("HighScore", 0);
    }

    protected void calcHighScore(Context context, String score){
        final int nowScore = Integer.parseInt(score);
        final int highScore = loadHighScore(context);

        if(nowScore > highScore){
            saveHighScore(context, nowScore);
        }

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event){
        if(event.getAction() == KeyEvent.ACTION_DOWN){
            switch(event.getKeyCode()){
                case KeyEvent.KEYCODE_BACK:
                    // 終了していいか、ダイアログで確認
                    showDialog(this, "Quit game", "Are you sure you want to quit?");
                    return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }


    //ダイアログ
    protected void showDialog(final Context context, String title, String text){
        AlertDialog ad = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){
                        Intent title = new Intent(context, TitleActivity.class);
                        title.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(title);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int whichButton){

                    }
                })

                .show();
    }


}

