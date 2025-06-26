package com.example.muzic.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.content.res.ColorStateList;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.util.UnstableApi;

import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.databinding.ActivitySettingsBinding;
import com.example.muzic.utils.SettingsSharedPrefManager;
import com.example.muzic.utils.ThemeManager;
import com.example.muzic.utils.AudioQualityManager;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.button.MaterialButton;

@UnstableApi
public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;
    private ThemeManager themeManager;
    private AudioQualityManager audioQualityManager;
    private static final String KEY_DOWNLOAD_CELLULAR = "download_cellular_state";
    private static final String KEY_HIGH_QUALITY = "high_quality_state";
    private static final String KEY_STORE_CACHE = "store_cache_state";
    private static final String KEY_BLUR_BACKGROUND = "blur_background_state";
    private static final String KEY_EXPLICIT = "explicit_state";

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        ApplicationClass app = (ApplicationClass) getApplication();
        themeManager = app.getThemeManager();
        audioQualityManager = app.getAudioQualityManager();
        final SettingsSharedPrefManager settingsSharedPrefManager = new SettingsSharedPrefManager(this);

        // Apply current theme colors from ApplicationClass
        applyThemeColors();

        // Initialize switches with saved state if available, otherwise use SharedPreferences
        if (savedInstanceState != null) {
            binding.downloadOverCellular.setChecked(savedInstanceState.getBoolean(KEY_DOWNLOAD_CELLULAR, settingsSharedPrefManager.getDownloadOverCellular()));
            binding.highQualityTrack.setChecked(savedInstanceState.getBoolean(KEY_HIGH_QUALITY, settingsSharedPrefManager.getHighQualityTrack()));
            binding.storeInCache.setChecked(savedInstanceState.getBoolean(KEY_STORE_CACHE, settingsSharedPrefManager.getStoreInCache()));
            binding.blurPlayerBackground.setChecked(savedInstanceState.getBoolean(KEY_BLUR_BACKGROUND, settingsSharedPrefManager.getBlurPlayerBackground()));
            binding.explicit.setChecked(savedInstanceState.getBoolean(KEY_EXPLICIT, settingsSharedPrefManager.getExplicit()));

            int selectedThemeId = savedInstanceState.getInt("selected_theme_id", R.id.original);
            binding.themeGroup.check(selectedThemeId);
        } else {
            binding.downloadOverCellular.setChecked(settingsSharedPrefManager.getDownloadOverCellular());
            binding.highQualityTrack.setChecked(settingsSharedPrefManager.getHighQualityTrack());
            binding.storeInCache.setChecked(settingsSharedPrefManager.getStoreInCache());
            binding.blurPlayerBackground.setChecked(settingsSharedPrefManager.getBlurPlayerBackground());
            binding.explicit.setChecked(settingsSharedPrefManager.getExplicit());
            binding.themeGroup.check(settingsSharedPrefManager.getTheme().equals("nebula") ? R.id.nebula : settingsSharedPrefManager.getTheme().equals("aqua") ? R.id.aqua : settingsSharedPrefManager.getTheme().equals("tangerine") ? R.id.tangerine : settingsSharedPrefManager.getTheme().equals("crimson love") ? R.id.crimson_love : settingsSharedPrefManager.getTheme().equals("blue depths") ? R.id.blue_depths : R.id.original);
        }

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

        setupThemeGroupListener();

        binding.returnImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the current state of all switches
        outState.putBoolean(KEY_DOWNLOAD_CELLULAR, binding.downloadOverCellular.getChecked());
        outState.putBoolean(KEY_HIGH_QUALITY, binding.highQualityTrack.getChecked());
        outState.putBoolean(KEY_STORE_CACHE, binding.storeInCache.getChecked());
        outState.putBoolean(KEY_BLUR_BACKGROUND, binding.blurPlayerBackground.getChecked());
        outState.putBoolean(KEY_EXPLICIT, binding.explicit.getChecked());

        int selectedThemeId = binding.themeGroup.getCheckedRadioButtonId();
        outState.putInt("selected_theme_id", selectedThemeId);
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

    private void applyThemeColors() {
        ApplicationClass app = (ApplicationClass) getApplication();
        ColorStateList themeColor = app.getCurrentThemeColors();
        
        // Update radio buttons color
        for (int i = 0; i < binding.themeGroup.getChildCount(); i++) {
            MaterialRadioButton radioButton = (MaterialRadioButton) binding.themeGroup.getChildAt(i);
            radioButton.setButtonTintList(themeColor);
        }

        // Update switches track color with theme
        ColorStateList switchTrackColors = new ColorStateList(
            new int[][] {
                new int[] { android.R.attr.state_checked },
                new int[] { -android.R.attr.state_checked }
            },
            new int[] {
                themeManager.getPrimaryColor(),
                getResources().getColor(R.color.textSec, getTheme())
            }
        );

        binding.downloadOverCellular.setThumbTintList(switchTrackColors);
        binding.highQualityTrack.setThumbTintList(switchTrackColors);
        binding.storeInCache.setThumbTintList(switchTrackColors);
        binding.blurPlayerBackground.setThumbTintList(switchTrackColors);
        binding.explicit.setThumbTintList(switchTrackColors);

        // Update dark mode toggle buttons
        for (int i = 0; i < binding.darkModeToggle.getChildCount(); i++) {
            View child = binding.darkModeToggle.getChildAt(i);
            if (child instanceof MaterialButton) {
                MaterialButton button = (MaterialButton) child;
                button.setStrokeColor(themeColor);
                
                // Set background tint for selected state
                ColorStateList backgroundTint = new ColorStateList(
                    new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] { -android.R.attr.state_checked }
                    },
                    new int[] {
                        themeManager.getPrimaryColor(),
                        getResources().getColor(android.R.color.transparent, getTheme())
                    }
                );
                button.setBackgroundTintList(backgroundTint);
                
                // Set text color for selected/unselected states
                ColorStateList textColor = new ColorStateList(
                    new int[][] {
                        new int[] { android.R.attr.state_checked },
                        new int[] { -android.R.attr.state_checked }
                    },
                    new int[] {
                        getResources().getColor(android.R.color.white, getTheme()),
                        themeManager.getPrimaryColor()
                    }
                );
                button.setTextColor(textColor);
            }
        }
    }

    // Update the theme group listener to also update ApplicationClass
    private void setupThemeGroupListener() {
        binding.themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedTheme = checkedId == R.id.nebula ? "nebula" :
                                 checkedId == R.id.aqua ? "aqua" :
                                 checkedId == R.id.tangerine ? "tangerine" :
                                 checkedId == R.id.crimson_love ? "crimson love" :
                                 checkedId == R.id.blue_depths ? "blue depths" :
                                 "original";
            
            SettingsSharedPrefManager settingsSharedPrefManager = new SettingsSharedPrefManager(this);
            settingsSharedPrefManager.setTheme(selectedTheme);
            
            // Update theme colors in ApplicationClass
            ApplicationClass app = (ApplicationClass) getApplication();
            app.updateThemeColors();
            
            // Apply updated theme colors
            applyThemeColors();

            // Show toast to inform user
            Toast.makeText(this, "Theme applied: " + selectedTheme, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reapply theme colors when activity resumes
        applyThemeColors();
    }
}