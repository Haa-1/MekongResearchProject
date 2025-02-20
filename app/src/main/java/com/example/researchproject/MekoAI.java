package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.Post;
import com.example.researchproject.iam.PostAdapterGrid;
import com.example.researchproject.iam.PostDetailActivity;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import okhttp3.*;

public class MekoAI extends AppCompatActivity {

    private TextView txtAIResponse;
    private ImageButton btnSearchAI;
    private EditText edtUserQuery;
    private DatabaseReference databaseReference;
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    private List<Post> filteredFirebaseData = new ArrayList<>();
    private String geminiResponse = "";

    // Gemini API
    private final String API_KEY = "AIzaSyDpowdMhSBVL9qKWQ_eVrsi7FKbb4_Y3yE";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meko_ai);
        txtAIResponse = findViewById(R.id.txtAIResponse);
        btnSearchAI = findViewById(R.id.btnSearchAI);
        edtUserQuery = findViewById(R.id.edtUserQuery);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        gridView = findViewById(R.id.gridView);
        // ✅ Initialize Firebase Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(this, filteredFirebaseData);
        gridView.setAdapter(postAdapter);
        // 📌 Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(MekoAI.this, HomeMekong.class));
            } else if (itemId == R.id.nav_ai) {
                startActivity(new Intent(MekoAI.this, MekoAI.class));
            } else if (itemId == R.id.nav_post) {
                startActivity(new Intent(MekoAI.this, PostActivity.class));
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(MekoAI.this, CartActivity.class));
            } else if (itemId == R.id.nav_info) {
                startActivity(new Intent(MekoAI.this, InformationActivity.class));
            } else {
                return false;
            }

            return true;
        });
        // ✅ Sự kiện nhấn vào từng item trong GridView
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = filteredFirebaseData.get(position); // Lấy dữ liệu Post từ vị trí được chọn

            Intent intent = new Intent(MekoAI.this, PostDetailActivity.class);
            intent.putExtra("postId", selectedPost.getPostId());
            intent.putExtra("title", selectedPost.getTitle());
            intent.putExtra("serviceInfo", selectedPost.getServiceInfo());
            intent.putExtra("price", selectedPost.getPrice());
            intent.putExtra("rentalTime", selectedPost.getRentalTime());
            intent.putExtra("address", selectedPost.getAddress());
            intent.putExtra("contact", selectedPost.getContact());
            intent.putExtra("imageUrl", selectedPost.getImageUrl());

            startActivity(intent); // Mở PostDetailActivity
        });
        // 💡 Handle AI Search Button
        btnSearchAI.setOnClickListener(v -> {
            String userQuery = edtUserQuery.getText().toString().trim();
            if (!userQuery.isEmpty()) {
                fetchFilteredFirebaseAndGemini(userQuery);
            } else {
                Toast.makeText(this, "Vui lòng nhập nội dung!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Fetch Filtered Firebase Data + Gemini API Response
    private void fetchFilteredFirebaseAndGemini(String userQuery) {
        CountDownLatch latch = new CountDownLatch(2); // Đợi cả Gemini và Firebase

        // 🔥 Firebase Query
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filteredFirebaseData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null && post.getTitle() != null) {
                        if (post.getTitle().toLowerCase().contains(userQuery.toLowerCase())) {
                            filteredFirebaseData.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
                latch.countDown(); // ✅ Firebase Done
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> txtAIResponse.setText("Lỗi Firebase: " + error.getMessage()));
                latch.countDown();
            }
        });

        // 🤖 Gemini API
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");

            try {
                JSONObject requestBody = new JSONObject();
                JSONArray partsArray = new JSONArray();
                JSONObject textObject = new JSONObject();
                textObject.put("text", userQuery);
                partsArray.put(textObject);

                JSONObject userContent = new JSONObject();
                userContent.put("role", "user");
                userContent.put("parts", partsArray);

                JSONArray contentsArray = new JSONArray();
                contentsArray.put(userContent);
                requestBody.put("contents", contentsArray);

                RequestBody body = RequestBody.create(requestBody.toString(), JSON);
                Request request = new Request.Builder().url(API_URL).post(body).build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);

                    geminiResponse = jsonResponse.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");
                } else {
                    geminiResponse = "Lỗi API Gemini: " + response.code();
                }
            } catch (Exception e) {
                geminiResponse = "Lỗi khi gọi API Gemini: " + e.getMessage();
                e.printStackTrace(); // ✅ Log chi tiết lỗi
            }

            latch.countDown();
        }).start();

        // ✅ Kết hợp Kết Quả và Hiển Thị
        new Thread(() -> {
            try {
                latch.await(); // Đợi cả Firebase và Gemini hoàn thành

                runOnUiThread(() -> {
                    StringBuilder firebaseResult = new StringBuilder();
                    for (Post post : filteredFirebaseData) {
                        firebaseResult.append("• ").append(post.getTitle())
                                .append(" - ").append(post.getPrice())
                                .append("\n");
                    }

                    String combinedResult = "🔥 Dịch vụ từ Firebase:\n" + firebaseResult +
                            "\n🤖 Phản hồi từ Gemini:\n" + geminiResponse;

                    txtAIResponse.setText(combinedResult);
                });
            } catch (InterruptedException e) {
                runOnUiThread(() -> txtAIResponse.setText("Lỗi kết hợp dữ liệu: " + e.getMessage()));
            }
        }).start();
    }

}
