package com.moutamid.telegramdummy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.moutamid.telegramdummy.databinding.ActivityOtpBinding;
import com.moutamid.telegramdummy.utili.Constants;
import com.otpview.OTPListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    ActivityOtpBinding binding;
    String number, verificationId;
    private CountDownTimer countDownTimer;
    long initialTimeMillis = 120 * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        number = getIntent().getStringExtra("number");

        binding.number.setText(number + "");

        binding.otpView.requestFocusOTP();

        binding.back.setOnClickListener(v -> onBackPressed());

        startTimer();

        binding.resend.setOnClickListener(v -> {
            binding.otpView.setOTP("");
            binding.timer.setVisibility(View.VISIBLE);
            binding.resend.setVisibility(View.GONE);
            startTimer();
            sendCode();
        });

        binding.otpView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(@NonNull String s) {
                if (!s.isEmpty()) {
                    checkCode(s);
                }
            }
        });

        sendCode();

    }

    private void sendCode() {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(Constants.auth())
                        .setPhoneNumber(number)
                        .setTimeout(120L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(mCallbacks)
                        .build();
        Constants.auth().useAppLanguage();
        // Force reCAPTCHA flow
        Constants.auth().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(false);
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential c) {
            final String code = c.getSmsCode();
            if (code != null) {
                checkCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Constants.dismissDialog();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                // Invalid request
            } else if (e instanceof FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
            } else if (e instanceof FirebaseAuthMissingActivityForRecaptchaException) {
                // reCAPTCHA verification attempted with null Activity
            }
            Toast.makeText(OTPActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(s, token);
            verificationId = s;
            binding.otpView.setOTP(s);
            Constants.dismissDialog();
        }
    };

    private void checkCode(String enteredCode) {
        try {
            Constants.showDialog();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredCode);

            Constants.auth().signInWithCredential(credential)
                    .addOnCompleteListener(OTPActivity.this, task -> {
                        Constants.dismissDialog();
                        if (task.isSuccessful()) {
                            binding.otpView.showSuccess();
                            Toast.makeText(OTPActivity.this, "Verification successful!", Toast.LENGTH_SHORT).show();
                            new Handler().postDelayed(() -> {
                                if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                                    startActivity(new Intent(OTPActivity.this, SaveInformationActivity.class).putExtra("number", number));
                                } else {
                                    startActivity(new Intent(OTPActivity.this, ChatListActivity.class));
                                    finish();
                                }
                            }, 1000);
                        } else {
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                binding.otpView.showError();
                                Toast.makeText(OTPActivity.this, "Invalid verification code", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Constants.dismissDialog();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(initialTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                // Convert milliseconds to seconds
                long seconds = millisUntilFinished / 1000;

                // Format seconds to MM:SS
                String time = String.format("%02d:%02d", seconds / 60, seconds % 60);

                // Update the TextView with the formatted time
                binding.timer.setText(time);
            }

            @Override
            public void onFinish() {
                // Handle timer finish event (e.g., show a message)
                binding.timer.setText("00:59");
                binding.timer.setVisibility(View.GONE);
                binding.resend.setVisibility(View.VISIBLE);
            }
        };

        // Start the countdown timer
        countDownTimer.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Constants.initDialog(this, "Checking Code");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the countdown timer to prevent leaks
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

}