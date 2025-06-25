package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import com.example.muzic.R;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText email;
    private EditText password;
    private Button signUp;
    private TextView loginPrompt;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @OptIn(markerClass = UnstableApi.class)
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.et_name);
        email = findViewById(R.id.et_email);
        password = findViewById(R.id.et_password);
        signUp = findViewById(R.id.btn_sign_up);
        loginPrompt = findViewById(R.id.tv_login_prompt);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        signUp.setOnClickListener(v -> {
            String txtName = name.getText().toString();
            String txtEmail = email.getText().toString();
            String txtPassword = password.getText().toString();

            if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword)) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (txtPassword.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(txtEmail, txtPassword)
                        .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();

                        HashMap<String, Object> map = new HashMap<>();
                        map.put("name", txtName);
                        map.put("id", userId);
                        map.put("email", txtEmail);

                        db.collection("users").document(userId).set(map)
                                .addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Firestore failed: " + task1.getException(), Toast.LENGTH_LONG).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }});
            }
        });
    }
} 