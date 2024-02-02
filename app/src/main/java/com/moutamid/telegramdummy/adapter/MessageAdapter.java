package com.moutamid.telegramdummy.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.card.MaterialCardView;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;
import com.moutamid.telegramdummy.utili.DeleteListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {

    Context context;
    ArrayList<MessageModel> list;
    String name;
    DeleteListener deleteListener;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_RIGHT_MEDIA = 2;
    private static final int MSG_TYPE_LEFT_MEDIA = 3;
    private static final int MSG_TYPE_RIGHT_MEDIA_CAPTION = 4;
    private static final int MSG_TYPE_LEFT_MEDIA_CAPTION = 5;
    private static final int DATE_TYPE = 6;

    public MessageAdapter(Context context, ArrayList<MessageModel> list, String name, DeleteListener deleteListener) {
        this.context = context;
        this.list = list;
        this.name = name;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public MessageVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == DATE_TYPE) {
            view = LayoutInflater.from(context).inflate(R.layout.date_item, parent, false);
        } else if (viewType == MSG_TYPE_LEFT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false);
        } else if (viewType == MSG_TYPE_RIGHT) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false);
        } else if (viewType == MSG_TYPE_RIGHT_MEDIA) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_right_media, parent, false);
        }  else if (viewType == MSG_TYPE_LEFT_MEDIA) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_left_media, parent, false);
        } else if (viewType == MSG_TYPE_RIGHT_MEDIA_CAPTION) {
            view = LayoutInflater.from(context).inflate(R.layout.chat_right_media_caption, parent, false);
        }  else {
            view = LayoutInflater.from(context).inflate(R.layout.chat_left_media_caption, parent, false);
        }
        return new MessageVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageVH holder, int position) {
        MessageModel model = list.get(holder.getAbsoluteAdapterPosition());

        if (!model.isDate()){
            ChatModel chatModel = (ChatModel) Stash.getObject(Constants.PASS_CHAT, ChatModel.class);
            String status = chatModel.getStatus();
            if (status.equalsIgnoreCase("online")) {
                holder.check.setImageResource(R.drawable.check);
            } else if (status.equalsIgnoreCase("typing...")) {
                holder.check.setImageResource(R.drawable.check);
            } else if (status.startsWith("last seen")) {
                holder.check.setImageResource(R.drawable.round_check_24);
            } else {
                holder.check.setImageResource(R.drawable.round_check_24);
            }

            holder.message.setText(model.getMessage());
            Glide.with(context).load(model.getImage()).placeholder(R.color.black).into(holder.imageView);
            String time = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(model.getTimestamp());
            holder.time.setText(time);
        } else {
            String time = new SimpleDateFormat("MMMM dd", Locale.getDefault()).format(model.getTimestamp());
            holder.time.setText(time);
        }

        if (model.isMedia()){
            holder.imageView.setOnClickListener(v -> openViewer(holder.getAbsoluteAdapterPosition()));
        }

        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onHoldClick(list.get(holder.getAbsoluteAdapterPosition()));
            return true;
        });

    }

    private void openViewer(int pos) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.imageviewer);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(true);
        dialog.show();

        RecyclerView recyclerView = dialog.findViewById(R.id.imageRC);
        MaterialCardView back = dialog.findViewById(R.id.back);
        TextView name = dialog.findViewById(R.id.name);

        name.setText(this.name);
        back.setOnClickListener(v -> dialog.dismiss());

        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setHasFixedSize(false);

        MessageModel model = list.get(pos);

        ArrayList<String> imageList = new ArrayList<>();
        for (MessageModel messageModel : list){
            if (messageModel.isMedia()){
                imageList.add(messageModel.getImage());
            }
        }
        int i = 0;
        for (int index = 0; index < imageList.size(); index++) {
            String s = imageList.get(index);
            if (model.getImage().equals(s)) {
                i = index;
                break;
            }
        }
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);
        ImageAdapter adapter = new ImageAdapter(context, imageList);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(i);
    }

    @Override
    public int getItemViewType(int position) {
        //get currently signed in user
        UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        if (!list.get(position).isDate()){
            if (userModel.getNumber().equals(list.get(position).getSenderID())){
                if (list.get(position).isMedia()){
                    if (!list.get(position).getMessage().isEmpty()){
                        return MSG_TYPE_RIGHT_MEDIA_CAPTION;
                    } else {
                        return MSG_TYPE_RIGHT_MEDIA;
                    }
                } else {
                    return MSG_TYPE_RIGHT;
                }
            } else {
                if (list.get(position).isMedia()){
                    if (!list.get(position).getMessage().isEmpty()){
                        return MSG_TYPE_LEFT_MEDIA_CAPTION;
                    } else {
                        return MSG_TYPE_LEFT_MEDIA;
                    }
                } else {
                    return MSG_TYPE_LEFT;
                }
            }
        } else {
            return DATE_TYPE;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MessageVH extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView imageView, check;

        public MessageVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.photo);
            check = itemView.findViewById(R.id.check);
        }
    }

}
