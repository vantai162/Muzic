package com.example.muzic.activities;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.example.muzic.R;
import com.example.muzic.databinding.ActivityUserBinding;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.example.muzic.utils.SettingsSharedPrefManager;
import com.example.muzic.utils.ThemeManager;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UserActivity extends AppCompatActivity {
    private ActivityUserBinding binding;
    private EditText name, password, newpassword, confirmnewpassword;
    private Button update;
    private Spinner genderSpinner;
    private TextView tvDob;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private String oldName = "", oldGender = "", oldBirthday = "";

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeMode();
        super.onCreate(savedInstanceState);
        
        binding = ActivityUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        String userID = mAuth.getUid();

        name = binding.etName;
        password = binding.etPassword;
        update = binding.btnUpdate;
        newpassword = binding.etNewPassword;
        confirmnewpassword = binding.etConfirmNewPassword;

        // Setup spinner
        genderSpinner = binding.spinnerGender;
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, R.layout.spinner_selected_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        // Setup datepicker
        tvDob = binding.tvDob;
        tvDob.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        tvDob.setText(dob);
                    }, year, month, day);
            datePickerDialog.show();
        });

        // Setup toolbar
        setSupportActionBar(binding.collapsingToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Setup collapsing toolbar
        binding.collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        // Setup name và image
        db.collection("users")
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        oldName = documentSnapshot.getString("name");
                        String profilePicture = documentSnapshot.getString("profilePicture");
                        oldGender = documentSnapshot.getString("gender");
                        oldBirthday = documentSnapshot.getString("birthday");

                        if (name != null) {
                            binding.userName.setText(oldName);
                            name.setText(oldName);
                            binding.collapsingToolbarLayout.setTitle(oldName);
                        }

                        if (profilePicture != null && !profilePicture.isEmpty()) {
                            Picasso.get()
                                    .load(profilePicture)
                                    .placeholder(R.drawable.bolt_24px)
                                    .error(R.drawable.bolt_24px)
                                    .into(binding.userImg);
                        } else {
                            binding.userImg.setImageResource(R.drawable.bolt_24px);
                        }

                        if (oldGender != null) {
                            int position = adapter.getPosition(oldGender);
                            if (position >= 0) {
                                genderSpinner.setSelection(position);
                            }
                        }

                        if (oldBirthday != null) {
                            tvDob.setText(oldBirthday);
                        }
                    } else {
                        Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show();
                        binding.userImg.setImageResource(R.drawable.bolt_24px);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "User load failed!", Toast.LENGTH_SHORT).show();
                    binding.userImg.setImageResource(R.drawable.bolt_24px);
                });

        // Setup button cập nhật
        update.setOnClickListener(v -> {
            String txtName = name.getText().toString().trim();
            String gender = genderSpinner.getSelectedItem().toString();
            String birth = tvDob.getText().toString().trim();

            String txtPassword = password.getText().toString().trim();
            String txtNewPassword = newpassword.getText().toString().trim();
            String txtConfirmNewPassword = confirmnewpassword.getText().toString().trim();

            Map<String, Object> updates = new HashMap<>();

            if (!txtName.equals(oldName)) updates.put("name", txtName);
            if (!gender.equals(oldGender)) updates.put("gender", gender);
            if (!birth.equals(oldBirthday)) updates.put("birthday", birth);

            if (!updates.isEmpty()) {
                db.collection("users").document(userID)
                        .update(updates)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Information updated!", Toast.LENGTH_SHORT).show();
                            oldName = txtName;
                            oldGender = gender;
                            oldBirthday = birth;
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }

            boolean wantToChangePassword = !TextUtils.isEmpty(txtPassword) &&
                                           !TextUtils.isEmpty(txtNewPassword) &&
                                           !TextUtils.isEmpty(txtConfirmNewPassword);

            if (wantToChangePassword) {
                if (txtNewPassword.length() < 8) {
                    Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!txtNewPassword.equals(txtConfirmNewPassword)) {
                    Toast.makeText(this, "The confirmation password doesn't match the new password", Toast.LENGTH_SHORT).show();
                    return;
                }

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), txtPassword);
                user.reauthenticate(credential)
                        .addOnSuccessListener(unused -> {
                            user.updatePassword(txtNewPassword)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Your password has been changed successfully", Toast.LENGTH_SHORT).show();
                                        password.setText("");
                                        newpassword.setText("");
                                        confirmnewpassword.setText("");
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Failed to change password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Incorrect password. Please try again", Toast.LENGTH_SHORT).show();
                        });
            } else if (!TextUtils.isEmpty(txtPassword) || !TextUtils.isEmpty(txtNewPassword) || !TextUtils.isEmpty(txtConfirmNewPassword)) {
                Toast.makeText(this, "All password fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup button add image
        binding.btnPickImage.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            binding.userImg .setImageURI(imageUri);
            uploadImageToFirebase(imageUri);
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        if (uri == null) return;

        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("profile_pictures/" + mAuth.getUid() + "_" + UUID.randomUUID());

        storageRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(downloadUri -> {
                                String url = downloadUri.toString();

                                Map<String, Object> img = new HashMap<>();
                                img.put("profilePicture", url);

                                db.collection("users").document(mAuth.getUid())
                                        .update("profilePicture", url)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Saved to users!", Toast.LENGTH_SHORT).show();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void applyThemeMode() {
        SettingsSharedPrefManager settings = new SettingsSharedPrefManager(this);
        String mode = settings.getDarkMode();
        if (mode.equals("on")) {
            ThemeManager.applyNightMode(true);
        } else if (mode.equals("off")) {
            ThemeManager.applyNightMode(false);
        } else {
            ThemeManager.applySystemDefaultMode();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}