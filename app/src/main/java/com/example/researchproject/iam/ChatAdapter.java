package com.example.researchproject.iam;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.noties.markwon.Markwon;

import com.example.researchproject.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<ChatMessage> chatMessages;
    private Context context;
    private Markwon markwon;

    public ChatAdapter(Context context, List<ChatMessage> chatMessages) {
        this.context = context;
        this.chatMessages = chatMessages;
        // ✅ Khởi tạo Markwon
        this.markwon = Markwon.create(context);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        // ✅ Kiểm tra xem có phải tin nhắn từ AI không
        if (!message.isUser()) {
            // Áp dụng định dạng Markdown cho phản hồi từ AI
            markwon.setMarkdown(holder.txtMessage, message.getMessage());
        } else {
            // Tin nhắn từ người dùng hiển thị bình thường
            holder.txtMessage.setText(message.getMessage());
        }
        // 🎨 Xử lý màu sắc và emoji
        SpannableString spannable = new SpannableString(message.getMessage());

        // 🔥 Tô màu chữ cho từ khóa "Meko AI"
        String keyword = "Meko AI";
        int start = message.getMessage().indexOf(keyword);
        if (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")),
                    start, start + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, start + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        // 🌟 Tô màu emoji (ví dụ các emoji phổ biến)
        String[] emojis = {"😊", "🎉", "🚀", "🔥", "💡"};
        for (String emoji : emojis) {
            int emojiStart = message.getMessage().indexOf(emoji);
            while (emojiStart >= 0) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD700")), // 🌟 Màu vàng
                        emojiStart, emojiStart + emoji.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                emojiStart = message.getMessage().indexOf(emoji, emojiStart + 1);
            }
        }

        holder.txtMessage.setText(spannable);

        // 🎭 Đổi avatar tùy theo người dùng hoặc AI
        if (message.isUser()) {
            holder.imgAvatar.setImageResource(R.drawable.user);
            holder.txtMessage.setBackgroundResource(R.drawable.bg_chat_bubble_user);
        } else {
            holder.imgAvatar.setImageResource(R.drawable.search_icon);
            holder.txtMessage.setBackgroundResource(R.drawable.bg_chat_bubble_ai);
        }
    }
    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;
        ImageView imgAvatar;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }
}
