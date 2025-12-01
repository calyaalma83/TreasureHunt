package com.example.treasurehunt;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

public class HowToPlayActivity extends AppCompatActivity {

    private Button btnTabTutorial, btnTabTips, btnBackToMenu;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        initViews();
        setupClickListeners();

        // Load fragment tutorial sebagai default
        loadFragment(new FragmentTutorial(), true);
    }

    private void initViews() {
        btnTabTutorial = findViewById(R.id.btnTabTutorial);
        btnTabTips = findViewById(R.id.btnTabTips);
        btnBackToMenu = findViewById(R.id.btnBackToMenu);
    }

    private void setupClickListeners() {
        // Tab Tutorial
        btnTabTutorial.setOnClickListener(v -> {
            loadFragment(new FragmentTutorial(), false);
            setActiveTab(btnTabTutorial);
        });

        // Tab Tips
        btnTabTips.setOnClickListener(v -> {
            loadFragment(new FragmentTips(), false);
            setActiveTab(btnTabTips);
        });

        // Back to Menu
        btnBackToMenu.setOnClickListener(v -> finish());
    }

    /**
     * Load fragment ke container
     * @param fragment Fragment yang akan di-load
     * @param isFirst Apakah ini fragment pertama (untuk inisialisasi)
     */
    private void loadFragment(Fragment fragment, boolean isFirst) {
        currentFragment = fragment;
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (isFirst) {
            transaction.add(R.id.fragmentContainer, fragment);
        } else {
            transaction.replace(R.id.fragmentContainer, fragment);
        }

        transaction.commit();
    }

    /**
     * Set tampilan tab yang aktif
     * @param activeButton Button yang aktif
     */
    private void setActiveTab(Button activeButton) {
        // Reset semua button ke style inactive
        btnTabTutorial.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnTabTutorial.setTextColor(getResources().getColor(android.R.color.white));
        btnTabTips.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        btnTabTips.setTextColor(getResources().getColor(android.R.color.white));

        // Set button aktif
        if (activeButton == btnTabTutorial) {
            btnTabTutorial.setBackgroundColor(0xFF00FFFF); // Cyan
            btnTabTutorial.setTextColor(0xFF0A0E27); // Dark
        } else if (activeButton == btnTabTips) {
            btnTabTips.setBackgroundColor(0xFF00FFFF); // Cyan
            btnTabTips.setTextColor(0xFF0A0E27); // Dark
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}