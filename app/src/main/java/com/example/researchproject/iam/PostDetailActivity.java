package com.example.researchproject.iam;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;

public class PostDetailActivity extends AppCompatActivity {

    private TextView txtTitle, txtServiceInfo, txtPrice, txtRentalTime, txtAddress, txtContact;
    private ImageView imgPost;
    private Button btnAddToCart, btnPay, btnReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        txtTitle = findViewById(R.id.txtTitle);
        txtServiceInfo = findViewById(R.id.txtServiceInfo);
        txtPrice = findViewById(R.id.txtPrice);
        txtRentalTime = findViewById(R.id.txtRentalTime);
        txtAddress = findViewById(R.id.txtAddress);
        txtContact = findViewById(R.id.txtContact);
        imgPost = findViewById(R.id.imgPost);

        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnPay = findViewById(R.id.btnPay);
        btnReview = findViewById(R.id.btnReview);

        // Nhận dữ liệu từ PostAdapter
        String title = getIntent().getStringExtra("title");
        String serviceInfo = getIntent().getStringExtra("serviceInfo");
        String price = getIntent().getStringExtra("price");
        String rentalTime = getIntent().getStringExtra("rentalTime");
        String address = getIntent().getStringExtra("address");
        String contact = getIntent().getStringExtra("contact");
        String imageUrl = getIntent().getStringExtra("imageUrl");

        // Hiển thị dữ liệu
        txtTitle.setText(title);
        txtServiceInfo.setText(serviceInfo);
        txtPrice.setText("Giá: " + price + " VND");
        txtRentalTime.setText("Thời gian thuê: " + rentalTime);
        txtAddress.setText("Địa chỉ: " + address);
        txtContact.setText("Liên hệ: " + contact);
        Glide.with(this).load(imageUrl).into(imgPost);

        // ✅ Xử lý sự kiện
        btnAddToCart.setOnClickListener(v -> {
            Toast.makeText(this, "Đã thêm vào giỏ hàng!", Toast.LENGTH_SHORT).show();
        });

        btnPay.setOnClickListener(v -> {
            Toast.makeText(this, "Chức năng thanh toán đang phát triển.", Toast.LENGTH_SHORT).show();
        });

        btnReview.setOnClickListener(v -> {
            Toast.makeText(this, "Mở tính năng đánh giá.", Toast.LENGTH_SHORT).show();
        });
    }
}
