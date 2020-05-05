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
import android.widget.TextView;

import maes.tech.intentanim.CustomIntent;

public class ToSignInActivity extends AppCompatActivity {

    CardView signInCard;
    ConstraintLayout signIn;
    TextView heading1, heading2, content1, signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_sign_in);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        initViews();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(ToSignInActivity.this, SignInActivity.class);

                Pair[] pairs = new Pair[5];
                pairs[0] = new Pair<View, String>(heading1, "heading1_transition");
                pairs[1] = new Pair<View, String>(heading2, "heading2_transition");
                pairs[2] = new Pair<View, String>(content1, "content1_transition");
                pairs[3] = new Pair<View, String>(signInCard, "signin_signup_card_transition");
                pairs[4] = new Pair<View, String>(signUp, "signin_signup_transition");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(ToSignInActivity.this, pairs);

                startActivity(signInIntent, activityOptions.toBundle());
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToSignInActivity.this, ToSignUpActivity.class));
                CustomIntent.customType(ToSignInActivity.this, "fadein-to-fadeout");
            }
        });
    }

    private void initViews() {
        heading1 = findViewById(R.id.heading1);
        heading2 = findViewById(R.id.heading2);
        content1 = findViewById(R.id.content1);
        signInCard = findViewById(R.id.sign_in_card);
        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.sign_up);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
