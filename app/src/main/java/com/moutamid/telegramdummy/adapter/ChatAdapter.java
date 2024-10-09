package com.moutamid.telegramdummy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.activities.ChatActivity;
import com.moutamid.telegramdummy.activities.EditContactActivity;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatVH> implements Filterable {

    Context context;
    Activity activity;
    ArrayList<ChatModel> list;
    ArrayList<ChatModel> listAll;

    public ChatAdapter(Context context, Activity activity, ArrayList<ChatModel> list) {
        this.context = context;
        this.activity = activity;
        this.list = list;
        this.listAll = new ArrayList<>(list);
    }

    @NonNull
    @Override
    public ChatVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatVH(LayoutInflater.from(context).inflate(R.layout.chat, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ChatVH holder, int position) {
        ChatModel model = list.get(holder.getAbsoluteAdapterPosition());
        holder.name.setText(model.getName());
        holder.lastMessage.setText(model.getLastMessage());
        holder.time.setText(Constants.getTime(model.getTimestamp()));
        Glide.with(context).load(model.getImage()).placeholder(new AvatarGenerator.AvatarBuilder(context)
                .setLabel(model.getName().trim().toUpperCase(Locale.ROOT))
                .setAvatarSize(50)
                .setBackgroundColor(model.getColor())
                .setTextSize(14)
                .toCircle()
                .build()).into(holder.profile);

        holder.itemView.setOnClickListener(v -> {
            Stash.put(Constants.PASS_CHAT, model);
            context.startActivity(new Intent(context, ChatActivity.class));
        });

        holder.profile.setOnClickListener(v -> {
            Stash.put(Constants.PASS_USER, model);
            context.startActivity(new Intent(context, EditContactActivity.class));
            activity.finish();
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            ArrayList<ChatModel> filterList = new ArrayList<>();
            if (charSequence.toString().isEmpty()){
                filterList.addAll(listAll);
            } else {
                for (ChatModel listModel : listAll){
                    if (listModel.getName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                        filterList.add(listModel);
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filterList;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends ChatModel>) results.values);
            notifyDataSetChanged();
        }
    };

    public class ChatVH extends RecyclerView.ViewHolder{
        CircleImageView profile;
        TextView name, time, lastMessage;
        public ChatVH(@NonNull View itemView) {
            super(itemView);

            profile = itemView.findViewById(R.id.profile);
            name = itemView.findViewById(R.id.name);
            time = itemView.findViewById(R.id.time);
            lastMessage = itemView.findViewById(R.id.lastMessage);

        }
    }

}
