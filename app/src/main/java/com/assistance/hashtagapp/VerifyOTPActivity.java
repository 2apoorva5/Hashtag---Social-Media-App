package com.assistance.hashtagapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.assistance.hashtagapp.Common.Common;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mukesh.OnOtpCompletionListener;
import com.mukesh.OtpView;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class VerifyOTPActivity extends AppCompatActivity {

    ImageView backArrow, verifyOTP;
    TextView mobileNumber, resend;
    OtpView otpView;
    ConstraintLayout verify;
    CardView verifyCard;

    Timer timer;
    int count = 60;
    AlertDialog progressDialog;

    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallback;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        initViews();
        initFirebase();

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        progressDialog = new SpotsDialog.Builder().setContext(VerifyOTPActivity.this)
                .setMessage("Verifying Mobile Number..")
                .setCancelable(false)
                .setTheme(R.style.SpotsDialog)
                .build();

        KeyboardVisibilityEvent.setEventListener(VerifyOTPActivity.this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(!isOpen)
                {
                    otpView.clearFocus();
                }
            }
        });

        mobileNumber.setText(String.format("+91%s", Common.signUpMobile));

        sendOTP();

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                VerifyOTPActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(count == 0)
                        {
                            resend.setText("Resend OTP");
                            resend.setAlpha(1.0f);
                            resend.setEnabled(true);
                            resend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    resendOTP();
                                    resend.setEnabled(false);
                                    resend.setAlpha(0.5f);
                                    count = 60;
                                }
                            });
                        }
                        else
                        {
                            resend.setText(String.format("Resend OTP in %d", count));
                            resend.setAlpha(0.5f);
                            resend.setEnabled(false);
                            count--;
                        }
                    }
                });
            }
        },0,1000);


        otpView.setOtpCompletionListener(new OnOtpCompletionListener() {
            @Override
            public void onOtpCompleted(final String otp) {
                if(otp.length() == 6)
                {
                    verify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UIUtil.hideKeyboard(VerifyOTPActivity.this);
                            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otp);
                            progressDialog.show();
                            signInWithPhoneAuthCredential(credential);
                        }
                    });
                }
                else
                {
                    verify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UIUtil.hideKeyboard(VerifyOTPActivity.this);
                            if(otp.isEmpty() || otp.length() < 6)
                            {
                                YoYo.with(Techniques.RubberBand)
                                        .duration(700)
                                        .repeat(1)
                                        .playOn(otpView);
                            }
                        }
                    });
                }
            }
        });
    }

    private void initViews() {
        backArrow = findViewById(R.id.arrow_back);
        mobileNumber = findViewById(R.id.mobile_number);
        resend = findViewById(R.id.resend_otp);
        verifyOTP = findViewById(R.id.verify_otp);
        otpView = findViewById(R.id.otp_view);
        verify = findViewById(R.id.verify);
        verifyCard = findViewById(R.id.verify_card);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void sendOTP()
    {
        mCallback = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    progressDialog.dismiss();
                    Alerter.create(VerifyOTPActivity.this)
                            .setText("Whoops! Seems like you've got an invalid code!")
                            .setTextAppearance(R.style.ErrorAlert)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.error)
                            .setDuration(3000)
                            .enableSwipeToDismiss()
                            .enableIconPulse(true)
                            .enableVibration(true)
                            .disableOutsideTouch()
                            .enableProgress(true)
                            .setProgressColorInt(getResources().getColor(android.R.color.white))
                            .show();
                    return;
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    progressDialog.dismiss();
                    Alerter.create(VerifyOTPActivity.this)
                            .setText("Too many requests at the moment. Try again after some time!")
                            .setTextAppearance(R.style.InfoAlert)
                            .setBackgroundColorRes(R.color.infoColor)
                            .setIcon(R.drawable.info)
                            .setDuration(3000)
                            .enableSwipeToDismiss()
                            .enableIconPulse(true)
                            .enableVibration(true)
                            .disableOutsideTouch()
                            .enableProgress(true)
                            .setProgressColorInt(getResources().getColor(android.R.color.white))
                            .show();
                    return;
                }
            }

            @Override
            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + Common.signUpMobile,
                60,
                TimeUnit.SECONDS,
                VerifyOTPActivity.this,
                mCallback);
    }

    private void resendOTP()
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + Common.signUpMobile,
                60,
                TimeUnit.SECONDS,
                VerifyOTPActivity.this,
                mCallback, mResendToken);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(VerifyOTPActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            FirebaseUser user = task.getResult().getUser();
                            AuthCredential authCredential = EmailAuthProvider.getCredential(Common.signUpEmail, Common.signUpPassword);
                            assert user != null;
                            user.linkWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {
                                        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                                        Map<String, Object> map = new HashMap<>();
                                        map.put("Name", Common.signUpName);
                                        map.put("Username", "");
                                        map.put("Bio", "");
                                        map.put("Gender", "");
                                        map.put("Date of Birth", "");
                                        map.put("Email", Common.signUpEmail);
                                        map.put("Mobile", Common.signUpMobile);
                                        map.put("Profile Pic", "");

                                        firestore.collection("Users").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                if(task.isSuccessful())
                                                {
                                                    progressDialog.dismiss();
                                                    startActivity(new Intent(VerifyOTPActivity.this, EditProfileActivity.class));
                                                    CustomIntent.customType(VerifyOTPActivity.this, "left-to-right");
                                                    finish();
                                                }
                                                else
                                                {
                                                    progressDialog.dismiss();
                                                    Alerter.create(VerifyOTPActivity.this)
                                                            .setText("Whoops! There was some error signing you up!")
                                                            .setTextAppearance(R.style.ErrorAlert)
                                                            .setBackgroundColorRes(R.color.errorColor)
                                                            .setIcon(R.drawable.error)
                                                            .setDuration(3000)
                                                            .enableSwipeToDismiss()
                                                            .enableIconPulse(true)
                                                            .enableVibration(true)
                                                            .disableOutsideTouch()
                                                            .enableProgress(true)
                                                            .setProgressColorInt(getResources().getColor(android.R.color.white))
                                                            .show();
                                                    return;
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Alerter.create(VerifyOTPActivity.this)
                                                .setText("Seems like this mobile number has already been used!")
                                                .setTextAppearance(R.style.InfoAlert)
                                                .setBackgroundColorRes(R.color.infoColor)
                                                .setIcon(R.drawable.info)
                                                .setDuration(3000)
                                                .enableSwipeToDismiss()
                                                .enableIconPulse(true)
                                                .enableVibration(true)
                                                .disableOutsideTouch()
                                                .enableProgress(true)
                                                .setProgressColorInt(getResources().getColor(android.R.color.white))
                                                .show();
                                        return;
                                    }
                                }
                            });
                        }
                        else
                        {
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException)
                            {
                                progressDialog.dismiss();
                                Alerter.create(VerifyOTPActivity.this)
                                        .setText("Whoops! Seems like you've got an invalid code!")
                                        .setTextAppearance(R.style.ErrorAlert)
                                        .setBackgroundColorRes(R.color.errorColor)
                                        .setIcon(R.drawable.error)
                                        .setDuration(3000)
                                        .enableSwipeToDismiss()
                                        .enableIconPulse(true)
                                        .enableVibration(true)
                                        .disableOutsideTouch()
                                        .enableProgress(true)
                                        .setProgressColorInt(getResources().getColor(android.R.color.white))
                                        .show();
                                return;
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        YoYo.with(Techniques.Shake)
                .duration(700)
                .repeat(3)
                .playOn(verifyOTP);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(VerifyOTPActivity.this, "fadein-to-fadeout");
    }
}
