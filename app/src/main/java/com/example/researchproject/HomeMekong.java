package com.example.researchproject;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.researchproject.Notification.AdNotificationService;
import com.example.researchproject.Notification.OrderNotificationService;
import com.example.researchproject.Notification.PostNotificationService;
import com.example.researchproject.iam.Ad;
import com.example.researchproject.iam.AdSliderAdapter;
import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.iam.Post;
import com.example.researchproject.iam.PostAdapterGrid;
import com.example.researchproject.iam.PostDetailActivity;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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
    // Define the originalPostList at the class level
    private List<Post> originalPostList = new ArrayList<>();
    private List<String> searchSuggestions; // Danh sách gợi ý
    private ArrayAdapter<String> suggestionAdapter;
    private DatabaseReference databaseReference;
    private AutoCompleteTextView searchView;
    private FirebaseAuth mAuth;
//    private TextView txtWelcome;
    private BottomNavigationView bottomNavigationView;
    private ViewPager2 viewPagerAds;
    private AdSliderAdapter adSliderAdapter;
    private List<Ad> adList;
    private DatabaseReference adsRef;
    private TabLayout tabDots;

    private static final int NOTIFICATION_PERMISSION_CODE = 1;

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
        gridView.setNestedScrollingEnabled(true);
        searchView = findViewById(R.id.searchView);
//        txtWelcome = findViewById(R.id.txtWelcome);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        viewPagerAds = findViewById(R.id.viewPagerAds);
        tabDots = findViewById(R.id.tabDots);
        adList = new ArrayList<>(); // ✅ Khởi tạo List để tránh null
        if (!adList.isEmpty()) {
            new TabLayoutMediator(tabDots, viewPagerAds, (tab, position) -> {}).attach();
        }
        adList = new ArrayList<>();
        adSliderAdapter = new AdSliderAdapter(this, adList);
        viewPagerAds.setAdapter(adSliderAdapter);

        // Kết nối Firebase
        adsRef = FirebaseDatabase.getInstance().getReference("Ads");
        loadAds();
        // Tạo hiệu ứng Slide Tự Động
        autoSlideAds();
        // Kết nối TabLayout với ViewPager2
        new TabLayoutMediator(tabDots, viewPagerAds,
                (tab, position) -> {}).attach();
        // Firebase Reference
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        // ✅ Lấy dữ liệu từ Firebase để tạo gợi ý
        loadSearchSuggestions();
        // Tạo Adapter cho gợi ý tìm kiếm
        suggestionAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, searchSuggestions);
        searchView.setAdapter(suggestionAdapter);
        // 👉 Lắng nghe khi người dùng chọn gợi ý
        searchView.setThreshold(1); // Hiển thị gợi ý sau khi nhập 1 ký tự
        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSuggestion = suggestionAdapter.getItem(position);
            searchView.setText(selectedSuggestion);
            filterPosts(selectedSuggestion); // Lọc và hiển thị kết quả trong GridView
        });
        // 👉 Lọc khi nhập text
        searchView.setOnDismissListener(() -> filterPosts(searchView.getText().toString()));
        searchView.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchView.getText().toString().trim();
            if (!query.isEmpty()) {
                filterPosts(query); // Lọc bài đăng khi người dùng nhấn "Enter"
            }
            return true;
        });
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

        //Yêu cầu quyền thông báo
        requestNotificationPermission();
        //Thong bao khi co bai viet tu nguoi khac
        new PostNotificationService(this);
        //Thông báo khi có người đặt đơn hàng từ bài viết của bạn
        new OrderNotificationService(this);
        //Thông báo khi có qc mới
        new AdNotificationService(this);

    }
    // ✅ Yêu cầu quyền thông báo
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền thông báo đã được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bạn cần cấp quyền để nhận thông báo!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    // ✅ Lấy dữ liệu để tạo gợi ý tìm kiếm & đảm bảo bài mới nhất lên đầu
    private void loadSearchSuggestions() {
        databaseReference.orderByChild("timestamp").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> uniqueSuggestions = new HashSet<>();
                List<Post> tempList = new ArrayList<>(); // Danh sách tạm thời để lưu bài đăng

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null) {
                        tempList.add(post);
                        uniqueSuggestions.add(post.getTitle());
                        uniqueSuggestions.add(post.getAddress());
                        uniqueSuggestions.add(post.getServiceInfo());
                    }
                }
                // ✅ Sắp xếp danh sách theo timestamp giảm dần (bài mới nhất lên đầu)
                tempList.sort((p1, p2) -> Long.compare(p2.getTimestamp(), p1.getTimestamp()));

                // ✅ Cập nhật danh sách bài đăng chính
                postList.clear();
                postList.addAll(tempList);
                originalPostList.clear();
                originalPostList.addAll(tempList);

                // ✅ Cập nhật gợi ý tìm kiếm
                searchSuggestions.clear();
                searchSuggestions.addAll(uniqueSuggestions);
                suggestionAdapter.notifyDataSetChanged();
                postAdapter.notifyDataSetChanged();
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
        for (Post post : originalPostList) { // Sử dụng originalPostList để lọc
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

    // ✅ Load Quảng Cáo Từ Firebase
    private void loadAds() {
        adsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (adList == null) {
                    adList = new ArrayList<>();
                }
                adList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Ad ad = dataSnapshot.getValue(Ad.class);
                    if (ad != null) {
                        adList.add(ad);
                    }
                }
                adSliderAdapter.notifyDataSetChanged();

                // ✅ Chỉ kết nối TabLayout nếu có quảng cáo
                if (!adList.isEmpty()) {
                    new TabLayoutMediator(tabDots, viewPagerAds, (tab, position) -> {}).attach();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeMekong.this, "Lỗi tải quảng cáo!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // ✅ Tạo Slide Tự Động Chạy
    private void autoSlideAds() {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int currentItem = viewPagerAds.getCurrentItem();
                int totalItems = adSliderAdapter.getItemCount();
                if (currentItem < totalItems - 1) {
                    viewPagerAds.setCurrentItem(currentItem + 1);
                } else {
                    viewPagerAds.setCurrentItem(0);
                }
                handler.postDelayed(this, 4000); // 4 giây chuyển slide
            }
        };
        handler.postDelayed(runnable, 4000);
    }
}