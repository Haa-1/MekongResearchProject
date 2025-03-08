package com.example.researchproject.iam;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.researchproject.HomeMekong;
import com.example.researchproject.InformationActivity;
import com.example.researchproject.MekoAI;
import com.example.researchproject.R;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import androidx.annotation.NonNull;
public class PostAdActivity extends AppCompatActivity {
    private EditText edtAdTitle;
    private ImageView imgAd;
    private Button btnSelectImage, btnPostAd;
    private Uri imageUri;
    private DatabaseReference databaseReference;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 100;
    private static final String IMGUR_CLIENT_ID = "eedb98d8d059752"; // Thay bằng Client ID của bạn
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ad);
        // 🟢 Ánh xạ View
        edtAdTitle = findViewById(R.id.edtAdTitle);
        imgAd = findViewById(R.id.imgAd);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnPostAd = findViewById(R.id.btnPostAd);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        Button btnPost = findViewById(R.id.btnPost);
        btnPost.setOnClickListener(v -> {
            Intent intent = new Intent(PostAdActivity.this, PostActivity.class);
            startActivity(intent);
        });
        // Xử lý sự kiện khi chọn item trong menu
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(PostAdActivity.this, HomeMekong.class));
                } else if (itemId == R.id.nav_ai) {
                    startActivity(new Intent(PostAdActivity.this, MekoAI.class)); // Sửa lại tên đúng
                } else if (itemId == R.id.nav_post) {
                    startActivity(new Intent(PostAdActivity.this, PostActivity.class));
                } else if (itemId == R.id.nav_cart) {
                    startActivity(new Intent(PostAdActivity.this, CartActivity.class));
                } else if (itemId == R.id.nav_info) {
                    startActivity(new Intent(PostAdActivity.this, InformationActivity.class));
                } else {
                    return false;
                }
                return true;
            }
        });
        // 🟡 Kết nối Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Advertisements");
        // 📁 Chọn hình ảnh
        btnSelectImage.setOnClickListener(v -> openFileChooser());
        // 🚀 Đăng quảng cáo
        btnPostAd.setOnClickListener(v -> uploadAd());
    }
    // ✅ Hàm mở bộ nhớ để chọn ảnh
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }
    // ✅ Nhận kết quả sau khi chọn ảnh
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imgAd); // Hiển thị ảnh đã chọn
        }
    }

    // ✅ Xử lý đăng quảng cáo (Upload ảnh lên Imgur và lưu vào Firebase)
    private void uploadAd() {
        String adTitle = edtAdTitle.getText().toString().trim();

        if (adTitle.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề và chọn hình ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Đang tải hình ảnh lên Imgur...");
        progressDialog.show();

        uploadImageToImgur(imageUri, new OnUploadSuccessListener() {
            @Override
            public void onSuccess(String imageUrl) {
                progressDialog.dismiss();
                saveAdToFirebase(adTitle, imageUrl);
            }
        }, new OnUploadFailureListener() {
            @Override
            public void onFailure(String errorMessage) {
                progressDialog.dismiss();
                Toast.makeText(PostAdActivity.this, "Lỗi tải ảnh: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Upload hình ảnh lên Imgur
    private void uploadImageToImgur(Uri imageUri, OnUploadSuccessListener successListener, OnUploadFailureListener failureListener) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
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

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> failureListener.onFailure(e.getMessage()));
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String responseData = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseData);
                            String imageUrl = jsonObject.getJSONObject("data").getString("link");

                            runOnUiThread(() -> successListener.onSuccess(imageUrl));
                        } catch (JSONException e) {
                            runOnUiThread(() -> failureListener.onFailure("Lỗi xử lý dữ liệu từ Imgur"));
                        }
                    } else {
                        runOnUiThread(() -> failureListener.onFailure("Lỗi tải ảnh lên Imgur"));
                    }
                }
            });

        } catch (Exception e) {
            failureListener.onFailure(e.getMessage());
        }
    }

    // ✅ Lưu thông tin quảng cáo vào Firebase Database
    private void saveAdToFirebase(String title, String imageUrl) {
        DatabaseReference adsRef = FirebaseDatabase.getInstance().getReference("Ads");
        String adId = adsRef.push().getKey();
        Ad ad = new Ad(adId, title, imageUrl);

        adsRef.child(adId).setValue(ad)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Đăng quảng cáo thành công!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Lỗi khi đăng quảng cáo!", Toast.LENGTH_SHORT).show());
    }

    // ✅ Interfaces để xử lý kết quả upload
    interface OnUploadSuccessListener {
        void onSuccess(String imageUrl);
    }

    interface OnUploadFailureListener {
        void onFailure(String errorMessage);
    }
}
