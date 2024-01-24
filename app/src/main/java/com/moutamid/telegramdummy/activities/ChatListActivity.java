package com.moutamid.telegramdummy.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.databinding.ActivityChatListBinding;
import com.moutamid.telegramdummy.fragment.ChatFragment;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ActivityChatListBinding binding;
    ChatFragment cf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);
        setSupportActionBar(binding.toolbar);
        cf = new ChatFragment();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.navView.setNavigationItemSelectedListener(this);
        binding.drawLayout.addDrawerListener(toggle);
        toggle.syncState();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, cf).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this, "Please Wait");
        updateNavHead();
    }

    private void updateNavHead() {
        View Header = binding.navView.getHeaderView(0);
        CircleImageView profile = Header.findViewById(R.id.profile);
        TextView name = Header.findViewById(R.id.name);
        TextView phoneNumber = Header.findViewById(R.id.phoneNumber);

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        Stash.put(Constants.STASH_USER, userModel);
                        name.setText(userModel.getName());
                        phoneNumber.setText(userModel.getNumber());
                        if (userModel.getImage().isEmpty()) {
                            Glide.with(this).load(
                                    new AvatarGenerator.AvatarBuilder(this)
                                            .setLabel(userModel.getName().toUpperCase(Locale.ROOT))
                                            .setAvatarSize(70)
                                            .setBackgroundColor(Constants.getRandomColor())
                                            .setTextSize(13)
                                            .toCircle()
                                            .build()
                            ).into(profile);
                        } else Glide.with(this).load(userModel.getImage()).into(profile);

                    }
                });

    }

    @Override
    public void onBackPressed() {
        if (binding.drawLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        MenuItem action_add = menu.findItem(R.id.action_add);
        SearchView searchView = (SearchView) searchItem.getActionView();
        android.widget.SearchView addView = (android.widget.SearchView) action_add.getActionView();

        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView v = (ImageView) addView.findViewById(searchImgId);
        v.setImageResource(R.drawable.user_plus_solid);

        addView.setQueryHint("Add Friend");
        searchView.setQueryHint("Search Chat");

        addView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchUser(query.trim());
                addView.clearFocus();
                MenuItemCompat.collapseActionView(action_add);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the submission of the search query
                searchView.clearFocus();
                MenuItemCompat.collapseActionView(searchItem);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle changes in the search query text
                if (cf.adapter != null) {
                    cf.adapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        return true;
    }

    private void searchUser(String query) {
        Constants.showDialog();
        Constants.databaseReference().child(Constants.USER).get()
                .addOnSuccessListener(dataSnapshot -> {
                    Constants.dismissDialog();
                    if (dataSnapshot.exists()) {
                        boolean check = false;
                        UserModel userModel = null;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            userModel = snapshot.getValue(UserModel.class);
                            if ((userModel.getUsername().equals(query) && !userModel.getID().equals(Constants.auth().getCurrentUser().getUid())) ||
                                    userModel.getNumber().contains(query)
                            ) {
                                check = true;
                                break;
                            }
                        }

                        if (check) {
                            ArrayList<ChatModel> list = Stash.getArrayList(Constants.CHAT_LIST, ChatModel.class);
                            if (list.size() > 0) {
                                boolean c = false;
                                for (ChatModel model : list) {
                                    if (userModel.getID().equals(model.getUserID())) {
                                        c = true;
                                        break;
                                    }
                                }

                                if (c) {
                                    Toast.makeText(this, "User already is in you chat list", Toast.LENGTH_SHORT).show();
                                } else {
                                    Stash.put(Constants.PASS_USER, userModel);
                                    createChat(userModel);
                                }

                            } else {
                                Stash.put(Constants.PASS_USER, userModel);
                                createChat(userModel);
                            }
                        } else {
                            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createChat(UserModel userModel) {
        Constants.initDialog(this, "Creating Chat with " + userModel.getName());
        ChatModel chatModel = new ChatModel(UUID.randomUUID().toString(), userModel.getID(), userModel.getName(), userModel.getImage(), userModel.getName() + " joined Telegram", new Date().getTime(), false);
        UserModel stashUser = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
        ChatModel receiver = new ChatModel(chatModel.getId(), stashUser.getID(), stashUser.getName(), stashUser.getImage(), stashUser.getName() + " joined Telegram", new Date().getTime(), false);
        Constants.databaseReference().child(Constants.CHATS).child(Constants.auth().getCurrentUser().getUid())
                .child(chatModel.getId()).setValue(chatModel)
                .addOnSuccessListener(unused -> {
                    Constants.databaseReference().child(Constants.CHATS).child(userModel.getID())
                            .child(chatModel.getId()).setValue(receiver)
                            .addOnSuccessListener(unused2 -> {
                                Constants.dismissDialog();
                                Stash.put(Constants.PASS_CHAT, chatModel);
                                Toast.makeText(this, "Happy Chatting!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, ChatActivity.class));
                            }).addOnFailureListener(e -> {
                                Constants.dismissDialog();
                                Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_setting) {
            startActivity(new Intent(ChatListActivity.this, SettingActivity.class));
        } else if (item.getItemId() == R.id.nav_support) {
            Uri mailtoUri = Uri.parse("mailto:example123@gmail.com" +
                    "?subject=" + Uri.encode("Help & Support") +
                    "&body=" + Uri.encode("Your Complain??"));
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, mailtoUri);
            startActivity(emailIntent);
        } else if (item.getItemId() == R.id.nav_invite) {
            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(playStoreIntent);
            } catch (ActivityNotFoundException e) {
                uri = Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName());
                playStoreIntent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(playStoreIntent);
            }
        }
        binding.drawLayout.closeDrawer(GravityCompat.START);
        return false;
    }

}