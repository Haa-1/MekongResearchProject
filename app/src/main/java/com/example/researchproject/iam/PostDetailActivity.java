package com.example.researchproject.iam;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.researchproject.HomeMekong;
import com.example.researchproject.InformationActivity;
import com.example.researchproject.MekoAI;
import com.example.researchproject.Payment.OrderInformationActivity;
import com.example.researchproject.R;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
public class PostDetailActivity extends AppCompatActivity {
    private TextView txtTitle, txtServiceInfo, txtPrice, txtRentalTime, txtAddress, txtContact;
    private ImageView imgService;
    private ImageButton btnAddToCart, btnPay, btnSubmitReview;
    private RatingBar ratingBar;
    private EditText edtReview;
    private TextView txtWelcome;
    private RecyclerView recyclerViewReviews;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private DatabaseReference reviewsRef, cartRef;
    private String postId;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);
        // 🎯 Initialize Views
        txtTitle = findViewById(R.id.txtTitle);
        txtServiceInfo = findViewById(R.id.txtServiceInfo);
        txtPrice = findViewById(R.id.txtPrice);
        txtRentalTime = findViewById(R.id.txtRentalTime);
//        txtWelcome = findViewById(R.id.txtWelcome);
        txtAddress = findViewById(R.id.txtAddress);
        txtContact = findViewById(R.id.txtContact);
        imgService = findViewById(R.id.imgService);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnPay = findViewById(R.id.btnPay);
        btnSubmitReview = findViewById(R.id.btnSubmitReview);
        ratingBar = findViewById(R.id.ratingBar);
        edtReview = findViewById(R.id.edtReview);
        recyclerViewReviews = findViewById(R.id.recyclerViewReviews);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // 🌟 Setup RecyclerView for Reviews
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(this, reviewList);
        recyclerViewReviews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReviews.setAdapter(reviewAdapter);
        // ✅ Set Active Tab in Bottom Navigation
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(PostDetailActivity.this, HomeMekong.class));
            } else if (itemId == R.id.nav_ai) {
                startActivity(new Intent(PostDetailActivity.this, MekoAI.class));
            } else if (itemId == R.id.nav_post) {
                startActivity(new Intent(PostDetailActivity.this, PostActivity.class));
            } else if (itemId == R.id.nav_cart) {
                startActivity(new Intent(PostDetailActivity.this, CartActivity.class));
            } else if (itemId == R.id.nav_info) {
                startActivity(new Intent(PostDetailActivity.this, InformationActivity.class));
            } else {
                return false;
            }
            return true;
        });

        // ✅ Get postId from Intent
        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Lỗi: Không tìm thấy bài đăng!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        reviewsRef = FirebaseDatabase.getInstance().getReference("Reviews").child(postId);
        cartRef = FirebaseDatabase.getInstance().getReference("Cart");
        // 📌 Get Data from Intent
        String title = getIntent().getStringExtra("title");
        String serviceInfo = getIntent().getStringExtra("serviceInfo");
        String price = getIntent().getStringExtra("price");
        String rentalTime = getIntent().getStringExtra("rentalTime");
        String address = getIntent().getStringExtra("address");
        String contact = getIntent().getStringExtra("contact");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        // ✅ Display Data
        txtTitle.setText(title);
        txtServiceInfo.setText(serviceInfo);
        txtPrice.setText("Giá: " + price + " VND");
        txtRentalTime.setText("Thời gian thuê: " + rentalTime);
        txtAddress.setText("Địa chỉ: " + address);
        txtContact.setText("Liên hệ: " + contact);
        Glide.with(this).load(imageUrl).into(imgService);
        // ✅ Add to Cart
        btnAddToCart.setOnClickListener(v -> {
            // Giả sử uid đã có sẵn từ quá trình đăng nhập
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);

            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String userEmail = snapshot.child("email").getValue(String.class); // ✅ Lấy userEmail
                    String cartItemId = cartRef.push().getKey();
                    if (cartItemId != null) {
                        HashMap<String, Object> cartItem = new HashMap<>();
                        cartItem.put("postId", postId);
                        cartItem.put("title", title);
                        cartItem.put("price", price);
                        cartItem.put("imageUrl", imageUrl);
                        cartItem.put("uid", uid); // ✅ Thêm uid vào giỏ hàng
                        cartItem.put("userEmail", userEmail); // ✅ Thêm userEmail vào giỏ hàng

                        cartRef.child(cartItemId).setValue(cartItem)
                                .addOnSuccessListener(aVoid ->
                                        Toast.makeText(PostDetailActivity.this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                                )
                                .addOnFailureListener(e ->
                                        Toast.makeText(PostDetailActivity.this, "Lỗi khi thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show()
                                );
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(PostDetailActivity.this, "Lỗi khi lấy thông tin người dùng!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        // ✅ Payment Button
        btnPay.setOnClickListener(v -> {
            Intent intent = new Intent(PostDetailActivity.this, OrderInformationActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("title", title);
            intent.putExtra("price", price);
//            intent.putExtra("rentalTime", rentalTime);
            startActivity(intent);
        });
        // ✅ Submit Review
        btnSubmitReview.setOnClickListener(v -> {
            String reviewText = edtReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (TextUtils.isEmpty(reviewText) || rating == 0) {
                Toast.makeText(this, "Vui lòng nhập đánh giá & chọn số sao!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 🔥 Save Review to Firebase
            String reviewId = reviewsRef.push().getKey();
            if (reviewId != null) {
                HashMap<String, Object> reviewMap = new HashMap<>();
                reviewMap.put("user", "Người dùng ẩn danh");
                reviewMap.put("rating", rating);
                reviewMap.put("comment", reviewText);
                reviewsRef.child(reviewId).setValue(reviewMap)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(this, "Đánh giá đã gửi!", Toast.LENGTH_SHORT).show();
                            edtReview.setText("");
                            ratingBar.setRating(0);
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Lỗi khi gửi đánh giá!", Toast.LENGTH_SHORT).show()
                        );
            }
        });
        // ✅ Load Reviews
        loadReviews();
    }
    private void loadReviews() {
        reviewsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reviewList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Review review = dataSnapshot.getValue(Review.class);
                    reviewList.add(review);
                }
                reviewAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PostDetailActivity.this, "Lỗi tải đánh giá!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
