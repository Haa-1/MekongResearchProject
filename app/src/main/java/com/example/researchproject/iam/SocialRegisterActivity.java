package com.example.researchproject.iam;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class SocialRegisterActivity extends AppCompatActivity {
    TextView tv_login;
    EditText editTextMobile;
    Button btnUser, btnFacebook, btnGoogle, btnApple, btn_continue_phone;
    ImageButton imgbtn_close;
    private static final int RC_SIGN_IN = 100;
    //private GoogleSignInClient googleSignInClient;
 //   private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social_register);
        tv_login = (TextView) findViewById(R.id.tv_login);
        btnFacebook = (Button) findViewById(R.id.btnFacebook);
        btnGoogle = (Button) findViewById(R.id.btnGoogle);
        btnApple = (Button) findViewById(R.id.btnApple);
        btnUser = (Button) findViewById(R.id.btnUser);
        btn_continue_phone = (Button) findViewById(R.id.btn_continue_phone);
        imgbtn_close = (ImageButton) findViewById(R.id.imgbtn_close);
        editTextMobile = (EditText) findViewById(R.id.editTextMobile);

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SocialRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        imgbtn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SocialRegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SocialRegisterActivity.this, EmailRegisterActivity.class);
                startActivity(intent);
            }
        });

        //Dang ky tai khoan bang so dien thoai
        btn_continue_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = editTextMobile.getText().toString().trim();
                if(mobile.isEmpty() || mobile.length() < 10){
                    editTextMobile.setError("Enter a valid mobile");
                    editTextMobile.requestFocus();
                    return;
                }
                Intent intent = new Intent(SocialRegisterActivity.this, VerifyPhoneActivity.class);
                intent.putExtra("mobile", mobile);
                startActivity(intent);
            }
        });



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

