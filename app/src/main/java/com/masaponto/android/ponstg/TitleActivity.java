package com.masaponto.android.ponstg;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.concurrent.TimeoutException;


public class TitleActivity extends Activity{

    private TextView highScore;
    private ImageButton mStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.title);

        highScore =(TextView)this.findViewById(R.id.title_hightscore);
        highScore.setText(""+loadHighScore(this));

        mStartButton = (ImageButton)this.findViewById(R.id.title_start_button);
        mStartButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent mainIntent = new Intent(v.getContext(), MainActivity.class);
                mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //mainIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(mainIntent);
            }
        });
    }

    protected int loadHighScore(Context context){
        SharedPreferences pref = context.getSharedPreferences("HighScore", Context.MODE_PRIVATE);
        return pref.getInt("HighScore", 0);
    }
}
