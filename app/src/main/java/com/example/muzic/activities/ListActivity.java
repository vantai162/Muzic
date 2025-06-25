package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.muzic.adapter.SelectLibraryAdapter;
import com.example.muzic.databinding.AddNewLibraryBottomSheetBinding;
import com.example.muzic.databinding.SelectLibraryBottomSheetBinding;
import com.example.muzic.model.TrackData;
import com.example.muzic.records.ProfilePicture;
import com.example.muzic.records.Artwork;
import com.example.muzic.records.CoverPhoto;
import com.example.muzic.records.User;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.example.muzic.ApplicationClass;
import com.example.muzic.R;
import com.example.muzic.adapter.ActivityListSongsItemAdapter;
import com.example.muzic.adapter.UserCreatedSongsListAdapter;
import com.example.muzic.databinding.ActivityListBinding;
import com.example.muzic.databinding.ActivityListMoreInfoBottomSheetBinding;
import com.example.muzic.databinding.UserCreatedListActivityMoreBottomSheetBinding;
import com.example.muzic.model.BasicDataRecord;
import com.example.muzic.network.AudiusApiService;
import com.example.muzic.network.AudiusRepository;
import com.example.muzic.network.AudiusApiClient;
import com.example.muzic.records.Playlist;
import com.example.muzic.records.PlaylistResponse;
import com.example.muzic.records.Track;
import com.example.muzic.records.sharedpref.SavedLibrariesAudius;
import com.example.muzic.utils.SharedPreferenceManager;
import com.example.muzic.utils.customview.BottomSheetItemView;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.media3.common.util.UnstableApi;

@UnstableApi
public class ListActivity extends AppCompatActivity {

