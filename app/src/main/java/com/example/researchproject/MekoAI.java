package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.ChatAdapter;
import com.example.researchproject.iam.ChatMessage;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.noties.markwon.Markwon;
import okhttp3.*;

public class MekoAI extends AppCompatActivity {
    private RecyclerView recyclerViewChat;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
//    private TextView txtAIResponse;
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

        // 🎯 Ánh xạ View
//        txtAIResponse = findViewById(R.id.txtAIResponse);
        btnSearchAI = findViewById(R.id.btnSearchAI);
        edtUserQuery = findViewById(R.id.edtUserQuery);
        gridView = findViewById(R.id.gridView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);

        // Khởi tạo Markwon để hiển thị Markdown
        Markwon markwon = Markwon.create(this);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        // ✅ Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(this, filteredFirebaseData);
        gridView.setAdapter(postAdapter);
        btnSearchAI.setOnClickListener(v -> sendUserMessage());
        // 🎯 Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;

            if (itemId == R.id.nav_home) {
                intent = new Intent(MekoAI.this, HomeMekong.class);
            } else if (itemId == R.id.nav_ai) {
                intent = new Intent(MekoAI.this, MekoAI.class);
            } else if (itemId == R.id.nav_post) {
                intent = new Intent(MekoAI.this, PostActivity.class);
            } else if (itemId == R.id.nav_cart) {
                intent = new Intent(MekoAI.this, CartActivity.class);
            } else if (itemId == R.id.nav_info) {
                intent = new Intent(MekoAI.this, InformationActivity.class);
            }

            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });

        // 🎯 Xử lý nhấn vào từng item trong GridView
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = filteredFirebaseData.get(position);

            Intent intent = new Intent(MekoAI.this, PostDetailActivity.class);
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

        // 🎯 Xử lý nút tìm kiếm
        btnSearchAI.setOnClickListener(v -> {
            String userQuery = edtUserQuery.getText().toString().trim();

            // ✅ Lấy từ khóa trong dấu ngoặc đơn () để tìm Firebase
            String keyword = extractKeyword(userQuery);

            // 🔥 Gửi nội dung nhập cho Gemini (dù có từ khóa hay không)
            sendRequestToGemini(userQuery);

            // 🟡 Tìm kiếm trong Firebase nếu có từ khóa trong dấu ngoặc
            if (!keyword.isEmpty()) {
                fetchFilteredFirebase(keyword);
            } else {
                Toast.makeText(this, "Bạn có thể đặt từ khóa trong dấu ngoặc đơn () để tìm kiếm dễ dàng hơn!", Toast.LENGTH_LONG).show();
            }
        });
    }

    // 🎯 Hàm LẤY TỪ KHÓA trong dấu ngoặc đơn ()
    private String extractKeyword(String input) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1).trim(); // Trả về từ khóa trong dấu ngoặc
        }
        return ""; // Không tìm thấy từ khóa
    }

    // 🔥 Firebase Query (Chỉ tìm từ khóa trong dấu ngoặc)
    private void fetchFilteredFirebase(String keyword) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filteredFirebaseData.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null && containsKeyword(post, keyword)) {
                        filteredFirebaseData.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> {
                    // Thêm tin nhắn lỗi vào danh sách chat
                    chatMessages.add(new ChatMessage("❌ Hệ thống MekongGo đang gặp lỗi: " + error.getMessage(), false)); // false vì đây là tin nhắn từ hệ thống/AI
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1); // Cập nhật RecyclerView
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1); // Cuộn đến tin nhắn mới
                });
            }
        });
    }

    // 🤖 Gửi yêu cầu đến Gemini API với toàn bộ nội dung nhập
    private void sendRequestToGemini(String userMessage) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject requestBody = new JSONObject();
        try {
            JSONArray partsArray = new JSONArray();
            JSONObject textObject = new JSONObject();
            textObject.put("text", userMessage);
            partsArray.put(textObject);

            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            userContent.put("parts", partsArray);

            JSONArray contentsArray = new JSONArray();
            contentsArray.put(userContent);
            requestBody.put("contents", contentsArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("❌ Hệ thống Meko AI đang gặp lỗi, xin lỗi nhiều nha.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String geminiResponse = "";
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        geminiResponse = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                    } catch (Exception e) {
                        geminiResponse = "⚠️ Lỗi Meko AI xử lý dữ liệu.";
                    }
                } else {
                    geminiResponse = "⚠️ Lỗi Meko AI : " + response.code();
                }

                String finalGeminiResponse = geminiResponse;
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage(finalGeminiResponse, false)); // Phản hồi từ AI
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);


                });
            }
        });
    }


    private void sendUserMessage() {
        String userMessage = edtUserQuery.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            chatMessages.add(new ChatMessage(userMessage, true)); // Người dùng gửi
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            edtUserQuery.setText("");
            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

            sendRequestToGemini(userMessage);
        }
    }
    // 🎯 Kiểm tra từ khóa trong tất cả các trường của Post
    private boolean containsKeyword(Post post, String keyword) {
        keyword = keyword.toLowerCase();
        return (post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword)) ||
                (post.getServiceInfo() != null && post.getServiceInfo().toLowerCase().contains(keyword)) ||
                (post.getPrice() != null && post.getPrice().toLowerCase().contains(keyword)) ||
                (post.getRentalTime() != null && post.getRentalTime().toLowerCase().contains(keyword)) ||
                (post.getAddress() != null && post.getAddress().toLowerCase().contains(keyword)) ||
                (post.getContact() != null && post.getContact().toLowerCase().contains(keyword));
    }
}
