package com.assistance.hashtagapp.LoginSignUp;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.assistance.hashtagapp.R;

import maes.tech.intentanim.CustomIntent;

public class ToSignInActivity extends AppCompatActivity {

    CardView signInCard;
    ConstraintLayout signIn;
    TextView heading1, heading2, content1, signUp;
    ImageView signinIllustration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_sign_up);

        getWindow().setExitTransition(null);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));

        initViews();
        setActionOnViews();
    }

    private void initViews() {
        heading1 = findViewById(R.id.heading1);
        heading2 = findViewById(R.id.heading2);
        content1 = findViewById(R.id.content1);
        signInCard = findViewById(R.id.sign_in_card);
        signIn = findViewById(R.id.sign_in);
        signUp = findViewById(R.id.sign_up);
        signinIllustration = findViewById(R.id.signin_illustration);
    }

    private void setActionOnViews() {
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = new Intent(ToSignInActivity.this, SignInActivity.class);

                Pair[] pairs = new Pair[6];
                pairs[0] = new Pair<View, String>(heading1, "heading1_transition");
                pairs[1] = new Pair<View, String>(heading2, "heading2_transition");
                pairs[2] = new Pair<View, String>(content1, "content1_transition");
                pairs[3] = new Pair<View, String>(signInCard, "signin_signup_card_transition");
                pairs[4] = new Pair<View, String>(signUp, "signin_signup_transition");
                pairs[5] = new Pair<View, String>(signinIllustration, "signin_signup_illustration_transition");

                ActivityOptions activityOptions = ActivityOptions.makeSceneTransitionAnimation(ToSignInActivity.this, pairs);

                startActivity(signInIntent, activityOptions.toBundle());
                finish();
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ToSignInActivity.this, ToSignUpActivity.class));
                CustomIntent.customType(ToSignInActivity.this, "fadein-to-fadeout");
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
