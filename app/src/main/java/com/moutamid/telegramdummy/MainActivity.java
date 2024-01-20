package com.moutamid.telegramdummy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.moutamid.telegramdummy.activities.OTPActivity;
import com.moutamid.telegramdummy.databinding.ActivityMainBinding;
import com.moutamid.telegramdummy.utili.Constants;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
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
            String number = binding.phoneNumber.getEditText().getText().toString();
            Constants.showDialog();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    number,  // Phone number to verify
                    60,            // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    null,          // Activity (for callback binding)
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            // The SMS verification code has been sent to the provided phone number
                            // Save the verification ID and resending token so we can use them later
                            // You can store these values in SharedPreferences or any other suitable storage
                            Constants.dismissDialog();
                            Intent intent = new Intent(MainActivity.this, OTPActivity.class);
                            intent.putExtra("verificationId", verificationId);
                            intent.putExtra("number", number);
                            startActivity(intent);
                        }

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                            Constants.dismissDialog();
                            // This callback will be invoked in two situations:
                            // 1 - Instant verification. In some cases, the phone number can be instantly verified without needing to send or enter a verification code.
                            // 2 - Auto-retrieval. On some devices, Google Play services can automatically detect the incoming verification SMS and perform verification without user action.
                            // You can handle both cases here, if needed.
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Constants.dismissDialog();
                            Log.d(Constants.TAG, "onVerificationFailed: " + e.getMessage());
                            Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this);
    }
}