package com.metarhia.metacom.Chat;


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

import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chat2, container, false);
        ButterKnife.bind(this, v);

        final TextView toolbar_title = (TextView) ButterKnife.findById(v, R.id.toolbar_title);
        toolbar_title.setText(getString(R.string.chatname));
        final ImageView toolbar_back = (ImageView) ButterKnife.findById(v, R.id.toolbar_back);
        toolbar_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        final ImageView attach = (ImageView) ButterKnife.findById(v, R.id.attach);
        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: choose file to send
                Toast.makeText(getContext(), "file was sent", Toast.LENGTH_SHORT).show();
            }
        });

        final ListView messagesList = ButterKnife.findById(v, R.id.messages_list);
        MessageAdapter adapter = new MessageAdapter(getActivity(), getMessageExamples());
        messagesList.setAdapter(adapter);

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

            View message_right_layout = view.findViewById(R.id.message_right_layout);
            View message_left_layout = view.findViewById(R.id.message_left_layout);
            if (m.isIncoming()) {
                message_right_layout.setVisibility(View.GONE);
                message_left_layout.setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.message_left_text)).setText(m.getContent());
            } else {
                message_right_layout.setVisibility(View.VISIBLE);
                message_left_layout.setVisibility(View.GONE);
                ((TextView) view.findViewById(R.id.message_right_text)).setText(m.getContent());
            }

            return view;
        }
    }

}
