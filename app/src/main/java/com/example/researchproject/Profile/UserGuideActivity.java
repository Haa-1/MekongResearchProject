package com.example.researchproject.Profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;


import com.example.researchproject.R;

public class UserGuideActivity extends AppCompatActivity {

    private Button btnUserInfo, btnSecurity, btnNotifications, btnHelp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_guide);

        // Ánh xạ các Button
        btnUserInfo = findViewById(R.id.btn_user_info);
        btnSecurity = findViewById(R.id.btn_security);
        btnNotifications = findViewById(R.id.btn_notifications);
        btnHelp = findViewById(R.id.btn_help);

        // Xử lý sự kiện khi nhấn vào từng Button
        btnUserInfo.setOnClickListener(v -> openGuideDetail("Hướng dẫn chung"));
        btnSecurity.setOnClickListener(v -> openGuideDetail("Hướng dẫn đặt xe"));
        btnNotifications.setOnClickListener(v -> openGuideDetail("Hướng dẫn thanh toán"));
        btnHelp.setOnClickListener(v -> openGuideDetail("Quy chế hoạt động"));
    }

    private void openGuideDetail(String title) {
        Intent intent = new Intent(this, GuideDetailActivity.class);
        intent.putExtra("GUIDE_TITLE", title);
        startActivity(intent);
    }
}