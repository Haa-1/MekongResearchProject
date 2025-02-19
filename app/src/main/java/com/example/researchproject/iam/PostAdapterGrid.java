package com.example.researchproject.iam;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.example.researchproject.R;

import java.util.ArrayList;
import java.util.List;

public class PostAdapterGrid extends ArrayAdapter<Post> implements Filterable {
    private Context context;
    private List<Post> postList;
    private List<Post> originalPostList; // Danh sách gốc để khôi phục khi tìm kiếm


    public PostAdapterGrid(Context context, List<Post> postList) {
        super(context, R.layout.item_grid_post, postList);
        this.context = context;
        this.postList = postList;
        this.originalPostList = new ArrayList<>(postList); // Sao chép danh sách ban đầu
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // ✅ Kiểm tra nếu danh sách rỗng
        if (postList == null || postList.isEmpty()) {
            Log.e("PostAdapterGrid", "Lỗi: postList rỗng, không có dữ liệu để hiển thị!");
            return new View(context); // Trả về một View trống để tránh lỗi
        }

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_grid_post, parent, false);
        }

        // Ánh xạ các View
        TextView txtTitle = convertView.findViewById(R.id.txtTitle);
        ImageView imgPost = convertView.findViewById(R.id.imgService);
        TextView txtPrice = convertView.findViewById(R.id.txtPrice);

        // ✅ Kiểm tra vị trí hợp lệ trước khi truy cập phần tử
        if (position >= postList.size()) {
            Log.e("PostAdapterGrid", "Lỗi: vị trí " + position + " vượt quá kích thước danh sách!");
            return convertView;
        }

        // Gán dữ liệu vào GridView
        Post post = postList.get(position);
        txtTitle.setText(post.getTitle());
        txtPrice.setText("Giá: " + post.getPrice() + " VND");
        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(imgPost);

        return convertView;

    }


    // ✅ Thêm bộ lọc tìm kiếm
    public void filter(String text) {
        List<Post> filteredList = new ArrayList<>();
        if (text.isEmpty()) {
            filteredList.addAll(originalPostList); // Hiển thị tất cả nếu ô tìm kiếm rỗng
        } else {
            String searchText = text.toLowerCase();
            for (Post post : originalPostList) {
                if (post.getTitle().toLowerCase().contains(searchText) ||
                        post.getServiceInfo().toLowerCase().contains(searchText) ||
                        post.getAddress().toLowerCase().contains(searchText)) {
                    filteredList.add(post);
                }
            }
        }
        postList.clear();
        postList.addAll(filteredList);
        notifyDataSetChanged(); // Cập nhật UI
    }
    public void updateData(List<Post> newPostList) {
        this.postList.clear();               // Xóa danh sách cũ
        this.postList.addAll(newPostList);   // Thêm dữ liệu mới
        notifyDataSetChanged();              // Cập nhật lại Adapter
    }


}
