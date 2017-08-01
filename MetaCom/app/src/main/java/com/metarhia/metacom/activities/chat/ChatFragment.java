package com.metarhia.metacom.activities.chat;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.BackPressedHandler;
import com.metarhia.metacom.interfaces.ChatReconnectionListener;
import com.metarhia.metacom.interfaces.FileDownloadedListener;
import com.metarhia.metacom.interfaces.FileUploadedCallback;
import com.metarhia.metacom.interfaces.LeaveRoomCallback;
import com.metarhia.metacom.interfaces.MessageListener;
import com.metarhia.metacom.interfaces.MessageSentCallback;
import com.metarhia.metacom.models.ChatRoom;
import com.metarhia.metacom.models.ChatRoomsManager;
import com.metarhia.metacom.models.Message;
import com.metarhia.metacom.models.UserConnectionsManager;
import com.metarhia.metacom.utils.Constants;
import com.metarhia.metacom.utils.PermissionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.metarhia.metacom.models.MessageType.FILE;
import static com.metarhia.metacom.models.MessageType.TEXT;
import static com.metarhia.metacom.utils.TextUtils.copyToClipboard;

/**
 * @author MariaKokshaikina
 */
public class ChatFragment extends Fragment implements MessageListener, MessageSentCallback,
        FileUploadedCallback, LeaveRoomCallback, FileDownloadedListener, BackPressedHandler, ChatReconnectionListener {

    public static final String CHAT_FRAGMENT_TAG = "ChatFragmentTag";
    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_CHAT_ROOM_NAME = "keyChatRoomName";
    private static final String TMP_METACOM_JPG = "/tmp-metacom.jpg";
    private static final String AUTHORITY_STRING = "com.metarhia.metacom.fileprovider";
    private static final int PICK_IMAGE_FROM_EXPLORER = 0;
    private static final int PICK_IMAGE_FROM_CAMERA = 1;
    private static final int TAKE_PHOTO = 2;
    private static final int FILE_EXPLORER = 3;

    @BindView(R.id.toolbar_title)
    TextView mToolbarTitle;
    @BindView(R.id.attach)
    ImageView mFileAttach;
    @BindView(R.id.messages_list)
    RecyclerView mMessagesView;
    @BindView(R.id.input_message)
    TextInputEditText mInputMessage;
    private Unbinder mUnbinder;

    private ArrayList<Message> mMessages;
    private MessagesAdapter mMessagesAdapter;
    private ChatRoom mChatRoom;
    private ChatRoomsManager mChatRoomsManager;
    private boolean isUIVisible = true;

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
            mChatRoom.setChatReconnectionListener(this);

            mChatRoom.setFileDownloadedListener(this);
            mChatRoomsManager = UserConnectionsManager.get().getConnection(connectionID)
                    .getChatRoomsManager();

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
        if (isUIVisible) {
            updateMessagesView();
        }
    }

    private void updateMessagesView() {
        mMessagesAdapter.notifyDataSetChanged();
        mMessagesView.smoothScrollToPosition(mMessages.size());
    }

    @Override
    public void onMessageSent(final Message message) {
        stopSpinner(message);
    }

    private void stopSpinner(Message message) {
        mMessages.get(mMessages.indexOf(message)).setWaiting(false);
        if (isUIVisible) {
            updateMessagesView();
        }
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
        handleBackPress();
    }

    public void leaveRoom() {
        mChatRoom.removeMessageListener(this);
        mChatRoomsManager.leaveChatRoom(mChatRoom, this);
    }

    @OnClick(R.id.attach)
    public void onFileAttachClick() {
        if (PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
            showFileChooser();
        } else {
            showForbidDialog();
        }
    }

    private void showForbidDialog() {
        Toast.makeText(getContext(), getString(R.string.permissions_are_not_granted), Toast
                .LENGTH_SHORT).show();
    }

    @OnClick(R.id.send)
    public void onSendMessageClick() {
        String messageText = mInputMessage.getText().toString();
        if (!messageText.isEmpty()) {
            Message message = new Message(TEXT, messageText, false);
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
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
//                Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
// TMP_METACOM_JPG));
                File f = new File(Environment.getExternalStorageDirectory() + TMP_METACOM_JPG);
                Uri uri = FileProvider.getUriForFile(getContext(), AUTHORITY_STRING, f);
                takePictureIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, PICK_IMAGE_FROM_CAMERA);
                }
                return true;
            case FILE_EXPLORER:
                Intent intent = new Intent();
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, getString(R.string
                        .select_file)), PICK_IMAGE_FROM_EXPLORER);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == PICK_IMAGE_FROM_EXPLORER || requestCode == PICK_IMAGE_FROM_CAMERA) &&
                resultCode == Activity.RESULT_OK) {
            Uri fileUri = null;
            switch (requestCode) {
                case PICK_IMAGE_FROM_EXPLORER: {
                    fileUri = data.getData();
                    break;
                }
                case PICK_IMAGE_FROM_CAMERA: {
//                    fileUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory() +
// TMP_METACOM_JPG));
                    File f = new File(Environment.getExternalStorageDirectory() + TMP_METACOM_JPG);
                    fileUri = FileProvider.getUriForFile(getContext(), AUTHORITY_STRING, f);
                    break;
                }
            }
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
        final Message message = new Message(TEXT,
                getResources().getString(R.string.uploaded_file), false);
        onMessageReceived(message);
    }

    @Override
    public void onFileUploadError(final String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLeavedRoom() {
        getActivity().finish();
    }

    @Override
    public void onLeaveError(final String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFileDownloaded(String path) {
        Message message = new Message(FILE, Constants.composeFilePathInfo(path), true);
        displayNewMessage(message);
    }

    @Override
    public void onFileDownloadError() {
        Toast.makeText(getContext(), R.string.downloading_error, Toast.LENGTH_SHORT).show();
    }

    private void openFile(String filePath) {
        Uri selectedUri = Uri.parse("file:///" + filePath);
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(selectedUri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(selectedUri, mimeType);
        startActivity(Intent.createChooser(intent, getString(R.string.open_file)));
    }

    @Override
    public void onPause() {
        super.onPause();
        isUIVisible = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isUIVisible = true;
        updateMessagesView();
    }

    @Override
    public void handleBackPress() {
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
                        leaveRoom();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onConnectionLost() {
        // TODO implement
    }

    @Override
    public void onRejoinSuccess(boolean hasInterlocutor) {
        // TODO implement
    }

    @Override
    public void onRejoinError(String errorMessage) {
        // TODO implement
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

        private List<Message> messages;

        MessagesAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            return messages.get(position).isIncoming() ? 1 : 0;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int resource = (viewType == 0) ? R.layout.message_out : R.layout.message_in;
            View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            return new MessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            final Message message = messages.get(position);
            final String messageContent = message.getContent();
            holder.messageText.setText(messageContent);
            holder.messageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    copyToClipboard(getActivity(), messageContent);
                    Toast.makeText(getContext(), getString(R.string.copied_message), Toast
                            .LENGTH_SHORT).show();
                    return true;
                }
            });
            holder.messageSpinner.setVisibility(message.isWaiting() ? View.VISIBLE : View.GONE);
            if (message.getType() == FILE && message.isIncoming()) {
                holder.messageText.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = messageContent.substring(messageContent.indexOf('/'));
                        ChatFragment.this.openFile(path);
                    }
                });
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
            }
        }
    }
}
