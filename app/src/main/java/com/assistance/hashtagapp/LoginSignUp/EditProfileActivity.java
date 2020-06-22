package com.assistance.hashtagapp.LoginSignUp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.assistance.hashtagapp.BottomSheets.UsernameRequirementsBottomSheet;
import com.assistance.hashtagapp.ParentActivity;
import com.assistance.hashtagapp.R;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shreyaspatil.MaterialDialog.MaterialDialog;
import com.shreyaspatil.MaterialDialog.interfaces.DialogInterface;
import com.tapadoo.alerter.Alerter;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener;
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import maes.tech.intentanim.CustomIntent;

public class EditProfileActivity extends AppCompatActivity {

    ImageView close, confirm, choosePhoto;
    CircleImageView userProfilePic;
    EditText userName, userAlias, userAbout;
    ChipGroup genderGroup;
    TextView userDob, userEmail, userMobile;
    CardView saveCard;
    ConstraintLayout save;

    Uri imageUri = null;

    AlertDialog progressDialog;

    FirebaseAuth firebaseAuth;
    CollectionReference userRef;
    StorageReference storageReference;

    String gender = null;
    public final static String USERNAME_PATTERN = "^(?=\\S+$)[a-z0-9_.]{6,20}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        //Transparent StatusBar
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        initViews();
        initFirebase();
        setActionOnViews();

        progressDialog = new SpotsDialog.Builder().setContext(EditProfileActivity.this)
                .setMessage("Saving profile...")
                .setCancelable(false)
                .setTheme(R.style.SpotsDialog)
                .build();

        KeyboardVisibilityEvent.setEventListener(EditProfileActivity.this, new KeyboardVisibilityEventListener(){
            @Override
            public void onVisibilityChanged(boolean isOpen) {
                if(!isOpen)
                {
                    userName.clearFocus();
                    userAlias.clearFocus();
                    userAbout.clearFocus();
                }
            }
        });

