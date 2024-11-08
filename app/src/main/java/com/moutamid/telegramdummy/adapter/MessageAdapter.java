package com.moutamid.telegramdummy.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;
import com.moutamid.telegramdummy.utili.DeleteListener;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageVH> {
    private static final String TAG = "MessageAdapter";

    private final Context context;
    private final ArrayList<MessageModel> list;
    private final String name;
    private final DeleteListener deleteListener;

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT = 1;
    private static final int MSG_TYPE_RIGHT_MEDIA = 2;
    private static final int MSG_TYPE_LEFT_MEDIA = 3;
    private static final int MSG_TYPE_RIGHT_MEDIA_CAPTION = 4;
    private static final int MSG_TYPE_LEFT_MEDIA_CAPTION = 5;
    private static final int DATE_TYPE = 6;

    // Constructor
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
        switch (viewType) {
            case DATE_TYPE:
                view = LayoutInflater.from(context).inflate(R.layout.date_item, parent, false);
                break;
            case MSG_TYPE_LEFT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_left, parent, false);
                break;
            case MSG_TYPE_RIGHT:
                view = LayoutInflater.from(context).inflate(R.layout.chat_right, parent, false);
                break;
            case MSG_TYPE_RIGHT_MEDIA:
                view = LayoutInflater.from(context).inflate(R.layout.chat_right_media, parent, false);
                break;
            case MSG_TYPE_LEFT_MEDIA:
                view = LayoutInflater.from(context).inflate(R.layout.chat_left_media, parent, false);
                break;
            case MSG_TYPE_RIGHT_MEDIA_CAPTION:
                view = LayoutInflater.from(context).inflate(R.layout.chat_right_media_caption, parent, false);
                break;
            case MSG_TYPE_LEFT_MEDIA_CAPTION:
                view = LayoutInflater.from(context).inflate(R.layout.chat_left_media_caption, parent, false);
                break;
            default:
                throw new IllegalStateException("Unexpected viewType: " + viewType);
        }
        return new MessageVH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageVH holder, int position) {
        MessageModel model = list.get(position);
        holder.setIsRecyclable(false);

        // Handle display for message or date
        if (!model.isDate()) {
            // Set the checkmark status (e.g., online, typing, last seen)
            setCheckStatus(holder, model);

            // Adjust padding for shorter messages
            adjustMessagePadding(holder, model);
            String message = model.getMessage();
            if (message.length() > 120) {
                message += "\t\t\t\t\t";
            }

            holder.message.setText(formatMessage(message));
            holder.time.setText(formatTime(model.getTimestamp(), "hh:mm aa"));

            // Load media if present
            if (model.isMedia()) {
                loadImage(holder, model.getImage());
            }

        } else {
            // Set formatted date in case it's a date item
            holder.time.setText(formatTime(model.getTimestamp(), "MMMM dd"));
        }

        // Handle long press for delete
        holder.itemView.setOnLongClickListener(v -> {
            deleteListener.onHoldClick(model);
            return true;
        });
    }


    private void setCheckStatus(MessageVH holder, MessageModel model) {
        ChatModel chatModel = (ChatModel) Stash.getObject(Constants.PASS_CHAT, ChatModel.class);
        String status = chatModel.getStatus();

        if ("online".equalsIgnoreCase(status) || "typing...".equalsIgnoreCase(status)) {
            holder.check.setImageResource(R.drawable.check);
        } else {
            holder.check.setImageResource(R.drawable.round_check_24);
        }
    }

    private void adjustMessagePadding(MessageVH holder, MessageModel model) {
        if (model.getMessage().length() <= 43) {
            holder.message.setPaddingRelative(holder.dpToPx(12), holder.dpToPx(8), holder.dpToPx(60), holder.dpToPx(8));
        }
    }

    private void loadImage(MessageVH holder, String imageUrl) {
        Log.d(TAG, "Loading image: " + imageUrl);
        Glide.with(context).load(imageUrl).placeholder(R.color.black).into(holder.imageView);
        holder.imageView.setOnClickListener(v -> openImageViewer(holder.getAbsoluteAdapterPosition()));
    }

    // Opens image viewer with the list of media
    private void openImageViewer(int pos) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.imageviewer);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(true);
        dialog.show();

        // Setup recycler view inside dialog for image swiping
        RecyclerView recyclerView = dialog.findViewById(R.id.imageRC);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        ArrayList<String> imageList = new ArrayList<>();
        for (MessageModel messageModel : list) {
            if (messageModel.isMedia()) {
                imageList.add(messageModel.getImage());
            }
        }

        int currentImageIndex = imageList.indexOf(list.get(pos).getImage());

        // Attach snap helper for smooth scrolling
        PagerSnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        ImageAdapter adapter = new ImageAdapter(context, imageList, dialog::dismiss);
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(currentImageIndex);
    }

    @Override
    public int getItemViewType(int position) {
        MessageModel message = list.get(position);
        UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);

        if (message.isDate()) {
            return DATE_TYPE;
        }

        if (userModel.getNumber().equals(message.getSenderID())) {
            return message.isMedia() ? (message.getMessage().isEmpty() ? MSG_TYPE_RIGHT_MEDIA : MSG_TYPE_RIGHT_MEDIA_CAPTION) : MSG_TYPE_RIGHT;
        } else {
            return message.isMedia() ? (message.getMessage().isEmpty() ? MSG_TYPE_LEFT_MEDIA : MSG_TYPE_LEFT_MEDIA_CAPTION) : MSG_TYPE_LEFT;
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static boolean containsLineBreak(String text) {
        return text != null && (text.indexOf('\n') != -1 || text.indexOf('\r') != -1);
    }


    // Formats the message into chunks for better display
    private static @NonNull StringBuilder formatMessage(String message) {
        StringBuilder result = new StringBuilder();
        if (containsLineBreak(message)) {
            Log.d(TAG, "formatMessage: BREAK");
            result.append(message);
            result.append("\n");
        } else {
            String[] words = message.split(" "); // .replaceAll("([\\r\\n]+)", "\n").replaceAll("\\s", " ")
            StringBuilder currentChunk = new StringBuilder();
            for (String word : words) {
                if (currentChunk.length() + word.length() + 1 > 44) {
                    result.append(currentChunk).append("\n");
                    currentChunk = new StringBuilder();
                }
                if (currentChunk.length() > 0) {
                    currentChunk.append(" ");
                }
                currentChunk.append(word);
            }
            if (currentChunk.length() > 0) {
                result.append(currentChunk);
            }
        }
        return result;
    }

    // Formats the time or date for display
    private static String formatTime(long timestamp, String pattern) {
        return new SimpleDateFormat(pattern, Locale.getDefault()).format(timestamp);
    }

    // ViewHolder class
    public static class MessageVH extends RecyclerView.ViewHolder {
        TextView message, time;
        ImageView imageView, check;

        public MessageVH(@NonNull View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            time = itemView.findViewById(R.id.time);
            imageView = itemView.findViewById(R.id.photo);
            check = itemView.findViewById(R.id.check);
        }

        // Converts dp to pixels
        public int dpToPx(int dp) {
            return (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, dp, itemView.getResources().getDisplayMetrics());
        }
    }
}