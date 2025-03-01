package com.example.researchproject.iam;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.researchproject.Payment.OrderInformationActivity;
import com.example.researchproject.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {
    private Context context;
    private List<Post> cartList;
    public CartAdapter(Context context, List<Post> cartList) {
        this.context = context;
        this.cartList = cartList;
    }
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Post post = cartList.get(position);
        holder.txtTitle.setText(post.getTitle());
        holder.txtPrice.setText("Giá: " + post.getPrice() + " VND");

        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.search_icon)
                .error(R.drawable.search_icon)
                .into(holder.imgService);
        // ✅ Nhấn vào item → chuyển sang PostDetailActivity
        holder.itemView.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Post selectedPost = cartList.get(currentPosition);
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", selectedPost.getPostId());
                intent.putExtra("title", selectedPost.getTitle());
                intent.putExtra("serviceInfo", selectedPost.getServiceInfo());
                intent.putExtra("price", selectedPost.getPrice());
                intent.putExtra("rentalTime", selectedPost.getRentalTime());
                intent.putExtra("address", selectedPost.getAddress());
                intent.putExtra("contact", selectedPost.getContact());
                intent.putExtra("imageUrl", selectedPost.getImageUrl());
                context.startActivity(intent);
            }
        });
        // ✅ Xử lý nút Xóa
        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            // ⚠️ Kiểm tra vị trí hợp lệ
            if (currentPosition == RecyclerView.NO_POSITION || currentPosition >= cartList.size()) {
                Toast.makeText(context, "Vị trí không hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            Post currentPost = cartList.get(currentPosition);
            String postId = currentPost.getPostId();

            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "Không tìm thấy Post ID!", Toast.LENGTH_SHORT).show();
                return;
            }
            // 🔥 Xóa khỏi Firebase
            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart");
            cartRef.orderByChild("postId").equalTo(postId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    dataSnapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                                        // ✅ Lấy vị trí lại ngay trước khi xóa
                                        int updatedPosition = holder.getAdapterPosition();
                                        if (updatedPosition != RecyclerView.NO_POSITION && updatedPosition < cartList.size()) {
                                            cartList.remove(updatedPosition); // Xóa trong danh sách
                                            notifyItemRemoved(updatedPosition); // Cập nhật UI
                                            Toast.makeText(context, "Đã xóa khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(e -> {
                                        Toast.makeText(context, "Lỗi khi xóa khỏi Firebase!", Toast.LENGTH_SHORT).show();
                                    });
                                }
                            } else {
                                Toast.makeText(context, "Không tìm thấy sản phẩm!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(context, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
        // ✅ Nút Thanh Toán (Chưa triển khai)
        holder.btnPay.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Post selectedPost = cartList.get(currentPosition);

                // 🔹 Tạo Intent để chuyển sang OrderInformationActivity
                Intent intent = new Intent(context, OrderInformationActivity.class);
                intent.putExtra("postId", selectedPost.getPostId());
                intent.putExtra("title", selectedPost.getTitle());
                intent.putExtra("price", selectedPost.getPrice());

                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Lỗi khi chọn sản phẩm!", Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public int getItemCount() {
        return cartList.size();
    }
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        TextView txtTitle, txtPrice;
        ImageView imgService;
        ImageButton btnDelete, btnPay;
        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            imgService = itemView.findViewById(R.id.imgService);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnPay = itemView.findViewById(R.id.btnPay);
        }
    }
}
