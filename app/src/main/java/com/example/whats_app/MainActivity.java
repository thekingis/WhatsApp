package com.example.whats_app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.whats_app.Utils.Constants;
import com.example.whats_app.Utils.SharedPrefMngr;
import com.github.kittinunf.fuel.Fuel;
import com.github.kittinunf.fuel.core.FuelError;
import com.github.kittinunf.fuel.core.Handler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import kotlin.Pair;

public class MainActivity extends AppCompatActivity {

    Context context;
    EditText editText;
    Button button;
    boolean requestingData;
    SharedPrefMngr sharedPrefMngr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        sharedPrefMngr = new SharedPrefMngr(context);

        if(sharedPrefMngr.isLoggedIn()){
            Constants.myId = sharedPrefMngr.getUserId();
            Intent intent = new Intent(context, HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        requestingData = false;

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        button.setOnClickListener(v ->{
            String name = editText.getText().toString();
            if(!name.isEmpty() && !requestingData){
                requestingData = true;
                List<Pair<String, String>> parameters = new ArrayList<>();
                parameters.add(new Pair<>("name", name));
                Fuel.INSTANCE.post(Constants.addUserUrl, parameters).responseString(new Handler<String>() {
                    @Override
                    public void success(String s) {
                        try {
                            JSONObject object = new JSONObject(s);
                            int userId = object.getInt("id");
                            String userName = object.getString("name");
                            String imageUrl = object.getString("imageUrl");
                            sharedPrefMngr.saveLogin(userId, userName, imageUrl);
                            Intent intent = new Intent(context, HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void failure(@NonNull FuelError fuelError) {

                    }
                });
            }
        });

    }

}