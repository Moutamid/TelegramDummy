package com.moutamid.telegramdummy.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.adapter.MessageAdapter;
import com.moutamid.telegramdummy.databinding.ActivityChatBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.models.MessageModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.text.SimpleDateFormat;
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
    boolean isSearchEnable = false;
    String imageLink = "";
    Uri imageUri;

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

        binding.send.setOnClickListener(v -> sendMessage(binding.message.getText().toString(), false));
        binding.camera.setOnClickListener(v -> openCamera());

        binding.message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().isEmpty()) {
                    binding.send.setVisibility(View.GONE);
                    binding.attach.setVisibility(View.VISIBLE);
                    binding.camera.setVisibility(View.VISIBLE);
                } else {
                    binding.send.setVisibility(View.VISIBLE);
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
        Constants.initDialog(this, "Please Wait");
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
                            adapter = new MessageAdapter(ChatActivity.this, list, chatModel.getName());
                            binding.chatRC.setAdapter(adapter);
                            binding.chatRC.scrollToPosition(list.size() - 1);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Constants.dismissDialog();
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        Constants.dismissDialog();
                        adapter.notifyAll();
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Constants.dismissDialog();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Constants.dismissDialog();
                    }
                });
    }

    private void sendMessage(String message, boolean isMedia) {
        binding.message.setText("");
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
                                send(message, isMedia);
                            }).addOnFailureListener(e -> {
                                Constants.dismissDialog();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void send(String message, boolean isMedia) {
        MessageModel messageModel = new MessageModel(UUID.randomUUID().toString(), Constants.auth().getCurrentUser().getUid(), message, imageLink, new Date().getTime(), isMedia);
        Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(chatModel.getUserID())
                .child(messageModel.getId()).setValue(messageModel)
                .addOnSuccessListener(unused -> {
                    Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(Constants.auth().getCurrentUser().getUid())
                            .child(messageModel.getId()).setValue(messageModel)
                            .addOnSuccessListener(unused1 -> imageLink = "")
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
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
            }
            return false;
        });
        MenuPopupHelper menuHelper = new MenuPopupHelper(this, (MenuBuilder) popupMenu.getMenu(), binding.menu);
        menuHelper.setForceShowIcon(true);
        menuHelper.show();
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
        Constants.databaseReference().child(Constants.CHATS).child(Constants.auth().getCurrentUser().getUid()).child(chatModel.getId()).removeValue()
                .addOnSuccessListener(unused -> {
                    if (checked) {
                        Constants.databaseReference().child(Constants.CHATS).child(chatModel.getUserID()).child(chatModel.getId()).removeValue()
                                .addOnSuccessListener(unused1 -> onBackPressed())
                                .addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        onBackPressed();
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearHistory(boolean checked) {
        Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(Constants.auth().getCurrentUser().getUid()).removeValue()
                .addOnSuccessListener(unused -> {
                    chatModel.setLastMessage("History was cleared");
                    chatModel.setTimestamp(new Date().getTime());
                    Constants.databaseReference().child(Constants.CHATS).child(Constants.auth().getCurrentUser().getUid())
                            .child(chatModel.getId()).setValue(chatModel);
                }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        if (checked) {
            UserModel userModel = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
            ChatModel receiver = new ChatModel(chatModel.getId(), userModel.getID(), userModel.getName(), userModel.getImage(), "History was cleared", new Date().getTime(), false);
            Constants.databaseReference().child(Constants.MESSAGES).child(chatModel.getId()).child(chatModel.getUserID()).removeValue()
                    .addOnSuccessListener(unused -> {
                        Constants.databaseReference().child(Constants.CHATS).child(chatModel.getUserID())
                                .child(chatModel.getId()).setValue(receiver);
                    }).addOnFailureListener(e -> Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show());
        }
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
        MaterialCardView back = dialog.findViewById(R.id.back);
        TextView name = dialog.findViewById(R.id.name);

        name.setText(chatModel.getName());
        back.setOnClickListener(v -> dialog.dismiss());

        Glide.with(this).load(imageUri).into(image);

        send.setOnClickListener(v -> {
            dialog.dismiss();
            uploadImage(message.getText().toString().trim());
        });

    }

    private void uploadImage(String s) {
        Constants.showDialog();
        String time = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault()).format(new Date().getTime());
        Constants.storageReference(Constants.auth().getCurrentUser().getUid()).child(Constants.MESSAGES).child(time)
                .putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    Constants.dismissDialog();
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        imageLink = uri.toString();
                        String message = s.isEmpty() ? "Photo" : s;
                        sendMessage(message, true);
                    });
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        }
    }
}