package com.moutamid.telegramdummy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {

    Context context;
    ArrayList<MessageModel> list;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    public MessageAdapter(Context context, ArrayList<MessageModel> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false);
        } else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false);
        }
        return new MessageVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageVH holder, int position) {
        MessageModel model = list.get(holder.getAbsoluteAdapterPosition());
        if (!model.isMedia()) {
            holder.message.setText(model.getMessage());
        }
        String time = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(model.getTimestamp());
        holder.time.setText(time);
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        return Constants.auth().getCurrentUser().getUid().equals(list.get(position).getSenderID()) ? MSG_TYPE_RIGHT : MSG_TYPE_LEFT;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MessageVH extends RecyclerView.ViewHolder{
        TextView message, time;
        public MessageVH(@NonNull View itemView) {
            super(itemView);
            message =itemView.findViewById(R.id.message);
            time =itemView.findViewById(R.id.time);
        }
    }

}
