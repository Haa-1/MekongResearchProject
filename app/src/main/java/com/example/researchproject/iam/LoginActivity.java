package com.example.researchproject.iam;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.HomeMekong;
import com.example.researchproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private Button btn_login_submit;
    private EditText et_login_user, et_password;
    private TextView tv_re_password, tv_signup;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Khởi tạo UI
        btn_login_submit = findViewById(R.id.btn_login_submit);
        et_login_user = findViewById(R.id.et_login_user);
        et_password = findViewById(R.id.et_password);
        tv_re_password = findViewById(R.id.tv_re_password);
        tv_signup = findViewById(R.id.tv_signup);
        mAuth = FirebaseAuth.getInstance();


        Intent intent = getIntent();
        if (intent!=null) {
            Bundle ex = intent.getExtras();
            if (ex!=null){
                et_login_user.setText(ex.getString("email"));
                et_password.setText(ex.getString("password"));
            }}


        btn_login_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = et_login_user.getText().toString();
                String password = et_password.getText().toString();
                if (email.isEmpty()||password.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Do not leave blank!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!isValidEmail(email)) {
                    Toast.makeText(LoginActivity.this, "Invalid email address!", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(LoginActivity.this, HomeMekong.class);
                                    startActivity(intent);

                                } else {
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Wrong Account Or Password!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        tv_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SocialRegisterActivity.class);
                startActivity(intent);
            }
        });
        tv_re_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent in = new Intent(LoginActivity.this, ForgotPassActivity.class);
                startActivity(in);
            }
        });
    }
    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}
