package com.example.treasurehunt;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class FinishActivity extends AppCompatActivity {

    private TextView trophyIcon, tvTotalCheckpoints, tvTotalScore, tvTimeTaken, tvMotivation;
    private Button btnPlayAgain, btnBackToMenu, btnShare;

    private int totalCheckpoints;
    private int totalScore;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish);

        initViews();
        getIntentData();
        displayStats();
        setupAnimations();
        setupClickListeners();
    }

    private void initViews() {
        trophyIcon = findViewById(R.id.trophyIcon);
        tvTotalCheckpoints = findViewById(R.id.tvTotalCheckpoints);
        tvTotalScore = findViewById(R.id.tvTotalScore);
        tvTimeTaken = findViewById(R.id.tvTimeTaken);
        tvMotivation = findViewById(R.id.tvMotivation);
        btnPlayAgain = findViewById(R.id.btnPlayAgain);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
        btnShare = findViewById(R.id.btnShare);
    }

    private void getIntentData() {
        Intent intent = getIntent();
        totalCheckpoints = intent.getIntExtra("totalCheckpoints", 5);
        totalScore = intent.getIntExtra("totalScore", 0);
        startTime = intent.getLongExtra("startTime", 0);
    }

    private void displayStats() {
        // Tampilkan total checkpoint
        tvTotalCheckpoints.setText(String.valueOf(totalCheckpoints));

        // Tampilkan total skor
        tvTotalScore.setText(String.valueOf(totalScore));

        // Hitung dan tampilkan waktu bermain
        String timeString = calculateTimePlayed();
        tvTimeTaken.setText(timeString);

        // Set motivational message berdasarkan skor
        setMotivationalMessage();
    }

    private String calculateTimePlayed() {
        if (startTime == 0) {
            // Jika tidak ada startTime, tampilkan default
            return "--:--";
        }

        long endTime = System.currentTimeMillis();
        long elapsedMillis = endTime - startTime;

        // Convert ke menit dan detik
        long totalSeconds = elapsedMillis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }

    private void setMotivationalMessage() {
        String message;

        if (totalScore >= 500) {
            message = "[ LEGENDARY HUNTER - SEMPURNA! ]";
        } else if (totalScore >= 400) {
            message = "[ EXPERT HUNTER - LUAR BIASA! ]";
        } else if (totalScore >= 300) {
            message = "[ SKILLED HUNTER - HEBAT! ]";
        } else if (totalScore >= 200) {
            message = "[ APPRENTICE HUNTER - BAGUS! ]";
        } else {
            message = "[ ROOKIE HUNTER - TERUS BERLATIH! ]";
        }

        tvMotivation.setText(message);
    }

    private void setupAnimations() {
        // Animasi trophy bounce/scale
        ScaleAnimation scaleAnim = new ScaleAnimation(
                0.8f, 1.1f, // from X, to X
                0.8f, 1.1f, // from Y, to Y
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleAnim.setDuration(800);
        scaleAnim.setRepeatCount(Animation.INFINITE);
        scaleAnim.setRepeatMode(Animation.REVERSE);
        trophyIcon.startAnimation(scaleAnim);

        // Animasi fade untuk motivational text
        AlphaAnimation fadeAnim = new AlphaAnimation(0.4f, 1f);
        fadeAnim.setDuration(1000);
        fadeAnim.setRepeatCount(Animation.INFINITE);
        fadeAnim.setRepeatMode(Animation.REVERSE);
        tvMotivation.startAnimation(fadeAnim);
    }

    private void setupClickListeners() {
        // Play Again - restart game
        btnPlayAgain.setOnClickListener(v -> {
            Intent intent = new Intent(FinishActivity.this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Back to Menu - kembali ke MainActivity
        btnBackToMenu.setOnClickListener(v -> {
            Intent intent = new Intent(FinishActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        // Share Result - bagikan hasil
        btnShare.setOnClickListener(v -> shareResult());
    }

    private void shareResult() {
        String shareText = "🏆 TREASURE HUNT - MISSION COMPLETE! 🏆\n\n" +
                "📍 Total Checkpoint: " + totalCheckpoints + "\n" +
                "⭐ Total Skor: " + totalScore + "\n" +
                "⏱️ Waktu: " + tvTimeTaken.getText().toString() + "\n\n" +
                "Ayo main Treasure Hunt dan kalahkan skorku! 🎮";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Treasure Hunt Score");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Bagikan hasil via:"));
    }

    // Disable back button agar tidak kembali ke MapActivity
    @Override
    public void onBackPressed() {
        // Langsung ke MainActivity
        Intent intent = new Intent(FinishActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}