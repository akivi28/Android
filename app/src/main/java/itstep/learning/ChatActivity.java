package itstep.learning;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatActivity extends AppCompatActivity {
    private final String chatUrl = "https://chat.momentfor.fun/";
//    private TextView tvTitle;
    private LinearLayout chatContainer;
    private ScrollView chatScroller;
    private EditText etAuthor, etMessage;
    private final ExecutorService threadPool = Executors.newFixedThreadPool(3);
    private final Gson gson = new Gson();
    private static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT);
    private final List<ChatMessage> messages = new ArrayList<>();
    private static final String authorFileName = "author.name";
    private final Handler handler = new Handler();

    private View vBell;
    private Animation bellAnimation;

    private final Map<String, String> emoji = new HashMap<String, String>() { {
        put(":):", new String(Character.toChars(0x1F600))); // Grinning Face
        put(":D:", new String(Character.toChars(0x1F603))); // Smiling Face
        put(":;):", new String(Character.toChars(0x1F609))); // Winking Face
        put(":P:", new String(Character.toChars(0x1F61B))); // Tongue Out
        put(":'(:", new String(Character.toChars(0x1F622))); // Crying Face
        put(":(:", new String(Character.toChars(0x1F641))); // Frowning Face
        // Animals
        put(":cat:", new String(Character.toChars(0x1F408))); // Cat
        put(":dog:", new String(Character.toChars(0x1F436))); // Dog
        put(":fox:", new String(Character.toChars(0x1F98A))); // Fox
        put(":panda:", new String(Character.toChars(0x1F43C))); // Panda
        // Objects
        put(":heart:", new String(Character.toChars(0x2764))); // Heart
        put(":star:", new String(Character.toChars(0x2B50))); // Star
        put(":fire:", new String(Character.toChars(0x1F525))); // Fire
        put(":phone:", new String(Character.toChars(0x1F4F1))); // Mobile Phone
        // Nature
        put(":sun:", new String(Character.toChars(0x2600))); // Sun
        put(":moon:", new String(Character.toChars(0x1F319))); // Crescent Moon
        put(":tree:", new String(Character.toChars(0x1F333))); // Deciduous Tree
        put(":flower:", new String(Character.toChars(0x1F33C))); // Blossom
        // Food
        put(":apple:", new String(Character.toChars(0x1F34E))); // Red Apple
        put(":pizza:", new String(Character.toChars(0x1F355))); // Pizza
        put(":coffee:", new String(Character.toChars(0x2615))); // Hot Beverage
        put(":cake:", new String(Character.toChars(0x1F382))); // Birthday Cake
        // Flags
        put(":flag_us:", new String(Character.toChars(0x1F1FA)) + new String(Character.toChars(0x1F1F8))); // US Flag
        put(":flag_fr:", new String(Character.toChars(0x1F1EB)) + new String(Character.toChars(0x1F1F7))); // France Flag
        put(":flag_jp:", new String(Character.toChars(0x1F1EF)) + new String(Character.toChars(0x1F1F5))); // Japan Flag
        // Symbols
        put(":check:", new String(Character.toChars(0x2714))); // Check Mark
        put(":cross:", new String(Character.toChars(0x274C))); // Cross Mark
        put(":warning:", new String(Character.toChars(0x26A0)));
    } } ;
    private MediaPlayer incomingMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
