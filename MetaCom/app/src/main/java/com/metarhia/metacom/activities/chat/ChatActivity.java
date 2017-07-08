package com.metarhia.metacom.activities.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;

/**
 * @author MariaKokshaikina
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONNECTION_ID = "extraConnectionId";
    public static final String EXTRA_CHAT_ROOM_NAME = "extraChatRoomName";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int connectionID = getIntent().getIntExtra(EXTRA_CONNECTION_ID, -1);
        String chatRoomName = getIntent().getStringExtra(EXTRA_CHAT_ROOM_NAME);

        getSupportFragmentManager().beginTransaction()
                .add(R.id.chat_container, ChatFragment.newInstance(connectionID, chatRoomName))
                .commit();
    }
}
