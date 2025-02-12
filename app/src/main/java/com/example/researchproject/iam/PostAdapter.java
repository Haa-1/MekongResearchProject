package com.example.researchproject.iam;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> implements Filterable {

    private Context context;
    private List<Post> postList;          // Danh sách gốc
    private List<Post> filteredPostList;  // Danh sách đã lọc

    public PostAdapter(Context context, List<Post> postList) {
        this.context = context;
        this.postList = postList;
        this.filteredPostList = new ArrayList<>(postList); // Khởi tạo danh sách lọc ban đầu
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = filteredPostList.get(position);
        holder.txtTitle.setText(post.getTitle());
        holder.txtPrice.setText("Giá: " + post.getPrice() + " VND");
        holder.txtAddress.setText("Địa chỉ: " + post.getAddress());

        Glide.with(context).load(post.getImageUrl()).into(holder.imgPost);

        // Xử lý khi nhấn vào bài đăng
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PostDetailActivity.class);
            intent.putExtra("title", post.getTitle());
            intent.putExtra("serviceInfo", post.getServiceInfo());
            intent.putExtra("price", post.getPrice());
            intent.putExtra("rentalTime", post.getRentalTime());
            intent.putExtra("address", post.getAddress());
            intent.putExtra("contact", post.getContact());
            intent.putExtra("imageUrl", post.getImageUrl());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return filteredPostList.size(); // Hiển thị danh sách đã lọc
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtPrice, txtAddress;
        ImageView imgPost;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            imgPost = itemView.findViewById(R.id.imgPost);
        }
    }

    // ✅ Lọc dữ liệu khi tìm kiếm
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Post> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(postList); // Không có từ khóa thì hiển thị toàn bộ
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Post post : postList) {
                        if (post.getTitle().toLowerCase().contains(filterPattern) ||
                                post.getAddress().toLowerCase().contains(filterPattern) ||
                                post.getServiceInfo().toLowerCase().contains(filterPattern)) {
                            filteredList.add(post); // Thêm bài đăng phù hợp vào danh sách
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                filteredPostList.clear();
                filteredPostList.addAll((List) results.values);
                notifyDataSetChanged(); // Cập nhật RecyclerView
            }
        };
    }
}