        DocumentReference userInfo = userRef.document(firebaseAuth.getCurrentUser().getEmail());
        userInfo.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(e != null)
                {
                    Alerter.create(EditProfileActivity.this)
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
                else if(documentSnapshot != null && documentSnapshot.exists())
                {
                    String image = documentSnapshot.getData().get("ProfilePic").toString();
                    String name = documentSnapshot.getData().get("Name").toString();
                    String alias = documentSnapshot.getData().get("Username").toString();
                    String about = documentSnapshot.getData().get("About").toString();
                    String gender = documentSnapshot.getData().get("Gender").toString();
                    String dob = documentSnapshot.getData().get("DOB").toString();
                    String email = documentSnapshot.getData().get("Email").toString();
                    String mobile = documentSnapshot.getData().get("Mobile").toString();

                    if(image.isEmpty())
                    {
                        imageUri = null;
                        userProfilePic.setImageResource(R.drawable.user_avatar);
                    }
                    else
                    {
                        Uri photoUri = Uri.parse(image);
                        Glide.with(EditProfileActivity.this).load(photoUri).centerCrop().into(userProfilePic);
                    }

                    userName.setText(name);
                    userAlias.setText(alias);
                    userAbout.setText(about);

                    if(gender.equals(""))
                    {
                        genderGroup.clearCheck();
                    }
                    else if(gender.equals("Male"))
                    {
                        genderGroup.check(R.id.male_chip);
                    }
                    else if(gender.equals("Female"))
                    {
                        genderGroup.check(R.id.female_chip);
                    }
                    else if(gender.equals("Other"))
                    {
                        genderGroup.check(R.id.other_chip);
                    }

                    userDob.setText(dob);
                    userEmail.setText(email);
                    userMobile.setText(mobile);
                }
            }
        });
    }

    private void initViews() {
        close = findViewById(R.id.close);
        confirm = findViewById(R.id.confirm);
        choosePhoto = findViewById(R.id.choose_photo);
        userProfilePic = findViewById(R.id.user_profile_pic);
        userName = findViewById(R.id.user_name);
        userAlias = findViewById(R.id.user_alias);
        userAbout = findViewById(R.id.user_about);
        userEmail = findViewById(R.id.user_email);
        genderGroup = findViewById(R.id.gender_group);
        userDob = findViewById(R.id.user_dob);
        userMobile = findViewById(R.id.user_mobile);
        saveCard = findViewById(R.id.save_card);
        save = findViewById(R.id.save);
    }

    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseFirestore.getInstance().collection("Users");
        storageReference = FirebaseStorage.getInstance().getReference("UserProfilePics/");
    }

    private void setActionOnViews() {
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        choosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog dialog = new MaterialDialog.Builder(EditProfileActivity.this)
                        .setTitle("Choose Action!")
                        .setMessage("Choose an action to continue!")
                        .setCancelable(false)
                        .setAnimation(R.raw.take_photo)
                        .setPositiveButton("Edit", R.drawable.ic_material_dialog_camera, new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                selectImage();
                            }
                        })
                        .setNegativeButton("Remove", R.drawable.ic_material_dialog_remove, new MaterialDialog.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                dialogInterface.dismiss();
                                imageUri = null;
                                userProfilePic.setImageResource(R.drawable.user_avatar);
                            }
                        }).build();
                dialog.show();
            }
        });

        userName.addTextChangedListener(editProfileTextWatcher);
        userAlias.addTextChangedListener(editProfileTextWatcher);

        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("SELECT YOUR DOB");
        final MaterialDatePicker materialDatePicker = builder.build();

        userDob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                materialDatePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                userDob.setText(materialDatePicker.getHeaderText());
            }
        });
    }

    private TextWatcher editProfileTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            final String name = userName.getText().toString().trim();
            final String alias = userAlias.getText().toString().trim();

            if(!name.isEmpty() && !alias.isEmpty())
            {
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.hideKeyboard(EditProfileActivity.this);
                        if(!alias.matches(USERNAME_PATTERN))
                        {
                            final UsernameRequirementsBottomSheet usernameRequirementsBottomSheet = new UsernameRequirementsBottomSheet();
                            usernameRequirementsBottomSheet.show(getSupportFragmentManager(), "usernameRequirementsBottomSheet");
                            usernameRequirementsBottomSheet.setEnterTransition(R.anim.item_animation_slide_from_bottom);
                            usernameRequirementsBottomSheet.setExitTransition(R.anim.item_animation_fall_down);
                            return;
                        }
                        else
                        {
                            progressDialog.show();
                            userRef.whereEqualTo("Username", alias)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                                        if(documentSnapshots.isEmpty())
                                        {
                                            uploadDataToFirebase();
                                        }
                                        else
                                        {
                                            progressDialog.dismiss();
                                            Alerter.create(EditProfileActivity.this)
                                                    .setText("That username you chose already exists. Try a different one!")
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
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Alerter.create(EditProfileActivity.this)
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
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Alerter.create(EditProfileActivity.this)
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
                    }
                });

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.hideKeyboard(EditProfileActivity.this);
                        if(!alias.matches(USERNAME_PATTERN))
                        {
                            final UsernameRequirementsBottomSheet usernameRequirementsBottomSheet = new UsernameRequirementsBottomSheet();
                            usernameRequirementsBottomSheet.show(getSupportFragmentManager(), "usernameRequirementsBottomSheet");
                            usernameRequirementsBottomSheet.setEnterTransition(R.anim.item_animation_slide_from_bottom);
                            usernameRequirementsBottomSheet.setExitTransition(R.anim.item_animation_fall_down);
                            return;
                        }
                        else
                        {
                            progressDialog.show();
                            userRef.whereEqualTo("Username", alias)
                                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if(task.isSuccessful())
                                    {
                                        List<DocumentSnapshot> documentSnapshots = task.getResult().getDocuments();
                                        if(documentSnapshots.isEmpty())
                                        {
                                            uploadDataToFirebase();
                                        }
                                        else
                                        {
                                            progressDialog.dismiss();
                                            Alerter.create(EditProfileActivity.this)
                                                    .setText("That username you chose already exists. Try a different one!")
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
                                    else
                                    {
                                        progressDialog.dismiss();
                                        Alerter.create(EditProfileActivity.this)
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
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Alerter.create(EditProfileActivity.this)
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
                    }
                });
            }
            else
            {
                save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.hideKeyboard(EditProfileActivity.this);
                        if(name.isEmpty())
                        {
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(userName);
                            Alerter.create(EditProfileActivity.this)
                                    .setText("This field can't be empty.")
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
                        else if(alias.isEmpty())
                        {
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(userAlias);
                            Alerter.create(EditProfileActivity.this)
                                    .setText("This field can't be empty.")
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
                });

                confirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        UIUtil.hideKeyboard(EditProfileActivity.this);
                        if(name.isEmpty())
                        {
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(userName);
                            Alerter.create(EditProfileActivity.this)
                                    .setText("This field can't be empty.")
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
                        else if(alias.isEmpty())
                        {
                            YoYo.with(Techniques.Shake)
                                    .duration(700)
                                    .repeat(1)
                                    .playOn(userAlias);
                            Alerter.create(EditProfileActivity.this)
                                    .setText("This field can't be empty.")
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
                });
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void selectImage() {
        ImagePicker.Companion.with(EditProfileActivity.this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 1080)
                .start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK)
        {
            imageUri = data.getData();

            Glide.with(EditProfileActivity.this).load(imageUri).centerCrop().into(userProfilePic);
        }
        else if(resultCode == ImagePicker.RESULT_ERROR)
        {
            Alerter.create(EditProfileActivity.this)
                    .setText("Whoa! That ran into some error.")
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
        else
        {
            return;
        }
    }

    private void uploadDataToFirebase() {
        final String nameValue = userName.getText().toString().trim();
        final String aliasValue = userAlias.getText().toString().trim();
        final String aboutValue = userAbout.getText().toString().trim();

        if(genderGroup.getCheckedChipId() == R.id.male_chip)
        {
            gender = "Male";
        }
        else if(genderGroup.getCheckedChipId() == R.id.female_chip)
        {
            gender = "Female";
        }
        else if(genderGroup.getCheckedChipId() == R.id.other_chip)
        {
            gender = "Other";
        }
        else if(genderGroup.isSelected() == false)
        {
            gender = "";
        }

        final String dobValue = userDob.getText().toString().trim();
        final String emailValue = userEmail.getText().toString().trim();
        final String mobileValue = userMobile.getText().toString().trim();

        if(imageUri != null)      //Upload Profile Pic too
        {
            final StorageReference fileRef = storageReference.child(firebaseAuth.getCurrentUser().getUid() + ".img");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    final String imageValue = uri.toString();

                                    DocumentReference documentReference = userRef.document(emailValue);
                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful())
                                            {
                                                userRef.document(emailValue)
                                                        .update("Name", nameValue,
                                                                "Username", aliasValue,
                                                                "About", aboutValue,
                                                                "Gender", gender,
                                                                "DOB", dobValue,
                                                                "Email", emailValue,
                                                                "Mobile", mobileValue,
                                                                "ProfilePic", imageValue)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                progressDialog.dismiss();
                                                                startActivity(new Intent(EditProfileActivity.this, ParentActivity.class));
                                                                CustomIntent.customType(EditProfileActivity.this, "fadein-to-fadeout");
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Alerter.create(EditProfileActivity.this)
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
                                                Alerter.create(EditProfileActivity.this)
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
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Alerter.create(EditProfileActivity.this)
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
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Alerter.create(EditProfileActivity.this)
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
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Alerter.create(EditProfileActivity.this)
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
            DocumentReference documentReference = userRef.document(emailValue);
            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful())
                    {
                        userRef.document(emailValue)
                                .update("Name", nameValue,
                                        "Username", aliasValue,
                                        "About", aboutValue,
                                        "Gender", gender,
                                        "DOB", dobValue,
                                        "Email", emailValue,
                                        "Mobile", mobileValue,
                                        "ProfilePic", "")
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        startActivity(new Intent(EditProfileActivity.this, ParentActivity.class));
                                        CustomIntent.customType(EditProfileActivity.this, "fadein-to-fadeout");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        progressDialog.dismiss();
                                        Alerter.create(EditProfileActivity.this)
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
                        Alerter.create(EditProfileActivity.this)
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
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Alerter.create(EditProfileActivity.this)
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
    }

    public static void setWindowFlag(EditProfileActivity editProfileActivity, final int bits, boolean on) {
        Window window = editProfileActivity.getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();

        if(on){
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
        }
        window.setAttributes(layoutParams);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void finish() {
        super.finish();
        CustomIntent.customType(EditProfileActivity.this, "fadein-to-fadeout");
    }
}
