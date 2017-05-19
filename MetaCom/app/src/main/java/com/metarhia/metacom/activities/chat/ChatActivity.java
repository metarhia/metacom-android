package com.metarhia.metacom.activities.chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.metarhia.metacom.R;

public class ChatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.chat_container, new ChatFragment())
                .commit();
    }
}
