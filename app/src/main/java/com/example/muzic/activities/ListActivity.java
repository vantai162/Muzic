package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.muzic.records.ProfilePicture;
import com.example.muzic.records.Artwork;
import com.example.muzic.records.CoverPhoto;
import com.example.muzic.records.User;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ActivityListBinding binding;
    private final List<String> trackQueue = new ArrayList<>();
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
        /*binding.playAllBtn.setOnClickListener(view -> {
            if (!trackQueue.isEmpty()) {
                ((ApplicationClass) getApplicationContext()).setTrackQueue(trackQueue);
                ((ApplicationClass) getApplicationContext()).nextTrack();
                startActivity(new Intent(ListActivity.this, MusicOverviewActivity.class)
                        .putExtra("id", ApplicationClass.MUSIC_ID));
            }
        });*/

        binding.addToLibrary.setOnClickListener(view -> handleAddToLibrary());
        //binding.addMoreSongs.setOnClickListener(view ->
            //startActivity(new Intent(ListActivity.this, SearchActivity.class)));
        binding.moreIcon.setOnClickListener(view -> onMoreIconClicked());
    }

    private void handleAddToLibrary() {
        if (playlistData == null) return;

        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        SavedLibrariesAudius savedLibraries = sharedPreferenceManager.getSavedLibrariesData();

        if (isPlaylistInLibrary(playlistData, savedLibraries)) {
            showRemoveFromLibraryDialog(sharedPreferenceManager);
        } else {
            addPlaylistToLibrary(sharedPreferenceManager);
        }
    }

    private void showRemoveFromLibraryDialog(SharedPreferenceManager sharedPreferenceManager) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Are you sure?")
                .setMessage("Do you want to remove this playlist from your library?")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    int index = getPlaylistIndexInLibrary(playlistData, sharedPreferenceManager.getSavedLibrariesData());
                    if (index != -1) {
                        sharedPreferenceManager.removeLibraryFromSavedLibraries(index);
                        Snackbar.make(binding.getRoot(), "Removed from Library", Snackbar.LENGTH_SHORT).show();
                        updatePlaylistInLibraryStatus();
                        finish();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void addPlaylistToLibrary(SharedPreferenceManager sharedPreferenceManager) {
        // We'll need to fetch the tracks for this playlist first
        audiusRepository.getPlaylistTracks(playlistData.id(), tracks -> {
            SavedLibrariesAudius.Library library = new SavedLibrariesAudius.Library(
                    playlistData.id(),
                    false,
                    false,
                    playlistData.playlistName(),
                    playlistData.artwork().x480(),
                    playlistData.description(),
                    tracks
            );
            sharedPreferenceManager.addLibraryToSavedLibraries(library);
            Snackbar.make(binding.getRoot(), "Added to Library", Snackbar.LENGTH_SHORT).show();
            updatePlaylistInLibraryStatus();
        }, error -> {
            Log.e("ListActivity", "Error fetching playlist tracks: " + error);
            Snackbar.make(binding.getRoot(), "Failed to add to library", Snackbar.LENGTH_SHORT).show();
        });
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
        if (playlistJson == null) {
            Log.e("ListActivity", "No playlist data found in intent");
            return;
        }
        Log.d("ListActivity", "Received playlist data: " + playlistJson);

        try {
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
                onPlaylistFetched(cachedPlaylist);
                return;
            }

            Log.d("ListActivity", "No cached playlist found, fetching from API");
            audiusRepository.getPlaylist(playlistId, fetchedPlaylist -> {
                Log.d("ListActivity", "Playlist fetched successfully");
                onPlaylistFetched(fetchedPlaylist);
            }, error -> {
                Log.e("ListActivity", "Error fetching playlist: " + error);
                // If API call fails, use the playlist data from intent
                Log.d("ListActivity", "Using playlist data from intent as fallback");
                onPlaylistFetched(playlist);
            });
        } catch (Exception e) {
            Log.e("ListActivity", "Error parsing playlist data: " + e.getMessage());
            Snackbar.make(binding.getRoot(), "Failed to load playlist", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void onPlaylistFetched(Playlist playlist) {
        Log.d("ListActivity", "Starting onPlaylistFetched");
        playlistData = playlist;

        binding.albumTitle.setText(playlist.playlistName());
        binding.albumSubTitle.setText(playlist.user().name());
        
        String artworkUrl = playlist.artwork() != null ? playlist.artwork().x480() : "";
        Log.d("ListActivity", "Loading artwork from URL: " + artworkUrl);
        if (!artworkUrl.isEmpty()) {
            Picasso.get().load(Uri.parse(artworkUrl)).into(binding.albumCover);
        }

        // Fetch tracks for this playlist
        Log.d("ListActivity", "Fetching tracks for playlist: " + playlist.id());
        audiusRepository.getPlaylistTracks(playlist.id(), tracks -> {
            Log.d("ListActivity", "Tracks fetched successfully, count: " + (tracks != null ? tracks.size() : 0));
            ActivityListSongsItemAdapter adapter = new ActivityListSongsItemAdapter(tracks);
            binding.recyclerView.setAdapter(adapter);

            trackQueue.clear();
            if (tracks != null) {
                for (Track track : tracks) {
                    trackQueue.add(track.id());
                }
            }
        }, error -> {
            Log.e("ListActivity", "Error fetching playlist tracks: " + error);
            Snackbar.make(binding.getRoot(), "Failed to load tracks", Snackbar.LENGTH_SHORT).show();
        });

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
        SavedLibrariesAudius savedLibraries = sharedPreferenceManager.getSavedLibrariesData();
        
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

        binding.albumTitle.setText(userPlaylist.name());
        binding.albumSubTitle.setText(userPlaylist.description());
        Picasso.get().load(Uri.parse(userPlaylist.image())).into(binding.albumCover);
        
        binding.recyclerView.setAdapter(new UserCreatedSongsListAdapter(userPlaylist.tracks()));
        
        for (Track track : userPlaylist.tracks()) {
            trackQueue.add(track.id());
        }
    }

    private void onMoreIconClicked() {
        if (playlistData == null) return;

        if (isUserCreated) {
            onMoreIconClickedUserCreated();
            return;
        }

        showPlaylistBottomSheet();
    }

    private void showPlaylistBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        final ActivityListMoreInfoBottomSheetBinding sheetBinding = ActivityListMoreInfoBottomSheetBinding.inflate(getLayoutInflater());

        sheetBinding.albumTitle.setText(binding.albumTitle.getText());
        sheetBinding.albumSubTitle.setText(binding.albumSubTitle.getText());
        Picasso.get().load(Uri.parse(playlistData.artwork().x480())).into(sheetBinding.coverImage);

        setupLibraryButton(sheetBinding);
        setupArtistSection(sheetBinding);

        bottomSheetDialog.setContentView(sheetBinding.getRoot());
        bottomSheetDialog.show();
    }

    private void setupLibraryButton(ActivityListMoreInfoBottomSheetBinding sheetBinding) {
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        SavedLibrariesAudius savedLibraries = sharedPreferenceManager.getSavedLibrariesData();

        boolean isInLibrary = isPlaylistInLibrary(playlistData, savedLibraries);
        sheetBinding.addToLibrary.getTitleTextView().setText(isInLibrary ? "Remove from library" : "Add to library");
        sheetBinding.addToLibrary.getIconImageView().setImageResource(isInLibrary ? R.drawable.round_close_24 : R.drawable.round_add_24);

        sheetBinding.addToLibrary.setOnClickListener(view -> {
            binding.addToLibrary.performClick();
            sheetBinding.getRoot().getParent().getParent();
        });
    }

    private void setupArtistSection(ActivityListMoreInfoBottomSheetBinding sheetBinding) {
        for (ArtistData artist : artistData) {
            try {
                String imgUrl = artist.image().isEmpty() ? "" : artist.image();
                BottomSheetItemView artistView = new BottomSheetItemView(this, artist.name(), imgUrl, artist.id());
                /*artistView.setOnClickListener(view -> {
                    startActivity(new Intent(this, ArtistProfileActivity.class)
                            .putExtra("data", new Gson().toJson(
                                    new BasicDataRecord(artist.id(), artist.name(), "", imgUrl)))
                    );
                });*/
                sheetBinding.main.addView(artistView);
            } catch (Exception e) {
                Log.e("ListActivity", "Error setting up artist view", e);
            }
        }
    }

    private void onMoreIconClickedUserCreated() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.MyBottomSheetDialogTheme);
        final UserCreatedListActivityMoreBottomSheetBinding sheetBinding = 
            UserCreatedListActivityMoreBottomSheetBinding.inflate(getLayoutInflater());

        sheetBinding.albumTitle.setText(binding.albumTitle.getText());
        sheetBinding.albumSubTitle.setText(binding.albumSubTitle.getText());
        Picasso.get().load(Uri.parse(playlistData.artwork().x480())).into(sheetBinding.coverImage);

        sheetBinding.removeLibrary.setOnClickListener(view -> {
            bottomSheetDialog.dismiss();
            binding.addToLibrary.performClick();
        });

        bottomSheetDialog.setContentView(sheetBinding.getRoot());
        bottomSheetDialog.show();
    }

    private void updatePlaylistInLibraryStatus() {
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(this);
        SavedLibrariesAudius savedLibraries = sharedPreferenceManager.getSavedLibrariesData();
        
        binding.addToLibrary.setImageResource(
            isPlaylistInLibrary(playlistData, savedLibraries) ? 
            R.drawable.round_done_24 : 
            R.drawable.round_add_24
        );
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
}