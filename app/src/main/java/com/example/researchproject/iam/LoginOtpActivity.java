package com.example.researchproject.iam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.MainActivity;
import com.example.researchproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {
    private EditText edphone, edotp;
    private Button btnloginotp, btngetotp;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String smsVerify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_otp);

        edotp = findViewById(R.id.edotp);
        edphone = findViewById(R.id.edphone);
        btngetotp = findViewById(R.id.btngetotp);
        btnloginotp = findViewById(R.id.btnloginotp);
        mAuth = FirebaseAuth.getInstance();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                edotp.setText(phoneAuthCredential.getSmsCode());
            }
            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                Toast.makeText(LoginOtpActivity.this, "The number of code submissions exceeded. Please try again later!", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                smsVerify = verificationId;
            }
        };

        btngetotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber = edphone.getText().toString().trim();

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginOtpActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                } else if (!phoneNumber.matches("[0-9]+")) {
                    Toast.makeText(LoginOtpActivity.this, "Phone numbers must contain only numeric characters", Toast.LENGTH_SHORT).show();
                } else {
                    getOTP(phoneNumber);
                }
            }
        });
        btnloginotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userOTP = edotp.getText().toString();

                if (userOTP.isEmpty()) {
                    Toast.makeText(LoginOtpActivity.this, "Please enter OTP!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Gọi hàm verifyOTP để xác nhận OTP
                verifyOtp(userOTP);
            }
        });
    }
    private void getOTP (String phoneNumber){
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+84"+phoneNumber)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        // If no activity is passed, reCAPTCHA verification can not be used.
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }
    private void verifyOtp (String code){
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(smsVerify, code);
        signInWithPhoneAuthCredential(credential);
    }
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(LoginOtpActivity.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(LoginOtpActivity.this, MainActivity.class);
                            startActivity(intent);

                            FirebaseUser user = task.getResult().getUser();
                            // Update UI
                        } else {
                            // Sign in failed, display a message and update the UI
                            edotp.setError("OTP Incorrect");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}
