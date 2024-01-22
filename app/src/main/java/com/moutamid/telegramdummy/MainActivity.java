package com.moutamid.telegramdummy;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hbb20.CountryCodePicker;
import com.moutamid.telegramdummy.activities.OTPActivity;
import com.moutamid.telegramdummy.databinding.ActivityMainBinding;
import com.moutamid.telegramdummy.utili.Constants;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Constants.checkApp(this);

        String code = binding.countryPick.getSelectedCountryCodeWithPlus();
        binding.phoneNumber.getEditText().setText(code);

        binding.countryPick.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                String code = binding.countryPick.getSelectedCountryCodeWithPlus();
                binding.phoneNumber.getEditText().setText(code);
            }
        });

        binding.next.setOnClickListener(v -> {
            String number = binding.phoneNumber.getEditText().getText().toString().trim();
            Intent intent = new Intent(MainActivity.this, OTPActivity.class);
            intent.putExtra("number", number);
            startActivity(intent);
        });
    }
}