    private ActivityListBinding binding;
    private Playlist playlistData;
    private boolean isUserCreated = false;
    private final List<ArtistData> artistData = new ArrayList<>();
    private AudiusRepository audiusRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        audiusRepository = new AudiusRepository();
        setupViews();
        setupClickListeners();
        showShimmerData();
        showData();
    }

    private void setupViews() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.addMoreSongs.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        binding.playAllBtn.setOnClickListener(view -> {
            if (playlistData != null) {
                // Fetch tracks for this playlist
                audiusRepository.getPlaylistTracks(playlistData.id(), tracks -> {
                    if (tracks != null && !tracks.isEmpty()) {
                        // Convert all tracks to TrackData
                        ArrayList<TrackData> playlist = new ArrayList<>();
                        for (Track track : tracks) {
                            playlist.add(convertToTrackData(track));
                        }
                        
                        // Update playlist in ApplicationClass and start playing
                        ApplicationClass app = (ApplicationClass) getApplication();
                        app.updatePlaylist(playlist, 0);
                        app.playCurrentTrack();
                        
                        // Open MusicOverviewActivity
                        startActivity(new Intent(ListActivity.this, MusicOverviewActivity.class));
                    }
                }, error -> {
                    Log.e("ListActivity", "Error fetching playlist tracks: " + error);
                    Snackbar.make(binding.getRoot(), "Failed to play tracks", Snackbar.LENGTH_SHORT).show();
                });
            }
        });

        binding.addToLibrary.setOnClickListener(view -> handleAddToLibrary());
        binding.moreIcon.setOnClickListener(view -> onMoreIconClicked());
    }

    private TrackData convertToTrackData(Track track) {
        TrackData trackData = new TrackData();
        trackData.id = track.id();
        trackData.title = track.title();
        trackData.track_cid = track.trackCid();
        trackData.duration = track.duration();
        
        // Create and set User
        com.example.muzic.model.User userData = new com.example.muzic.model.User();
        userData.name = track.user().name();
        userData.id = track.user().id();
        trackData.user = userData;
        
        // Create and set Artwork
        com.example.muzic.model.Artwork artworkData = new com.example.muzic.model.Artwork();
        artworkData._480x480 = track.artwork().x480();
        artworkData._150x150 = track.artwork().x150();
        artworkData._1000x1000 = track.artwork().x1000();
        trackData.artwork = artworkData;
        
        // Set other fields
        trackData.description = track.description();
        trackData.genre = track.genre();
        trackData.mood = track.mood();
        trackData.release_date = track.releaseDate();
        trackData.repost_count = track.repostCount();
        trackData.favorite_count = track.favoriteCount();
        trackData.tags = track.tags();
        trackData.downloadable = track.downloadable();
        trackData.play_count = track.playCount();
        trackData.permalink = track.permalink();
        trackData.is_streamable = track.isStreamable();
        
        return trackData;
    }

    private void handleAddToLibrary() {
        if (playlistData == null) return;

        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        List<Playlist> savedPlaylists = sharedPreferenceManager.getSavedPlaylists();

        // Show select library bottom sheet
        View bottomSheetView = getLayoutInflater().inflate(R.layout.select_library_bottom_sheet, null);
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        dialog.setContentView(bottomSheetView);

        // Get views
        RecyclerView librariesRecyclerView = bottomSheetView.findViewById(R.id.libraries_recycler_view);
        TextView emptyText = bottomSheetView.findViewById(R.id.empty_text);
        MaterialButton createNewLibrary = bottomSheetView.findViewById(R.id.create_new_library);

        // Setup RecyclerView
        librariesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        SelectLibraryAdapter adapter = new SelectLibraryAdapter(savedPlaylists, library -> {
            // Add current track to selected library
            audiusRepository.getPlaylistTracks(playlistData.id(), tracks -> {
                if (tracks != null && !tracks.isEmpty()) {
                    // Get the first track (since we're adding from a single track view)
                    Track trackToAdd = tracks.get(0);
                    
                    // Add track to library
                    SavedLibrariesAudius.Library updatedLibrary = new SavedLibrariesAudius.Library(
                        library.id(),
                        false,
                        false,
                        library.playlistName(),
                        library.artwork().x480(),
                        library.description(),
                        new ArrayList<>(Collections.singletonList(trackToAdd))
                    );
                    sharedPreferenceManager.addLibraryToSavedLibraries(updatedLibrary);
                    
                    // Show success message
                    Snackbar.make(binding.getRoot(), "Added to " + library.playlistName(), Snackbar.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            }, error -> {
                Log.e("ListActivity", "Error fetching track: " + error);
                Snackbar.make(binding.getRoot(), "Failed to add to library", Snackbar.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        });
        librariesRecyclerView.setAdapter(adapter);

        // Show empty state if no libraries
        emptyText.setVisibility(savedPlaylists.isEmpty() ? View.VISIBLE : View.GONE);
        librariesRecyclerView.setVisibility(savedPlaylists.isEmpty() ? View.GONE : View.VISIBLE);

        // Handle create new library button
        createNewLibrary.setOnClickListener(v -> {
            dialog.dismiss();
            showAddLibraryDialog();
        });

        dialog.show();
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

        SharedPreferenceManager.getInstance(this).addPlaylistToSavedPlaylists(newPlaylist);
        dialog.dismiss();
        
        // Show select library dialog again
        handleAddToLibrary();
    }

    @SuppressLint("SimpleDateFormat")
    private String formatMillis(long millis) {
        return new SimpleDateFormat("MM-dd-yyyy HH:mm a").format(new Date(millis));
    }

    private void showShimmerData() {
        List<Track> data = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            data.add(new Track(
                    new Artwork("", "", ""),  // empty URLs for 150x150, 480x480, 1000x1000
                    "",  // description
                    "",  // genre
                    "<shimmer>",  // id
                    "",  // trackCid
                    "",  // mood
                    "",  // releaseDate
                    0,   // repostCount
                    0,   // favoriteCount
                    "",  // tags
                    "",  // title
                    new User(
                        0,    // albumCount
                        "",   // artistPickTrackId
                        "",   // bio
                        new CoverPhoto("", ""),  // coverPhoto
                        0,    // followeeCount
                        0,    // followerCount
                        false, // doesFollowCurrentUser
                        "",   // handle
                        "",   // id
                        false, // isVerified
                        "",   // location
                        "",   // name
                        0,    // playlistCount
                        new ProfilePicture("", "", ""),  // profilePicture
                        0,    // repostCount
                        0,    // trackCount
                        false, // isDeactivated
                        true,  // isAvailable
                        "",   // ercWallet
                        "",   // splWallet
                        0,    // supporterCount
                        0,    // supportingCount
                        0     // totalAudioBalance
                    ),
                    0,   // duration
                    false, // downloadable
                    0,    // playCount
                    "",   // permalink
                    false // isStreamable
            ));
        }
        binding.recyclerView.setAdapter(new ActivityListSongsItemAdapter(data));
    }

    private void showData() {
        if (getIntent().getExtras() == null) {
            Log.e("ListActivity", "No extras found in intent");
            return;
        }

        if (getIntent().getExtras().getBoolean("createdByUser", false)) {
            Log.d("ListActivity", "Loading user created playlist");
            onUserCreatedFetch();
            return;
        }

        // Get playlist data from intent
        String playlistJson = getIntent().getExtras().getString("data");
        boolean isMoodPlaylist = getIntent().getBooleanExtra("isMoodPlaylist", false);
        if (playlistJson == null) {
            Log.e("ListActivity", "No playlist data found in intent");
            return;
        }
        Log.d("ListActivity", "Received playlist data: " + playlistJson);

        try {
            // Try to parse as CustomPlaylistData first
            CustomPlaylistData customData = new Gson().fromJson(playlistJson, CustomPlaylistData.class);
            if (customData != null) {
                // Convert CustomPlaylistData to Playlist
                Playlist playlist = new Playlist(
                    customData.artwork,
                    customData.description,
                    customData.permalink,
                    customData.id,
                    customData.isAlbum,
                    customData.playlistName,
                    customData.repostCount,
                    customData.favoriteCount,
                    customData.totalPlayCount,
                    customData.user
                );
                onPlaylistFetched(playlist, isMoodPlaylist ? customData.tracks : null);
                return;
            }

            // If not CustomPlaylistData, try parsing as regular Playlist
            Playlist playlist = new Gson().fromJson(playlistJson, Playlist.class);
            if (playlist == null) {
                Log.e("ListActivity", "Failed to parse playlist data");
                return;
            }
            
            String playlistId = playlist.id();
            Log.d("ListActivity", "Parsed playlist ID: " + playlistId);

            SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
            Playlist cachedPlaylist = sharedPreferenceManager.getCachedPlaylist(playlistId);
            
            if (cachedPlaylist != null) {
                Log.d("ListActivity", "Found cached playlist, loading from cache");
                onPlaylistFetched(cachedPlaylist, null);
                return;
            }

            Log.d("ListActivity", "No cached playlist found, fetching from API");
            audiusRepository.getPlaylist(playlistId, fetchedPlaylist -> {
                Log.d("ListActivity", "Playlist fetched successfully");
                onPlaylistFetched(fetchedPlaylist, null);
            }, error -> {
                Log.e("ListActivity", "Error fetching playlist: " + error);
                // If API call fails, use the playlist data from intent
                Log.d("ListActivity", "Using playlist data from intent as fallback");
                onPlaylistFetched(playlist, null);
            });
        } catch (Exception e) {
            Log.e("ListActivity", "Error parsing playlist data: " + e.getMessage());
            Snackbar.make(binding.getRoot(), "Failed to load playlist", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void onPlaylistFetched(Playlist playlist, List<Track> providedTracks) {
        Log.d("ListActivity", "Starting onPlaylistFetched");
        playlistData = playlist;

        binding.albumTitle.setText(playlist.playlistName());
        binding.albumSubTitle.setText(playlist.user().name());
        
        // Get mood image resource if available
        int moodImageRes = getIntent().getIntExtra("moodImageRes", -1);
        String artworkUrl = playlist.artwork() != null ? playlist.artwork().x480() : "";
        
        // Prioritize mood image over artwork URL
        if (moodImageRes > 0) {
            binding.albumCover.setImageResource(moodImageRes);
        } else if (!artworkUrl.isEmpty()) {
            Picasso.get().load(Uri.parse(artworkUrl)).into(binding.albumCover);
        }

        // If providedTracks is not null, use it directly
        if (providedTracks != null) {
            Log.d("ListActivity", "Using provided tracks for playlist");
            ActivityListSongsItemAdapter adapter = new ActivityListSongsItemAdapter(providedTracks);
            binding.recyclerView.setAdapter(adapter);
        } else {
            // Fetch tracks for this playlist
            Log.d("ListActivity", "Fetching tracks for playlist: " + playlist.id());
            audiusRepository.getPlaylistTracks(playlist.id(), tracks -> {
                Log.d("ListActivity", "Tracks fetched successfully, count: " + (tracks != null ? tracks.size() : 0));
                ActivityListSongsItemAdapter adapter = new ActivityListSongsItemAdapter(tracks);
                binding.recyclerView.setAdapter(adapter);
            }, error -> {
                Log.e("ListActivity", "Error fetching playlist tracks: " + error);
                Snackbar.make(binding.getRoot(), "Failed to load tracks", Snackbar.LENGTH_SHORT).show();
            });
        }

        artistData.clear();
        if (playlist.user() != null) {
            artistData.add(new ArtistData(
                    playlist.user().name(),
                    playlist.user().id(),
                    playlist.user().profilePicture() != null ? playlist.user().profilePicture().x150() : ""
            ));
        }

        updatePlaylistInLibraryStatus();
        Log.d("ListActivity", "Finished onPlaylistFetched");
    }

    private void onUserCreatedFetch() {
        isUserCreated = true;
        binding.shareIcon.setVisibility(View.INVISIBLE);
        binding.addToLibrary.setVisibility(View.INVISIBLE);
        binding.addMoreSongs.setVisibility(View.VISIBLE);

        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
            if (savedLibraries == null || savedLibraries.lists().isEmpty()) {
                finish();
                return;
            }

            String playlistId = getIntent().getExtras().getString("id");
            SavedLibrariesAudius.Library userPlaylist = null;

            for (SavedLibrariesAudius.Library library : savedLibraries.lists()) {
                if (library.id().equals(playlistId)) {
                    userPlaylist = library;
                    break;
                }
            }

            if (userPlaylist == null) {
                finish();
                return;
            }

            // Create Playlist object from user created library
            playlistData = new Playlist(
                    new Artwork(userPlaylist.image(), userPlaylist.image(), userPlaylist.image()),
                    userPlaylist.description(),
                    userPlaylist.id(),
                    userPlaylist.id(),
                    false,
                    userPlaylist.name(),
                    0,
                    0,
                    0,
                    new User(
                            0,
                            "",
                            "",
                            new CoverPhoto("", ""),
                            0,
                            0,
                            false,
                            "local",
                            "local",
                            false,
                            "",
                            "Local Library",
                            0,
                            new ProfilePicture("", "", ""),
                            0,
                            0,
                            false,
                            true,
                            "",
                            "",
                            0,
                            0,
                            0
                    )
            );

            binding.albumTitle.setText(userPlaylist.name());
            binding.albumSubTitle.setText(userPlaylist.description());
            Picasso.get().load(Uri.parse(userPlaylist.image())).into(binding.albumCover);

            binding.recyclerView.setAdapter(new UserCreatedSongsListAdapter(userPlaylist.tracks()));
        });
    }

    private void onMoreIconClicked() {
        if (playlistData == null){
            Log.e("Error","!");
            return;
        }

        if (isUserCreated) {
            showUserCreatedPlaylistMoreMenu();
        } else {
            showAudiusPlaylistMoreMenu();
        }
    }

    private void showUserCreatedPlaylistMoreMenu() {
        View moreView = getLayoutInflater().inflate(R.layout.user_created_list_more_bottom_sheet, null);
        BottomSheetDialog moreDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        moreDialog.setContentView(moreView);

        // Handle delete playlist
        moreView.findViewById(R.id.delete_playlist).setOnClickListener(v -> {
            moreDialog.dismiss();
            
            // Show confirmation dialog
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialog_App)
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    // Get saved libraries and playlists
                    SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
                    sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
                        List<Playlist> savedPlaylists = sharedPreferenceManager.getSavedPlaylists();
                        String playlistId = getIntent().getExtras().getString("id");

                        // Remove from SavedLibrariesAudius
                        if (savedLibraries != null && savedLibraries.lists() != null) {
                            List<SavedLibrariesAudius.Library> updatedLibraries = new ArrayList<>();
                            for (SavedLibrariesAudius.Library library : savedLibraries.lists()) {
                                if (!library.id().equals(playlistId)) {
                                    updatedLibraries.add(library);
                                }
                            }
                            SavedLibrariesAudius newSavedLibraries = new SavedLibrariesAudius(updatedLibraries);
                            sharedPreferenceManager.setSavedLibrariesData(newSavedLibraries);
                        }

                        // Remove from SavedPlaylists
                        if (savedPlaylists != null) {
                            List<Playlist> updatedPlaylists = new ArrayList<>();
                            for (Playlist playlist : savedPlaylists) {
                                if (!playlist.id().equals(playlistId)) {
                                    updatedPlaylists.add(playlist);
                                }
                            }
                            sharedPreferenceManager.setSavedPlaylists(updatedPlaylists);
                        }

                        // Show success message and finish activity
                        Snackbar.make(binding.getRoot(), "Playlist deleted", Snackbar.LENGTH_SHORT).show();
                        finish();
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        moreDialog.show();
    }

    private void showAudiusPlaylistMoreMenu() {
        View moreView = getLayoutInflater().inflate(R.layout.activity_list_more_info_bottom_sheet, null);
        BottomSheetDialog moreDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        moreDialog.setContentView(moreView);

        // Set playlist info
        TextView titleView = moreView.findViewById(R.id.album_title);
        TextView subtitleView = moreView.findViewById(R.id.album_sub_title);
        ImageView coverView = moreView.findViewById(R.id.cover_image);

        titleView.setText(playlistData.playlistName());
        subtitleView.setText(playlistData.user().name());
        if (playlistData.artwork() != null && !playlistData.artwork().x480().isEmpty()) {
            Picasso.get().load(Uri.parse(playlistData.artwork().x480())).into(coverView);
        }

        // Setup Add/Remove from library button
        BottomSheetItemView addToLibraryBtn = moreView.findViewById(R.id.add_to_library);
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
            boolean isInLibrary = isPlaylistInLibrary(playlistData, savedLibraries);

            addToLibraryBtn.getTitleTextView().setText(isInLibrary ? "Remove from library" : "Add to library");
            addToLibraryBtn.getIconImageView().setImageResource(isInLibrary ? R.drawable.round_close_24 : R.drawable.round_add_24);
            addToLibraryBtn.setOnClickListener(view -> {
                binding.addToLibrary.performClick();
                moreDialog.dismiss();
            });
        });

        // Setup artist section
        LinearLayout mainLayout = moreView.findViewById(R.id.main);
        for (ArtistData artist : artistData) {
            try {
                String imgUrl = artist.image().isEmpty() ? "" : artist.image();
                BottomSheetItemView artistView = new BottomSheetItemView(this, artist.name(), imgUrl, artist.id());
                mainLayout.addView(artistView);

                // Handle artist click
                artistView.setOnClickListener(v -> {
                    moreDialog.dismiss();
                    Intent intent = new Intent(this, ArtistProfileActivity.class);
                    intent.putExtra("id", artist.id());
                    startActivity(intent);
                });
            } catch (Exception e) {
                Log.e("ListActivity", "Error setting up artist view", e);
            }
        }

        moreDialog.show();
    }

    private void updatePlaylistInLibraryStatus() {
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        sharedPreferenceManager.getSavedLibrariesData().addOnSuccessListener(savedLibraries -> {
            binding.addToLibrary.setImageResource(
                    isPlaylistInLibrary(playlistData, savedLibraries) ?
                            R.drawable.round_done_24 :
                            R.drawable.round_add_24
            );
        });
    }

    private boolean isPlaylistInLibrary(Playlist playlist, SavedLibrariesAudius savedLibraries) {
        if (savedLibraries == null || savedLibraries.lists() == null || savedLibraries.lists().isEmpty()) {
            return false;
        }
        return savedLibraries.lists().stream().anyMatch(library -> library.id().equals(playlist.id()));
    }

    private int getPlaylistIndexInLibrary(Playlist playlist, SavedLibrariesAudius savedLibraries) {
        if (savedLibraries == null || savedLibraries.lists() == null || savedLibraries.lists().isEmpty()) {
            return -1;
        }
        
        for (int i = 0; i < savedLibraries.lists().size(); i++) {
            if (savedLibraries.lists().get(i).id().equals(playlist.id())) {
                return i;
            }
        }
        return -1;
    }

    public void backPress(View view) {
        finish();
    }

    private record ArtistData(String name, String id, String image) {}

    // Add CustomPlaylistData class definition
    private static class CustomPlaylistData {
        public Artwork artwork;
        public String description;
        public String permalink;
        public String id;
        public boolean isAlbum;
        public String playlistName;
        public int repostCount;
        public int favoriteCount;
        public int totalPlayCount;
        public com.example.muzic.records.User user;
        public List<Track> tracks;
    }
}