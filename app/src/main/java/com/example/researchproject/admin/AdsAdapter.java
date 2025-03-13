package com.example.researchproject.admin;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;
import com.example.researchproject.iam.Ad;
import com.example.researchproject.iam.Post;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
public class AdsAdapter extends RecyclerView.Adapter<AdsAdapter.AdViewHolder> {
    private List<Ad> adList;
    private Context context;
    public AdsAdapter(Context context, List<Ad> adList) {
        this.context = context;
        this.adList = adList;
    }
    @NonNull
    @Override
    public AdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ad, parent, false);
        return new AdViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull AdViewHolder holder, int position) {
        Ad ad = adList.get(position);
        holder.edtAdTitle.setText(ad.getTitle());
        Glide.with(context)
                .load(ad.getImageUrl())
                .placeholder(R.drawable.search_icon)
                .error(R.drawable.search_icon)
                .into(holder.imgAd);
    }
    @Override
    public int getItemCount() {
        return adList.size();
    }
    public static class AdViewHolder extends RecyclerView.ViewHolder {
        TextView edtAdTitle;
        ImageView imgAd;
        public AdViewHolder(@NonNull View itemView) {
            super(itemView);
            edtAdTitle = itemView.findViewById(R.id.edtAdTitle);
            imgAd = itemView.findViewById(R.id.imgAd);
        }
    }
}


