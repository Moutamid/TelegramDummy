package com.moutamid.telegramdummy.activities;

import static com.moutamid.telegramdummy.utili.Constants.TAG;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.adapter.MessageAdapter;
import com.moutamid.telegramdummy.databinding.ActivityChatBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;
import com.moutamid.telegramdummy.utili.DeleteListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    ArrayList<MessageModel> list;
    ChatModel chatModel;
    MessageAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 1001;
    boolean isSearchEnable = false;
    Uri imageUri = Uri.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        list = new ArrayList<>();

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.menu.setOnClickListener(v -> showPopupMenu());

        chatModel = (ChatModel) Stash.getObject(Constants.PASS_CHAT, ChatModel.class);

        binding.name.setText(chatModel.getName());
        binding.status.setText(chatModel.getStatus());

        binding.chatRC.setHasFixedSize(false);
        binding.chatRC.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        Glide.with(this).load(chatModel.getImage()).placeholder(new AvatarGenerator.AvatarBuilder(this)
                .setLabel(chatModel.getName().toUpperCase(Locale.ROOT))
                .setAvatarSize(50)
                .setBackgroundColor(chatModel.getColor())
                .setTextSize(14)
                .toCircle()
                .build()).into(binding.profile);

        binding.message.requestFocus();

        binding.mainLayout.setOnClickListener(v -> {
            Stash.put(Constants.PASS_USER, chatModel);
            startActivity(new Intent(this, EditContactActivity.class));
            finish();
        });

        binding.emoji.setOnClickListener(v -> {
            binding.message.requestFocus();
        });

        binding.attach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), PICK_IMAGE_REQUEST);
        });

        // IMPORTANT : "\t\t\t" these are important to add the space at the end of the message for UI
        binding.send.setOnClickListener(v -> send(binding.message.getText().toString() + "\t\t\t", false, binding.message.getText().toString()));
        binding.receive.setOnClickListener(v -> receive(binding.message.getText().toString() + "\t\t", false, binding.message.getText().toString()));

        binding.camera.setOnClickListener(v -> openCamera());

        binding.message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    binding.send.setVisibility(View.GONE);
                    binding.receive.setVisibility(View.GONE);
                    binding.attach.setVisibility(View.VISIBLE);
                    binding.camera.setVisibility(View.VISIBLE);
                } else {
                    binding.send.setVisibility(View.VISIBLE);
                    binding.receive.setVisibility(View.VISIBLE);
                    binding.attach.setVisibility(View.GONE);
                    binding.camera.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    private void openCamera() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        getMessages();
    }

    private void getMessages() {
        list = Stash.getArrayList(chatModel.getId(), MessageModel.class);
        adapter = new MessageAdapter(this, list, chatModel.getName(), deleteListener);
        binding.chatRC.setAdapter(adapter);
        binding.chatRC.scrollToPosition(list.size() - 1);
    }

    private void receive(String message, boolean isMedia, String last) {
        if (!message.isEmpty() || isMedia) {
            binding.message.setText("");
            chatModel.setLastMessage(last);
            chatModel.setTimestamp(new Date().getTime());
            ArrayList<ChatModel> chatList = Stash.getArrayList(Constants.USER, ChatModel.class);
            int index = retrieveIndex(chatList);
            chatList.set(index, chatModel);
            Stash.put(Constants.USER, chatList);
            MessageModel messageModel = new MessageModel(UUID.randomUUID().toString(), chatModel.getId(), message, imageUri.toString(), new Date().getTime(), isMedia, false);
            list.add(messageModel);
            Stash.put(chatModel.getId(), list);
            adapter.notifyItemInserted(list.size() - 1);
            binding.chatRC.scrollToPosition(list.size() - 1);
        }
    }

    private void send(String message, boolean isMedia, String last) {
        if (!message.isEmpty() || isMedia) {
            binding.message.setText("");
            chatModel.setLastMessage(last);
            chatModel.setTimestamp(new Date().getTime());
            ArrayList<ChatModel> chatList = Stash.getArrayList(Constants.USER, ChatModel.class);
            int index = retrieveIndex(chatList);
            chatList.set(index, chatModel);
            Stash.put(Constants.USER, chatList);
            UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
            MessageModel messageModel = new MessageModel(UUID.randomUUID().toString(), userModel.getNumber(), message, imageUri.toString(), new Date().getTime(), isMedia, false);
            list.add(messageModel);
            Stash.put(chatModel.getId(), list);
            adapter.notifyItemInserted(list.size() - 1);
            binding.chatRC.scrollToPosition(list.size() - 1);
        }
    }

    @SuppressLint("RestrictedApi")
    private void showPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, binding.menu);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.search) {
                searchList();
                return true;
            } else if (itemId == R.id.clear) {
                showDeleteDialog(true);
                return true;
            } else if (itemId == R.id.delete) {
                showDeleteDialog(false);
                return true;
            } else if (itemId == R.id.add) {
                addDate();
                return true;
            }
            return false;
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), binding.menu);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
    }

    private void addDate() {
        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select a Date");
        final MaterialDatePicker<Long> datePicker = builder.build();
        datePicker.addOnPositiveButtonClickListener(selection -> {
            datePicker.dismiss();
            list.add(new MessageModel(UUID.randomUUID().toString(), "", "", "", selection, false, true));
            adapter = new MessageAdapter(this, list, chatModel.getName(), deleteListener);
            binding.chatRC.setAdapter(adapter);
            binding.chatRC.scrollToPosition(list.size() - 1);
            Stash.put(chatModel.getId(), list);
        });
        datePicker.show(getSupportFragmentManager(), datePicker.toString());
    }

    private void showDeleteDialog(boolean isClear) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.delete_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(true);
        dialog.show();

        TextView heading = dialog.findViewById(R.id.heading);
        TextView message = dialog.findViewById(R.id.message);
        MaterialCheckBox check = dialog.findViewById(R.id.check);
        MaterialButton cancel = dialog.findViewById(R.id.cancel);
        MaterialButton delete = dialog.findViewById(R.id.delete);

        if (isClear) {
            heading.setText("Clear History");
            message.setText("Are you sure you want to clear your chat history with " + chatModel.getName() + "?");
            check.setText("Also delete for " + chatModel.getName());
            delete.setText("Delete");
        } else {
            heading.setText("Delete Chat");
            message.setText("Permanently delete the chat with " + chatModel.getName() + "?");
            check.setText("Also delete for " + chatModel.getName());
            delete.setText("Delete chat");
        }

        cancel.setOnClickListener(v -> dialog.dismiss());

        delete.setOnClickListener(v -> {
            dialog.dismiss();
            if (isClear) {
                clearHistory(check.isChecked());
            } else {
                deleteChat(check.isChecked());
            }
        });

    }

    private void deleteChat(boolean checked) {
        Stash.clear(chatModel.getId());
        ArrayList<ChatModel> list = Stash.getArrayList(Constants.USER, ChatModel.class);
        int index = retrieveIndex(list);
        list.remove(index);
        Stash.put(Constants.USER, list);
        Toast.makeText(this, "Chat Deleted", Toast.LENGTH_SHORT).show();
        onBackPressed();
    }

    private int retrieveIndex(ArrayList<ChatModel> list) {
        for (int i = 0; i < list.size(); i++) {
            ChatModel model = list.get(i);
            if (model.getId().equals(chatModel.getId())) {
                return i;
            }
        }
        return -1;
    }

    DeleteListener deleteListener = new DeleteListener() {
        @Override
        public void onHoldClick(MessageModel messageModel) {
            Dialog dialog = new Dialog(ChatActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.delete_dialog);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(true);
            dialog.show();

            TextView heading = dialog.findViewById(R.id.heading);
            TextView message = dialog.findViewById(R.id.message);
            MaterialCheckBox check = dialog.findViewById(R.id.check);
            MaterialButton cancel = dialog.findViewById(R.id.cancel);
            MaterialButton delete = dialog.findViewById(R.id.delete);


            heading.setText("Delete Message");
            message.setText("Are you sure you want to delete this message?");
            check.setText("Also delete for " + chatModel.getName());
            delete.setText("Delete");

            cancel.setOnClickListener(v -> dialog.dismiss());

            delete.setOnClickListener(v -> {
                dialog.dismiss();
                deleteMessage(messageModel);
            });

        }
    };

    private void deleteMessage(MessageModel messageModel) {
        int i = retrievePosition(list, messageModel.getId());
        Log.d(TAG, "deleteMessage: " + messageModel.getId());
        Log.d(TAG, "deleteMessage: " + i);
        if (i != -1){
            list.remove(i);
            adapter.notifyItemRemoved(i);
            Stash.put(chatModel.getId(), list);

            chatModel.setLastMessage("Message Deleted");
            chatModel.setTimestamp(new Date().getTime());
            ArrayList<ChatModel> chatList = Stash.getArrayList(Constants.USER, ChatModel.class);
            int index = retrieveIndex(chatList);
            chatList.set(index, chatModel);
            Stash.put(Constants.USER, chatList);

        }
    }

    private void clearHistory(boolean checked) {
        list.clear();
        Stash.clear(chatModel.getId());
        chatModel.setLastMessage("History was cleared");
        chatModel.setTimestamp(new Date().getTime());
        ArrayList<ChatModel> chatList = Stash.getArrayList(Constants.USER, ChatModel.class);
        int index = retrieveIndex(chatList);
        chatList.set(index, chatModel);
        Stash.put(Constants.USER, chatList);
        getMessages();
    }

    private void searchList() {
        isSearchEnable = true;
        binding.mainLayout.setVisibility(View.GONE);
        binding.searchView.setVisibility(View.VISIBLE);
        binding.searchView.requestFocus();
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                int i = searchAndRetrievePosition(list, query);
                if (i == -1) {
                    Toast.makeText(ChatActivity.this, "No message found", Toast.LENGTH_SHORT).show();
                } else
                    binding.chatRC.scrollToPosition(i);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

    }

    public static int searchAndRetrievePosition(ArrayList<MessageModel> modelList, String searchMessage) {
        for (int index = 0; index < modelList.size(); index++) {
            MessageModel model = modelList.get(index);
            if (model.getMessage().contains(searchMessage)) {
                return index;
            }
        }
        return -1;
    }
    public static int retrievePosition(ArrayList<MessageModel> modelList, String id) {
        for (int index = 0; index < modelList.size(); index++) {
            MessageModel model = modelList.get(index);
            if (model.getId().equals(id)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            openImageViewer();
        }
    }

    private void openImageViewer() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.image_picker_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dialog.setCancelable(true);
        dialog.show();

        ImageView image = dialog.findViewById(R.id.image);
        EditText message = dialog.findViewById(R.id.message);
        MaterialCardView send = dialog.findViewById(R.id.send);
        MaterialCardView receive = dialog.findViewById(R.id.receive);
        MaterialCardView back = dialog.findViewById(R.id.back);
        TextView name = dialog.findViewById(R.id.name);

        name.setText(chatModel.getName());
        back.setOnClickListener(v -> dialog.dismiss());

        Glide.with(this).load(imageUri).into(image);


        // IMPORTANT : "\t\t\t" these are important to add the space at the end of the message for UI
        send.setOnClickListener(v -> {
            dialog.dismiss();
            send(message.getText().toString() + "\t\t\t", true, "Photo");
            imageUri = Uri.EMPTY;
        });
        receive.setOnClickListener(v -> {
            dialog.dismiss();
            receive(message.getText().toString() + "\t\t\t", true, "Photo");
            imageUri = Uri.EMPTY;
        });

    }

    @Override
    public void onBackPressed() {
        if (isSearchEnable) {
            isSearchEnable = false;
            binding.mainLayout.setVisibility(View.VISIBLE);
            binding.searchView.setVisibility(View.GONE);
            binding.searchView.setQuery("", false);
        } else {
            super.onBackPressed();
            Stash.clear(Constants.PASS_CHAT);
        }
    }
}