package com.example.muzic.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.databinding.ActivitySettingsBinding;
import com.example.muzic.utils.SettingsSharedPrefManager;
import com.example.muzic.utils.ThemeManager;
import com.example.muzic.utils.AudioQualityManager;
import com.google.android.material.button.MaterialButtonToggleGroup;

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    private ThemeManager themeManager;
    private AudioQualityManager audioQualityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        themeManager = new ThemeManager(this);
        audioQualityManager = ((ApplicationClass) getApplication()).getAudioQualityManager();
        final SettingsSharedPrefManager settingsSharedPrefManager = new SettingsSharedPrefManager(this);

        // Set up dark mode toggle group
        MaterialButtonToggleGroup darkModeToggle = binding.darkModeToggle;
        
        // Set initial button state based on current theme mode
        int currentMode = themeManager.getThemeMode();
        switch (currentMode) {
            case ThemeManager.MODE_ON:
                darkModeToggle.check(R.id.btn_on);
                break;
            case ThemeManager.MODE_OFF:
                darkModeToggle.check(R.id.btn_off);
                break;
            case ThemeManager.MODE_SYSTEM:
            default:
                darkModeToggle.check(R.id.btn_default);
                break;
        }

        // Listen for toggle changes
        darkModeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_on) {
                    themeManager.setThemeMode(ThemeManager.MODE_ON);
                } else if (checkedId == R.id.btn_off) {
                    themeManager.setThemeMode(ThemeManager.MODE_OFF);
                } else if (checkedId == R.id.btn_default) {
                    themeManager.setThemeMode(ThemeManager.MODE_SYSTEM);
                }
            }
        });

        binding.downloadOverCellular.setOnCheckChangeListener(value -> {
            settingsSharedPrefManager.setDownloadOverCellular(value);
            updateAudioQuality();
        });

        binding.highQualityTrack.setOnCheckChangeListener(value -> {
            settingsSharedPrefManager.setHighQualityTrack(value);
            updateAudioQuality();
            showQualityToast(value);
        });

        binding.storeInCache.setOnCheckChangeListener(settingsSharedPrefManager::setStoreInCache);
        binding.blurPlayerBackground.setOnCheckChangeListener(settingsSharedPrefManager::setBlurPlayerBackground);
        binding.explicit.setOnCheckChangeListener(settingsSharedPrefManager::setExplicit);

        binding.downloadOverCellular.setChecked(settingsSharedPrefManager.getDownloadOverCellular());
        binding.highQualityTrack.setChecked(settingsSharedPrefManager.getHighQualityTrack());
        binding.storeInCache.setChecked(settingsSharedPrefManager.getStoreInCache());
        binding.blurPlayerBackground.setChecked(settingsSharedPrefManager.getBlurPlayerBackground());
        binding.explicit.setChecked(settingsSharedPrefManager.getExplicit());

        binding.returnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void updateAudioQuality() {
        ((ApplicationClass) getApplication()).updateTrackQuality();
    }

    private void showQualityToast(boolean isHighQuality) {
        String quality = audioQualityManager.getCurrentQuality();
        String message = "Audio quality set to " + quality;
        if (!isHighQuality) {
            message += " (Low quality mode)";
        } else if (!audioQualityManager.shouldUseCellular()) {
            message += " (High quality on WiFi only)";
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}