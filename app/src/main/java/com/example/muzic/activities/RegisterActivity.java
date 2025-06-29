package com.example.muzic.activities;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import com.example.muzic.R;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText email;
    private EditText password;
    private Button signUp;
    private TextView loginPrompt;
    private Spinner genderSpinner;
    private TextView tvDob;
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
        signUp.setBackgroundResource(R.drawable.bg_gradient_orange);
        signUp.setBackgroundTintList(null);

        loginPrompt = findViewById(R.id.tv_login_prompt);

        genderSpinner = findViewById(R.id.spinner_gender);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, R.layout.spinner_selected_item);
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        genderSpinner.setAdapter(adapter);

        tvDob = findViewById(R.id.tv_dob);
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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        signUp.setOnClickListener(v -> {
            String txtName = name.getText().toString();
            String txtEmail = email.getText().toString();
            String gender = genderSpinner.getSelectedItem().toString();
            String birth = tvDob.getText().toString();
            String txtPassword = password.getText().toString();

            if (TextUtils.isEmpty(txtEmail) || TextUtils.isEmpty(txtPassword) || TextUtils.isEmpty(txtName) || TextUtils.isEmpty(gender) || TextUtils.isEmpty(birth)) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (txtPassword.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else {
                mAuth.createUserWithEmailAndPassword(txtEmail, txtPassword)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Gửi email xác minh
                                    firebaseUser.sendEmailVerification()
                                            .addOnCompleteListener(verifyTask -> {
                                                if (verifyTask.isSuccessful()) {
                                                    String userId = firebaseUser.getUid();

                                                    HashMap<String, Object> map = new HashMap<>();
                                                    map.put("name", txtName);
                                                    map.put("id", userId);
                                                    map.put("email", txtEmail);
                                                    map.put("gender", gender);
                                                    map.put("birthday", birth);

                                                    db.collection("users").document(userId).set(map)
                                                            .addOnCompleteListener(task1 -> {
                                                                if (task1.isSuccessful()) {
                                                                    Toast.makeText(RegisterActivity.this,
                                                                            "Registration successful!\nPlease check your email to verify your account.",
                                                                            Toast.LENGTH_LONG).show();

                                                                    // Đăng xuất để chờ xác minh email
                                                                    FirebaseAuth.getInstance().signOut();

                                                                    // Quay về màn hình đăng nhập
                                                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    Toast.makeText(RegisterActivity.this, "Firestore failed: " + task1.getException(), Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "Failed to send verification email.", Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }
} 