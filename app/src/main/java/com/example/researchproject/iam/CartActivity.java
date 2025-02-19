package com.example.researchproject.iam;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.researchproject.HomeMekong;
import com.example.researchproject.InformationActivity;
import com.example.researchproject.MekoAI;
import com.example.researchproject.R;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCart;
    private CartAdapter cartAdapter;
    private List<Post> cartList;
    private DatabaseReference cartRef;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        recyclerViewCart = findViewById(R.id.recyclerViewCart);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        recyclerViewCart.setLayoutManager(new LinearLayoutManager(this));

        cartList = new ArrayList<>();
        cartAdapter = new CartAdapter(this, cartList);
        recyclerViewCart.setAdapter(cartAdapter);

        // Xử lý sự kiện khi chọn item trong menu
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(CartActivity.this, HomeMekong.class));
                } else if (itemId == R.id.nav_ai) {
                    startActivity(new Intent(CartActivity.this, MekoAI.class));
                } else if (itemId == R.id.nav_post) {
                    startActivity(new Intent(CartActivity.this, PostActivity.class));
                } else if (itemId == R.id.nav_cart) {
                    startActivity(new Intent(CartActivity.this, CartActivity.class));
                } else if (itemId == R.id.nav_info) {
                    startActivity(new Intent(CartActivity.this, InformationActivity.class));
                } else {
                    return false;
                }

                return true;
            }
        });
        cartRef = FirebaseDatabase.getInstance().getReference("Cart");

        // Lấy danh sách sản phẩm trong giỏ hàng
        loadCartData();
    }

    private void loadCartData() {
        cartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                cartList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    cartList.add(post);
                }
                cartAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CartActivity.this, "Lỗi tải dữ liệu giỏ hàng!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
