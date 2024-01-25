package com.moutamid.telegramdummy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.fxn.stash.Stash;
import com.moutamid.telegramdummy.activities.ChatListActivity;
import com.moutamid.telegramdummy.models.UserModel;
import com.moutamid.telegramdummy.utili.Constants;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        new Handler().postDelayed(() -> {
            UserModel user = (UserModel) Stash.getObject(Constants.STASH_USER, UserModel.class);
            if (user != null ) {
                startActivity(new Intent(SplashScreenActivity.this, ChatListActivity.class));
                finish();
            } else {
                startActivity(new Intent(SplashScreenActivity.this, MainActivity.class));
                finish();
            }
        }, 1000);

    }
}