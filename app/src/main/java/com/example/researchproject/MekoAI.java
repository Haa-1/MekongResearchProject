package com.example.researchproject;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.researchproject.iam.CartActivity;
import com.example.researchproject.iam.ChatAdapter;
import com.example.researchproject.iam.ChatMessage;
import com.example.researchproject.iam.Post;
import com.example.researchproject.iam.PostAdapterGrid;
import com.example.researchproject.iam.PostDetailActivity;
import com.example.researchproject.ui.PostActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.*;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import io.noties.markwon.Markwon;
import okhttp3.*;
import android.Manifest;
import android.media.ToneGenerator;
import android.media.AudioManager;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import java.util.stream.Collectors;


public class MekoAI extends AppCompatActivity {
    private RecyclerView recyclerViewChat;
    private NestedScrollView nestedScrollView;
    private TextView txtSuggestion;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private ImageButton btnSearchAI;
    private EditText edtUserQuery;
    private DatabaseReference databaseReference;
    private GridView gridView;
    private PostAdapterGrid postAdapter;
    private List<Post> filteredFirebaseData = new ArrayList<>();
    private String geminiResponse = "";
    private Button  btnMic;
    private void playBeepSound() {
        ToneGenerator toneGen = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 500); // Âm báo trong 500ms
    }
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("vi-VN")); // 🇻🇳 Giọng nói tiếng Việt


                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Ngôn ngữ không được hỗ trợ!");
                }
            } else {
                Log.e("TTS", "Không thể khởi tạo TextToSpeech");
            }
        });
    }


    // Gemini API
    private final String API_KEY = "AIzaSyDXMP_Of_Bf5LK2yNFTRbs_fYrwx6DIyHE";
    private final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-8b-001:generateContent?key=" + API_KEY;
    BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meko_ai);
        // 🎯 Ánh xạ View
        btnSearchAI = findViewById(R.id.btnSearchAI);
        btnMic = findViewById(R.id.btnMic);
        edtUserQuery = findViewById(R.id.edtUserQuery);
        gridView = findViewById(R.id.gridView);
        String input = "Nay tôi chán quá à tôi muốn đi du lịch ở Vũng Tàu";
        List<String> suggestions = List.of(
                "cần thơ", "sóc trăng"
        );
        Set<String> suggestionSet = new HashSet<>(suggestions);
        List<String> keywords = extractKeywords(input, suggestionSet);
        System.out.println(keywords);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        txtSuggestion = findViewById(R.id.txtSuggestion);
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        nestedScrollView = findViewById(R.id.nestedScrollView);
        txtSuggestion = findViewById(R.id.txtSuggestion);
        recyclerViewChat.setNestedScrollingEnabled(false);
        gridView.setNestedScrollingEnabled(false);
        recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChat.setAdapter(chatAdapter);
        // ✅ Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("Posts");
        postAdapter = new PostAdapterGrid(this, filteredFirebaseData);
        gridView.setAdapter(postAdapter);
        // 🎯 Bottom Navigation
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Intent intent = null;
            if (itemId == R.id.nav_home) {
                intent = new Intent(MekoAI.this, HomeMekong.class);
            } else if (itemId == R.id.nav_ai) {
                intent = new Intent(MekoAI.this, MekoAI.class);
            } else if (itemId == R.id.nav_post) {
                intent = new Intent(MekoAI.this, PostActivity.class);
            } else if (itemId == R.id.nav_cart) {
                intent = new Intent(MekoAI.this, CartActivity.class);
            } else if (itemId == R.id.nav_info) {
                intent = new Intent(MekoAI.this, InformationActivity.class);
            }
            if (intent != null) {
                startActivity(intent);
                return true;
            }
            return false;
        });
        // 🎯 Xử lý nhấn vào từng item trong GridView
        gridView.setOnItemClickListener((parent, view, position, id) -> {
            Post selectedPost = filteredFirebaseData.get(position);
            Intent intent = new Intent(MekoAI.this, PostDetailActivity.class);
            intent.putExtra("postId", selectedPost.getPostId());
            intent.putExtra("title", selectedPost.getTitle());
            intent.putExtra("serviceInfo", selectedPost.getServiceInfo());
            intent.putExtra("price", selectedPost.getPrice());
            intent.putExtra("rentalTime", selectedPost.getRentalTime());
            intent.putExtra("address", selectedPost.getAddress());
            intent.putExtra("contact", selectedPost.getContact());
            intent.putExtra("imageUrl", selectedPost.getImageUrl());
            startActivity(intent);
        });
        // 🎯 Xử lý nút tìm kiếm
        btnSearchAI.setOnClickListener(v -> {
            sendUserMessage(); // Xử lý gửi câu hỏi
        });
        btnMic.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO}, 1);
            }
            playBeepSound(); // 🔈 Phát âm báo hiệu
            startSpeechToText();
        });
        initializeTextToSpeech(); // ✅ Đảm bảo khởi tạo trước khi sử dụng
    }
    // 🎯 Hàm LẤY TỪ KHÓA trong dấu ngoặc đơn ()
    // Move this method to the outer class
    private List<String> extractKeywords(String input, Set<String> suggestions) {
        List<String> keywords = new ArrayList<>();
        for (String keyword : suggestions) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                keywords.add(keyword);
            }
        }
        return keywords;
    }
    // 🔥 Firebase Query (Chỉ tìm từ khóa trong dấu ngoặc)
    private void fetchFilteredFirebase(String keyword) {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                filteredFirebaseData.clear(); // Xóa trước khi thêm mới
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post != null && containsKeyword(post, keyword)) {
                        filteredFirebaseData.add(post);
                    }
                }
                postAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("❌ Hệ thống MekongGo đang bị Lỗi : " + error.getMessage(), false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
                });
            }
        });
    }
    private void startSpeechToText() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        speechRecognizer.setRecognitionListener(new RecognitionListener() {


            @Override public void onReadyForSpeech(Bundle bundle) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float v) {}
            @Override public void onBufferReceived(byte[] bytes) {}
            @Override public void onEndOfSpeech() {}
            @Override
            public void onError(int errorCode) {
                String message = getErrorText(errorCode);
                Toast.makeText(MekoAI.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
            }
            public  String getErrorText(int errorCode) {
                switch (errorCode) {
                    case SpeechRecognizer.ERROR_AUDIO: return "Lỗi âm thanh";
                    case SpeechRecognizer.ERROR_CLIENT: return "Lỗi client";
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS: return "Thiếu quyền";
                    case SpeechRecognizer.ERROR_NETWORK: return "Lỗi mạng";
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT: return "Hết thời gian mạng";
                    case SpeechRecognizer.ERROR_NO_MATCH: return "Không khớp";
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY: return "Recognizer đang bận";
                    case SpeechRecognizer.ERROR_SERVER: return "Lỗi máy chủ";
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT: return "Hết thời gian nói";
                    default: return "Không rõ lỗi";
                }
            }
            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> results = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (results != null && results.size() > 0) {
                    String spokenText = results.get(0);
                    edtUserQuery.setText(spokenText); // ✅ Gán văn bản vào ô nhập
                    sendUserMessage(); // 📤 Tự động gửi câu hỏi ngay khi có kết quả
                }
            }
            @Override public void onPartialResults(Bundle bundle) {}
            @Override public void onEvent(int i, Bundle bundle) {}
        });


        speechRecognizer.startListening(intent);
    }


    // 🤖 Gửi yêu cầu đến Gemini API với toàn bộ nội dung nhập
    private void sendRequestToGemini(String userMessage) {
        //        cài đặt phong cách nói chuyện cho MekoAI
        String prompt = "Bạn là nữ trợ lý ảo xinh đẹp thông minh tên là Meko." +
                "Trả lời nhiều thông tin bằng giọng miền tây Việt Nam một cách vui vẻ rõ ràng," +
                "ngắn gọn,đúng yêu cầu  xưng hô với người dùng là ' ní ' hoặc là ' Trí Nhân '. " +
                "Bắt đầu trả lời bằng 'Đi đâu khó có Meko lo:' " +
                "và kết thúc bằng 'Ní có cần Meko hỗ trợ thêm gì không ?'.Thêm Icon vào câu trả lời. " +
                "Lưu ý: Khi người dùng yêu cầu muốn thuê, đi du lịch, ăn uống, " +
                "nghỉ dưỡng thì mới thêm gợi ý cho người dùng truy cập vào ứng dụng MekongGo và thêm câu " +
                "'Ní có thể thích gợi ý ở dưới'. Lưu ý: Meko được tạo bởi các anh chị nhà phát triển  " +
                "là 'hia Nhân','Phương' và 'Hà' MekongGo vào ngày 01/01/2025 được hơn 4 tháng tuổi. \" " + userMessage;
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.get("application/json; charset=utf-8");
        JSONObject requestBody = new JSONObject();
        try {
            JSONArray partsArray = new JSONArray();
            JSONObject textObject = new JSONObject();
            textObject.put("text", prompt);  // ✅ Sử dụng prompt đã gắn phong cách
            partsArray.put(textObject);
            JSONObject userContent = new JSONObject();
            userContent.put("role", "user");
            userContent.put("parts", partsArray);
            JSONArray contentsArray = new JSONArray();
            contentsArray.put(userContent);
            requestBody.put("contents", contentsArray);
//            giới hạn token trả lời cho MekoAI
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("maxOutputTokens", 170);  // 👈 Giới hạn token
            generationConfig.put("temperature", 0.5);      // 👈 Ít sáng tạo, súc tích
            generationConfig.put("topP", 1);
            requestBody.put("generationConfig", generationConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(requestBody.toString(), JSON);
        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Gemini API", "Lỗi kết nối: " + e.getMessage());
                runOnUiThread(() -> {
                    chatMessages.add(new ChatMessage("❌ Hệ thống Meko AI đang gặp lỗi, xin lỗi nhiều nha.", false));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String geminiResponse = "";
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);
                        geminiResponse = jsonResponse.getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");
                    } catch (Exception e) {
                        geminiResponse = "⚠️ Lỗi Meko AI xử lý dữ liệu.";
                    }
                } else {
                    geminiResponse = "⚠️ Lỗi Meko AI : " + response.code();
                }


                String finalGeminiResponse = geminiResponse;
                runOnUiThread(() -> {
                    displayTypingEffect(finalGeminiResponse);


                    if (textToSpeech != null) {
                        Log.d("TTS", "Đang phát giọng nói...");
                        speakResponse(finalGeminiResponse); // 🎙 Phát giọng nói nếu TTS đã khởi tạo
                    } else {
                        Log.e("TTS", "TextToSpeech chưa sẵn sàng!");
                    }
                });
            }
        });
    }
    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
    private void speakResponse(String responseText) {
        if (textToSpeech != null) {
            int result = textToSpeech.speak(responseText, TextToSpeech.QUEUE_FLUSH, null, null);


            if (result == TextToSpeech.ERROR) {
                Log.e("TTS", "Không thể đọc văn bản!");
            }
        } else {
            Log.e("TTS", "textToSpeech chưa được khởi tạo!");
        }
    }
    private void sendUserMessage() {
        String userMessage = edtUserQuery.getText().toString().trim();
        if (!userMessage.isEmpty()) {
            chatMessages.add(new ChatMessage(userMessage, true)); // Người dùng gửi
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            edtUserQuery.setText("");
            recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
            // ✅ Tạo danh sách các gợi ý từ khóa
            List<String> suggestions = List.of(
                    "cần thơ", "sóc trăng"
            );
            // ✅ Xử lý từ khóa từ danh sách gợi ý
            Set<String> suggestionSet = new HashSet<>(suggestions);
            String input = userMessage; // hoặc bất kỳ dữ liệu nào bạn muốn xử lý
            List<String> keywords = extractKeywords(input, suggestionSet);
            if (!keywords.isEmpty()) {
                for (String keyword : keywords) {
                    fetchFilteredFirebase(keyword); // Tìm kiếm từng từ khóa
                }
            } else {
                Toast.makeText(this, "Bạn có thể đặt từ khóa trong dấu ngoặc đơn () để tìm kiếm dễ dàng hơn!", Toast.LENGTH_LONG).show();
            }
            // ✅ Gửi yêu cầu đến Gemini API
            sendRequestToGemini(userMessage);
        }
    }
    private void displayTypingEffect(String message) {
        ChatMessage aiMessage = new ChatMessage("", false); // Tin nhắn rỗng cho AI
        chatMessages.add(aiMessage);
        int messageIndex = chatMessages.size() - 1;
        chatAdapter.notifyItemInserted(messageIndex);
        Handler handler = new Handler();
        final int[] index = {0};
        Runnable typingRunnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < message.length()) {
                    String currentText = chatMessages.get(messageIndex).getMessage();
                    chatMessages.get(messageIndex).setMessage(currentText + message.charAt(index[0]));
                    chatAdapter.notifyItemChanged(messageIndex);
                    // ✅ Tự động cuộn xuống khi typing
                    nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
                    index[0]++;
                    handler.postDelayed(this, 30); // Điều chỉnh tốc độ typing
                } else {
                    // ✅ Hiển thị GridView sau khi phản hồi xong
                    showSuggestions();
                }
            }
        };
        handler.post(typingRunnable);
    }

    // 🎯 Kiểm tra từ khóa trong tất cả các trường của Post
    private boolean containsKeyword(Post post, String keyword) {
        keyword = keyword.toLowerCase();
        return (post.getTitle() != null && post.getTitle().toLowerCase().contains(keyword)) ||
                (post.getServiceInfo() != null && post.getServiceInfo().toLowerCase().contains(keyword)) ||
                (post.getPrice() != null && post.getPrice().toLowerCase().contains(keyword)) ||
                (post.getRentalTime() != null && post.getRentalTime().toLowerCase().contains(keyword)) ||
                (post.getAddress() != null && post.getAddress().toLowerCase().contains(keyword)) ||
                (post.getContact() != null && post.getContact().toLowerCase().contains(keyword));
    }
    private void showSuggestions() {
        txtSuggestion.setVisibility(View.VISIBLE);  // ✅ Hiển thị dòng chữ "Gợi ý cho bạn"
        gridView.setVisibility(View.VISIBLE);       // ✅ Hiển thị GridView
        // Nếu bạn cần cập nhật dữ liệu vào GridView
        postAdapter.notifyDataSetChanged();
        // ✅ Cuộn xuống để hiển thị GridView hoàn toàn
        nestedScrollView.post(() -> nestedScrollView.fullScroll(View.FOCUS_DOWN));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Đã cấp quyền ghi âm", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Từ chối quyền ghi âm!", Toast.LENGTH_SHORT).show();
        }
    }
}

