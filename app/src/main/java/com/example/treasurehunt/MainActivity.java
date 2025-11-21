package com.example.treasurehunt;

import android.app.AlertDialog;
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

        btnStartGame.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        btnHowToPlay.setOnClickListener(v -> showHowToPlayPopup());
    }

    private void playScanningAnimation() {
        Animation fade = new AlphaAnimation(0.2f, 1f);
        fade.setDuration(900);
        fade.setRepeatCount(Animation.INFINITE);
        fade.setRepeatMode(Animation.REVERSE);
        scanningText.startAnimation(fade);
    }

    private void showHowToPlayPopup() {
        new AlertDialog.Builder(this)
                .setTitle("HOW TO PLAY")
                .setMessage(
                        "1. Tekan START GAME.\n" +
                                "2. Kamu akan diarahkan ke MAP.\n" +
                                "3. Bergeraklah mendekati lokasi harta.\n" +
                                "4. Jika jarak kamu < 30 meter – kamu menang!\n\n" +
                                "// TIP:\n" +
                                "- Aktifkan GPS.\n" +
                                "- Bergerak secara fisik di dunia nyata.\n" +
                                "- Hati-hati di jalan :)"
                )
                .setPositiveButton("OK", null)
                .show();
    }
}