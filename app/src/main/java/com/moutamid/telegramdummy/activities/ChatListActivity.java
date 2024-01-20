package com.moutamid.telegramdummy.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.google.android.material.navigation.NavigationView;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.databinding.ActivityChatListBinding;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatListActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    ActivityChatListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);
        setSupportActionBar(binding.toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                binding.drawLayout, binding.toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        binding.navView.setNavigationItemSelectedListener(this);
        binding.drawLayout.addDrawerListener(toggle);
        toggle.syncState();

    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
        updateNavHead();
    }

    private void updateNavHead() {
        View Header = binding.navView.getHeaderView(0);
        CircleImageView profile = Header.findViewById(R.id.profile);
        TextView name = Header.findViewById(R.id.name);
        TextView phoneNumber = Header.findViewById(R.id.phoneNumber);

        Constants.databaseReference().child(Constants.USER).child(Constants.auth().getCurrentUser().getUid())
                .get().addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()){
                        UserModel userModel = dataSnapshot.getValue(UserModel.class);
                        name.setText(userModel.getName());
                        phoneNumber.setText(userModel.getNumber());
                        if (userModel.getImage().isEmpty()) {
                            Glide.with(this).load(
                                    new AvatarGenerator.AvatarBuilder(this)
                                            .setLabel(userModel.getName().toUpperCase(Locale.ROOT))
                                            .setAvatarSize(70)
                                            .setBackgroundColor(getRandomColor())
                                            .setTextSize(13)
                                            .toCircle()
                                            .build()
                            ).into(profile);
                        } else Glide.with(this).load(userModel.getImage()).into(profile);

                    }
                });

    }

    public static int getRandomColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        return Color.rgb(r, g, b);
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
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Handle the submission of the search query
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle changes in the search query text
                return true;
            }
        });

        return true;
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