package com.example.researchproject.Profile;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.Scanner;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
//import com.squareup.picasso.Picasso;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView imgProfile, btnUpload;
    private EditText etNickname, etPhone, etEmail, etFacebook, etGoogle;
    private Button btnSave;
    private Uri imageUri;
    private FirebaseAuth auth;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752";
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

        // Ánh xạ view
        imgProfile = findViewById(R.id.img_profile);
        btnUpload = findViewById(R.id.btn_upload);
        etNickname = findViewById(R.id.et_nickname);
        etPhone = findViewById(R.id.et_phone);
        etEmail = findViewById(R.id.et_email);
        etFacebook = findViewById(R.id.et_facebook);
        etGoogle = findViewById(R.id.et_google);
        btnSave = findViewById(R.id.btn_save);

        loadUserData();

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        imgProfile.setImageURI(imageUri); // Hiển thị ảnh đã chọn
                    }
                }
        );
        // Nút chọn ảnh đại diện
        btnUpload.setOnClickListener(v -> {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES}, REQUEST_STORAGE_PERMISSION);
        } else {
            openFileChooser();
        }
        });

        // Nút lưu thông tin
        btnSave.setOnClickListener(v -> saveUserData());
    }



    private void loadUserData() {
        userRef.get().addOnSuccessListener(snapshot -> {
            etNickname.setText(snapshot.child("nickname").getValue(String.class));
            etPhone.setText(snapshot.child("phoneNumber").getValue(String.class));
            etEmail.setText(snapshot.child("email").getValue(String.class));
            etFacebook.setText(snapshot.child("facebook").getValue(String.class));
            etGoogle.setText(snapshot.child("google").getValue(String.class));

            String imageUrl = snapshot.child("profileImageUrl").getValue(String.class);
            if (imageUrl != null) {
                // Load ảnh từ Imgur vào ImageView
            }
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Lọc chỉ chọn file ảnh
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Chỉ hiện các file có thể mở được
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData(); // Lưu URI vào biến toàn cục
            Glide.with(this).load(imageUri).into(imgProfile); // Hiển thị ảnh đã chọn
        }
    }
    // Xử lý khi người dùng cấp quyền truy cập bộ nhớ
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openFileChooser();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền truy cập bộ nhớ để chọn ảnh!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
        } else {
            openFileChooser();
        }
    }

    private void saveUserData() {
        progressDialog = ProgressDialog.show(this, "Uploading", "Please wait...", true);

        if (imageUri != null) {
            uploadImageToImgur(imageUri, this::updateUserProfile, errorMessage -> {
                progressDialog.dismiss();
                Toast.makeText(EditProfileActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            });
        } else {
            updateUserProfile(null);
        }
    }


    private void updateUserProfile(String imageUrl) {
        String nickname = etNickname.getText().toString();
        String phone = etPhone.getText().toString();
        String email = etEmail.getText().toString();
        String facebook = etFacebook.getText().toString();
        String google = etGoogle.getText().toString();

        userRef.child("nickname").setValue(nickname);
        userRef.child("phoneNumber").setValue(phone);
        userRef.child("email").setValue(email);
        userRef.child("facebook").setValue(facebook);
        userRef.child("google").setValue(google);

        if (imageUrl != null) {
            userRef.child("profileImageUrl").setValue(imageUrl);
        }

        progressDialog.dismiss();
        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, new Intent().putExtra("updated", true));
        finish();
    }

    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = Objects.requireNonNull(inputStream).read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                byte[] imageBytes = baos.toByteArray();
                String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                OkHttpClient client = new OkHttpClient();
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("image", encodedImage)
                        .build();
                Request request = new Request.Builder()
                        .url("https://api.imgur.com/3/image")
                        .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseData);
                    String imageUrl = jsonObject.getJSONObject("data").getString("link");
                    runOnUiThread(() -> successListener.onSuccess(imageUrl));
                } else {
                    runOnUiThread(() -> failureListener.onFailure("Upload failed"));
                }
            } catch (Exception e) {
                runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
            }
        }).start();
    }

    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }

    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }


//    private void saveUserData() {
//        String nickname = etNickname.getText().toString();
//        String phone = etPhone.getText().toString();
//        String email = etEmail.getText().toString();
//        String facebook = etFacebook.getText().toString();
//        String google = etGoogle.getText().toString();
//
//        // Cập nhật dữ liệu vào Firebase
//        userRef.child("nickname").setValue(nickname);
//        userRef.child("phoneNumber").setValue(phone);
//        userRef.child("email").setValue(email);
//        userRef.child("facebook").setValue(facebook);
//        userRef.child("google").setValue(google);
//
//        // Nếu có ảnh mới, cập nhật lên Firebase Storage
//        if (imageUri != null) {
//            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
//                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                    userRef.child("profileImageUrl").setValue(uri.toString());
//                });
//            });
//        }
//
//        Toast.makeText(this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
//
//        // Quay về SettingFragment và gửi dữ liệu mới
//        Intent resultIntent = new Intent();
//        resultIntent.putExtra("updated", true);
//        setResult(RESULT_OK, resultIntent);
//        finish();
//    }
}