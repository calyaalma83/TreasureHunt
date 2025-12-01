package com.example.treasurehunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView scanningText;
    private Button btnStartGame, btnHowToPlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanningText = findViewById(R.id.scanningText);
        btnStartGame = findViewById(R.id.btnStartGame);
        btnHowToPlay = findViewById(R.id.btnHowToPlay);

        playScanningAnimation();

        // Tombol Start Game -> MapActivity
        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        // Tombol How To Play -> HowToPlayActivity (Fragment)
        btnHowToPlay.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HowToPlayActivity.class);
            startActivity(intent);
        });
    }

    private void playScanningAnimation() {
        Animation fade = new AlphaAnimation(0.2f, 1f);
        fade.setDuration(900);
        fade.setRepeatCount(Animation.INFINITE);
        fade.setRepeatMode(Animation.REVERSE);
        scanningText.startAnimation(fade);
    }
}