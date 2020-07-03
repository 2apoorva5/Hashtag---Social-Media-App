package com.assistance.hashtagapp;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.assistance.hashtagapp.Fragments.HomeFragment;
import com.assistance.hashtagapp.Fragments.InboxFragment;
import com.assistance.hashtagapp.Fragments.ProfileFragment;
import com.assistance.hashtagapp.Fragments.SearchFragment;
import com.assistance.hashtagapp.LoginSignUp.EditProfileActivity;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

public class ParentActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    CollectionReference userRef;

    SpaceNavigationView spaceNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent);

        //Transparent StatusBar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        getSupportFragmentManager().beginTransaction().replace(R.id.parent_fragment_container, new HomeFragment()).commit();

        initViews();
        initFirebase();
        setSpaceNavigation(savedInstanceState);
    }

    private void initViews() {
        spaceNavigationView = findViewById(R.id.space_bottom_navigation);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("Users");
    }

    private void setSpaceNavigation(Bundle savedInstanceState) {
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem(null, R.drawable.ic_tab_home));
        spaceNavigationView.addSpaceItem(new SpaceItem(null, R.drawable.ic_tab_search));
        spaceNavigationView.addSpaceItem(new SpaceItem(null, R.drawable.ic_tab_inbox));
        spaceNavigationView.addSpaceItem(new SpaceItem(null, R.drawable.ic_tab_profile));

        spaceNavigationView.setCentreButtonSelectable(true);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                Toast.makeText(ParentActivity.this,"onCentreButtonClick", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                Fragment fragment = null;
                switch (itemIndex) {
                    case 0 :
                        fragment = new HomeFragment();
                        break;
                    case 1 :
                        fragment = new SearchFragment();
                        break;
                    case 2 :
                        fragment = new InboxFragment();
                        break;
                    case 3 :
                        fragment = new ProfileFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.parent_fragment_container, fragment).commit();
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
                Fragment fragment = null;
                switch (itemIndex) {
                    case 0 :
                        fragment = new HomeFragment();
                        break;
                    case 1 :
                        fragment = new SearchFragment();
                        break;
                    case 2 :
                        fragment = new InboxFragment();
                        break;
                    case 3 :
                        fragment = new ProfileFragment();
                        break;
                }
                getSupportFragmentManager().beginTransaction().replace(R.id.parent_fragment_container, fragment).commit();
            }
        });
    }

    public static void setWindowFlag(ParentActivity parentActivity, final int bits, boolean on) {
        Window window = parentActivity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        if(on){
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }
}
