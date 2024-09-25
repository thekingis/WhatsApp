package com.example.whats_app.Utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import io.socket.client.IO;
import io.socket.client.Socket;

public class SocketServer {

    public Socket socket = null;
    public static final SocketServer INSTANCE = new SocketServer();
    public static Handler UIHandler;
    static
    {
        UIHandler = new Handler(Looper.getMainLooper());
    }
    public static void runOnUI(Runnable runnable) {
        UIHandler.post(runnable);
    }

    SocketServer(){
        if(socket == null){
            try {
                socket = IO.socket(Constants.socketUrl);
                JSONObject object = new JSONObject();
                object.put("name", Constants.name);
                object.put("id", Constants.myId);
                socket.on(Socket.EVENT_CONNECT, args -> runOnUI(() -> socket.emit("connected", object)));
                socket.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
