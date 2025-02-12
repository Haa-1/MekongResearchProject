package com.example.researchproject.iam;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.HomeMekong;
import com.example.researchproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Calendar;

public class EmailRegisterActivity extends AppCompatActivity {

    private static final String TAG = "EmailRegisterActivity";

    private EditText et_email, et_register_password, et_dob, et_nickname;
    private Button btn_continue;
    private ImageButton imgbtn_close;
    private TextView tv_login2;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_register);



        // Khởi tạo Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Ánh xạ các View
        et_email = findViewById(R.id.et_email);
        et_register_password = findViewById(R.id.et_register_password);
        et_dob = findViewById(R.id.et_dob);
        et_nickname = findViewById(R.id.et_nickname);
        btn_continue = findViewById(R.id.btn_continue);
        imgbtn_close = findViewById(R.id.imgbtn_close);
        tv_login2 = findViewById(R.id.tv_login2);

        // Xử lý sự kiện khi nhấn nút Save (Đăng ký)
        btn_continue.setOnClickListener(view -> registerEmail());

        imgbtn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmailRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Xử lý sự kiện khi nhấn vào Date of Birth
        et_dob.setOnClickListener(view -> showDatePicker());


        // Chuyển sang màn hình đăng nhập
        tv_login2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(EmailRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDatePicker() {
        // Lấy ngày hiện tại
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Hiển thị DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {

                 // Định dạng ngày tháng năm thành chuỗi "dd/MM/yyyy"
                  String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                 et_dob.setText(selectedDate);
                },
                year, month, day
        );

            // Giới hạn ngày tối đa là ngày hiện tại (không cho chọn ngày trong tương lai)
            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

            datePickerDialog.show();

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){
            currentUser.reload();
        }
    }

    private void registerEmail() {
        String email = et_email.getText().toString().trim();
        String password = et_register_password.getText().toString().trim();
        String dob = et_dob.getText().toString().trim();
        String nickname = et_nickname.getText().toString().trim();

        if (password.isEmpty() || password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Character.isUpperCase(password.charAt(0))) {
            Toast.makeText(this, "Password must start with an uppercase letter!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (dob.isEmpty()) {
            Toast.makeText(this, "Date of Birth cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nickname.isEmpty()) {
            Toast.makeText(this, "Nickname cannot be empty!", Toast.LENGTH_SHORT).show();
            return;
        }


        // Đăng ký tài khoản với Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Intent intent = new Intent(EmailRegisterActivity.this, HomeMekong.class);
                            startActivity(intent);
                            finishAffinity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(EmailRegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }




}

