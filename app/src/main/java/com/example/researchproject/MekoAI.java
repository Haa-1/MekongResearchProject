package com.example.researchproject;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
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
    private NestedScrollView nestedScrollView;
    private TextView txtSuggestion;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
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
        btnSearchAI = findViewById(R.id.btnSearchAI);
        edtUserQuery = findViewById(R.id.edtUserQuery);
        gridView = findViewById(R.id.gridView);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        txtSuggestion = findViewById(R.id.txtSuggestion);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        txtSuggestion = findViewById(R.id.txtSuggestion);
        recyclerViewChat.setNestedScrollingEnabled(false);
        gridView.setNestedScrollingEnabled(false);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        // ✅ Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(this, filteredFirebaseData);
        gridView.setAdapter(postAdapter);
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
            sendUserMessage(); // Xử lý gửi câu hỏi
        });
    }
    // 🎯 Hàm LẤY TỪ KHÓA trong dấu ngoặc đơn ()
    private List<String> extractKeywords(String input) {
        List<String> keywords = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            keywords.add(matcher.group(1).trim());
        }
        return keywords;
    }
    // 🔥 Firebase Query (Chỉ tìm từ khóa trong dấu ngoặc)
    private void fetchFilteredFirebase(String keyword) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filteredFirebaseData.clear(); // Xóa trước khi thêm mới
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
                    chatMessages.add(new ChatMessage("❌ Hệ thống MekongGo đang bị Lỗi : " + error.getMessage(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
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
                // ✅ Thêm hiệu ứng "typing" với delay 50ms
                runOnUiThread(() -> displayTypingEffect(finalGeminiResponse));
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

            // ✅ Xử lý từ khóa trong dấu ngoặc đơn ()
            List<String> keywords = extractKeywords(userMessage);
            if (!keywords.isEmpty()) {
                for (String keyword : keywords) {
                    fetchFilteredFirebase(keyword); // Tìm kiếm từng từ khóa
                }
            } else {
                Toast.makeText(this, "Bạn có thể đặt từ khóa trong dấu ngoặc đơn () để tìm kiếm dễ dàng hơn!", Toast.LENGTH_LONG).show();
            }
            // ✅ Gửi yêu cầu đến Gemini API
            sendRequestToGemini(userMessage);
        }
    }
    private void displayTypingEffect(String message) {
        ChatMessage aiMessage = new ChatMessage("", false); // Tin nhắn rỗng cho AI
        chatMessages.add(aiMessage);
        int messageIndex = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(messageIndex);
        Handler handler = new Handler();
        final int[] index = {0};
        Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < message.length()) {
                    String currentText = chatMessages.get(messageIndex).getMessage();
                    chatMessages.get(messageIndex).setMessage(currentText + message.charAt(index[0]));
                    chatAdapter.notifyItemChanged(messageIndex);
                    // ✅ Tự động cuộn xuống khi typing
                    nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
                    index[0]++;
                    handler.postDelayed(this, 30); // Điều chỉnh tốc độ typing
                } else {
                    // ✅ Hiển thị GridView sau khi phản hồi xong
                    showSuggestions();
                }
            }
        };

        handler.post(typingRunnable);
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
    private void showSuggestions() {
        txtSuggestion.setVisibility(View.VISIBLE);  // ✅ Hiển thị dòng chữ "Gợi ý cho bạn"
        gridView.setVisibility(View.VISIBLE);       // ✅ Hiển thị GridView
        // Nếu bạn cần cập nhật dữ liệu vào GridView
        postAdapter.notifyDataSetChanged();
        // ✅ Cuộn xuống để hiển thị GridView hoàn toàn
        nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
    }
}
