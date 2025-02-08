package com.example.researchproject.iam;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterUserActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText et_fullname, et_email, et_phone, et_register_password, et_dob, et_nickname;
    private Button btn_save;
    private ImageButton imgbtn_close;
    private TextView tv_login2;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        // Khởi tạo Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Ánh xạ các View
        et_fullname = findViewById(R.id.et_fullname);
        et_email = findViewById(R.id.et_email);
        et_phone = findViewById(R.id.et_phone);
        et_register_password = findViewById(R.id.et_register_password);
        et_dob = findViewById(R.id.et_dob);
        et_nickname = findViewById(R.id.et_nickname);
        btn_save = findViewById(R.id.btn_save);
        imgbtn_close = findViewById(R.id.imgbtn_close);
        tv_login2 = findViewById(R.id.tv_login2);

        // Xử lý sự kiện khi nhấn nút Save (Đăng ký)
        btn_save.setOnClickListener(view -> registerUser());

        // Xử lý sự kiện khi nhấn nút đóng
        imgbtn_close.setOnClickListener(view -> finish());

        // Chuyển sang màn hình đăng nhập
        tv_login2.setOnClickListener(view -> {
            Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void registerUser() {
        String fullname = et_fullname.getText().toString().trim();
        String email = et_email.getText().toString().trim();
        String phone = et_phone.getText().toString().trim();
        String password = et_register_password.getText().toString().trim();
        String dob = et_dob.getText().toString().trim();
        String nickname = et_nickname.getText().toString().trim();

        if (fullname.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || dob.isEmpty() || nickname.isEmpty()) {
            Toast.makeText(this, "Please enter complete information!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Invalid email address!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6 || !Character.isUpperCase(password.charAt(0))) {
            Toast.makeText(this, "Password must be at least 6 characters and capitalize the first letter!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Đăng ký tài khoản với Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(RegisterUserActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();

                        // Chuyển sang màn hình đăng nhập
                        Intent intent = new Intent(RegisterUserActivity.this, LoginActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("password", password);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(RegisterUserActivity.this, "Registration failed. Please try again!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}

