package com.example.adria.chatproject.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adria.chatproject.Activities.LoginActivity;
import com.example.adria.chatproject.Fragments.SettingFragment;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Configuration;
import com.example.adria.chatproject.Utilities.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;


import java.util.List;


import static android.content.Context.MODE_PRIVATE;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.SettingsViewHolder> {

    private List<Configuration> profileConfig;


    public SettingsAdapter(List<Configuration> profileConfig){
        this.profileConfig = profileConfig;

    }

    @NonNull
    @Override
    public SettingsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_settings_layout, viewGroup, false);
        return new SettingsViewHolder(v);
    }



    @Override
    public void onBindViewHolder(@NonNull SettingsViewHolder holder, int i) {
        holder.itemView.setTag(profileConfig.get(i));

        Configuration config = profileConfig.get(i);

        holder.label.setText(config.getLabel());
        holder.icon.setImageResource(config.getIcon());
    }

    @Override
    public int getItemCount() {
        return profileConfig.size();
    }

    class SettingsViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{

        TextView label;
        ImageView icon;

        public SettingsViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            label = view.findViewById(R.id.item_text_view);
            icon = view.findViewById(R.id.item_image_view);
        }


        AuthCredential credential;


        @Override
        public void onClick(View view) {
            String email = SettingFragment.currentUser.getEmail();
            FirebaseUser user = SettingFragment.currentUser;
            Context context = view.getContext();
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);
            int itemPosition = getAdapterPosition();
            Configuration config = SettingFragment.listConfig.get(itemPosition);

            // ----ODHLASENIE SA Z APLIKACIE----
            if(config.getLabel().equals(Constants.SIGNOUT_LABEL)){
                FirebaseAuth.getInstance().signOut();
                Intent loginIntent = new Intent(context, LoginActivity.class);
                context.startActivity(loginIntent);
            }

            // ----ZMENA EMAILU----
            if (config.getLabel().equals(Constants.EMAIL_LABEL_CHANGE)){
                View vewInflater = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_edit_email,  (ViewGroup) view, false);
                final EditText input = (EditText)vewInflater.findViewById(R.id.edit_username);
                input.setText(email);
                //dialogove okno umozni uzivatelovi zmenit email
                new AlertDialog.Builder(context)
                        .setView(vewInflater)
                        .setPositiveButton("Uložiť", (dialogInterface, i) -> {
                            String newEmail = input.getText().toString();
                            if (!email.equals(newEmail) && !newEmail.isEmpty()) {
                                SettingsViewHolder.this.changeUserEmail(newEmail, context, prefs, user);
                            } else if (email.equals(newEmail)){
                                Toast.makeText(context, "Pre zmenu emailu je potrebný zadať iný ako je aktualny", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }else{
                                Toast.makeText(context, "Nový email nemôže byť prázdny", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }

                        })
                        .setNegativeButton("Zrušiť", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }

            // ----ZMENA POUZIVATELSKEHO MENA----
            if(config.getLabel().equals(Constants.USERNAME_LABEL_CHANGE)){
                View vewInflater = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_edit_username,  (ViewGroup) view, false);
                final EditText input = (EditText)vewInflater.findViewById(R.id.edit_username);
                input.setText(SettingFragment.userName.getText());
                //dialogove okno umozni uzivatelovi zmenit meno
                new AlertDialog.Builder(context)
                        .setView(vewInflater)
                        .setPositiveButton("Uložiť", (dialogInterface, i) -> {
                            String newName = input.getText().toString();
                            if (!SettingFragment.userName.getText().equals(newName) && !newName.isEmpty()) {
                                SettingsViewHolder.this.changeUserName(newName, context, prefs, user);
                            } else if (SettingFragment.userName.getText().equals(newName)){
                                Toast.makeText(context, "Nové meno musí byť dolišné ako pôvodné meno", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }else {
                                Toast.makeText(context, "Nové meno nesmie byť prázdne", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }

                        })
                        .setNegativeButton("Zrušiť", (dialogInterface, i) -> dialogInterface.dismiss()).show();
            }

            // ----ZMENA HESLA----
            if(config.getLabel().equals(Constants.PASSWORD_CHANGE)){

                View vewInflater = LayoutInflater.from(context)
                        .inflate(R.layout.dialog_edit_password,  (ViewGroup) view, false);
                final EditText password1 = (EditText)vewInflater.findViewById(R.id.dialog_password1);
                final EditText password2 = vewInflater.findViewById(R.id.dialog_password2);
                //dialogove okno umozni uzivatelovi zmenit heslo
                new AlertDialog.Builder(context)
                        .setView(vewInflater)
                        .setPositiveButton("Zmeniť", (dialogInterface, i) -> {
                            String passwd1 = password1.getText().toString();
                            String passwd2 = password2.getText().toString();

                            if (!prefs.getString(Constants.LOGIN_PASSWD, "1234").equals(passwd1) && passwd1.length() > 5 && passwd1.equals(passwd2)) {
                                SettingsViewHolder.this.changeUserPassword(passwd1, context, prefs, user);
                            } else if (passwd1.length() < 6) {
                                Toast.makeText(context, "Nové musí obsahovať najmenej 6 znakov", Toast.LENGTH_SHORT).show();
                            } else if (!passwd1.equals(passwd2)){
                                Toast.makeText(context, "Heslá sa musia zhodovať", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "Nové heslo sa nemôže zhodovať s pôvodným", Toast.LENGTH_SHORT).show();
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("Zrušiť", (dialogInterface, i) -> dialogInterface.dismiss()).show();

            }
        }


        private void changeUserEmail(String newEmail, Context context, SharedPreferences prefs, FirebaseUser user) {
            credential = EmailAuthProvider
                    .getCredential(SettingFragment.userEmail.getText().toString(), prefs.getString(Constants.LOGIN_PASSWD, "1234"));

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> user.updateEmail(newEmail)
                            .addOnSuccessListener(aVoid -> {

                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putString(Constants.LOGIN_EMAIL, newEmail);
                                editor.apply();
                                SettingFragment.userEmail.setText(newEmail);
                                Toast.makeText(context, "Email bol úspešne zmenený", Toast.LENGTH_SHORT).show();
                            }).addOnFailureListener(e -> {
                                Log.d("Exception", e.getLocalizedMessage());
                                Toast.makeText(context, "Email sa nepodarilo zmeniť skús to ešte raz", Toast.LENGTH_LONG).show();
                            }));
        }


        private void changeUserPassword(String newPassword, Context context, SharedPreferences prefs, FirebaseUser user) {

            credential = EmailAuthProvider
                    .getCredential(SettingFragment.userEmail.getText().toString(), prefs.getString(Constants.LOGIN_PASSWD, "1234"));

            user.reauthenticate(credential)
                    .addOnCompleteListener(task -> user.updatePassword(newPassword).addOnSuccessListener(aVoid -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Constants.LOGIN_PASSWD, newPassword);
                        editor.apply();
                        Toast.makeText(context, "Heslo bolo úspešne zmenené", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                                Log.i("Exception", e.getLocalizedMessage());
                                Toast.makeText(context, "Heslo sa nepodarilo zmeniť skús to ešte raz", Toast.LENGTH_SHORT).show();

                            }
                    ));




        }

        private void changeUserName(String newName, Context context, SharedPreferences prefs, FirebaseUser user) {

            String userId = user.getUid();
            FirebaseFirestore.getInstance().collection(Constants.USERS_REF).document(userId)
                    .update(Constants.USER_NAME, newName)
                    .addOnSuccessListener(aVoid -> {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(Constants.USER_NAME, newName);
                        editor.apply();
                        Log.i("POUZIVATELSKEMENO", prefs.getString(Constants.USER_NAME, "userName"));
                        SettingFragment.userName.setText(newName);
                        Toast.makeText(context, "Používaťeľské meno bolo úspešne zmenené", Toast.LENGTH_SHORT).show();
                    }).addOnFailureListener(e -> {
                        Log.d("Exception", e.getLocalizedMessage());
                        Toast.makeText(context, "Používaťeľské meno sa nepodarilo zmeniť, skús to ešte raz", Toast.LENGTH_SHORT).show();

                    });
        }
    }

}
