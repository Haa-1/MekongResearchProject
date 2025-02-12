package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.iam.Post;
import com.example.researchproject.iam.PostAdapter;
import com.example.researchproject.ui.PostActivity;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.droidsonroids.gif.GifImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
public class HomeMekong extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private DatabaseReference databaseReference;
    private GifImageView imgToggleChat;
    private EditText edtMessage;
    private Button btnSend;
    private TextView txtResponse;
    private View chatLayout;
    private FirebaseAuth mAuth;
    private TextView txtWelcome;
    private Button btnPost;
    private Button btnLogout;
    private SearchView searchView; // ✅ Thêm SearchView


    // Thay thế bằng API Key của bạn từ Google Cloud
    private final String API_KEY = "AIzaSyDpowdMhSBVL9qKWQ_eVrsi7FKbb4_Y3yE";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_mekong);

        mAuth = FirebaseAuth.getInstance();
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.searchView); // Kết nối với SearchView trong XML

        imgToggleChat = findViewById(R.id.imgToggleChat);
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        txtResponse = findViewById(R.id.txtResponse);
        chatLayout = findViewById(R.id.chatLayout);
        txtWelcome = findViewById(R.id.txtWelcome);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(this, postList);
        recyclerView.setAdapter(postAdapter);
        btnLogout = findViewById(R.id.btnLogout);
        btnPost = findViewById(R.id.btnPost);  // Ánh xạ nút Post
        // Tham chiếu Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        // Xử lý sự kiện khi nhấn nút "Post"
        btnPost.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMekong.this, PostActivity.class);
            startActivity(intent);
        });

        // Lấy email người dùng đang đăng nhập
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
        txtWelcome.setText("Chào mừng, " + userEmail + "!");

        // Ẩn/Hiện khung chat khi nhấn vào GIF
        imgToggleChat.setOnClickListener(v -> {
            if (chatLayout.getVisibility() == View.GONE) {
                chatLayout.setVisibility(View.VISIBLE);
            } else {
                chatLayout.setVisibility(View.GONE);
            }
        });

        // Xử lý khi nhấn nút "Gửi"
        btnSend.setOnClickListener(v -> {
            String message = edtMessage.getText().toString().trim();
            if (!message.isEmpty()) {
                sendMessageToGemini(message);
            } else {
                Toast.makeText(this, "Vui lòng nhập tin nhắn!", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Đặt đoạn xử lý đăng xuất VÀO bên trong onCreate
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(HomeMekong.this, LoginActivity.class));
            finish(); // Đóng HomeMekong để không quay lại khi nhấn nút back
        });
    }

    // Gửi tin nhắn đến API Gemini
    private void sendMessageToGemini(String message) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject requestBody = new JSONObject();
        try {
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(new JSONObject().put("text", message)));
            contents.put(content);

            requestBody.put("contents", contents);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();

        // Thực hiện request trong luồng riêng
        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseData = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseData);
                    String reply = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONArray("content")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> txtResponse.setText(reply));
                } else {
                    runOnUiThread(() -> txtResponse.setText("Lỗi khi nhận phản hồi từ Gemini!"));
                }
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> txtResponse.setText("Lỗi: " + e.getMessage()));
            }
        }).start();

        // Lắng nghe sự thay đổi dữ liệu
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear(); // Xóa danh sách cũ để tránh trùng lặp
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    postList.add(post);
                }
                postAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi nếu có
            }
        });
        // ✅ Xử lý tìm kiếm
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                postAdapter.getFilter().filter(query); // Lọc khi nhấn tìm kiếm
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                postAdapter.getFilter().filter(newText); // Lọc khi thay đổi nội dung tìm kiếm
                return false;
            }
        });
    }
}
