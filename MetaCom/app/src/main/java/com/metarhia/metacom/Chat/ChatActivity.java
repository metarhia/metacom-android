package com.metarhia.metacom.Chat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.metarhia.metacom.MainFragment;
import com.metarhia.metacom.R;

import static com.metarhia.metacom.MainFragment.MainFragmentTag;

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
