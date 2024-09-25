package com.example.whats_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.whats_app.Interfaces.ChatListener;
import com.example.whats_app.Utils.Constants;
import com.example.whats_app.Utils.SharedPrefMngr;
import com.example.whats_app.Utils.SocketServer;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.socket.client.Socket;
import kotlin.Pair;

public class ChatActivity extends AppCompatActivity {

    Context context;
    ChatListener chatListener;
    LinearLayout backBtn, sendBtn, chatBody;
    TextView nameTextView;
    String name, imageUrl;
    int myId, chatUserId;
    ImageView sendIcon;
    EditText editText;
    Socket socket;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        context = this;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        chatListener = (ChatListener) intent.getSerializableExtra("chatListener");
        chatUserId = bundle.getInt("chatUserId");
        name = bundle.getString("name");
        imageUrl = bundle.getString("imageUrl");

        socket = SocketServer.INSTANCE.socket;
        sharedPrefMngr = new SharedPrefMngr(this);
        myId = sharedPrefMngr.getUserId();

        backBtn = findViewById(R.id.backBtn);
        editText = findViewById(R.id.editText);
        nameTextView = findViewById(R.id.nameTextView);
        sendBtn = findViewById(R.id.sendBtn);
        sendIcon = findViewById(R.id.sendIcon);
        chatBody = findViewById(R.id.chatBody);

        nameTextView.setText(name);
        backBtn.setOnClickListener(v -> finish());

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String msgText = charSequence.toString();
                msgText = msgText.replaceAll(" ", "");
                if(msgText.isEmpty())
                    sendIcon.setImageResource(R.drawable.ic_mic);
                else
                    sendIcon.setImageResource(R.drawable.ic_send);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });
        sendBtn.setOnClickListener(v -> {
            String msgText = editText.getText().toString();
            if(!msgText.replaceAll(" ", "").isEmpty()){
                try {
                    @SuppressLint("SimpleDateFormat") String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    JSONObject emitObject = new JSONObject();
                    emitObject.put("myId", myId);
                    emitObject.put("chatUserId", chatUserId);
                    emitObject.put("name", Constants.name);
                    emitObject.put("imageUrl", Constants.imageUrl);
                    emitObject.put("msgText", msgText);
                    emitObject.put("date", date);

                    JSONObject object = new JSONObject();
                    object.put("user", chatUserId);
                    object.put("name", name);
                    object.put("imageUrl", imageUrl);
                    object.put("message", msgText);
                    object.put("date", date);
                    chatListener.onSendChat(object);

                    socket.emit("sendMessage", emitObject);
                    editText.setText("");
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.right_side, null, false);
                    TextView textView = linearLayout.findViewById(R.id.textView);
                    textView.setText(msgText);
                    chatBody.addView(linearLayout);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        socket.on("receiveMessage", args -> {
            try {
                JSONObject msgObject = new JSONObject(args[0].toString());
                int id = msgObject.getInt("id");
                int msgId = msgObject.getInt("msgId");
                int userFrom = msgObject.getInt("userFrom");
                String message = msgObject.getString("message");
                String dateTime = msgObject.getString("dateTime");
                if(userFrom == chatUserId){
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.left_side, null, false);
                    TextView textView = linearLayout.findViewById(R.id.textView);
                    textView.setText(message);
                    runOnUiThread(() -> chatBody.addView(linearLayout));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        fetchChatData();

    }

    private void fetchChatData() {
        List<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>("userId", String.valueOf(chatUserId)));
        parameters.add(new Pair<>("myId", String.valueOf(myId)));
        Fuel.INSTANCE.post(Constants.fetchChatUrl, parameters).responseString(new Handler<String>() {
            @Override
            public void success(String s) {
                try {
                    JSONArray array = new JSONArray(s);
                    for(int i = 0; i < array.length(); i++){
                        JSONObject object = array.getJSONObject(i);
                        String id = object.getString("id");
                        String message = object.getString("message");
                        String date = object.getString("date");
                        boolean fromMe = object.getBoolean("fromMe");
                        int chatLayout = fromMe ? R.layout.right_side : R.layout.left_side;
                        LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(chatLayout, null, false);
                        TextView textView = linearLayout.findViewById(R.id.textView);
                        textView.setText(message);
                        runOnUiThread(() -> chatBody.addView(linearLayout));
                    }
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void failure(@NonNull FuelError fuelError) {
            }
        });
    }

}