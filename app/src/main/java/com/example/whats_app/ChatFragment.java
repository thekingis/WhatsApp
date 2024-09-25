package com.example.whats_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.ArrayList;
import java.util.List;

import io.socket.client.Socket;
import kotlin.Pair;

public class ChatFragment extends Fragment {

    Context context;
    ChatListener chatListener;
    TextView textView;
    LinearLayout chatView;
    int userId;
    SharedPrefMngr sharedPrefMngr;
    HomeActivity homeActivity;
    Socket socket;

    public static android.os.Handler UIHandler;
    static
    {
        UIHandler = new android.os.Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    public ChatFragment(Context context, ChatListener chatListener) {
        this.context = context;
        this.chatListener = chatListener;
        sharedPrefMngr = new SharedPrefMngr(context);
        userId = sharedPrefMngr.getUserId();
        homeActivity = (HomeActivity) context;
        socket = SocketServer.INSTANCE.socket;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        chatView = view.findViewById(R.id.chatView);
        textView = view.findViewById(R.id.startChatBtn);

        textView.setOnClickListener(v -> homeActivity.fetchData());

        new android.os.Handler().postDelayed(this::fetchChatData, 100);

        socket.on("receiveMessage", args -> {
            try {
                JSONObject msgObject = new JSONObject(args[0].toString());
                int id = msgObject.getInt("id");
                int msgId = msgObject.getInt("msgId");
                int userFrom = msgObject.getInt("userFrom");
                String name = msgObject.getString("name");
                String message = msgObject.getString("message");
                String imageUrl = msgObject.getString("imageUrl");
                String dateTime = msgObject.getString("dateTime");
                runOnUI(() -> {
                    textView.setVisibility(View.GONE);
                    chatView.setVisibility(View.VISIBLE);
                    LinearLayout linearLayout = chatView.findViewWithTag(userFrom);
                    boolean isNull = linearLayout == null;
                    if(!isNull)
                        chatView.removeView(linearLayout);
                    linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_box, null, false);
                    TextView nameTextView = linearLayout.findViewById(R.id.nameTextView);
                    TextView msgTextView = linearLayout.findViewById(R.id.msgTextView);

                    nameTextView.setText(name);
                    msgTextView.setText(message);
                    linearLayout.setTag(userFrom);

                    linearLayout.setOnClickListener(v -> {
                        Bundle bundle = new Bundle();
                        bundle.putInt("chatUserId", userFrom);
                        bundle.putString("name", name);
                        bundle.putString("imageUrl", imageUrl);
                        Intent intent = new Intent(context, ChatActivity.class);
                        intent.putExtra("chatListener", chatListener);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    });
                    chatView.addView(linearLayout, 0);
                });
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        return view;
    }

    private void fetchChatData() {
        List<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>("userId", String.valueOf(userId)));
        Fuel.INSTANCE.post(Constants.allChatsUrl, parameters).responseString(new Handler<String>() {
            @Override
            public void success(String s) {
                runOnUI(() -> {
                    try {
                        JSONObject object = new JSONObject(s);
                        boolean hasData = object.getBoolean("hasData");
                        if(hasData){
                            textView.setVisibility(View.GONE);
                            chatView.setVisibility(View.VISIBLE);
                            JSONArray array = object.getJSONArray("data");
                            for(int i = 0; i < array.length(); i++){
                                JSONObject chatObj = array.getJSONObject(i);
                                JSONObject lastMsgData = chatObj.getJSONObject("lastMsgData");
                                String id = chatObj.getString("id");
                                String user = chatObj.getString("user");
                                String name = chatObj.getString("name");
                                String date = chatObj.getString("date");
                                String imageUrl = chatObj.getString("imageUrl");
                                String message = lastMsgData.getString("message");
                                boolean fromMe = lastMsgData.getBoolean("fromMe");

                                LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_box, null,false);
                                TextView nameTextView = linearLayout.findViewById(R.id.nameTextView);
                                TextView msgTextView = linearLayout.findViewById(R.id.msgTextView);

                                nameTextView.setText(name);
                                msgTextView.setText(message);
                                linearLayout.setTag(user);

                                linearLayout.setOnClickListener(v -> {
                                    Bundle bundle = new Bundle();
                                    bundle.putInt("chatUserId", Integer.parseInt(user));
                                    bundle.putString("name", name);
                                    bundle.putString("imageUrl", imageUrl);
                                    Intent intent = new Intent(context, ChatActivity.class);
                                    intent.putExtra("chatListener", chatListener);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                });
                                chatView.addView(linearLayout);
                            }
                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public void failure(@NonNull FuelError fuelError) {
            }
        });
    }

    public void updateChat(JSONObject msgObject){
        try {
            String user = msgObject.getString("user");
            String name = msgObject.getString("name");
            String message = msgObject.getString("message");
            String imageUrl = msgObject.getString("imageUrl");
            String dateTime = msgObject.getString("date");
            runOnUI(() -> {
                textView.setVisibility(View.GONE);
                chatView.setVisibility(View.VISIBLE);
                LinearLayout linearLayout = chatView.findViewWithTag(user);
                boolean isNull = linearLayout == null;
                if(!isNull)
                    chatView.removeView(linearLayout);
                linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_box, null, false);
                TextView nameTextView = linearLayout.findViewById(R.id.nameTextView);
                TextView msgTextView = linearLayout.findViewById(R.id.msgTextView);

                nameTextView.setText(name);
                msgTextView.setText(message);
                linearLayout.setTag(user);

                linearLayout.setOnClickListener(v -> {
                    Bundle bundle = new Bundle();
                    bundle.putInt("chatUserId", Integer.parseInt(user));
                    bundle.putString("name", name);
                    bundle.putString("imageUrl", imageUrl);
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("chatListener", chatListener);
                    intent.putExtras(bundle);
                    startActivity(intent);
                });
                chatView.addView(linearLayout, 0);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}