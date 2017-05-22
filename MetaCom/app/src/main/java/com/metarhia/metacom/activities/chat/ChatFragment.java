package com.metarhia.metacom.activities.chat;


import android.content.Context;
import android.os.Bundle;
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
import com.metarhia.metacom.models.Message;
import com.metarhia.metacom.models.MessageType;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private Unbinder mUnbinder;

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

    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat2, container, false);
        mUnbinder = ButterKnife.bind(this, v);

        mToolbarTitle.setText(getString(R.string.chatname));

        MessageAdapter adapter = new MessageAdapter(getActivity(), getMessageExamples());
        mMessagesListView.setAdapter(adapter);

        return v;
    }

    private ArrayList<Message> getMessageExamples() {
        ArrayList<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String mes = "";
            for (int j = 0; j < 10; j++) {
                mes = mes.concat(i + "");
            }
            messages.add(new Message(MessageType.TEXT, mes, (i % 2 == 0)));
        }
        return messages;
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
        // todo send message
        Toast.makeText(getContext(), "message was sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }
}
