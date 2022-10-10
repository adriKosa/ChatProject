package com.example.adria.chatproject.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adria.chatproject.Adapters.SettingsAdapter;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Configuration;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ImageUtils;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class SettingFragment extends Fragment {
    public static TextView userName;
    public static TextView userEmail;

    ImageView userImg;
    SharedPreferences preferences;
    public static FirebaseUser currentUser;

    public static List<Configuration> listConfig = new ArrayList<>();
    public static RecyclerView settingListView;
    private SettingsAdapter adapter;

    private static final int PICK_IMAGE = 1994;
    private LovelyProgressDialog waitingDialog;


    private Context context;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        context = view.getContext();
        preferences = context.getApplicationContext().getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);

        userImg = view.findViewById(R.id.settings_user_image);
        userName = view.findViewById(R.id.settings_user_name);
        userEmail = view.findViewById(R.id.settings_user_email);

        Log.i("OBRAZOK", "toto je obrazok" + preferences.getString(Constants.USER_IMG, "default"));

        setupArrayListInfo();

        setNewImg();

        settingListView = view.findViewById(R.id.settings_recycler_view);


        waitingDialog = new LovelyProgressDialog(context);

        Log.i("StatoOfApp", "onCreateView");


        return view;
    }

    private void setupUserInfo(){
        setImageAvatar(context, preferences.getString(Constants.USER_IMG, "default"));
        userName.setText(preferences.getString(Constants.USER_NAME, "userName"));
        userEmail.setText(preferences.getString(Constants.LOGIN_EMAIL, "userEmail"));
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.i("StatoOfApp", "onResume");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (!userName.getText().toString().equals(preferences.getString(Constants.USER_NAME, "")) && currentUser != null){
            Log.i("StatoOfApp", "true");
            setupUserInfo();
            adapter = new SettingsAdapter(listConfig);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
            settingListView.setLayoutManager(layoutManager);
            settingListView.setAdapter(adapter);


        }
    }



    public static void setupArrayListInfo(){
        listConfig.clear();
        Configuration userNameConfig = new Configuration(Constants.USERNAME_LABEL_CHANGE,  R.drawable.ic_user);
        listConfig.add(userNameConfig);

        Configuration emailConfig = new Configuration(Constants.EMAIL_LABEL_CHANGE, R.drawable.ic_email);
        listConfig.add(emailConfig);

        Configuration resetPass = new Configuration(Constants.PASSWORD_CHANGE, R.drawable.ic_resend);
        listConfig.add(resetPass);

        Configuration signout = new Configuration(Constants.SIGNOUT_LABEL , R.drawable.ic_logout);
        listConfig.add(signout);
    }

    private void setNewImg() {
        userImg.setOnClickListener(view1 -> new AlertDialog.Builder(context)
                .setTitle("Zmena fotky")
                .setMessage("Si si istý že chceš zmeniť profilovú fotku?")
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_PICK);
                    SettingFragment.this.startActivityForResult(Intent.createChooser(intent, "Vyber fotku"), PICK_IMAGE);
                    dialogInterface.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss()).show());

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(context, "Vyskytla sa chyba, skúste to znovu.", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(data.getData());

                Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
                imgBitmap = ImageUtils.cropToSquare(imgBitmap);
                InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
                final Bitmap liteImage = ImageUtils.makeImageLite(is,
                        imgBitmap.getWidth(), imgBitmap.getHeight(),
                        ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

                String imageBase64 = ImageUtils.encodeBase64(liteImage);


                waitingDialog.setCancelable(false)
                        .setTitle("Nahrávanie novej profilovej fotky")
                        .setTopColorRes(R.color.colorPrimary)
                        .show();

                FirebaseFirestore.getInstance().collection(Constants.USERS_REF).
                        document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .update(Constants.USER_IMG, imageBase64)
                        .addOnSuccessListener(aVoid -> {
                            waitingDialog.dismiss();
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(Constants.USER_IMG, imageBase64);
                            editor.apply();
                            userImg.setImageDrawable(ImageUtils.roundedImage(context, liteImage));

                            new LovelyInfoDialog(context)
                                    .setTopColorRes(R.color.colorPrimary)
                                    .setTitle("Úspešné")
                                    .setMessage("Profilový obrázok bol zmenený.")
                                    .show();
                        }).addOnFailureListener(e -> {
                            waitingDialog.dismiss();
                            Log.d("Exception", e.getLocalizedMessage());
                            new LovelyInfoDialog(context)
                                    .setTopColorRes(R.color.colorAccent)
                                    .setTitle("Neúšpešné")
                                    .setMessage("Nepodarilo sa zmeniť profilový obrázok")
                                    .show();
                        });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void setImageAvatar(Context context, String imgBase64){
        try {
            Resources res = getResources();
            //ak sa ulozeny obrazok rovna defeult tak ponecha defaultny obrazok
            Bitmap src;
            if (imgBase64.equals("default")) {
                src = BitmapFactory.decodeResource(res, R.drawable.default_user_img);
            } else {
                byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }

            userImg.setImageDrawable(ImageUtils.roundedImage(context, src));
        }catch (Exception e){
        }
    }
}

