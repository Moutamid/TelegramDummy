package com.moutamid.telegramdummy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;
import com.moutamid.telegramdummy.activities.SaveInformationActivity;
import com.moutamid.telegramdummy.databinding.ActivityMainBinding;
import com.moutamid.telegramdummy.utili.Constants;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Constants.adjustFontScale(MainActivity.this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        String code = binding.countryPick.getSelectedCountryCodeWithPlus();
        binding.phoneNumber.getEditText().setText(code);

        binding.countryPick.setOnCountryChangeListener(() -> {
            String code1 = binding.countryPick.getSelectedCountryCodeWithPlus();
            binding.phoneNumber.getEditText().setText(code1);
        });

        binding.next.setOnClickListener(v -> {
            String number = binding.phoneNumber.getEditText().getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, SaveInformationActivity.class);
            intent.putExtra("number", number);
            startActivity(intent);
            finish();
        });
    }
}