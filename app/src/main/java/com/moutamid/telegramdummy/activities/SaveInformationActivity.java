package com.moutamid.telegramdummy.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.moutamid.telegramdummy.MainActivity;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.databinding.ActivitySaveInformationBinding;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.Locale;
import java.util.UUID;

public class SaveInformationActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    ActivitySaveInformationBinding binding;
    String number;
    Uri imageUri = Uri.EMPTY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.adjustFontScale(this);
        binding = ActivitySaveInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        number = getIntent().getStringExtra("number");

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.profile.setOnClickListener(v -> {
            if (Constants.checkPermission(this)) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(Intent.createChooser(intent, "Pick Image"), PICK_IMAGE_REQUEST);
            } else {
                String[] permissions;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissions = new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    };
                    shouldShowRequestPermissionRationale(permissions[0]);
                    shouldShowRequestPermissionRationale(permissions[1]);
                    shouldShowRequestPermissionRationale(permissions[2]);
                } else {
                    permissions = new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                    };
                    shouldShowRequestPermissionRationale(permissions[0]);
                }
                ActivityCompat.requestPermissions(this, permissions, 222);
            }
        });

        final int[] color = {Constants.getRandomColor()};

        binding.firstname.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (imageUri == Uri.EMPTY) {
                    if (!s.toString().isEmpty()) {
                        binding.lastName.setEnabled(true);
                        Glide.with(SaveInformationActivity.this)
                                .load(
                                        new AvatarGenerator.AvatarBuilder(SaveInformationActivity.this)
                                                .setLabel(s.toString().trim().toUpperCase(Locale.ROOT))
                                                .setAvatarSize(70)
                                                .setBackgroundColor(color[0])
                                                .setTextSize(13)
                                                .toCircle()
                                                .build()
                                ).into(binding.profile);
                    } else {
                        binding.lastName.setEnabled(false);
                        color[0] = Constants.getRandomColor();
                        Glide.with(SaveInformationActivity.this).load(R.drawable.add_image).into(binding.profile);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.next.setOnClickListener(v -> {
            if (binding.firstname.getEditText().getText().toString().isEmpty()){
                Toast.makeText(this, "Firstname is required", Toast.LENGTH_SHORT).show();
            } else {
                String name = binding.firstname.getEditText().getText().toString() + (binding.lastName.getEditText().getText().toString().isEmpty() ? "" : " " + binding.lastName.getEditText().getText().toString());
                UserModel userModel = new UserModel(UUID.randomUUID().toString(), name.trim(), number, imageUri.toString(), color[0]);
                Stash.put(Constants.STASH_USER, userModel);
                startActivity(new Intent(this, ChatListActivity.class));
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(binding.profile);
        }
    }
}