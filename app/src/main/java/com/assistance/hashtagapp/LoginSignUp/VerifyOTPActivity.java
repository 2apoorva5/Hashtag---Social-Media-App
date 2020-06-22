package com.assistance.hashtagapp.LoginSignUp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.assistance.hashtagapp.R;
import com.chaos.view.PinView;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskExecutors;
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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;
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

    ImageView close, verifyOTP;
    TextView mobileNumber, resendOtp;
    PinView otpPinView;
    ConstraintLayout verify;
    CardView verifyCard;

    Timer timer;
    int count = 60;

    PhoneAuthProvider.ForceResendingToken resendingToken;

    AlertDialog progressDialog;

    FirebaseAuth firebaseAuth;
    CollectionReference userRef;

    String name, email, mobile, password;
    String codeBySystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorAccent));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        progressDialog = new SpotsDialog.Builder().setContext(VerifyOTPActivity.this)
                .setMessage("Verifying Mobile...")
                .setCancelable(false)
                .setTheme(R.style.SpotsDialog)
                .build();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        email = intent.getStringExtra("email");
        mobile = intent.getStringExtra("mobile");
        password = intent.getStringExtra("password");

        initViews();
        initFirebase();
        setActionOnViews();

        KeyboardVisibilityEvent.setEventListener(VerifyOTPActivity.this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(!isOpen)
                {
                    otpPinView.clearFocus();
                }
            }
        });

        sendVerificationCodeToUser(mobile);
    }

    private void initViews() {
        close = findViewById(R.id.close);
        mobileNumber = findViewById(R.id.mobile_number);
        resendOtp = findViewById(R.id.resend_otp);
        verifyOTP = findViewById(R.id.verify_otp);
        otpPinView = findViewById(R.id.verify_otp_pinview);
        verify = findViewById(R.id.verify);
        verifyCard = findViewById(R.id.verify_card);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("Users");
    }

    private void setActionOnViews() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mobileNumber.setText(String.format(mobile));

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                VerifyOTPActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(count == 0)
                        {
                            resendOtp.setText("Resend OTP");
                            resendOtp.setAlpha(1.0f);
                            resendOtp.setEnabled(true);
                            resendOtp.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    resendOTP();
                                    resendOtp.setEnabled(false);
                                    resendOtp.setAlpha(0.5f);
                                    count = 60;
                                }
                            });
                        }
                        else
                        {
                            resendOtp.setText(String.format("Resend OTP in %d", count));
                            resendOtp.setAlpha(0.5f);
                            resendOtp.setEnabled(false);
                            count--;
                        }
                    }
                });
            }
        },0,1000);

        verify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UIUtil.hideKeyboard(VerifyOTPActivity.this);

                String codeEnteredByUser = otpPinView.getText().toString();
                if(!codeEnteredByUser.isEmpty() && codeEnteredByUser.length() == 6) {
                    verifyCode(codeEnteredByUser);
                } else {
                    YoYo.with(Techniques.RubberBand)
                            .duration(700)
                            .repeat(1)
                            .playOn(otpPinView);
                }
            }
        });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks =
            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                    super.onCodeSent(s, forceResendingToken);
                    resendingToken = forceResendingToken;
                    codeBySystem = s;
                }

                @Override
                public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                    String code = phoneAuthCredential.getSmsCode();
                    if(code!=null) {
                        otpPinView.setText(code);
                        verifyCode(code);
                    }
                }

                @Override
                public void onVerificationFailed(@NonNull FirebaseException e) {
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        progressDialog.dismiss();
                        Alerter.create(VerifyOTPActivity.this)
                                .setText("Whoa! Seems like you've got an invalid code!")
                                .setTextAppearance(R.style.ErrorAlert)
                                .setBackgroundColorRes(R.color.errorColor)
                                .setIcon(R.drawable.ic_error)
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
                                .setIcon(R.drawable.ic_info)
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
            };

    private void sendVerificationCodeToUser(String mobile) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
        );
    }

    private void resendOTP()
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobile,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks, resendingToken);
    }

    private void verifyCode(String code) {
        progressDialog.show();
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(codeBySystem, code);
        signInUsingCredential(credential);
    }

    private void signInUsingCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            FirebaseUser user = task.getResult().getUser();
                            AuthCredential authCredential = EmailAuthProvider.getCredential(email, password);
                            assert user != null;
                            user.linkWithCredential(authCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful())
                                    {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("Name", name);
                                        map.put("Username", "");
                                        map.put("About", "");
                                        map.put("Gender", "");
                                        map.put("DOB", "");
                                        map.put("Email", email);
                                        map.put("Mobile", mobile);
                                        map.put("ProfilePic", "");

                                        userRef.document(email).set(map)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressDialog.dismiss();
                                                        timer.cancel();
                                                        verify.setEnabled(false);
                                                        MaterialDialog materialDialog = new MaterialDialog.Builder(VerifyOTPActivity.this)
                                                                .setTitle("Registered - Set Username!")
                                                                .setMessage("Cool! You've been registered with us. Now, set up a username to get going.")
                                                                .setAnimation(R.raw.confirm)
                                                                .setCancelable(false)
                                                                .setPositiveButton("Proceed", R.drawable.ic_material_dialog_okay, new MaterialDialog.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int which) {
                                                                        dialogInterface.dismiss();
                                                                        startActivity(new Intent(VerifyOTPActivity.this, EditProfileActivity.class));
                                                                        CustomIntent.customType(VerifyOTPActivity.this, "fadein-to-fadeout");
                                                                        finish();
                                                                    }
                                                                })
                                                                .setNegativeButton("Cancel", R.drawable.ic_material_dialog_cancel, new MaterialDialog.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialogInterface, int which) {
                                                                        dialogInterface.dismiss();
                                                                        finishAffinity();
                                                                    }
                                                                })
                                                                .build();
                                                        materialDialog.show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        progressDialog.dismiss();
                                                        Alerter.create(VerifyOTPActivity.this)
                                                                .setText("Whoa! That ran into some error. Could be a network issue.")
                                                                .setTextAppearance(R.style.ErrorAlert)
                                                                .setBackgroundColorRes(R.color.errorColor)
                                                                .setIcon(R.drawable.ic_error)
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
                                                });
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Alerter.create(VerifyOTPActivity.this)
                                                .setText("Seems like this mobile number has already been used. Try another!")
                                                .setTextAppearance(R.style.InfoAlert)
                                                .setBackgroundColorRes(R.color.infoColor)
                                                .setIcon(R.drawable.ic_info)
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
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Alerter.create(VerifyOTPActivity.this)
                                            .setText("Whoa! That ran into some error. Could be a network issue.")
                                            .setTextAppearance(R.style.ErrorAlert)
                                            .setBackgroundColorRes(R.color.errorColor)
                                            .setIcon(R.drawable.ic_error)
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
                            });
                        } else {
                            if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                progressDialog.dismiss();
                                Alerter.create(VerifyOTPActivity.this)
                                        .setText("Whoa! Verification Failed. Try Again!")
                                        .setTextAppearance(R.style.ErrorAlert)
                                        .setBackgroundColorRes(R.color.errorColor)
                                        .setIcon(R.drawable.ic_error)
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
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
