package com.moutamid.telegramdummy.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.moutamid.telegramdummy.databinding.ActivitySaveInformationBinding;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.UUID;

public class SaveInformationActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1001;
    ActivitySaveInformationBinding binding;
    String number;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySaveInformationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        number = getIntent().getStringExtra("number");

        binding.firstname.getEditText().requestFocus();

        binding.profile.setOnClickListener(v -> {
            if (checkPermission()) {
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

        binding.next.setOnClickListener(v -> {
            binding.username.setErrorEnabled(false);
            binding.firstname.setErrorEnabled(false);
            if (!binding.username.getEditText().getText().toString().isEmpty()) {
                binding.username.setErrorEnabled(true);
                binding.username.setError("Username is required");
            } else if (!binding.firstname.getEditText().getText().toString().isEmpty()) {
                binding.firstname.setErrorEnabled(true);
                binding.firstname.setError("First name is required");
            } else {
                Constants.showDialog();
                String username = binding.username.getEditText().getText().toString().trim();
                checkUsername(username);
            }
        });

    }

    private void checkUsername(String username) {
        Constants.databaseReference().child(Constants.USER).get()
                .addOnSuccessListener(dataSnapshot -> {
                    if (dataSnapshot.exists()) {
                        boolean check = false;
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            if (userModel.getUsername().equals(username)){
                                check = true;
                                break;
                            }
                        }
                        if (check){
                            binding.username.setErrorEnabled(true);
                            binding.username.setError("Username is not available");
                            Toast.makeText(this, "Try a different username", Toast.LENGTH_SHORT).show();
                        } else {
                            if (imageUri != null) {
                                uploadImage();
                            } else uploadData("");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void uploadData(String image) {
        boolean b = !binding.lastName.getEditText().getText().toString().isEmpty();
        String fullName = binding.firstname.getEditText().getText().toString().trim() + " " + binding.lastName.getEditText().getText().toString().trim();
        String name = b ? fullName : binding.firstname.getEditText().getText().toString().trim();
        UserModel userModel = new UserModel(Constants.auth().getCurrentUser().getUid(), binding.username.getEditText().getText().toString().trim(), name, number, image);
        Constants.databaseReference().child(Constants.USER).child(userModel.getID()).setValue(userModel)
                .addOnSuccessListener(unused -> {
                    Constants.dismissDialog();
                    startActivity(new Intent(SaveInformationActivity.this, ChatListActivity.class));
                    finish();
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadImage() {
        Constants.storageReference(Constants.auth().getCurrentUser().getUid()).child("profileImages").child(UUID.randomUUID().toString())
                .putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        uploadData(uri.toString());
                    });
                }).addOnFailureListener(e -> {
                    Constants.dismissDialog();
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
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