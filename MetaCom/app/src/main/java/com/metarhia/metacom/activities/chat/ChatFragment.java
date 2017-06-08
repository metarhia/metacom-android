package com.metarhia.metacom.activities.chat;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.models.ChatRoom;
import com.metarhia.metacom.models.Message;
import com.metarhia.metacom.models.MessageType;
import com.metarhia.metacom.models.UserConnectionsManager;

import java.util.ArrayList;
import java.util.List;

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
    private static final int PICK_IMAGE = 0;
    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.toolbar_back)
    ImageView mToolbarBack;
    @BindView(R.id.attach)
    ImageView mFileAttach;
    @BindView(R.id.send)
    ImageView mSendMessage;
    @BindView(R.id.messages_list)
    RecyclerView mMessagesView;
    @BindView(R.id.input_message)
    TextInputEditText mInputMessage;
    private Unbinder mUnbinder;
    private ArrayList<Message> mMessages;
    private MessagesAdapter mMessagesAdapter;
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

            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            mMessagesView.setLayoutManager(llm);

            mMessagesAdapter = new MessagesAdapter(mMessages);
            mMessagesView.setAdapter(mMessagesAdapter);

        }
        return v;
    }

    @Override
    public void onMessageReceived(Message message) {
        mMessages.add(message);
        mMessagesAdapter.notifyDataSetChanged();
        mMessagesView.smoothScrollToPosition(mMessages.size());
    }

    @Override
    public void onMessageSent(Message message) {
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
        showFileChooser();
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

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();

            // TODO use stream from fileUri to upload file
            String fileName = fileUri.toString();
            fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.length());
            final Message message = new Message(MessageType.FILE, String.format(getString(R.string.uploaded_file), fileName), false);
            onMessageReceived(message);

            // todo remove
            final Message message1 = new Message(MessageType.FILE, String.format(getString(R.string.uploaded_file), fileName), true);
            onMessageReceived(message1);
        }
    }

    public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

        private List<Message> messages;

        public MessagesAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            int messageType = 0; // message_out
            if (messages.get(position).isIncoming()) {
                messageType = 1; // message_in
            }
            return messageType;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int resource = -1;
            switch (viewType) {
                case 0: {
                    resource = R.layout.message_out;
                    break;
                }
                case 1: {
                    resource = R.layout.message_in;
                    break;
                }
            }
            View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            MessageViewHolder mvh = new MessageViewHolder(v);
            return mvh;
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            holder.messageText.setText(messages.get(position).getContent());
            // spinner
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        public class MessageViewHolder extends RecyclerView.ViewHolder {

            private TextView messageText;
            private ProgressBar messageSpinner;

            public MessageViewHolder(View itemView) {
                super(itemView);
                messageText = ButterKnife.findById(itemView, R.id.message_text);
                messageSpinner = ButterKnife.findById(itemView, R.id.spinner);
            }
        }
    }
}
