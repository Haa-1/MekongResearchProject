package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SearchView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.iam.Post;
import com.example.researchproject.iam.PostAdapterGrid;
import com.example.researchproject.iam.PostDetailActivity;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
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
import java.lang.reflect.Field;
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
    public static List<Post> postList;
    private List<String> searchSuggestions; // Danh sách gợi ý
    private ArrayAdapter<String> suggestionAdapter;
    private DatabaseReference databaseReference;
    private AutoCompleteTextView searchView;
    private FirebaseAuth mAuth;
    private TextView txtWelcome;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_mekong);

        mAuth = FirebaseAuth.getInstance();
        postList = new ArrayList<>();
        searchSuggestions = new ArrayList<>();

        gridView = findViewById(R.id.gridView);
        postAdapter = new PostAdapterGrid(this, postList);
        gridView.setAdapter(postAdapter);

        searchView = findViewById(R.id.searchView);
        txtWelcome = findViewById(R.id.txtWelcome);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
        txtWelcome.setText("Chào mừng, " + userEmail + "!");

        // Firebase Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");

        // ✅ Lấy dữ liệu từ Firebase để tạo gợi ý
        loadSearchSuggestions();

        // Tạo Adapter cho gợi ý tìm kiếm
        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, searchSuggestions);
        searchView.setAdapter(suggestionAdapter);

        // 👉 Lắng nghe khi người dùng chọn gợi ý
        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSuggestion = suggestionAdapter.getItem(position);
            searchView.setText(selectedSuggestion); // Hiển thị gợi ý đã chọn
            filterPosts(selectedSuggestion);       // Lọc bài đăng theo gợi ý
        });
        searchView.setThreshold(1); // Hiển thị gợi ý sau khi nhập 1 ký tự
        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSuggestion = suggestionAdapter.getItem(position);
            searchView.setText(selectedSuggestion);
            filterPosts(selectedSuggestion); // Lọc và hiển thị kết quả trong GridView
        });

        // 👉 Lọc khi nhập text
        searchView.setOnDismissListener(() -> filterPosts(searchView.getText().toString()));

        // GridView Item Click → Xem chi tiết
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

        // Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(HomeMekong.this, HomeMekong.class));
            } else if (itemId == R.id.nav_ai) {
                startActivity(new Intent(HomeMekong.this, MekoAI.class));
            } else if (itemId == R.id.nav_post) {
                startActivity(new Intent(HomeMekong.this, PostActivity.class));
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(HomeMekong.this, CartActivity.class));
            } else if (itemId == R.id.nav_info) {
                startActivity(new Intent(HomeMekong.this, InformationActivity.class));
            } else {
                return false;
            }
            return true;
        });
    }

    // ✅ Lấy dữ liệu để tạo gợi ý tìm kiếm
    private void loadSearchSuggestions() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                searchSuggestions.clear();
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        postList.add(post);
                        // Thêm gợi ý từ tiêu đề và địa chỉ bài đăng
                        searchSuggestions.add(post.getTitle());
                        searchSuggestions.add(post.getAddress());
                        searchSuggestions.add(post.getServiceInfo());
                    }
                }
                suggestionAdapter.notifyDataSetChanged(); // Cập nhật gợi ý
                postAdapter.notifyDataSetChanged();       // Cập nhật GridView
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeMekong.this, "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ✅ Lọc bài đăng theo gợi ý hoặc nội dung nhập
    private void filterPosts(String query) {
        List<Post> filteredList = new ArrayList<>();

        for (Post post : postList) {
            if (post.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                    post.getAddress().toLowerCase().contains(query.toLowerCase()) ||
                    post.getServiceInfo().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(post);
            }
        }

        // Cập nhật GridView
        postAdapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy kết quả!", Toast.LENGTH_SHORT).show();
        }
    }

}
