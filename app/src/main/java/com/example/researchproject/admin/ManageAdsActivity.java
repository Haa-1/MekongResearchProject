package com.example.researchproject.admin;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.researchproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ManageAdsActivity extends AppCompatActivity {
    private ListView listViewAds;
    private ArrayAdapter<String> adapter;
    private List<String> adList;
    private DatabaseReference adRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_ads);

        listViewAds = findViewById(R.id.listViewAds);
        adRef = FirebaseDatabase.getInstance().getReference("Ads");
        adList = new ArrayList<>();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, adList);
        listViewAds.setAdapter(adapter);

        loadAds();
    }

    private void loadAds() {
        adRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                adList.clear();
                for (DataSnapshot adSnapshot : snapshot.getChildren()) {
                    String title = adSnapshot.child("title").getValue(String.class);
                    adList.add(title);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ManageAdsActivity.this, "Lỗi tải quảng cáo!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
