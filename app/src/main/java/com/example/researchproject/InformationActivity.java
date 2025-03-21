package com.example.researchproject;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.researchproject.History.OrderHistoryActivity;
import com.example.researchproject.Notification.NotificationActivity;
import com.example.researchproject.Profile.EditProfileActivity;
import com.example.researchproject.Profile.UserGuideActivity;
import com.example.researchproject.databinding.ActivityInformationBinding;
import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.mekoaipro.MekoAIPro;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InformationActivity extends AppCompatActivity {
    TextView tv_profile;
    private ActivityInformationBinding binding;
    private FirebaseAuth mAuth;
    private Button  btnLogout, btnHistoryOrder,btnMekoAIPro, btnNotification;
    BottomNavigationView bottomNavigationView;
    private TextView tvProfile, tvPhone, tvEmail, tvFacebook, tvGoogle;
    private ActivityResultLauncher<Intent> launcher;
    private DatabaseReference userRef;

    private ImageView imgProfile, imgbtnAccountEdit, btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // ✅ Khởi tạo FirebaseAuth và đặt ngôn ngữ
        mAuth = FirebaseAuth.getInstance();
        // ✅ Hiển thị email người dùng
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
        btnLogout = findViewById(R.id.btnLogout);
        btnMekoAIPro=findViewById(R.id.btnMekoAIPro);
        btnHistoryOrder = findViewById(R.id.btnHistoryOrder);
        btnNotification=findViewById(R.id.btnNotification);

        // Ánh xạ view
        imgProfile = findViewById(R.id.img_profile);
        tvProfile = findViewById(R.id.tv_profile);
        tvPhone = findViewById(R.id.tv_phone);
        tvEmail = findViewById(R.id.tv_email);
        tvFacebook = findViewById(R.id.tv_facebook);
        tvGoogle = findViewById(R.id.tv_google);
        imgbtnAccountEdit = findViewById(R.id.imgbtn_account_edit);

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        // Xử lý sự kiện khi chọn item trong menu
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(InformationActivity.this, HomeMekong.class));
                } else if (itemId == R.id.nav_ai) {
                    startActivity(new Intent(InformationActivity.this, MekoAI.class)); // Sửa lại tên đúng
                } else if (itemId == R.id.nav_post) {
                    startActivity(new Intent(InformationActivity.this, PostActivity.class));
                } else if (itemId == R.id.nav_cart) {
                    startActivity(new Intent(InformationActivity.this, CartActivity.class));
                } else if (itemId == R.id.nav_info) {
                    startActivity(new Intent(InformationActivity.this, InformationActivity.class));
                } else {
                    return false;
                }
                return true;
            }
        });

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String uid=currentUser.getUid();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid);


        // ✅ Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(InformationActivity.this, LoginActivity.class));
            finish();
        });
        btnMekoAIPro.setOnClickListener(v-> {
            startActivity(new Intent(InformationActivity.this, MekoAIPro.class));
        });

        btnHistoryOrder.setOnClickListener(v -> {
            startActivity(new Intent(InformationActivity.this, OrderHistoryActivity.class));
        });
        btnNotification.setOnClickListener(view ->
        {
            startActivity(new Intent(InformationActivity.this, NotificationActivity.class));
        });
        loadUserData();

        // Đăng ký launcher để cập nhật khi chỉnh sửa xong
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null && result.getData().getBooleanExtra("updated", false)) {
                            loadUserData();
                        }
                    }
                });
             // Nút chỉnh sửa thông tin
                imgbtnAccountEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(this, EditProfileActivity.class);
                    launcher.launch(intent);
                    Log.e("btnAccountEdit", "btnAccountEdit clicked");
                    Log.d("DEBUG", "btnAccountEdit clicked");
                });
//
        Button btnHelp = findViewById(R.id.btnHelp);
        btnHelp.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserGuideActivity.class);
            startActivity(intent);
        });
//
//        // Nút đóng (trở về màn hình trước)
//        btnClose.setOnClickListener(v -> finish());

        // Nút đăng xuất
        //btnLogout.setOnClickListener(v -> logoutUser());

    }
    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvProfile.setText(snapshot.child("nickname").getValue(String.class));
                tvPhone.setText(snapshot.child("phoneNumber").exists() ?
                        snapshot.child("phoneNumber").getValue(String.class) : "Chưa xác thực");
                tvEmail.setText(snapshot.child("email").exists() ?
                        snapshot.child("email").getValue(String.class) : "Chưa xác thực");
                tvFacebook.setText(snapshot.child("facebook").exists() ?
                        snapshot.child("facebook").getValue(String.class) : "Chưa xác thực");
                tvGoogle.setText(snapshot.child("google").exists() ?
                        snapshot.child("google").getValue(String.class) : "Chưa xác thực");

                // Load ảnh từ Imgur bằng Glide
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                    Glide.with(InformationActivity.this)
                            .load(profileImageUrl)
                            .into(imgProfile);
                } else {
                    imgProfile.setImageResource(R.drawable.profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi
            }
        });
    }

}