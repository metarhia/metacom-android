package com.metarhia.metacom.activities.chat;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.metacom.R;

/**
 * @author MariaKokshaikina
 */
public class ChatActivity extends AppCompatActivity {

    public static final String EXTRA_CONNECTION_ID = "extraConnectionId";
    public static final String EXTRA_CHAT_ROOM_NAME = "extraChatRoomName";

    private ChatFragment mChatFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        int connectionID = getIntent().getIntExtra(EXTRA_CONNECTION_ID, -1);
        String chatRoomName = getIntent().getStringExtra(EXTRA_CHAT_ROOM_NAME);

        mChatFragment = ChatFragment.newInstance(connectionID, chatRoomName);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.chat_container, mChatFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.exit)
                .setMessage(R.string.confirm_exit)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mChatFragment.leaveRoom();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
