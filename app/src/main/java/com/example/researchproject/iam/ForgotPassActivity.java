package com.example.researchproject.iam;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.checkerframework.checker.nullness.qual.NonNull;

public class ForgotPassActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_pass);
        Button btnsenemail = findViewById(R.id.btnsenemail);
        EditText edmail = findViewById(R.id.edmail);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();



        btnsenemail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = edmail.getText().toString();
                if (email.isEmpty()){
                    Toast.makeText(ForgotPassActivity.this, "Email cannot be left blank!", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(ForgotPassActivity.this, "We have sent an email to your inbox to change your password!" + email, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ForgotPassActivity.this, "Unable to send email. Please check your email address.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


            }
        });
    }
}
