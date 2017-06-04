package com.metarhia.metacom.activities.chat;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.models.ChatRoom;
import com.metarhia.metacom.models.Message;
import com.metarhia.metacom.models.MessageType;
import com.metarhia.metacom.models.UserConnectionsManager;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment implements MessageListener, MessageSentCallback {

    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_CHAT_ROOM_NAME = "keyChatRoomName";

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;

    @BindView(R.id.toolbar_back)
    ImageView mToolbarBack;

    @BindView(R.id.attach)
    ImageView mFileAttach;

    @BindView(R.id.send)
    ImageView mSendMessage;

    @BindView(R.id.messages_list)
    ListView mMessagesListView;

    @BindView(R.id.input_message)
    TextInputEditText mInputMessage;

    private Unbinder mUnbinder;
    private ArrayList<Message> mMessages;
    private MessageAdapter mMessageAdapter;
    private ChatRoom mChatRoom;

    public static ChatFragment newInstance(int connectionID, String chatRoomName) {
        Bundle args = new Bundle();
        args.putInt(KEY_CONNECTION_ID, connectionID);
        args.putString(KEY_CHAT_ROOM_NAME, chatRoomName);

        ChatFragment fragment = new ChatFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        mUnbinder = ButterKnife.bind(this, v);

        if (getArguments() != null) {

            int connectionID = getArguments().getInt(KEY_CONNECTION_ID);
            String chatRoomName = getArguments().getString(KEY_CHAT_ROOM_NAME);

            mChatRoom = UserConnectionsManager.get().getConnection(connectionID)
                    .getChatRoomsManager().getChatRoom(chatRoomName);
            mToolbarTitle.setText(chatRoomName);

            mMessages = new ArrayList<>();
            mMessageAdapter = new MessageAdapter(getActivity(), mMessages);
            mMessagesListView.setAdapter(mMessageAdapter);

            getMessageQueueExamples();

        }
        return v;
    }

    private void getMessageQueueExamples() {
        for (int i = 0; i < 10; i++) {
            String mes = "";
            for (int j = 0; j < 10; j++) {
                mes = mes.concat(i + "");
            }
            final Message message = new Message(MessageType.TEXT, mes, (i % 2 == 0));
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onMessageReceived(message);
                }
            }, 500 * i);
        }
    }

    @Override
    public void onMessageReceived(Message message) {
        mMessages.add(message);
        mMessageAdapter.notifyDataSetChanged();
        mMessagesListView.setSelection(mMessageAdapter.getCount() - 1);
    }

    @Override
    public void onMessageSent() {
        // todo onMessageSent
    }

    @Override
    public void onMessageSentError(String message) {
        // todo onMessageSentError
    }

    @OnClick(R.id.toolbar_back)
    public void onToolbarBackClick() {
        getActivity().finish();
    }

    @OnClick(R.id.attach)
    public void onFileAttachClick() {
        // todo choose file to send
        Toast.makeText(getContext(), "file was sent", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.send)
    public void onSendMessageClick() {
        String messageText = mInputMessage.getText().toString();
        if (!messageText.isEmpty()) {
            Message message = new Message(MessageType.TEXT, messageText, false);
            // todo send message and display it
            onMessageReceived(message);
            mInputMessage.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    public class MessageAdapter extends BaseAdapter {
        Context ctx;
        LayoutInflater lInflater;
        ArrayList<Message> messages;

        public MessageAdapter(Context context, ArrayList<Message> messages) {
            ctx = context;
            this.messages = messages;
            lInflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Object getItem(int i) {
            return messages.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = lInflater.inflate(R.layout.message, parent, false);
            }

            Message m = messages.get(position);

            View messageRightLayout = ButterKnife.findById(view, R.id.message_right_layout);
            View messageLeftLayout = ButterKnife.findById(view, R.id.message_left_layout);
            TextView messageLeftText = ButterKnife.findById(view, R.id.message_left_text);
            TextView messageRightText = ButterKnife.findById(view, R.id.message_right_text);
            if (m.isIncoming()) {
                messageRightLayout.setVisibility(View.GONE);
                messageLeftLayout.setVisibility(View.VISIBLE);
                messageLeftText.setText(m.getContent());
            } else {
                messageRightLayout.setVisibility(View.VISIBLE);
                messageLeftLayout.setVisibility(View.GONE);
                messageRightText.setText(m.getContent());
            }

            return view;
        }
    }
}
