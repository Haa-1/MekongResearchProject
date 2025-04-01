package com.example.researchproject;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.iam.Ad;
import com.example.researchproject.iam.AdSliderAdapter;
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
    private RecyclerView recyclerViewAds;
    private PostAdapterGrid postAdapter;
    private List<Post> filteredFirebaseData = new ArrayList<>();
    private List<Ad> filteredAdData = new ArrayList<>(); // To store filtered ads
    private AdSliderAdapter adAdapter; // Adapter for RecyclerView
    private String geminiResponse = "";
    // Gemini API
    private final String API_KEY = "AIzaSyDXMP_Of_Bf5LK2yNFTRbs_fYrwx6DIyHE";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-8b-001:generateContent?key=" + API_KEY;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meko_ai);
        // 🎯 Ánh xạ View
        btnSearchAI = findViewById(R.id.btnSearchAI);
        edtUserQuery = findViewById(R.id.edtUserQuery);
        gridView = findViewById(R.id.gridView);
        recyclerViewAds = findViewById(R.id.recyclerViewAds);
        String input = "Đi du lịch cần thơ rất thú vị, tôi cũng thích sóc trăng và huế.";
        List<String> suggestions = List.of(
                "cần thơ", "sóc trăng", "huế", "xe", "du lịch",
                "an giang", "vũng tàu", "bạc liêu", "bắc giang", "bắc kạn",
                "bắc ninh", "bến tre", "bình định", "bình dương", "bình phước",
                "bình thuận", "cà mau", "cao bằng", "đà nẵng", "đắk lắk",
                "đắk nông", "điện biên", "đồng nai", "đồng tháp", "gia lai",
                "hà giang", "hà nam", "hà nội", "hà tĩnh", "hải dương",
                "hải phòng", "hậu giang", "hoà bình", "hưng yên", "khánh hoà",
                "kiên giang", "kon tum", "lai châu", "lâm đồng", "lạng sơn",
                "lào cai", "long an", "nam định", "nghệ an", "ninh bình",
                "ninh thuận", "phú thọ", "phú yên", "quảng bình", "quảng nam",
                "quảng ngãi", "quảng ninh", "quảng trị", "sơn la", "tây ninh",
                "thái bình", "thái nguyên", "thanh hoá", "thừa thiên huế", "tiền giang","nghĩ dưỡng","lễ","biển","giá",
                "trà vinh", "hello","nihao","thuê","chạy","thấp","tuyên quang", "vĩnh long", "vĩnh phúc", "yên bái"
        );
        List<String> keywords = extractKeywords(input, suggestions);
        System.out.println(keywords);
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
        recyclerViewAds.setNestedScrollingEnabled(false);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        // ✅ Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(this, filteredFirebaseData);
        databaseReference  = FirebaseDatabase.getInstance().getReference("Ads"); // Reference for Ads
        adAdapter = new AdSliderAdapter(this, filteredAdData);
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
    // Move this method to the outer class
    private List<String> extractKeywords(String input, List<String> suggestions) {
        List<String> keywords = new ArrayList<>();
        for (String suggestion : suggestions) {
            if (input.contains(suggestion)) {
                keywords.add(suggestion);
            }
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
                Log.e("Gemini API", "Lỗi kết nối: " + e.getMessage());
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("❌ Hệ thống Meko AI đang gặp lỗi, xin lỗi nhiều nha.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String geminiResponse = "";
                if (response.isSuccessful() && response.body() != null) {
                    Log.e("Gemini API", "Lỗi HTTP " + response.code() + ": " + response.message());
                    try {
                        String responseData = response.body().string();
                        Log.d("Gemini API", "Phản hồi: " + responseData);
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
        // Khởi tạo RecyclerView và Adapter cho quảng cáo
        RecyclerView recyclerViewAds = findViewById(R.id.recyclerViewAds);
        adAdapter = new AdSliderAdapter(this, filteredAdData); // filteredAdData lưu danh sách quảng cáo
        recyclerViewAds.setLayoutManager(new LinearLayoutManager(this)); // Hiển thị dạng danh sách dọc
        recyclerViewAds.setAdapter(adAdapter);
        // Xử lý thông điệp người dùng
        String userMessage = edtUserQuery.getText().toString().trim(); // Nhập liệu từ người dùng
        if (!userMessage.isEmpty()) {
            // Thêm vào giao diện chat
            chatMessages.add(new ChatMessage(userMessage, true)); // Người dùng gửi tin nhắn
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            edtUserQuery.setText(""); // Xóa nội dung sau khi gửi
            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);

            // Danh sách từ khóa gợi ý
            List<String> suggestions = List.of(
                    "cần thơ", "sóc trăng", "huế", "xe", "du lịch",
                    "an giang", "vũng tàu", "bạc liêu", "bắc giang", "bắc kạn",
                    "bắc ninh", "bến tre", "bình định", "bình dương", "bình phước",
                    "bình thuận", "cà mau", "cao bằng", "đà nẵng", "đắk lắk",
                    "đắk nông", "điện biên", "đồng nai", "đồng tháp", "gia lai",
                    "hà giang", "hà nam", "hà nội", "hà tĩnh", "hải dương",
                    "hải phòng", "hậu giang", "hoà bình", "hưng yên", "khánh hoà",
                    "kiên giang", "kon tum", "lai châu", "lâm đồng", "lạng sơn",
                    "lào cai", "long an", "nam định", "nghệ an", "ninh bình",
                    "ninh thuận", "phú thọ", "phú yên", "quảng bình", "quảng nam",
                    "quảng ngãi", "quảng ninh", "quảng trị", "sơn la", "tây ninh",
                    "thái bình", "thái nguyên", "thanh hoá", "thừa thiên huế", "tiền giang", "nghĩ dưỡng", "lễ", "biển", "giá",
                    "trà vinh", "thuê", "hello", "nihao", "thấp", "tuyên quang", "vĩnh long", "vĩnh phúc", "yên bái"
            );
            // Trích xuất từ khóa từ thông điệp
            List<String> keywords = extractKeywords(userMessage, suggestions);
            if (!keywords.isEmpty()) {
                // Tìm kiếm và hiển thị quảng cáo cho từng từ khóa
                for (String keyword : keywords) {
                    // Lọc dữ liệu từ "Posts"
                    fetchFilteredFirebase(keyword);
                    // Lọc dữ liệu từ "Ads"
                    fetchFilteredAds(keyword);
                }
            } else {
                Toast.makeText(this, "Bạn có thể đặt từ khóa trong dấu ngoặc đơn () để tìm kiếm dễ dàng hơn!", Toast.LENGTH_LONG).show();
            }
            // Gửi yêu cầu đến Gemini API (nếu cần)
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
    private void fetchFilteredAds(String keyword) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filteredAdData.clear(); // Xóa trước khi thêm mới
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ad ad = dataSnapshot.getValue(Ad.class);
                    if (ad != null && containsKeywordAd(ad, keyword)) {
                        filteredAdData.add(ad);
                    }
                }
                adAdapter.notifyDataSetChanged();
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
    // Helper method to check if Ad contains the keyword
    private boolean containsKeywordAd(Ad ad, String keyword) {
        return (ad.getTitle() != null && ad.getTitle().toLowerCase().contains(keyword.toLowerCase())) ||
                (ad.getImageUrl() != null && ad.getImageUrl().toLowerCase().contains(keyword.toLowerCase()));
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
        gridView.setVisibility(View.VISIBLE);// ✅ Hiển thị GridView
        recyclerViewAds.setVisibility(View.VISIBLE);
        // Nếu bạn cần cập nhật dữ liệu vào GridView
        postAdapter.notifyDataSetChanged();
        adAdapter.notifyDataSetChanged();
        // ✅ Cuộn xuống để hiển thị GridView hoàn toàn
        nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
    }
}
