package com.assistance.hashtagapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import maes.tech.intentanim.CustomIntent;

public class ToSignUpActivity extends AppCompatActivity {

    CardView signUpCard;
    ConstraintLayout signUp;
    TextView heading1, heading2, content1, signIn;
    ImageView signupIllustration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_sign_up);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        initViews();
        setActionOnViews();
    }

    private void initViews() {
        heading1 = findViewById(R.id.heading1);
        heading2 = findViewById(R.id.heading2);
        content1 = findViewById(R.id.content1);
        signUpCard = findViewById(R.id.sign_up_card);
        signUp = findViewById(R.id.sign_up);
        signIn = findViewById(R.id.sign_in);
        signupIllustration = findViewById(R.id.signup_illustration);
    }

    private void setActionOnViews() {
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(ToSignUpActivity.this, SignUpActivity.class);

                Pair[] pairs = new Pair[6];
                pairs[0] = new Pair<View, String>(heading1, "heading1_transition");
                pairs[1] = new Pair<View, String>(heading2, "heading2_transition");
                pairs[2] = new Pair<View, String>(content1, "content1_transition");
                pairs[3] = new Pair<View, String>(signUpCard, "signin_signup_card_transition");
                pairs[4] = new Pair<View, String>(signIn, "signin_signup_transition");
                pairs[5] = new Pair<View, String>(signupIllustration, "signin_signup_illustration_transition");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(ToSignUpActivity.this, pairs);

                startActivity(signUpIntent, activityOptions.toBundle());
            }
        });

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToSignUpActivity.this, ToSignInActivity.class));
                CustomIntent.customType(ToSignUpActivity.this, "fadein-to-fadeout");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
