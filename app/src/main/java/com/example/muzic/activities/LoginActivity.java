package com.example.muzic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.UnstableApi;

import com.example.muzic.R;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.FirebaseAuth;
import com.example.muzic.utils.SettingsSharedPrefManager;
import com.example.muzic.utils.ThemeManager;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button login, loginemail;
    private TextView registerPrompt, forgotPassword;
    private FirebaseAuth mAuth;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyThemeMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);

        login = findViewById(R.id.btn_login);
        login.setBackgroundResource(R.drawable.bg_gradient_orange);
        login.setBackgroundTintList(null);

        loginemail = findViewById(R.id.btn_email_link_login);
        loginemail.setBackgroundResource(R.drawable.bg_gradient_orange);
        loginemail.setBackgroundTintList(null);

        registerPrompt = findViewById(R.id.tv_register_prompt);
        forgotPassword = findViewById(R.id.tv_forgot_password);

        forgotPassword.setOnClickListener(v -> {
            String userEmail = email.getText().toString();
            if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.sendPasswordResetEmail(userEmail)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Please check your email", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        registerPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            finish();
        });

        login.setOnClickListener(v -> {
            String txtEmail = email.getText().toString();
            String txtPassword = password.getText().toString();

            if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.signInWithEmailAndPassword(txtEmail, txtPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null && user.isEmailVerified()) {
                                    // Email đã xác minh → vào app
                                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Email chưa xác minh
                                    FirebaseAuth.getInstance().signOut(); // Đăng xuất luôn
                                    Toast.makeText(LoginActivity.this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        loginemail.setOnClickListener(v -> {
            String userEmail = email.getText().toString();
            if (TextUtils.isEmpty(userEmail)) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else {
                ActionCodeSettings settings = ActionCodeSettings.newBuilder()
                        .setHandleCodeInApp(true)
                        .setUrl("https://uitmuzic.page.link/loginemail")
                        .setAndroidPackageName("com.example.muzic", true, null)
                        .build();

                mAuth.sendSignInLinkToEmail(userEmail, settings)
                        .addOnSuccessListener(unused -> {
                            Toast.makeText(this, "Please check your email", Toast.LENGTH_LONG).show();
                            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putString("emailForSignIn", userEmail)
                                    .apply();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        });
            }
        });
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
}