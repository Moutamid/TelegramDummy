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
import com.moutamid.telegramdummy.databinding.ActivityEditContactBinding;
import com.moutamid.telegramdummy.models.ChatModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditContactActivity extends AppCompatActivity {
    ActivityEditContactBinding binding;
    private static final int PICK_IMAGE_REQUEST = 1001;
    Uri imageUri = Uri.EMPTY;
    String status = "Online";
    Calendar currentTime = Calendar.getInstance();
    int currentHour = currentTime.get(Calendar.HOUR_OF_DAY);
    int currentMinute = currentTime.get(Calendar.MINUTE);
    MaterialTimePicker materialTimePicker;
    ChatModel passed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.adjustFontScale(this);
        binding = ActivityEditContactBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        passed = (ChatModel) Stash.getObject(Constants.PASS_USER, ChatModel.class);

        binding.back.setOnClickListener(v -> onBackPressed());

        updateView();

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

        final int[] color = {passed.getColor()};

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
                        Glide.with(EditContactActivity.this)
                                .load(
                                        new AvatarGenerator.AvatarBuilder(EditContactActivity.this)
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
                        Glide.with(EditContactActivity.this).load(R.drawable.add_image).into(binding.profile);
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
                int index = retrieveIndex(list);
                String name = binding.firstname.getEditText().getText().toString() + (binding.lastName.getEditText().getText().toString().isEmpty() ? "" : " " + binding.lastName.getEditText().getText().toString());
                ChatModel chatModel = new ChatModel(
                        binding.phoneNumber.getEditText().getText().toString().trim(), binding.countryPick.getSelectedCountryCodeAsInt(),
                        name.trim(), imageUri.toString(), passed.getLastMessage(),
                        passed.getTimestamp(), status.trim(), color[0]
                );
                list.set(index, chatModel);
                Stash.put(Constants.USER, list);
                Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });

    }

    private void updateView() {
        String[] name = passed.getName().split(" ");
        binding.firstname.getEditText().setText(name[0]);
        if (name.length > 1) {
            binding.lastName.getEditText().setText(name[1]);
        }
        status = passed.getStatus();
        if (status.equalsIgnoreCase("online")) {
            binding.online.setChecked(true);
        } else if (status.equalsIgnoreCase("typing...")) {
            binding.typing.setChecked(true);
        } else if (status.startsWith("last seen")) {
            binding.time.setChecked(true);
            binding.status.setVisibility(View.VISIBLE);
            binding.status.getEditText().setText(status);
        } else {
            binding.custom.setChecked(true);
            binding.status.setVisibility(View.VISIBLE);
            binding.status.getEditText().setText(status);
        }
        binding.phoneNumber.getEditText().setText(passed.getId());
        Glide.with(EditContactActivity.this)
                .load(passed.getImage()).placeholder(
                        new AvatarGenerator.AvatarBuilder(EditContactActivity.this)
                                .setLabel(passed.getName().trim().toUpperCase(Locale.ROOT))
                                .setAvatarSize(70)
                                .setBackgroundColor(passed.getColor())
                                .setTextSize(13)
                                .toCircle()
                                .build()
                ).into(binding.profile);
        binding.countryPick.setDefaultCountryUsingPhoneCode(passed.getCode());
    }

    private int retrieveIndex(ArrayList<ChatModel> list) {
        for (int i = 0; i < list.size(); i++) {
            ChatModel model = list.get(i);
            if (model.getId().equals(passed.getId())) {
                return i;
            }
        }
        return -1;
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
        Stash.clear(Constants.PASS_USER);
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