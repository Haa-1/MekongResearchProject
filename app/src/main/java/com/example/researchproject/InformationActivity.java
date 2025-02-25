package com.example.researchproject;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.researchproject.databinding.ActivityInformationBinding;
import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.LoginActivity;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
public class InformationActivity extends AppCompatActivity {
    TextView tv_profile;
    private ActivityInformationBinding binding;
    private FirebaseAuth mAuth;
    private Button  btnLogout;
    BottomNavigationView bottomNavigationView;
    private TextView txtWelcome;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tv_profile = (TextView) findViewById(R.id.tv_profile);
        binding = ActivityInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // ✅ Khởi tạo FirebaseAuth và đặt ngôn ngữ
        mAuth = FirebaseAuth.getInstance();
        txtWelcome = findViewById(R.id.txtWelcome);
        // ✅ Hiển thị email người dùng
        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "User";
        txtWelcome.setText("Email:  " + userEmail );
        btnLogout = findViewById(R.id.btnLogout);
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
        // ✅ Xử lý đăng xuất
        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(InformationActivity.this, LoginActivity.class));
            finish();
        });
    }
}