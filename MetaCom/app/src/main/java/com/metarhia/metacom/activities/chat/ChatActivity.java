package com.metarhia.metacom.activities.chat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.BackPressedHandler;

import static com.metarhia.metacom.activities.chat.ChatFragment.CHAT_FRAGMENT_TAG;

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

        if (savedInstanceState == null) {
            if (connectionID != -1) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.chat_container, ChatFragment.newInstance(connectionID,
                                chatRoomName), CHAT_FRAGMENT_TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onBackPressed() {
        ((BackPressedHandler) getSupportFragmentManager().findFragmentByTag(CHAT_FRAGMENT_TAG))
                .handleBackPress();
    }
}
