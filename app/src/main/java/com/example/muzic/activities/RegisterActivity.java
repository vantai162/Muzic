package com.example.muzic.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.muzic.R;
/*import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;*/

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText name;
    private EditText password;
    private Button signUp;
    private TextView loginPrompt;

    //private DatabaseReference mRootRef;
    //private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.et_name);
        password = findViewById(R.id.et_password);
        signUp = findViewById(R.id.btn_sign_up);
        loginPrompt = findViewById(R.id.tv_login_prompt);

        //mRootRef = FirebaseDatabase.getInstance().getReference();
        //mAuth = FirebaseAuth.getInstance();

        loginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });

        signUp.setOnClickListener(v -> {
            String txtName = name.getText().toString();
            String txtPassword = password.getText().toString();

            if (TextUtils.isEmpty(txtName) || TextUtils.isEmpty(txtPassword)) {
                Toast.makeText(RegisterActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (txtPassword.length() < 8) {
                Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else {
                //registerUser(txtName, txtPassword);
            }
        });
    }

    /*private void registerUser(final String name, final String password) {
        String email = name.replaceAll("\\s", "") + "@muzic.app";

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String userId = mAuth.getCurrentUser().getUid();
                DatabaseReference userRef = mRootRef.child("Users").child(userId);

                HashMap<String, Object> map = new HashMap<>();
                map.put("name", name);
                map.put("id", userId);
                map.put("email", email);

                userRef.setValue(map).addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }*/
} 