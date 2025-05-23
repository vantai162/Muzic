package com.example.muzic.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

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

public class SettingsActivity extends AppCompatActivity {

    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_settings);
        final SettingsSharedPrefManager settingsSharedPrefManager = new SettingsSharedPrefManager(this);

        binding.downloadOverCellular.setOnCheckChangeListener(settingsSharedPrefManager::setDownloadOverCellular);
        binding.highQualityTrack.setOnCheckChangeListener(settingsSharedPrefManager::setHighQualityTrack);
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
}