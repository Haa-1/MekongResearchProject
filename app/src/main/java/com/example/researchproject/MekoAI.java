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
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.*;

public class MekoAI extends AppCompatActivity {

    private TextView txtAIResponse;
    private Button btnSearchAI;
    private EditText edtUserQuery;
    private SearchView searchView;
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    private List<Post> postList;

    private DatabaseReference databaseReference;

    // Gemini API
    private final String API_KEY = "AIzaSyDpowdMhSBVL9qKWQ_eVrsi7FKbb4_Y3yE";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=" + API_KEY;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meko_ai);

        // Ánh xạ view
        txtAIResponse = findViewById(R.id.txtAIResponse);
        btnSearchAI = findViewById(R.id.btnSearchAI);
        edtUserQuery = findViewById(R.id.edtUserQuery);
        searchView = findViewById(R.id.searchView);
        gridView = findViewById(R.id.gridView);

        // Firebase
        postList = new ArrayList<>();
        postAdapter = new PostAdapterGrid(this, postList);
        gridView.setAdapter(postAdapter);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Xử lý sự kiện khi chọn item trong menu
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(MekoAI.this, HomeMekong.class));
                } else if (itemId == R.id.nav_ai) {
                    startActivity(new Intent(MekoAI.this, MekoAI.class)); // Sửa lại tên đúng
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
            }
        });
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        // Load dữ liệu từ Firebase
        loadFirebaseData();

        // 🔍 Tìm kiếm dữ liệu trong Firebase
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterFirebaseData(query); // Tìm trong Firebase
                sendRequestToGemini(query); // Đồng thời gửi câu hỏi đến Gemini
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterFirebaseData(newText);
                return false;
            }
        });

        // 🤖 Hỏi Gemini AI
        btnSearchAI.setOnClickListener(v -> {
            String userQuery = edtUserQuery.getText().toString().trim();
            if (!userQuery.isEmpty()) {
                sendRequestToGemini(userQuery);
            } else {
                Toast.makeText(this, "Vui lòng nhập câu hỏi!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Load tất cả dữ liệu từ Firebase
    private void loadFirebaseData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MekoAI.this, "Lỗi tải dữ liệu!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Tìm kiếm dữ liệu trong Firebase
    private void filterFirebaseData(String query) {
        List<Post> filteredList = new ArrayList<>();
        for (Post post : postList) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    post.getServiceInfo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(post);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy dữ liệu phù hợp!", Toast.LENGTH_SHORT).show();
        }

        // Cập nhật GridView với dữ liệu lọc
        postAdapter.updateData(filteredList);
    }

    // ✅ Gửi yêu cầu đến Gemini API
    private void sendRequestToGemini(String userQuery) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");

        JSONObject requestBody = new JSONObject();
        try {
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

                    String reply = jsonResponse
                            .getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> txtAIResponse.setText("🤖 Gemini AI: " + reply));
                } else {
                    runOnUiThread(() -> txtAIResponse.setText("Lỗi API Gemini!"));
                }
            } catch (IOException | JSONException e) {
                runOnUiThread(() -> txtAIResponse.setText("Lỗi: " + e.getMessage()));
            }
        }).start();
    }
}
