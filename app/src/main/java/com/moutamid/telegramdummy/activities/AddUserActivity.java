package com.moutamid.telegramdummy.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.avatarfirst.avatargenlib.AvatarGenerator;
import com.bumptech.glide.Glide;
import com.fxn.stash.Stash;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.moutamid.telegramdummy.R;
import com.moutamid.telegramdummy.databinding.ActivityAddUserBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddUserActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    ActivityAddUserBinding binding;
    Uri imageUri = Uri.EMPTY;
    String status = "Online";
    Calendar currentTime = Calendar.getInstance();
    int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
    int currentMinute = currentTime.get(Calendar.MINUTE);
    MaterialTimePicker materialTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.adjustFontScale(this);
        binding = ActivityAddUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String code = binding.countryPick.getSelectedCountryCodeWithPlus();
        binding.phoneNumber.getEditText().setText(code);

        binding.back.setOnClickListener(v -> onBackPressed());

        binding.countryPick.setOnCountryChangeListener(() -> {
            String code1 = binding.countryPick.getSelectedCountryCodeWithPlus();
            binding.phoneNumber.getEditText().setText(code1);
        });

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

        materialTimePicker = new MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_24H)
                .setHour(currentHour)
                .setMinute(currentMinute)
                .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                .setTitleText("Select Time")
                .build();

        binding.firstname.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (imageUri == Uri.EMPTY) {
                    if (!s.toString().isEmpty()) {
                        binding.lastName.setEnabled(true);
                        Glide.with(AddUserActivity.this)
                                .load(
                                        new AvatarGenerator.AvatarBuilder(AddUserActivity.this)
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
                        Glide.with(AddUserActivity.this).load(R.drawable.add_image).into(binding.profile);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.online.setOnClickListener(v -> {
            binding.status.setVisibility(View.GONE);
            status = "Online";
        });
        binding.typing.setOnClickListener(v -> {
            binding.status.setVisibility(View.GONE);
            status = "typing...";
        });
        binding.time.setOnClickListener(v -> {
            binding.status.setVisibility(View.VISIBLE);
            materialTimePicker.show(getSupportFragmentManager(), "tag");
        });
        binding.custom.setOnClickListener(v -> {
            binding.status.setVisibility(View.VISIBLE);
            String s = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date().getTime());
            status = "Today at " + s;
            binding.status.getEditText().setText(status);
        });

        materialTimePicker.addOnPositiveButtonClickListener(v -> {
            int selectedHour = materialTimePicker.getHour();
            int selectedMinute = materialTimePicker.getMinute();
            String time = selectedHour + ":" + selectedMinute;
            status = "last seen at " + time;
            binding.status.getEditText().setText(status);
        });

        materialTimePicker.addOnNegativeButtonClickListener(v -> {
            String s = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date().getTime());
            status = "last seen at " + s;
            binding.status.getEditText().setText(status);
        });

        binding.status.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                status = s.toString();
                if (!s.toString().startsWith("last seen")) {
                    binding.custom.setChecked(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.next.setOnClickListener(v -> {
            if (valid()) {
                ArrayList<ChatModel> list = Stash.getArrayList(Constants.USER, ChatModel.class);
                String name = binding.firstname.getEditText().getText().toString() + (binding.lastName.getEditText().getText().toString().isEmpty() ? "" : " " + binding.lastName.getEditText().getText().toString());
                ChatModel chatModel = new ChatModel(
                        binding.phoneNumber.getEditText().getText().toString().trim(), binding.countryPick.getSelectedCountryCodeAsInt(),
                        name.trim(), imageUri.toString(), name + " joined Telegram",
                        new Date().getTime(), status.trim(), color[0]
                );
                boolean check = false;
                for (ChatModel model : list) {
                    if (model.getId().equals(chatModel.getId())) {
                        check = true;
                        break;
                    }
                }

                if (check) {
                    Toast.makeText(this, "Contact already found in history", Toast.LENGTH_SHORT).show();
                } else {
                    list.add(chatModel);
                    Stash.put(Constants.USER, list);
                    Toast.makeText(this, "Contact Saved", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }
        });

    }

    private boolean valid() {
        if (binding.firstname.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Firstname is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (binding.phoneNumber.getEditText().getText().toString().isEmpty()) {
            Toast.makeText(this, "Phone Number is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, ChatListActivity.class));
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