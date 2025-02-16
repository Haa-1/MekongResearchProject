package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.iam.Post;
import com.example.researchproject.iam.PostAdapterGrid;
import com.example.researchproject.iam.PostDetailActivity;
import com.example.researchproject.ui.PostActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;

public class HomeMekong extends AppCompatActivity {
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    private List<Post> postList;
    private DatabaseReference databaseReference;
    private GifImageView imgToggleChat;
    private EditText edtMessage;
    private Button btnSend;
    private TextView txtResponse, txtWelcome;
    private View chatLayout;
    private FirebaseAuth mAuth;
    private Button btnPost, btnLogout;
    private SearchView searchView;

    // API Key từ Google Cloud
    private final String API_KEY = "AIzaSyDpowdMhSBVL9qKWQ_eVrsi7FKbb4_Y3yE";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_mekong);

        // ✅ Khởi tạo FirebaseAuth và đặt ngôn ngữ
        mAuth = FirebaseAuth.getInstance();

        // ✅ Ánh xạ View
        postList = new ArrayList<>();
        gridView = findViewById(R.id.gridView);
        postAdapter = new PostAdapterGrid(this, postList);
        gridView.setAdapter(postAdapter);
        searchView = findViewById(R.id.searchView);
        imgToggleChat = findViewById(R.id.imgToggleChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        txtResponse = findViewById(R.id.txtResponse);
        chatLayout = findViewById(R.id.chatLayout);
        txtWelcome = findViewById(R.id.txtWelcome);
        btnPost = findViewById(R.id.btnPost);
        btnLogout = findViewById(R.id.btnLogout);

        // ✅ Hiển thị email người dùng
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
        txtWelcome.setText("Chào mừng, " + userEmail + "!");

        // ✅ Xử lý hiển thị/ẩn Chat khi nhấn GIF
        imgToggleChat.setOnClickListener(v -> {
            chatLayout.setVisibility(chatLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });

        // ✅ Xử lý gửi tin nhắn đến API Gemini
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToGemini(message);
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeMekong.this, LoginActivity.class));
            finish();
        });

        // ✅ Xử lý nút "Đăng Tin"
        btnPost.setOnClickListener(v -> {
            startActivity(new Intent(HomeMekong.this, PostActivity.class));
        });

        // ✅ Lắng nghe dữ liệu từ Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        // Lắng nghe dữ liệu từ Firebase
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                        Log.d("FirebaseData", "Đã tải bài đăng: " + post.getTitle());
                    } else {
                        Log.e("FirebaseData", "Lỗi: Bài đăng bị null!");
                    }
                }

                if (postList.isEmpty()) {
                    Log.e("FirebaseData", "Danh sách bài đăng vẫn rỗng sau khi tải!");
                }

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseData", "Lỗi Firebase: " + error.getMessage());
            }
        });

        // Click vào mỗi item trong GridView
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = postList.get(position);
            Intent intent = new Intent(HomeMekong.this, PostDetailActivity.class);
            intent.putExtra("postId", selectedPost.getPostId());
            intent.putExtra("title", selectedPost.getTitle());
            intent.putExtra("serviceInfo", selectedPost.getServiceInfo());
            intent.putExtra("price", selectedPost.getPrice());
            intent.putExtra("rentalTime", selectedPost.getRentalTime());
            intent.putExtra("address", selectedPost.getAddress());
            intent.putExtra("contact", selectedPost.getContact());
            intent.putExtra("imageUrl", selectedPost.getImageUrl());
            startActivity(intent);
        });

        // Xử lý tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                postAdapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                postAdapter.filter(newText);
                return false;
            }
        });
    }

    // ✅ Gửi tin nhắn đến API Gemini
    private void sendMessageToGemini(String message) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject requestBody = new JSONObject();
        try {
            // ✅ Tạo JSON phần "parts"
            JSONArray partsArray = new JSONArray();
            JSONObject textObject = new JSONObject();
            textObject.put("text", message);
            partsArray.put(textObject);

            // ✅ Tạo JSON phần "contents"
            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");  // Đúng role của API
            userContent.put("parts", partsArray);

            // ✅ Đưa vào JSON chính
            JSONArray contentsArray = new JSONArray();
            contentsArray.put(userContent);

            requestBody.put("contents", contentsArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);

                    Log.d("GeminiAPI", "Response: " + responseData); // ✅ Log để kiểm tra JSON trả về

                    // ✅ Fix lỗi lấy response
                    String reply = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts") // ✅ Lấy mảng "parts"
                            .getJSONObject(0) // ✅ Lấy phần tử đầu tiên
                            .getString("text"); // ✅ Lấy nội dung text

                    runOnUiThread(() -> txtResponse.setText(reply));
                } else {
                    runOnUiThread(() -> txtResponse.setText("Lỗi API Gemini: " + response.code()));
                }
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> txtResponse.setText("Lỗi: " + e.getMessage()));
            }
        }).start();
    }

}
