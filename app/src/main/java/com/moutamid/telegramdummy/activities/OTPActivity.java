package com.moutamid.telegramdummy.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.moutamid.telegramdummy.databinding.ActivityOtpBinding;
import com.moutamid.telegramdummy.utili.Constants;
import com.otpview.OTPListener;

import java.util.concurrent.TimeUnit;

public class OTPActivity extends AppCompatActivity {
    ActivityOtpBinding binding;
    String number,verificationId;
    private CountDownTimer countDownTimer;
    long initialTimeMillis = 60 * 1000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        number = getIntent().getStringExtra("number");
        verificationId = getIntent().getStringExtra("verificationId");

        binding.number.setText(number+"");

        binding.otpView.requestFocusOTP();

        binding.back.setOnClickListener(v -> onBackPressed());

        startTimer();

        binding.resend.setOnClickListener(v -> {
            binding.otpView.setOTP("");
            binding.timer.setVisibility(View.VISIBLE);
            binding.resend.setVisibility(View.GONE);
            startTimer();
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    number,  // Phone number to verify
                    60,            // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    null,          // Activity (for callback binding)
                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        @Override
                        public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                            OTPActivity.this.verificationId = verificationId;
                        }

                        @Override
                        public void onVerificationCompleted(PhoneAuthCredential credential) {
                           // Constants.dismissDialog();
                        }

                        @Override
                        public void onVerificationFailed(FirebaseException e) {
                            Constants.dismissDialog();
                            Log.d(Constants.TAG, "onVerificationFailed: " + e.getMessage());
                            Toast.makeText(OTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        binding.otpView.setOtpListener(new OTPListener() {
            @Override
            public void onInteractionListener() {

            }

            @Override
            public void onOTPComplete(@NonNull String s) {
                if (!s.isEmpty()){
                    checkCode(s);
                }
            }
        });

    }

    private void checkCode(String enteredCode) {
        Constants.showDialog();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, enteredCode);

        Constants.auth().signInWithCredential(credential)
                .addOnCompleteListener(OTPActivity.this, task -> {
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
        Constants.initDialog(this);
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