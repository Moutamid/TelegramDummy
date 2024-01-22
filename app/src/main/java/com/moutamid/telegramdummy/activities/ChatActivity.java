package com.moutamid.telegramdummy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.moutamid.telegramdummy.adapter.MessageAdapter;
import com.moutamid.telegramdummy.databinding.ActivityChatBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    ArrayList<MessageModel> list;
    ChatModel chatModel;
    MessageAdapter adapter;
    private static final int PICK_IMAGE_REQUEST = 1001;

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

        binding.chatRC.setHasFixedSize(false);
        binding.chatRC.setLayoutManager(new LinearLayoutManager(this));

        Glide.with(this).load(chatModel.getImage()).placeholder(new AvatarGenerator.AvatarBuilder(this)
                .setLabel(chatModel.getName().toUpperCase(Locale.ROOT))
                .setAvatarSize(50)
                .setBackgroundColor(Constants.getRandomColor())
                .setTextSize(14)
                .toCircle()
                .build()).into(binding.profile);

        binding.message.requestFocus();

        binding.emoji.setOnClickListener(v -> {
            binding.message.requestFocus();
            /*InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                binding.message.setInputType(EditorInfo.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
                inputMethodManager.showSoftInput(binding.message, InputMethodManager.SHOW_IMPLICIT);
            }*/
        });

        binding.attach.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent, "Pick Image"), PICK_IMAGE_REQUEST);
        });

        binding.send.setOnClickListener(v -> sendMessage());

        binding.message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    binding.send.setVisibility(View.GONE);
                    binding.attach.setVisibility(View.VISIBLE);
                } else {
                    binding.send.setVisibility(View.VISIBLE);
                    binding.attach.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this, "");
        list.clear();
        getMessages();
    }

    private void getMessages() {
        Constants.showDialog();
        Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(Constants.auth().getCurrentUser().getUid())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Constants.dismissDialog();
                        if (snapshot.exists()) {
                            MessageModel model = snapshot.getValue(MessageModel.class);
                            list.add(model);
                            list.sort(Comparator.comparing(MessageModel::getTimestamp));
                            adapter = new MessageAdapter(ChatActivity.this, list);
                            binding.chatRC.setAdapter(adapter);
                            binding.chatRC.scrollToPosition(list.size() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Constants.dismissDialog();
                        if (snapshot.exists()) {
                            adapter.notifyAll();
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Constants.dismissDialog();
                        if (snapshot.exists()) {

                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Constants.dismissDialog();
                        if (snapshot.exists()) {

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();

                    }
                });
    }

    private void sendMessage() {
        String message = binding.message.getText().toString();
        chatModel.setLastMessage(message);
        chatModel.setTimestamp(new Date().getTime());
        UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        ChatModel receiver = new ChatModel(chatModel.getId(), userModel.getID(), userModel.getName(), userModel.getImage(), message, new Date().getTime(), false);

        Constants.databaseReference().child(Constants.CHATS).child(Constants.auth().getCurrentUser().getUid())
                .child(chatModel.getId()).setValue(chatModel)
                .addOnSuccessListener(unused -> {
                    Constants.databaseReference().child(Constants.CHATS).child(chatModel.getUserID())
                            .child(chatModel.getId()).setValue(receiver)
                            .addOnSuccessListener(unused2 -> {
                                send(message);
                            }).addOnFailureListener(e -> {
                                Constants.dismissDialog();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void send(String message) {
        MessageModel messageModel = new MessageModel(UUID.randomUUID().toString(), Constants.auth().getCurrentUser().getUid(), message, new Date().getTime(), false);
        Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(chatModel.getUserID())
                .child(messageModel.getId()).setValue(messageModel)
                .addOnSuccessListener(unused -> {
                    binding.message.setText("");
                    Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(Constants.auth().getCurrentUser().getUid())
                            .child(messageModel.getId()).setValue(messageModel)
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showPopupMenu() {

    }

}