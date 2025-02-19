package com.example.researchproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
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
    private DatabaseReference databaseReference;
    private TextView  txtWelcome;
    private FirebaseAuth mAuth;
    private SearchView searchView;
    BottomNavigationView bottomNavigationView;
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
        txtWelcome = findViewById(R.id.txtWelcome);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Xử lý sự kiện khi chọn item trong menu
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(HomeMekong.this, HomeMekong.class));
                } else if (itemId == R.id.nav_ai) {
                    startActivity(new Intent(HomeMekong.this, MekoAI.class)); // Sửa lại tên đúng
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
            }
        });
        // ✅ Hiển thị email người dùng
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
        txtWelcome.setText("Chào mừng, " + userEmail + "!");

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
                        String postId = dataSnapshot.getKey(); // Lấy ID của bài đăng từ Firebase
                        post.setPostId(postId); // Gán ID vào bài đăng
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
            // Kiểm tra nếu danh sách rỗng
            if (postList == null || postList.isEmpty()) {
                Toast.makeText(HomeMekong.this, "Danh sách bài đăng chưa được tải!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Kiểm tra nếu vị trí hợp lệ
            if (position < 0 || position >= postList.size()) {
                Toast.makeText(HomeMekong.this, "Lỗi: Không tìm thấy bài đăng!", Toast.LENGTH_SHORT).show();
                return;
            }

            Post selectedPost = postList.get(position);
            if (selectedPost == null) {
                Toast.makeText(HomeMekong.this, "Lỗi: Dữ liệu bài đăng không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }

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
                if (postAdapter != null) {
                    postAdapter.filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (postAdapter != null) {
                    postAdapter.filter(newText);
                }
                return false;
            }
        });

    }
}
