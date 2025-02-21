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

    private final Context context;
    private final List<ChatMessage> chatMessages;
    private final Markwon markwon; // 👉 Markwon để hiển thị Markdown

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

        // 🔥 Nếu là phản hồi từ AI → áp dụng Markdown
        if (!message.isUser()) {
            markwon.setMarkdown(holder.txtMessage, message.getMessage());
        } else {
            // 💬 Nếu là người dùng → chỉ hiển thị văn bản bình thường
            holder.txtMessage.setText(message.getMessage());
        }

        // 🎨 Tạo Spannable để làm nổi bật từ khóa và emoji
        SpannableString spannable = new SpannableString(holder.txtMessage.getText());

        // ✅ Tô màu từ khóa "Meko AI"
        String keyword = "Meko AI";
        int start = spannable.toString().indexOf(keyword);
        while (start >= 0) {
            spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FF5722")),
                    start, start + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD),
                    start, start + keyword.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            start = spannable.toString().indexOf(keyword, start + 1);
        }

        // 😄 Tô màu các emoji
        String[] emojis = {"😊", "🎉", "🚀", "🔥", "💡", "💖", "😎", "✨"};
        for (String emoji : emojis) {
            int emojiStart = spannable.toString().indexOf(emoji);
            while (emojiStart >= 0) {
                spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#FFD700")), // Màu vàng
                        emojiStart, emojiStart + emoji.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                emojiStart = spannable.toString().indexOf(emoji, emojiStart + 1);
            }
        }

        // 👉 Gán lại sau khi chỉnh định dạng
        holder.txtMessage.setText(spannable);

        // 💡 Đổi avatar và màu nền dựa theo AI hoặc User
        if (message.isUser()) {
            holder.imgAvatar.setImageResource(R.drawable.user); // Icon người dùng
            holder.txtMessage.setBackgroundResource(R.drawable.bg_chat_bubble_user); // Nền màu xanh
        } else {
            holder.imgAvatar.setImageResource(R.drawable.search_icon); // Icon AI
            holder.txtMessage.setBackgroundResource(R.drawable.bg_chat_bubble_ai); // Nền màu xám
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
