package com.example.whats_app;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.whats_app.Adapters.ViewPagerAdapter;
import com.example.whats_app.Interfaces.ChatListener;
import com.example.whats_app.Utils.Constants;
import com.example.whats_app.Utils.SharedPrefMngr;
import com.example.whats_app.Utils.SocketServer;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import kotlin.Pair;

public class HomeActivity extends AppCompatActivity implements ChatListener {

    Context context;
    ViewPager viewPager;
    TabLayout tabLayout;
    ChatListener chatListener;
    LinearLayout editBtn, messageBtn, camBtn, callBtn, contactsView;
    ChatFragment chatFragment;
    StatusFragment statusFragment;
    CallsFragment callsFragment;
    Socket socket;
    SharedPrefMngr sharedPrefMngr;
    int userId;
    String userName, imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        context = this;
        chatListener = this;
        sharedPrefMngr = new SharedPrefMngr(this);
        userId = sharedPrefMngr.getUserId();
        userName = sharedPrefMngr.getUserName();
        imageUrl = sharedPrefMngr.getImageURL();
        Constants.myId = userId;
        Constants.name = userName;
        Constants.imageUrl = imageUrl;

        socket = SocketServer.INSTANCE.socket;

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        editBtn = findViewById(R.id.editBtn);
        messageBtn = findViewById(R.id.messageBtn);
        camBtn = findViewById(R.id.camBtn);
        callBtn = findViewById(R.id.callBtn);
        contactsView = findViewById(R.id.contactsView);

        setupViewPager();

        socket.on("receiveMessage", args -> {
            try {
                JSONObject msgObject = new JSONObject(args[0].toString());
                Log.e("DATA", msgObject.toString());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(3);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        chatFragment = new ChatFragment(context, chatListener);
        statusFragment = new StatusFragment(context);
        callsFragment = new CallsFragment(context);
        viewPagerAdapter.addFragment(chatFragment, "Chats");
        viewPagerAdapter.addFragment(statusFragment, "Status");
        viewPagerAdapter.addFragment(callsFragment, "Calls");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int tabIndex = tab.getPosition();
                if(tabIndex == 0){
                    messageBtn.setVisibility(View.VISIBLE);
                    editBtn.setVisibility(View.GONE);
                    camBtn.setVisibility(View.GONE);
                    callBtn.setVisibility(View.GONE);
                }
                if(tabIndex == 1){
                    messageBtn.setVisibility(View.GONE);
                    editBtn.setVisibility(View.VISIBLE);
                    camBtn.setVisibility(View.VISIBLE);
                    callBtn.setVisibility(View.GONE);
                }
                if(tabIndex == 2){
                    messageBtn.setVisibility(View.GONE);
                    editBtn.setVisibility(View.GONE);
                    camBtn.setVisibility(View.GONE);
                    callBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    public void fetchData() {
        List<Pair<String, String>> parameters = new ArrayList<>();
        parameters.add(new Pair<>("userId", String.valueOf(userId)));
        Fuel.INSTANCE.post(Constants.allUsersUrl, parameters).responseString(new Handler<String>() {
            @Override
            public void success(String s) {
                runOnUiThread(() -> {
                    try {
                        JSONArray array = new JSONArray(s);
                        if(contactsView.getChildCount() > 0)
                            contactsView.removeAllViews();
                        for(int i = 0; i < array.length(); i++){
                            JSONObject object = array.getJSONObject(i);
                            String id = object.getString("id");
                            String name = object.getString("name");
                            String imageUrl = object.getString("imageUrl");

                            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.chat_box, null, false);
                            TextView nameTextView = linearLayout.findViewById(R.id.nameTextView);
                            TextView msgTextView = linearLayout.findViewById(R.id.msgTextView);
                            LinearLayout rightBar = linearLayout.findViewById(R.id.rightBar);

                            rightBar.setVisibility(View.GONE);
                            msgTextView.setVisibility(View.GONE);
                            nameTextView.setText(name);
                            linearLayout.setOnClickListener(v -> {
                                Bundle bundle = new Bundle();
                                bundle.putInt("chatUserId", Integer.parseInt(id));
                                bundle.putString("name", name);
                                bundle.putString("imageUrl", imageUrl);
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtras(bundle);
                                intent.putExtra("chatListener", chatListener);
                                startActivity(intent);
                                contactsView.setVisibility(View.INVISIBLE);
                            });

                            contactsView.addView(linearLayout);
                        }
                        contactsView.setVisibility(View.VISIBLE);
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

    @Override
    public void onBackPressed() {
        if(contactsView.getVisibility() == View.VISIBLE){
            contactsView.setVisibility(View.GONE);
            return;
        }
        if(viewPager.getCurrentItem() > 0){
            viewPager.setCurrentItem(0, true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onSendChat(JSONObject object) {
        chatFragment.updateChat(object);
    }
}