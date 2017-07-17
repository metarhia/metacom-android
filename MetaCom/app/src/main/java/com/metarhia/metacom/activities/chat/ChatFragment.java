package com.metarhia.metacom.activities.chat;


import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.models.ChatRoom;
import com.metarhia.metacom.models.Message;
import com.metarhia.metacom.models.MessageType;
import com.metarhia.metacom.models.UserConnectionsManager;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author MariaKokshaikina
 */
public class ChatFragment extends Fragment implements MessageListener, MessageSentCallback,
        FileUploadedCallback {

    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_CHAT_ROOM_NAME = "keyChatRoomName";
    private static final int PICK_IMAGE = 0;
    private static final int TAKE_PHOTO = 1;
    private static final int FILE_EXPLORER = 2;
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

        registerForContextMenu(mFileAttach);

        if (getArguments() != null) {

            int connectionID = getArguments().getInt(KEY_CONNECTION_ID);
            String chatRoomName = getArguments().getString(KEY_CHAT_ROOM_NAME);

            mChatRoom = UserConnectionsManager.get().getConnection(connectionID)
                    .getChatRoomsManager().getChatRoom(chatRoomName);
            mChatRoom.addMessageListener(this);

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
    public void onMessageReceived(final Message message) {
        displayNewMessage(message);
    }

    private void displayNewMessage(Message message) {
        mMessages.add(message);
        mMessagesAdapter.notifyDataSetChanged();
        mMessagesView.smoothScrollToPosition(mMessages.size());
    }

    @Override
    public void onMessageSent(final Message message) {
        stopSpinner(message);
    }

    private void stopSpinner(Message message) {
        mMessages.get(mMessages.indexOf(message)).setWaiting(false);
        mMessagesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onMessageSentError(final String message) {
        displayError(message);
    }

    private void displayError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.toolbar_back)
    public void onToolbarBackClick() {
        // todo close connection to room
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                        getActivity().finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
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
            message.setWaiting(true);

            mChatRoom.sendMessage(message, this);
            displayNewMessage(message);

            mInputMessage.setText("");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private void showFileChooser() {

        getActivity().openContextMenu(mFileAttach);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == mFileAttach.getId()) {
            menu.add(0, TAKE_PHOTO, 0, R.string.take_photo);
            menu.add(0, FILE_EXPLORER, 0, R.string.file_explorer);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case TAKE_PHOTO:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PICK_IMAGE);
                }
                return true;
            case FILE_EXPLORER:
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select file"), PICK_IMAGE);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri fileUri = data.getData();

            try {
                InputStream is = getActivity().getContentResolver().openInputStream(fileUri);
                String mimeType = getActivity().getContentResolver().getType(fileUri);
                mChatRoom.uploadFile(is, mimeType, this);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onFileUploaded(String fileCode) {
        // TODO process successful file uploading

//        String fileName = fileUri.toString();
//        fileName = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.length());
//
//        final Message message = new Message(MessageType.FILE,
//                String.format(getString(R.string.uploaded_file), fileName), false);
//        onMessageReceived(message);
//
//        // todo remove
//        final Message message1 = new Message(MessageType.FILE,
//                String.format(getString(R.string.uploaded_file), fileName), true);
//        onMessageReceived(message1);
    }

    @Override
    public void onFileUploadError(String message) {
        // TODO process error message
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

        private List<Message> messages;

        MessagesAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            int messageType = 0; // message_out
            if (messages.get(position).isIncoming()) {
                messageType = 1; // message_in
            } // or file
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
            return new MessageViewHolder(v); // or FileViewHolder
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            Message message = messages.get(position);
            holder.messageText.setText(message.getContent());
            if (message.isWaiting()) {
                holder.messageSpinner.setVisibility(View.VISIBLE);
            } else {
                holder.messageSpinner.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {

            private TextView messageText;
            private ProgressBar messageSpinner;

            MessageViewHolder(View itemView) {
                super(itemView);
                messageText = ButterKnife.findById(itemView, R.id.message_text);
                messageSpinner = ButterKnife.findById(itemView, R.id.spinner);
                messageText.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("copy", messageText.getText());
                        clipboard.setPrimaryClip(clip);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), "Text was copied", Toast.LENGTH_SHORT).show();
                            }
                        });
                        return true;
                    }
                });
            }
        }
    }
}
