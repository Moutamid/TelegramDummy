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
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {

    Context context;
    ArrayList<MessageModel> list;
    String name;
    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;

    public MessageAdapter(Context context, ArrayList<MessageModel> list, String name) {
        this.context = context;
        this.list = list;
        this.name = name;
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
        holder.message.setText(model.getMessage());
        if (model.isMedia()) {
            if (model.getMessage().equals("Photo")) {
                holder.message.setVisibility(View.GONE);
            }
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(context).load(model.getImage()).placeholder(R.color.black).into(holder.imageView);

            holder.image.setOnClickListener(v -> {
                openViewer(holder.getAbsoluteAdapterPosition());
            });

        }
        String time = new SimpleDateFormat("hh:mm aa", Locale.getDefault()).format(model.getTimestamp());
        holder.time.setText(time);
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
        return userModel.getNumber().equals(list.get(position).getSenderID()) ? MSG_TYPE_RIGHT : MSG_TYPE_LEFT;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MessageVH extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView imageView;
        CardView image;

        public MessageVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            image = itemView.findViewById(R.id.image);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

}
