package com.example.whats_app.Interfaces;

import org.json.JSONObject;

import java.io.Serializable;

public interface ChatListener extends Serializable {

    void onSendChat(JSONObject object);

}
