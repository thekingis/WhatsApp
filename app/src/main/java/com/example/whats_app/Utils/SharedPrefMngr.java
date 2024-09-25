package com.example.whats_app.Utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefMngr {

    Context context;
    SharedPreferences shrdPref;
    public final String shrdPrefName = "shrdPrefName";
    public final String loggedIn = "loggedIn";
    public final String userID = "userID";
    public final String userName = "userName";
    public final String imageURL = "imageURL";

    public SharedPrefMngr(Context context){
        this.context = context;
        shrdPref = context.getSharedPreferences(shrdPrefName, Context.MODE_PRIVATE);
    }

    public boolean isLoggedIn(){
        return shrdPref.getBoolean(loggedIn, false);
    }

    public void saveLogin(int userId, String name, String imageUrl){
        SharedPreferences.Editor editor = shrdPref.edit();
        editor.putInt(userID, userId);
        editor.putString(userName, name);
        editor.putString(imageURL, imageUrl);
        editor.putBoolean(loggedIn, true);
        editor.apply();
    }

    public int getUserId(){
        return shrdPref.getInt(userID, 0);
    }

    public String getUserName(){
        return shrdPref.getString(userName, null);
    }

    public String getImageURL(){
        return shrdPref.getString(imageURL, "");
    }

    public void logout(){
        SharedPreferences.Editor editor = shrdPref.edit();
        editor.remove(userID);
        editor.remove(userName);
        editor.remove(loggedIn);
        editor.apply();
    }

}
