package com.example.researchproject.iam;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/*import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;*/

import com.example.researchproject.R;

import java.util.Arrays;

public class RegisterActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    //private GoogleSignInClient googleSignInClient;
 //   private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button btnGoogle = findViewById(R.id.btnGoogle);
        Button btnFacebook = findViewById(R.id.btnFacebook);
        Button btnUser = findViewById(R.id.btnUser);
        Button btnApple = findViewById(R.id.btnApple);

        // Google Sign-In Setup
       /*ignInClient = GoogleSignIn.getClient(this, gso);

        // Facebook Login Setup
        callbackManager = CallbackManager.Factory.create();

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
        btnFacebook.setOnClickListener(v -> signInWithFacebook());
        btnUser.setOnClickListener(v -> signInWithEmailOrPhone());
        btnApple.setOnClickListener(v -> signInWithApple());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Toast.makeText(RegisterActivity.this, "Facebook Login Successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(RegisterActivity.this, "Facebook Login Cancelled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(RegisterActivity.this, "Facebook Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithEmailOrPhone() {
        Toast.makeText(this, "Implement email/phone login", Toast.LENGTH_SHORT).show();
        // Chuyển đến màn hình đăng nhập bằng Email hoặc Số điện thoại
    }

    private void signInWithApple() {
        Toast.makeText(this, "Implement Apple login", Toast.LENGTH_SHORT).show();
        // Triển khai đăng nhập Apple nếu ứng dụng hỗ trợ
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Toast.makeText(this, "Google Login Successful: " + account.getEmail(), Toast.LENGTH_SHORT).show();
            } catch (ApiException e) {
                Toast.makeText(this, "Google Login Failed", Toast.LENGTH_SHORT).show();
            }
        }
    }*/
    }
}

