package com.example.researchproject.ui;


import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

public class PostActivity extends AppCompatActivity {

    private EditText edtTitle, edtServiceInfo, edtPrice, edtRentalTime, edtAddress, edtContact;
    private Button  btnPost;
    private ImageView imgService;
    private Uri imageUri;
    private Button btnUploadImage;


    private DatabaseReference databaseReference;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752"; // Thay bằng Client ID của bạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Ánh xạ giao diện
        edtTitle = findViewById(R.id.edtTitle);
        edtServiceInfo = findViewById(R.id.edtServiceInfo);
        edtPrice = findViewById(R.id.edtPrice);
        edtRentalTime = findViewById(R.id.edtRentalTime);
        edtAddress = findViewById(R.id.edtAddress);
        edtContact = findViewById(R.id.edtContact);
        btnPost = findViewById(R.id.btnPost);
        imgService = findViewById(R.id.imgService);
        btnUploadImage = findViewById(R.id.btnUploadImage);

        btnUploadImage.setOnClickListener(v -> openFileChooser());
        btnUploadImage.setOnClickListener(v -> checkStoragePermission());

        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");



// Kiểm tra quyền truy cập bộ nhớ trước khi chọn ảnh
        btnUploadImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSION);
            } else {
                openFileChooser();
            }
        });

        // Xử lý đăng bài
        btnPost.setOnClickListener(v -> uploadPost());
    }



    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*"); // Lọc chỉ chọn file ảnh
        intent.addCategory(Intent.CATEGORY_OPENABLE); // Chỉ hiện các file có thể mở được
        startActivityForResult(Intent.createChooser(intent, "Chọn hình ảnh"), PICK_IMAGE_REQUEST);
    }

    // Xử lý kết quả sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgService); // Hiển thị ảnh đã chọn
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

    // Xử lý đăng bài
    private void uploadPost() {
        String title = edtTitle.getText().toString().trim();
        String serviceInfo = edtServiceInfo.getText().toString().trim();
        String price = edtPrice.getText().toString().trim();
        String rentalTime = edtRentalTime.getText().toString().trim();
        String address = edtAddress.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();

        if (title.isEmpty() || serviceInfo.isEmpty() || price.isEmpty() || rentalTime.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang đăng tin...");
        progressDialog.show();

        if (imageUri != null) {
            uploadImageToImgur(imageUri, (imageUrl) -> {
                savePostToDatabase(title, serviceInfo, price, rentalTime, address, contact, imageUrl);
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
                finish();
            }, (errorMessage) -> {
                progressDialog.dismiss();
                Toast.makeText(PostActivity.this, "Lỗi khi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
            });
        } else {
            savePostToDatabase(title, serviceInfo, price, rentalTime, address, contact, "");
            progressDialog.dismiss();
            Toast.makeText(this, "Đăng tin thành công!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Upload ảnh lên Imgur
    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
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

            // ✅ Fix: Thêm key "image" để gửi đúng định dạng yêu cầu của Imgur
            RequestBody requestBody = RequestBody.create("image=" + encodedImage,
                    MediaType.parse("application/x-www-form-urlencoded"));

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID " + IMGUR_CLIENT_ID)
                    .post(requestBody)
                    .build();

            new Thread(() -> {
                try {
                    Response response = client.newCall(request).execute();
                    if (response.isSuccessful()) {
                        String responseData = Objects.requireNonNull(response.body()).string();
                        JSONObject jsonObject = new JSONObject(responseData);
                        String imageUrl = jsonObject.getJSONObject("data").getString("link");

                        runOnUiThread(() -> successListener.onSuccess(imageUrl));
                    } else {
                        runOnUiThread(() -> failureListener.onFailure("Tải lên Imgur thất bại."));
                    }
                } catch (Exception e) {
                    runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
                }
            }).start();

        } catch (Exception e) {
            failureListener.onFailure(e.getMessage());
        }
    }

    // Lưu thông tin bài đăng vào Firebase
    private void savePostToDatabase(String title, String serviceInfo, String price, String rentalTime, String address, String contact, String imageUrl) {
        String postId = databaseReference.push().getKey();
        HashMap<String, Object> postMap = new HashMap<>();
        postMap.put("title", title);
        postMap.put("serviceInfo", serviceInfo);
        postMap.put("price", price);
        postMap.put("rentalTime", rentalTime);
        postMap.put("address", address);
        postMap.put("contact", contact);
        postMap.put("imageUrl", imageUrl);

        if (postId != null) {
            databaseReference.child(postId).setValue(postMap);
        }
    }

    // Interfaces để xử lý callback khi upload ảnh
    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }

    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }
}
