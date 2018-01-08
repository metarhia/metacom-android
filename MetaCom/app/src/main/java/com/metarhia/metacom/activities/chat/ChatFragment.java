package com.metarhia.metacom.activities.chat;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.metarhia.metacom.BuildConfig;
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
import com.metarhia.metacom.utils.HistoryCallback;
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
import static com.metarhia.metacom.models.MessageType.INFO;
import static com.metarhia.metacom.models.MessageType.TEXT;
import static com.metarhia.metacom.utils.TextUtils.copyToClipboard;

/**
 * @author MariaKokshaikina
 */
public class ChatFragment extends Fragment implements MessageListener, MessageSentCallback,
        FileUploadedCallback, LeaveRoomCallback, FileDownloadedListener, BackPressedHandler,
        ChatReconnectionListener {

    public static final String CHAT_FRAGMENT_TAG = "ChatFragmentTag";
    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    private static final String KEY_CHAT_ROOM_NAME = "keyChatRoomName";
    private static final String KEY_MESSAGES_LIST = "keyMessagesList";
    private static final String KEY_EXIT_DIALOG = "keyExitDialog";

    private static final String TMP_METACOM_JPG = "/tmp-metacom.jpg";
    private static final String AUTHORITY_STRING = BuildConfig.APPLICATION_ID + ".provider";
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
    private boolean mExitDialog;

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
            mChatRoom.setMessageListener(this);
            mChatRoom.setChatReconnectionListener(this);

            mChatRoom.setFileDownloadedListener(this);
            mChatRoomsManager = UserConnectionsManager.get().getConnection(connectionID)
                    .getChatRoomsManager();

            mToolbarTitle.setText(chatRoomName);
        }

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mMessagesView.setLayoutManager(llm);

        if (savedInstanceState != null) {
            mMessages = (ArrayList<Message>) savedInstanceState.getSerializable(KEY_MESSAGES_LIST);
            if (savedInstanceState.getBoolean(KEY_EXIT_DIALOG)) {
                handleBackPress();
            }
        } else {
            mMessages = new ArrayList<>();
            String hasInterlocutorMessage = getString(mChatRoom.hasInterlocutor()
                    ? R.string
                    .has_interlocutor : R.string.no_interlocutor);
            mMessages.add(new Message(INFO, hasInterlocutorMessage, true));
        }

        mMessagesAdapter = new MessagesAdapter(mMessages);
        mMessagesView.setAdapter(mMessagesAdapter);

        // still doesn't scroll properly for few last messages in recycleview
        mMessagesView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, final int bottom,
                                       int oldLeft, int oldTop, int oldRight, final int oldBottom) {
                if (bottom < oldBottom) {
                    mMessagesView.smoothScrollBy(0, oldBottom - bottom);
                } else {
                    int direction = oldBottom - bottom;
                    if (mMessagesView.canScrollVertically(-direction)) {
                        mMessagesView.smoothScrollBy(0, direction);
                    }
                }
            }
        });

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_MESSAGES_LIST, mMessages);
        outState.putBoolean(KEY_EXIT_DIALOG, mExitDialog);
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
        removeErrorIcon(message);
    }

    private void removeErrorIcon(Message message) {
        mMessages.get(mMessages.indexOf(message)).setWaiting(false);
        if (isUIVisible) {
            updateMessagesView();
        }
    }

    @Override
    public void onMessageSentError(final String messageError) {
//        displayError(message);
    }

    private void displayError(String message) {
        if (isUIVisible) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.toolbar_back)
    public void onToolbarBackClick() {
        handleBackPress();
    }

    public void leaveRoom() {
        mChatRoom.setMessageListener(null);
        mChatRoomsManager.leaveChatRoom(mChatRoom, this);
    }

    @OnClick(R.id.attach)
    public void onFileAttachClick() {
        if (PermissionUtils.checkIfAlreadyHavePermission(getContext())) {
            showFileChooser();
        } else {
            if (PermissionUtils.checkVersion()) {
                PermissionUtils.requestForStoragePermission(this);
            }
        }
    }

    private void showForbidDialog() {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.permissions_are_not_granted),
                    Toast.LENGTH_SHORT).show();
        }
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
                                .select_file)),
                        PICK_IMAGE_FROM_EXPLORER);
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
                    File f = new File(Environment.getExternalStorageDirectory()
                            + TMP_METACOM_JPG);
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
        if (isUIVisible) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLeavedRoom() {
        getActivity().finish();
    }

    @Override
    public void onLeaveError(final String errorMessage) {
        if (isUIVisible) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFileDownloaded(String path) {
        Message message = new Message(FILE, Constants.composeFilePathInfo(path), true);
        displayNewMessage(message);
    }

    @Override
    public void onFileDownloadError() {
        if (isUIVisible) {
            Toast.makeText(getContext(), R.string.err_download_failed,
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openFile(String filePath) {
        Uri uri = FileProvider.getUriForFile(getActivity(),
                BuildConfig.APPLICATION_ID + ".provider",
                new File(filePath));

        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension
                (fileExtension);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        mExitDialog = true;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style
                .AlertDialogStyle);
        builder.setTitle(R.string.leave_chat)
                .setMessage(R.string.leave_chat_desc)
                .setCancelable(false)
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                mExitDialog = false;
                                dialog.cancel();
                            }
                        })
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        showSaveHistoryDialog();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color
                .black14)));
    }

    private void showSaveHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style
                .AlertDialogStyle);
        builder.setTitle(R.string.save_history)
                .setMessage(R.string.save_history_desc)
                .setCancelable(false)
                .setNegativeButton(R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                leaveRoom();
                            }
                        })
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        saveHistory();
                        leaveRoom();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
        alert.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color
                .black14)));
    }

    private void saveHistory() {
        mChatRoomsManager.saveHistory(mMessages, new HistoryCallback() {
            @Override
            public void onHistorySaved(String filename) {
                String message = String.format(getString(R.string.saved_history), filename);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onSaveError() {
                Toast.makeText(getActivity(),
                        getString(R.string.save_history_error), Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }

    @Override
    public void onConnectionLost() {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.connection_lost), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onRejoinSuccess(boolean hasInterlocutor) {
        if (isUIVisible) {
            Toast.makeText(getContext(), getString(R.string.connection_established), Toast
                    .LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRejoinError(String errorMessage) {
        if (isUIVisible) {
            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PermissionUtils.REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser();
                } else {
                    showForbidDialog();
                }
            }
        }
    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

        private static final int TYPE_INFO = 0;
        private static final int TYPE_INCOMING_FILE = 1;
        private static final int TYPE_TEXT_IN = 2;
        private static final int TYPE_TEXT_OUT = 3;

        private List<Message> messages;

        MessagesAdapter(List<Message> messages) {
            this.messages = messages;
        }

        @Override
        public int getItemViewType(int position) {
            Message message = messages.get(position);
            if (message.getType() == INFO) return TYPE_INFO;
            if (message.getType() == FILE && message.isIncoming()) return TYPE_INCOMING_FILE;
            if (message.getType() == TEXT)
                return message.isIncoming() ? TYPE_TEXT_IN : TYPE_TEXT_OUT;
            return -1;
        }

        @Override
        public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int resource = -1;
            if (viewType == TYPE_INFO) {
                resource = R.layout.message_info;
                View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                return new InfoMessageViewHolder(v);
            }
            if (viewType == TYPE_INCOMING_FILE) {
                resource = R.layout.message_file;
                View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
                return new FileMessageViewHolder(v);
            }
            if (viewType == TYPE_TEXT_IN) resource = R.layout.message_in;
            if (viewType == TYPE_TEXT_OUT) resource = R.layout.message_out;
            View v = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
            return new TextMessageViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MessageViewHolder holder, int position) {
            final Message message = messages.get(position);
            final String messageContent = message.getContent();
            if (holder instanceof TextMessageViewHolder) {
                TextMessageViewHolder textMessageViewHolder = (TextMessageViewHolder) holder;
                textMessageViewHolder.messageText.setText(messageContent);
                textMessageViewHolder.errorIcon.setVisibility(message.isWaiting() ? View
                        .VISIBLE : View.GONE);
                registerForContextMenu(textMessageViewHolder.messageLayout);
                textMessageViewHolder.messageLayout.setOnCreateContextMenuListener(new View
                        .OnCreateContextMenuListener() {
                    @Override
                    public void onCreateContextMenu(ContextMenu contextMenu, View view,
                                                    ContextMenu.ContextMenuInfo contextMenuInfo) {
                        contextMenu.add(getString(R.string.copy)).setOnMenuItemClickListener
                                (new MenuItem.OnMenuItemClickListener() {
                                    @Override
                                    public boolean onMenuItemClick(MenuItem menuItem) {
                                        copyToClipboard(getActivity(), messageContent);
                                        return false;
                                    }
                                });
                        if (message.isWaiting()) {
                            contextMenu.add(getString(R.string.resend))
                                    .setOnMenuItemClickListener(new MenuItem
                                            .OnMenuItemClickListener() {
                                        @Override
                                        public boolean onMenuItemClick(MenuItem menuItem) {
                                            mMessages.remove(message);
                                            mChatRoom.sendMessage(message, ChatFragment.this);
                                            displayNewMessage(message);
                                            return false;
                                        }
                                    });
                        }
                    }
                });
            }
            if (holder instanceof FileMessageViewHolder) {
                FileMessageViewHolder fileMessageViewHolder = (FileMessageViewHolder) holder;
                fileMessageViewHolder.fileImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String path = messageContent.substring(messageContent.indexOf('/'));
                        ChatFragment.this.openFile(path);
                    }
                });
            }
            if (holder instanceof InfoMessageViewHolder) {
                InfoMessageViewHolder infoMessageViewHolder = (InfoMessageViewHolder) holder;
                infoMessageViewHolder.messageText.setText(messageContent);
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {

            MessageViewHolder(View itemView) {
                super(itemView);
            }
        }

        class TextMessageViewHolder extends MessageViewHolder {

            private TextView messageText;
            private View messageLayout;
            private ImageView errorIcon;

            TextMessageViewHolder(View itemView) {
                super(itemView);
                messageText = ButterKnife.findById(itemView, R.id.message_text);
                messageLayout = ButterKnife.findById(itemView, R.id.message_layout);
                errorIcon = ButterKnife.findById(itemView, R.id.send_error);
            }

        }

        class FileMessageViewHolder extends MessageViewHolder {

            private ImageView fileImageView;

            private FileMessageViewHolder(View itemView) {
                super(itemView);
                fileImageView = ButterKnife.findById(itemView, R.id.file);
            }
        }

        class InfoMessageViewHolder extends MessageViewHolder {

            private TextView messageText;

            InfoMessageViewHolder(View itemView) {
                super(itemView);
                messageText = ButterKnife.findById(itemView, R.id.message_text);
            }
        }
    }
}
