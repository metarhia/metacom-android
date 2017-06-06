package com.metarhia.metacom.activities.chat;


import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.metarhia.metacom.R;
import com.metarhia.metacom.interfaces.JoinRoomCallback;
import com.metarhia.metacom.models.ChatRoomsManager;
import com.metarhia.metacom.models.UserConnectionsManager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatLoginFragment extends Fragment implements JoinRoomCallback {

    private static final String KEY_CONNECTION_ID = "keyConnectionId";
    @BindView(R.id.chat_name)
    TextInputEditText mChatNameEditText;
    @BindView(R.id.submit)
    AppCompatButton mButtonSubmit;
    private Unbinder mUnbinder;
    private ChatRoomsManager mManager;
    private int mID;

    public static ChatLoginFragment newInstance(int connectionID) {
        Bundle args = new Bundle();
        args.putInt(KEY_CONNECTION_ID, connectionID);
        ChatLoginFragment fragment = new ChatLoginFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chat_login, container, false);
        mUnbinder = ButterKnife.bind(this, view);

        if (getArguments() != null && getArguments().containsKey(KEY_CONNECTION_ID)) {
            mID = getArguments().getInt(KEY_CONNECTION_ID);
            mManager = UserConnectionsManager.get().getConnection(mID).getChatRoomsManager();
        }

        return view;
    }

    @OnClick(R.id.submit)
    public void onButtonSubmitClick() {
        String chatName = mChatNameEditText.getText().toString();
        if (!chatName.isEmpty()) {
            mButtonSubmit.setClickable(false);
            mManager.addChatRoom(chatName, this);
        }
    }

    @Override
    public void onJoinedRoom() {
        mButtonSubmit.setClickable(true);
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_CONNECTION_ID, mID);
        intent.putExtra(ChatActivity.EXTRA_CHAT_ROOM_NAME, mChatNameEditText.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onJoinError(String errorMessage) {
        mButtonSubmit.setClickable(true);
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
