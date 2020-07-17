package com.assistance.hashtagapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.assistance.hashtagapp.LoginSignUp.EditProfileActivity;
import com.assistance.hashtagapp.LoginSignUp.ToSignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import maes.tech.intentanim.CustomIntent;

public class SplashScreenActivity extends AppCompatActivity {

    ImageView appLogo;
    TextView appSlogan;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    CollectionReference userRef;

    Animation topAnimation, bottomAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(android.R.color.black));

        initView();
        initAnimation();
        initFirebase();
    }

    private void initView() {
        appLogo = findViewById(R.id.app_logo);
        appSlogan = findViewById(R.id.app_slogan);
    }

    private void initAnimation() {
        topAnimation = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_top_animation);
        bottomAnimation = AnimationUtils.loadAnimation(SplashScreenActivity.this, R.anim.splash_bottom_animation);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        userRef = FirebaseFirestore.getInstance().collection("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        int SPLASH_TIMER = 4000;

        appLogo.setAnimation(topAnimation);
        appSlogan.setAnimation(bottomAnimation);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(firebaseUser != null)
                {
                    userRef.document(firebaseAuth.getCurrentUser().getEmail())
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful())
                            {
                                if(task.getResult().exists())
                                {
                                    String alias = task.getResult().getData().get("Username").toString();
                                    if(!alias.equals(""))
                                    {
                                        Intent intent = new Intent(SplashScreenActivity.this, ParentActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                                        finish();
                                    }
                                    else
                                    {
                                        Intent intent = new Intent(SplashScreenActivity.this, EditProfileActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                                        finish();
                                    }
                                }
                                else
                                {
                                    Intent intent = new Intent(SplashScreenActivity.this, ToSignUpActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                                    finish();
                                }
                            }
                            else
                            {
                                finishAffinity();
                            }
                        }
                    });
                }
                else {
                    Intent intent = new Intent(SplashScreenActivity.this, ToSignUpActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    CustomIntent.customType(SplashScreenActivity.this, "fadein-to-fadeout");
                    finish();
                }
            }
        }, SPLASH_TIMER);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
