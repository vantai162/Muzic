package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.example.muzic.R;
import com.example.muzic.adapter.SavedLibrariesAdapter;
import com.example.muzic.databinding.ActivitySavedLibrariesBinding;
import com.example.muzic.databinding.AddNewLibraryBottomSheetBinding;
import com.example.muzic.records.Artwork;
import com.example.muzic.records.CoverPhoto;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.ProfilePicture;
import com.example.muzic.records.User;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.example.muzic.utils.SharedPreferenceManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

public class SavedLibrariesActivity extends AppCompatActivity {

    private static final String TAG = "SavedLibrariesActivity";
    private ActivitySavedLibrariesBinding binding;
    private List<Playlist> savedPlaylists;
    private SavedLibrariesAdapter adapter;
    private SharedPreferenceManager sharedPreferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySavedLibrariesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        setupRecyclerView();
        setupAddNewLibraryButton();
        loadSavedPlaylists();
    }

    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        OverScrollDecoratorHelper.setUpOverScroll(binding.recyclerView, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
        savedPlaylists = new ArrayList<>();
        adapter = new SavedLibrariesAdapter(savedPlaylists, playlist -> {
            // Show delete confirmation dialog on long click
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_App)
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Remove from SavedLibrariesAudius (Firestore)
                    sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
                        if (savedLibraries != null && savedLibraries.lists() != null) {
                            List<SavedLibrariesAudius.Library> updatedLibraries = new ArrayList<>();
                            for (SavedLibrariesAudius.Library library : savedLibraries.lists()) {
                                if (!library.id().equals(playlist.id())) {
                                    updatedLibraries.add(library);
                                }
                            }
                            SavedLibrariesAudius newSavedLibraries = new SavedLibrariesAudius(updatedLibraries);
                            sharedPreferenceManager.setSavedLibrariesData(newSavedLibraries);

                            // Update UI
                            int position = savedPlaylists.indexOf(playlist);
                            if (position != -1) {
                                savedPlaylists.remove(position);
                                adapter.notifyItemRemoved(position);
                                updateEmptyState();
                            }
                            Snackbar.make(binding.getRoot(), "Playlist deleted", Snackbar.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupAddNewLibraryButton() {
        binding.addNewLibrary.setOnClickListener(view -> showAddLibraryDialog());
    }

    private void showAddLibraryDialog() {
        AddNewLibraryBottomSheetBinding dialogBinding = AddNewLibraryBottomSheetBinding.inflate(getLayoutInflater());
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        dialog.setContentView(dialogBinding.getRoot());

        dialogBinding.cancel.setOnClickListener(v -> dialog.dismiss());
        dialogBinding.create.setOnClickListener(v -> createNewLibrary(dialogBinding, dialog));

        dialog.show();
    }

    private void createNewLibrary(AddNewLibraryBottomSheetBinding dialogBinding, BottomSheetDialog dialog) {
        String name = dialogBinding.edittext.getText().toString().trim();
        if (name.isEmpty()) {
            dialogBinding.edittext.setError("Name cannot be empty");
            return;
        }

        long currentTime = System.currentTimeMillis();
        String playlistId = "local_" + currentTime;

        Playlist newPlaylist = new Playlist(
                new Artwork("", "", ""),  // Empty artwork initially
                "Created on: " + formatMillis(currentTime), // Description
                playlistId, // permalink
                playlistId, // id
                false, // isAlbum
                name,  // playlist name
                0,    // repost count
                0,    // favorite count
                0,    // total play count
                new User(
                        0,                  // albumCount
                        "",                 // artistPickTrackId
                        "",                 // bio
                        new CoverPhoto("",""), // coverPhoto
                        0,                  // followeeCount
                        0,                  // followerCount
                        false,              // doesFollowCurrentUser
                        "local",            // handle
                        "local",            // id
                        false,              // isVerified
                        "",                 // location
                        "Local Library",    // name
                        0,                  // playlistCount
                        new ProfilePicture("","",""), // profilePicture
                        0,                  // repostCount
                        0,                  // trackCount
                        false,              // isDeactivated
                        true,               // isAvailable
                        "",                 // ercWallet
                        "",                 // splWallet
                        0,                  // supporterCount
                        0,                  // supportingCount
                        0                   // totalAudioBalance
                )
        );

        // Add to SavedLibrariesAudius (Firestore)
        SavedLibrariesAudius.Library newLibrary = new SavedLibrariesAudius.Library(
            playlistId,
            false,
            false,
            name,
            "",  // No artwork initially
            "Created on: " + formatMillis(currentTime),
            new ArrayList<>()  // Empty tracks list
        );
        sharedPreferenceManager.addLibraryToSavedLibraries(newLibrary);

        // Update UI
        savedPlaylists.add(newPlaylist);
        adapter.notifyItemInserted(savedPlaylists.size() - 1);
        updateEmptyState();
        Snackbar.make(binding.getRoot(), "Library created successfully", Snackbar.LENGTH_SHORT).show();
        dialog.dismiss();
    }

    private void loadSavedPlaylists() {
        // Chỉ lấy từ Firestore (SavedLibrariesAudius)
        sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(oldLibraries -> {
            savedPlaylists.clear();
            if (oldLibraries != null && oldLibraries.lists() != null) {
                for (SavedLibrariesAudius.Library library : oldLibraries.lists()) {
                    // Convert old library to new Playlist format
                    Playlist convertedPlaylist = new Playlist(
                            new Artwork("", "", ""),
                            library.description(),
                            library.id(),
                            library.id(),
                            false,
                            library.name(),
                            0, 0, 0,
                            new User(0, "", "", new CoverPhoto("",""), 0, 0, false,
                                    "local", "local", false, "", "Local Library", 0,
                                    new ProfilePicture("","",""), 0, 0, false, true,
                                    "", "", 0, 0, 0)
                    );
                    savedPlaylists.add(convertedPlaylist);
                }
            }
            adapter.notifyDataSetChanged();
            updateEmptyState();
        });
    }

    private void updateEmptyState() {
        binding.emptyListTv.setVisibility(savedPlaylists.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMillis(long millis) {
        return new SimpleDateFormat("MM-dd-yyyy HH:mm a").format(new Date(millis));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadSavedPlaylists();
    }

    public void backPress(View view) {
        finish();
    }
}