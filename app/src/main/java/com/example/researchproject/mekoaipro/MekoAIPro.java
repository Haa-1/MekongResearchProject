package com.example.researchproject.mekoaipro;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.researchproject.R;
import com.example.researchproject.mekoaipro.ChatAdapterPro;
import com.example.researchproject.mekoaipro.ChatMessagePro;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.*;
public class MekoAIPro extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapterPro chatAdapter;
    private List<ChatMessagePro> chatMessages;
    private EditText inputMessage;
    private ImageButton sendButton;
    private final OkHttpClient client = new OkHttpClient();
    private static final String OPENAI_API_KEY = "";  // Thay bằng API Key mới
    // Firebase
    private DatabaseReference chatHistoryRef;
    private String uid;
     //  private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meko_aipro);
        recyclerView = findViewById(R.id.recyclerView);
        inputMessage = findViewById(R.id.inputMessage);
        sendButton = findViewById(R.id.sendButton);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapterPro(chatMessages);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Lấy UID của người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
            chatHistoryRef = FirebaseDatabase.getInstance().getReference("chat_history").child(uid);
            loadChatHistory();
        } else {
            uid = null;
        }
        sendButton.setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                detectUserIntent(messageText);  // Gọi detectUserIntent tại đây
                sendMessage(messageText);
                inputMessage.setText("");
            }
        });

    }
    private void sendMessage(String messageText) {
        ChatMessagePro userMessage = new ChatMessagePro(messageText, true);
        chatMessages.add(userMessage);
        chatAdapter.notifyDataSetChanged();
        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);

        // Lưu tin nhắn của user vào Firebase
        saveMessageToFirebase(messageText, "user");

        // Lấy dữ liệu Firebase trước khi gửi lên OpenAI
        fetchTitlesFromFirebase((titles) -> {
            String combinedMessage = messageText + "\n\n Dữ liệu từ Firebase: " + titles;
            getResponseFromOpenAI(combinedMessage);
        });
    }

    private void getResponseFromOpenAI(String question) {
        // Lấy dữ liệu từ Firebase
        fetchTitlesFromFirebase((titles) -> {
            JSONObject jsonObject = new JSONObject();
            try {
                JSONArray messages = new JSONArray();
                // 1. Định nghĩa vai trò (system)
                JSONObject systemMessage = new JSONObject();
                systemMessage.put("role", "system");
                systemMessage.put("content", "Bạn là một trợ lý ảo thông minh của Trí Nhân tên là Meko.Trả lời nhiều thông tin bằng giọng miền tây Việt Nam một cách vui vẻ rõ ràng, xưng hô với người dùng là ' ní ' hoặc là ' Trí Nhân ', bắt đầu bằng'Phản hồi từ Meko:' và kết thúc bằng 'Ní cần hỗ trợ thêm gì không ?'.Thêm Icon vào câu trả lời\" ");
                messages.put(systemMessage);
                // 2. Lịch sử hội thoại (nếu cần)
                for (ChatMessagePro chatMessage : chatMessages) {
                    JSONObject message = new JSONObject();
                    message.put("role", chatMessage.isUser() ? "user" : "assistant");
                    message.put("content", chatMessage.getMessage());
                    messages.put(message);
                }
                // Ghép tiêu đề lấy từ Firebase vào câu hỏi của user
                String combinedInput = question  + titles;
                JSONObject currentMessage = new JSONObject();
                currentMessage.put("role", "user");
                currentMessage.put("content", combinedInput);
                messages.put(currentMessage);

                jsonObject.put("model", "");
                jsonObject.put("messages", messages);
                jsonObject.put("max_tokens", 150);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("OpenAIRequest",jsonObject.toString());
            RequestBody body = RequestBody.create(
                    jsonObject.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );
            Request request = new Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + OPENAI_API_KEY)
                    .header("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessagePro("Lỗi mạng: " + e.getMessage(), false));
                        chatAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject jsonResponse = new JSONObject(responseBody);
                            final String answer = jsonResponse.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");
                            runOnUiThread(() -> {
                                ChatMessagePro botMessage = new ChatMessagePro(answer.trim(), false);
                                chatMessages.add(botMessage);
                                chatAdapter.notifyDataSetChanged();
                                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                                saveMessageToFirebase(answer.trim(), "bot");
                            });
                        } catch (Exception e) {
                            runOnUiThread(() -> {
                                chatMessages.add(new ChatMessagePro("Lỗi xử lý phản hồi: " + e.getMessage(), false));
                                chatAdapter.notifyDataSetChanged();
                            });
                        }
                    } else {
                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessagePro("Lỗi API: " + response.message(), false));
                            chatAdapter.notifyDataSetChanged();
                        });
                    }
                }
            });
        });
    }

    private void saveMessageToFirebase(String message, String sender) {
        if (uid == null) return;
        DatabaseReference newMessageRef = chatHistoryRef.push();
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("message", message);
        messageData.put("sender", sender);
        messageData.put("timestamp", System.currentTimeMillis());
        newMessageRef.setValue(messageData);
    }
    private void detectUserIntent(String userMessage) {
        LanguageIdentifier languageIdentifier = LanguageIdentification.getClient();
        languageIdentifier.identifyLanguage(userMessage)
                .addOnSuccessListener(languageCode -> {
                    if (languageCode.equals("und")) {
                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessagePro("Không thể nhận diện ngôn ngữ.", false));
                            chatAdapter.notifyDataSetChanged();
                        });
                        return;
                    }
                    if (!languageCode.equals("vi") && !languageCode.equals("en")) {
                        runOnUiThread(() -> {
                            chatMessages.add(new ChatMessagePro("Chỉ hỗ trợ tiếng Việt và tiếng Anh.", false));
                            chatAdapter.notifyDataSetChanged();
                        });
                        return;
                    }
                    // Nếu ngôn ngữ hợp lệ, gọi OpenAI API
                    getResponseFromOpenAI(userMessage);
                })
                .addOnFailureListener(e -> {
                    runOnUiThread(() -> {
                        chatMessages.add(new ChatMessagePro("Lỗi nhận diện: " + e.getLocalizedMessage(), false));
                        chatAdapter.notifyDataSetChanged();
                    });
                });
    }
    private void fetchTitlesFromFirebase(FirebaseCallback callback) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference("Posts");
        DatabaseReference adsRef = FirebaseDatabase.getInstance().getReference("Ads");

        List<String> titles = new ArrayList<>();

        postsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    String title = postSnapshot.child("title").getValue(String.class);
                    if (title != null) {
                        titles.add(title);
                    }
                }
                adsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot adSnapshot : snapshot.getChildren()) {
                            String title = adSnapshot.child("title").getValue(String.class);
                            if (title != null) {
                                titles.add(title);
                            }
                        }
                        // Ghép tất cả tiêu đề lại thành một chuỗi
                        String result = TextUtils.join(" | ", titles);
                        callback.onCallback(result);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        callback.onCallback("");  // Trả về chuỗi rỗng nếu có lỗi
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onCallback("");  // Trả về chuỗi rỗng nếu có lỗi
            }
        });
    }
    interface FirebaseCallback {
        void onCallback(String titles);
    }

    private void loadChatHistory() {
        chatHistoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatMessages.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String message = data.child("message").getValue(String.class);
                    String sender = data.child("sender").getValue(String.class);
                    boolean isUser = sender != null && sender.equals("user");
                    chatMessages.add(new ChatMessagePro(message, isUser));
                }
                chatAdapter.notifyDataSetChanged();
                recyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MekoAIPro.this, "Lỗi tải lịch sử: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}