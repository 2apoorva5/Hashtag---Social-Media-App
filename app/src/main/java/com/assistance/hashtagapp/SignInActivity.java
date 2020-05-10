package com.assistance.hashtagapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Pair;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.List;

import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class SignInActivity extends AppCompatActivity {

    ImageView close, signinIllustration;
    EditText emailOrMobileField, passwordField;
    MaterialCheckBox showPassword;
    TextView heading1, heading2, content1, content2, content3, forgotPassword, termOfServices, privacyPolicy, signUp;
    ConstraintLayout signIn;
    CardView emailOrMobileCard, passwordCard, signInCard;

    FirebaseAuth firebaseAuth;
    CollectionReference userRef;

    AlertDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        getWindow().setEnterTransition(null);
        getWindow().setExitTransition(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        initViews();
        initFirebase();
        setActionOnViews();

        progressDialog = new SpotsDialog.Builder().setContext(SignInActivity.this)
                .setMessage("Logging you in..")
                .setCancelable(false)
                .setTheme(R.style.SpotsDialog)
                .build();

        KeyboardVisibilityEvent.setEventListener(SignInActivity.this, new KeyboardVisibilityEventListener() {
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(!isOpen)
                {
                    emailOrMobileField.clearFocus();
                    passwordField.clearFocus();
                }
            }
        });
    }

    private void initViews() {
        close = findViewById(R.id.close);
        heading1 = findViewById(R.id.heading1);
        heading2 = findViewById(R.id.heading2);
        signinIllustration = findViewById(R.id.signin_illustration);
        content1 = findViewById(R.id.content1);
        content2 = findViewById(R.id.content2);
        content3 = findViewById(R.id.content3);
        emailOrMobileField = findViewById(R.id.email_or_mobile_field);
        passwordField = findViewById(R.id.password_field);
        showPassword = findViewById(R.id.show_password);
        forgotPassword = findViewById(R.id.forgot_password);
        termOfServices = findViewById(R.id.terms_of_service);
        privacyPolicy = findViewById(R.id.privacy_policy);
        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.sign_up);
        emailOrMobileCard = findViewById(R.id.email_or_mobile_card);
        passwordCard = findViewById(R.id.password_card);
        signInCard = findViewById(R.id.sign_in_card);
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

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(SignInActivity.this, SignUpActivity.class);

                Pair[] pairs = new Pair[13];
                pairs[0] = new Pair<View, String>(heading1, "heading1_transition");
                pairs[1] = new Pair<View, String>(heading2, "heading2_transition");
                pairs[2] = new Pair<View, String>(content1, "content1_transition");
                pairs[3] = new Pair<View, String>(content2, "content2_transition");
                pairs[4] = new Pair<View, String>(content3, "content3_transition");
                pairs[5] = new Pair<View, String>(emailOrMobileCard, "email_card_transition");
                pairs[6] = new Pair<View, String>(passwordCard, "password_card_transition");
                pairs[7] = new Pair<View, String>(termOfServices, "tos_transition");
                pairs[8] = new Pair<View, String>(privacyPolicy, "pp_transition");
                pairs[9] = new Pair<View, String>(showPassword, "show_password_transition");
                pairs[10] = new Pair<View, String>(signInCard, "signin_signup_card_transition");
                pairs[11] = new Pair<View, String>(signUp, "signin_signup_transition");
                pairs[12] = new Pair<View, String>(signinIllustration, "signin_signup_illustration_transition");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(SignInActivity.this, pairs);

                startActivity(signUpIntent, activityOptions.toBundle());
                finish();
            }
        });

        showPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    passwordField.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                }
                else
                {
                    passwordField.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
                CustomIntent.customType(SignInActivity.this, "fadein-to-fadeout");
            }
        });

        emailOrMobileField.addTextChangedListener(signinTextWatcher);
        passwordField.addTextChangedListener(signinTextWatcher);
    }

    private TextWatcher signinTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String emailOrMobile = emailOrMobileField.getText().toString().trim();
            final String password = passwordField.getText().toString().trim();

            if(!emailOrMobile.isEmpty() && !password.isEmpty())
            {
                signIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.hideKeyboard(SignInActivity.this);
                        if(Patterns.EMAIL_ADDRESS.matcher(emailOrMobile).matches())
                        {
                            progressDialog.show();
                            login(emailOrMobile);
                        }
                        else if(emailOrMobile.matches("\\d{10}"))
                        {
                            progressDialog.show();
                            userRef.whereEqualTo("Mobile", emailOrMobile)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                                        if(documentSnapshots.isEmpty())
                                        {
                                            progressDialog.dismiss();
                                            YoYo.with(Techniques.Shake)
                                                    .duration(700)
                                                    .repeat(1)
                                                    .playOn(emailOrMobileCard);
                                            Alerter.create(SignInActivity.this)
                                                    .setText("Whoa! There's no account with that mobile number!")
                                                    .setTextAppearance(R.style.ErrorAlert)
                                                    .setBackgroundColorRes(R.color.errorColor)
                                                    .setIcon(R.drawable.error_icon)
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
                                        else
                                        {
                                            String email = documentSnapshots.get(0).get("Email").toString().trim();
                                            login(email);
                                        }
                                    }
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Alerter.create(SignInActivity.this)
                                                .setText("Whoa! That ran into some error_icon. Could be a network issue.")
                                                .setTextAppearance(R.style.ErrorAlert)
                                                .setBackgroundColorRes(R.color.errorColor)
                                                .setIcon(R.drawable.error_icon)
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
                                    Alerter.create(SignInActivity.this)
                                            .setText("Whoa! That ran into some error_icon. Could be a network issue.")
                                            .setTextAppearance(R.style.ErrorAlert)
                                            .setBackgroundColorRes(R.color.errorColor)
                                            .setIcon(R.drawable.error_icon)
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
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(emailOrMobileCard);
                            Alerter.create(SignInActivity.this)
                                    .setText("Please enter a valid Email or Mobile number!")
                                    .setTextAppearance(R.style.ErrorAlert)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.error_icon)
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
                signIn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.hideKeyboard(SignInActivity.this);
                        if(emailOrMobile.isEmpty())
                        {
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(emailOrMobileCard);
                        }
                        else if(password.isEmpty())
                        {
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(passwordCard);
                        }
                    }
                });
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void login(final String email)
    {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, passwordField.getText().toString().trim()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    userRef.document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                String alias = task.getResult().getData().get("Username").toString();
                                if(!alias.equals(""))
                                {
                                    progressDialog.dismiss();
                                    startActivity(new Intent(SignInActivity.this, NextActivity.class));
                                    CustomIntent.customType(SignInActivity.this, "fadein-to-fadeout");
                                    finish();
                                }
                                else
                                {
                                    progressDialog.dismiss();
                                    startActivity(new Intent(SignInActivity.this, EditProfileActivity.class));
                                    CustomIntent.customType(SignInActivity.this, "fadein-to-fadeout");
                                    finish();
                                }
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Alerter.create(SignInActivity.this)
                                        .setText("Whoa! That ran into some error_icon. Could be a network issue.")
                                        .setTextAppearance(R.style.ErrorAlert)
                                        .setBackgroundColorRes(R.color.errorColor)
                                        .setIcon(R.drawable.error_icon)
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
                            Alerter.create(SignInActivity.this)
                                    .setText("Whoa! That ran into some error_icon. Could be a network issue.")
                                    .setTextAppearance(R.style.ErrorAlert)
                                    .setBackgroundColorRes(R.color.errorColor)
                                    .setIcon(R.drawable.error_icon)
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
                    Alerter.create(SignInActivity.this)
                            .setText("Whoa! Seems like you've got invalid credentials!")
                            .setTextAppearance(R.style.ErrorAlert)
                            .setBackgroundColorRes(R.color.errorColor)
                            .setIcon(R.drawable.error_icon)
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
                Alerter.create(SignInActivity.this)
                        .setText("Whoa! That ran into some error_icon. Could be a network issue.")
                        .setTextAppearance(R.style.ErrorAlert)
                        .setBackgroundColorRes(R.color.errorColor)
                        .setIcon(R.drawable.error_icon)
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
