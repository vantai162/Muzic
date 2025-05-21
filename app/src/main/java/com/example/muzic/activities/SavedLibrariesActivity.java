/*package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;SavedLibrariesActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.example.muzic.R;
import com.example.muzic.adapters.SavedLibrariesAdapter;
import com.example.muzic.databinding.ActivitySavedLibrariesBinding;
import com.example.muzic.databinding.AddNewLibraryBottomSheetBinding;
import com.example.muzic.records.sharedpref.SavedLibraries;
import com.example.muzic.utils.SharedPreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class SavedLibrariesActivity extends AppCompatActivity {

    ActivitySavedLibrariesBinding binding;
    SavedLibraries savedLibraries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedLibrariesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OverScrollDecoratorHelper.setUpOverScroll(binding.recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

        binding.addNewLibrary.setOnClickListener(view -> {
            AddNewLibraryBottomSheetBinding addNewLibraryBottomSheetBinding = AddNewLibraryBottomSheetBinding.inflate(getLayoutInflater());
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
            bottomSheetDialog.setContentView(addNewLibraryBottomSheetBinding.getRoot());
            addNewLibraryBottomSheetBinding.cancel.setOnClickListener(view1 -> {
                bottomSheetDialog.dismiss();
            });
            addNewLibraryBottomSheetBinding.create.setOnClickListener(view1 -> {
                final String name = addNewLibraryBottomSheetBinding.edittext.getText().toString();
                if(name.isEmpty()) {
                    addNewLibraryBottomSheetBinding.edittext.setError("Name cannot be empty");
                    return;
                }
                addNewLibraryBottomSheetBinding.edittext.setError(null);
                Log.i("SavedLibrariesActivity", "BottomSheetDialog_create: " + name);

                final String currentTime = String.valueOf(System.currentTimeMillis());

                SavedLibraries.Library library = new SavedLibraries.Library(
                        "#"+currentTime,
                        true,
                        false,
                        name,
                        "",
                        "Created on :- " + formatMillis(Long.parseLong(currentTime)),
                        new ArrayList<>()
                );

                final SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
                sharedPreferenceManager.addLibraryToSavedLibraries(library);
                Snackbar.make(binding.getRoot(), "Library added successfully", Snackbar.LENGTH_SHORT).show();


                bottomSheetDialog.dismiss();

                showData(sharedPreferenceManager);
            });
            bottomSheetDialog.show();
        });

        showData();
    }

    private String formatMillis(long millis) {
        Date date = new Date(millis);
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm a");
        return simpleDateFormat.format(date);
    }


    private void showData(SharedPreferenceManager sharedPreferenceManager){
        savedLibraries = sharedPreferenceManager.getSavedLibrariesData();
        binding.emptyListTv.setVisibility(savedLibraries == null ? View.VISIBLE : View.GONE);
        if(savedLibraries != null) binding.recyclerView.setAdapter(new SavedLibrariesAdapter(savedLibraries.lists()));
    }
    private void showData() {
        showData(SharedPreferenceManager.getInstance(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        showData();
    }

    public void backPress(View view){
        finish();
    }
}*/