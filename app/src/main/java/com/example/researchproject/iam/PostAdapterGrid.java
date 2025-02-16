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
    private List<Post> filteredPostList;

    public PostAdapterGrid(Context context, List<Post> postList) {
        super(context, R.layout.item_grid_post, postList);
        this.context = context;
        this.postList = postList;
        this.filteredPostList = new ArrayList<>(postList);
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

        // ✅ Kiểm tra vị trí hợp lệ trước khi truy cập phần tử
        if (position >= postList.size()) {
            Log.e("PostAdapterGrid", "Lỗi: vị trí " + position + " vượt quá kích thước danh sách!");
            return convertView;
        }

        // Gán dữ liệu vào GridView
        Post post = postList.get(position);
        txtTitle.setText(post.getTitle());

        Glide.with(context)
                .load(post.getImageUrl())
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(imgPost);

        return convertView;
    }


    // ✅ Thêm bộ lọc tìm kiếm
    public void filter(String query) {
        List<Post> filteredList = new ArrayList<>();
        if (query.isEmpty()) {
            filteredList.addAll(postList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Post post : postList) {
                if (post.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        post.getAddress().toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(post);
                }
            }
        }
        clear();
        addAll(filteredList);
        notifyDataSetChanged();
    }

}