//        tvTitle = findViewById(R.id.chat_tv_title);
        LinearLayout emojiContainer = findViewById(R.id.chat_ll_emoji);
        chatContainer = findViewById(R.id.chat_ll_container);
        chatScroller = findViewById(R.id.chat_scroller);
        etAuthor = findViewById(R.id.chat_et_author);
        etMessage = findViewById(R.id.chat_et_message_text);
        findViewById(R.id.chat_btn_send).setOnClickListener(this::sendButtonClick);

        vBell         = findViewById( R.id.chat_bell         );
        bellAnimation = AnimationUtils.loadAnimation(this, R.anim.bell );

        incomingMessage = MediaPlayer.create( this, R.raw.hit_00 );

        LinearLayout mainLayout = findViewById(R.id.main);
        mainLayout.setOnClickListener(v -> hideKeyboard(v));
        chatContainer.setOnClickListener(v -> hideKeyboard(v));
        chatScroller.setOnClickListener(v -> hideKeyboard(v));


        String savedAuthor = loadAuthorFromFile();
        if (savedAuthor != null) {
            etAuthor.setText(savedAuthor);
        }

        loadChat();
        handler.post(this::periodic);
        chatScroller.addOnLayoutChangeListener( ( View v,
                                                  int left,    int top,    int right,    int bottom,
                                                  int leftWas, int topWas, int rightWas, int bottomWas) -> chatScroller.post(
                ()-> chatScroller.fullScroll( View.FOCUS_DOWN )
        ));

        for( Map.Entry<String, String> e : emoji.entrySet() ) {
            TextView tv = new TextView( this ) ;
            tv.setText( e.getValue() );
            tv.setTextSize( 20 );
            tv.setOnClickListener(v -> {
                etMessage.setText( etMessage.getText() + e.getValue() );
                etMessage.setSelection( etMessage.getText().length() );
            });
            emojiContainer.addView( tv );
        }

        urlToImgView(
                "https://www.assuropoil.fr/wp-content/uploads/2023/07/avoir-un-chat-sante.jpg",
                findViewById( R.id.chat_img )
        );

    }

    private void showNotification() {
        // Реєструємо канал у системі
        NotificationChannel channel = new NotificationChannel(
                "ChatChannel", "ChatChannel", NotificationManager.IMPORTANCE_DEFAULT );
        NotificationManager notificationManager = getSystemService( NotificationManager.class );
        notificationManager.createNotificationChannel( channel );
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ActivityCompat.checkSelfPermission( this,
                        android.Manifest.permission.POST_NOTIFICATIONS ) !=
                        PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { android.Manifest.permission.POST_NOTIFICATIONS },
                    1002 ) ;
            return;
        }
        // Надсилання повідомлення
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder( this, "ChatChannel" )
                        .setSmallIcon( android.R.drawable.star_big_on )
                        .setContentTitle( "Чат" )
                        .setContentText( "Нове повідомлення")
                        .setPriority( NotificationManager.IMPORTANCE_DEFAULT );
        Notification notification = builder.build();
        notificationManager.notify( 1001, notification );
    }
    private void urlToImgView( String url, ImageView imageView ) {
        CompletableFuture
                .supplyAsync( () -> {
                    try( InputStream inputStream = new URL(url).openStream() ) {
                        return BitmapFactory.decodeStream( inputStream );
                    }
                    catch( IOException ex ) {
                        Log.e( "urlToImgView", ex.getMessage() == null ? ex.getClass().toString() : ex.getMessage() );
                        return null;
                    }
                }, threadPool )
                .thenAccept( bmp -> runOnUiThread( () -> imageView.setImageBitmap(bmp) ) );
    }

    private void periodic(){
        loadChat();
        handler.postDelayed(this::periodic, 1000);
    }

    private String encodeEmoji( String input ) {
        for( Map.Entry<String, String> e : emoji.entrySet() ) {
            input = input.replace( e.getValue(), e.getKey() ) ;
        }
        return input;
    }
    private String decodeEmoji( String input ) {
        for( Map.Entry<String, String> e : emoji.entrySet() ) {
            input = input.replace( e.getKey(), e.getValue() ) ;
        }
        return input;
    }

    private void sendButtonClick(View view){
        String author = etAuthor.getText().toString();
        if(author.isEmpty()){
            Toast.makeText(this,"Fill field: Author", Toast.LENGTH_SHORT).show();
            return;
        }
        String message = etMessage.getText().toString();
        if(message.isEmpty()){
            Toast.makeText(this,"Fill field: Message", Toast.LENGTH_SHORT).show();
            return;
        }
        etAuthor.setEnabled(false);
        saveAuthorToFile(author);
        CompletableFuture.runAsync(()->
                sendChatMessage(new ChatMessage()
                        .setAuthor(author)
                        .setText(message)
                        .setMoment(sqlDateFormat.format(new Date())))
                ,threadPool);
    }

    private void saveAuthorToFile(String author) {
        try (FileOutputStream fos = openFileOutput(authorFileName, MODE_PRIVATE)) {
            fos.write(author.getBytes());
        } catch (IOException e) {
            Log.e("saveAuthorToFile", e.getMessage());
        }
    }
    private String loadAuthorFromFile() {
        StringBuilder author = new StringBuilder();

        try (FileInputStream fis = openFileInput(authorFileName);
             InputStreamReader isr = new InputStreamReader(fis);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                author.append(line);
            }
            return author.toString();
        } catch (IOException e) {
            Log.e("saveAuthorToFile", e.getMessage());
            return null;
        }
    }

    private void sendChatMessage(ChatMessage chatMessage){
        try {
            URL url = new URL(chatUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept","application/json");
            connection.setRequestProperty("Connection","close");

            OutputStream bodyStream = connection.getOutputStream();
            bodyStream.write(
                    String.format("author=%s&msg=%s",
                            URLEncoder.encode(chatMessage.getAuthor(), StandardCharsets.UTF_8.name()),
                            URLEncoder.encode(
                                    encodeEmoji( chatMessage.getText() ),
                                    StandardCharsets.UTF_8.name() )
                    ).getBytes(StandardCharsets.UTF_8)
            );
            bodyStream.flush();
            bodyStream.close();

            int statusCode = connection.getResponseCode();
            if (statusCode >= 200 && statusCode < 300){
                Log.i("sendChatMessage", "Message Sent");
                loadChat();
            }else {
                InputStream responseStream = connection.getErrorStream();
                Log.e("sendChatMessage",readString(responseStream));
                responseStream.close();
            }
            connection.disconnect();
        }
        catch (Exception e){
            Log.e("sendChatMessage",
                    e.getMessage() == null ? e.getClass().toString()  : e.getMessage());
        }
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    private void loadChat() {
        CompletableFuture
                .supplyAsync(this::getChatAsString, threadPool)
                .thenApply(this::processChatResponse)
                .thenAccept(m -> runOnUiThread(()-> displayChatMessages(m)) );
    }

    private String getChatAsString() {
        try (InputStream urlStream = new URL(chatUrl).openStream()) {
            return readString(urlStream);
        } catch (MalformedURLException ex) {
            Log.e("ChatActivity::loadChat",
                    ex.getMessage() == null ? "MalformedURLException" : ex.getMessage());
        } catch (IOException ex) {
            Log.e("ChatActivity::loadChat",
                    ex.getMessage() == null ? "IOException" : ex.getMessage());
        }
        return null;
    }

    private ChatMessage[] processChatResponse(String jsonString) {
        ChatResponse chatResponse = gson.fromJson(jsonString, ChatResponse.class);
        return chatResponse.data;
    }

    private void displayChatMessages(ChatMessage[] chatMessages) {

        boolean wasNew = false;
        for( ChatMessage cm : chatMessages ) {
            if( messages.stream().noneMatch( m -> m.getId().equals( cm.getId() ) ) ) {
                cm.setText( decodeEmoji( cm.getText() ) );
                messages.add( cm );
                wasNew = true;
            }
        }
        if( ! wasNew ) return ;

        messages.sort(Comparator.comparing(ChatMessage::getMoment));

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(10, 15, 10, 5);

        Drawable bgOther = getResources().getDrawable(R.drawable.chat_msg_other, getTheme());
        Drawable bgMy = getResources().getDrawable(R.drawable.chat_msg_my, getTheme());

        for (ChatMessage cm : messages) {
            if(cm.getView() != null) continue;

            LinearLayout linearLayout = new LinearLayout(ChatActivity.this);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            TextView tvAuthor = new TextView(ChatActivity.this);
            tvAuthor.setText(String.format("%s %s", cm.getAuthor(), cm.getMoment()));
            tvAuthor.setPadding(30, 5, 30, 5);
            linearLayout.addView(tvAuthor);

            TextView tvMessage = new TextView(ChatActivity.this);
            tvMessage.setText(cm.getText());
            tvMessage.setPadding(20, 5, 30, 5);
            linearLayout.addView(tvMessage);

            LinearLayout.LayoutParams messageLayoutParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            messageLayoutParams.setMargins(10, 15, 10, 5);

            if (Objects.equals(cm.author, etAuthor.getText().toString())) {
                messageLayoutParams.gravity = Gravity.END;
                linearLayout.setBackground(bgMy);
            } else {
                messageLayoutParams.gravity = Gravity.START;
                linearLayout.setBackground(bgOther);
            }

            linearLayout.setLayoutParams(messageLayoutParams);
            cm.setView(linearLayout);
            chatContainer.addView(linearLayout);
        }
        chatContainer.post( () -> {
            chatScroller.fullScroll( View.FOCUS_DOWN ) ;
            vBell.startAnimation( bellAnimation ) ;
            incomingMessage.start();
            showNotification();
        } ) ;
    }


    private String readString(InputStream stream) throws IOException {
        ByteArrayOutputStream byteBuilder = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int len;
        while ((len = stream.read(buffer)) != -1) {
            byteBuilder.write(buffer, 0, len);
        }
        String res = byteBuilder.toString(StandardCharsets.UTF_8.name());
        byteBuilder.close();
        return res;
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        threadPool.shutdownNow();
        super.onDestroy();
    }

    class ChatResponse {
        private int status;
        private ChatMessage[] data;

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public ChatMessage[] getData() {
            return data;
        }

        public void setData(ChatMessage[] data) {
            this.data = data;
        }
    }

    class ChatMessage {
        private String id;
        private String author;
        private String text;
        private String moment;
        private View view;

        public View getView() {
            return view;
        }

        public void setView(View view) {
            this.view = view;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAuthor() {
            return author;
        }

        public ChatMessage setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getText() {
            return text;
        }

        public ChatMessage setText(String text) {
            this.text = text;
            return this;
        }

        public String getMoment() {
            return moment;
        }

        public ChatMessage setMoment(String moment) {
            this.moment = moment;
            return this;
        }
    }
}