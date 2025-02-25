package com.example.researchproject.iam;

import android.content.Context;
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
import com.example.researchproject.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

        // Xóa sản phẩm khỏi giỏ hàng
        holder.btnDelete.setOnClickListener(v -> {
            String postId = post.getPostId();
            Log.d("CartAdapter", "Deleting Post ID: " + postId);

            if (postId == null || postId.isEmpty()) {
                Toast.makeText(context, "Không tìm thấy Post ID!", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference cartRef = FirebaseDatabase.getInstance().getReference("Cart");
            cartRef.child(postId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        cartList.remove(position);
                        notifyItemRemoved(position);
                        Toast.makeText(context, "Đã xóa khỏi giỏ hàng!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Lỗi khi xóa khỏi Firebase!", Toast.LENGTH_SHORT).show();
                    });
        });


        // Xử lý thanh toán
        holder.btnPay.setOnClickListener(v -> {
            Toast.makeText(context, "Chức năng thanh toán đang phát triển.", Toast.LENGTH_SHORT).show();
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
