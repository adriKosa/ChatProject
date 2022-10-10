package com.example.adria.chatproject.Activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;



import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText text;
    ProgressBar progressBar;
    SharedPreferences preferences;
    Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nastavenie Layoutu pre RegisterActivity
        setContentView(R.layout.activity_register);

        registerBtn = findViewById(R.id.registerRegisterBtn);
        progressBar = findViewById(R.id.registerProgressBar);
        progressBar.setVisibility(View.INVISIBLE);

        preferences = getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);
        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(this::regiserUser);
    }

    // Metóda na zaregistrovanie používateľa
    private void regiserUser(View mView){
        hideKeyboard(this);

        View view = getWindow().getDecorView().getRootView();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);

        text = view.findViewById(R.id.registerEmailTxt);
        String email = text.getText().toString();

        text = view.findViewById(R.id.registerUserNameTxt);
        String userName = text.getText().toString();

        text = view.findViewById(R.id.registerPasswordTxt1);
        String passwd1 = text.getText().toString();

        text = view.findViewById(R.id.registerPasswordTxt2);
        String passwd2 = text.getText().toString();

        if (passwd1.equals(passwd2) && (passwd1.length() >= 6) && !email.equals("")  && !userName.equals("")){

            auth.createUserWithEmailAndPassword(email, passwd1)
                    .addOnSuccessListener(authResult -> {
                        // bol vytvoreny uzivatel a následne mu nastavý používateľské meno
                        authResult.getUser().updateProfile(new UserProfileChangeRequest.Builder()
                                .setDisplayName(userName).build())
                                .addOnFailureListener(e -> Log.e("Exception", "Nebolo mozne pridat username: " + e.getLocalizedMessage()));

                        // Vytvorenie HashMap pre používateľské dáta
                        HashMap data = new HashMap<String, Object>();
                        data.put(Constants.USER_NAME, userName);
                        data.put(Constants.DATE_CREATED, FieldValue.serverTimestamp());
                        data.put(Constants.USER_IMG, Constants.USER_DEF_IMG);
                        data.put(Constants.STATUS, Constants.OFFLINE);

                        // Uloženie týchto dát do databázy
                        FirebaseFirestore.getInstance().collection(Constants.USERS_REF).document(authResult.getUser().getUid())
                                .set(data).addOnSuccessListener(o -> {

                            // Po úspešnom uložení do databazy sa tieto dáta uložia aj lokálne
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(Constants.LOGIN_EMAIL, email);
                            editor.putString(Constants.LOGIN_PASSWD, passwd1);
                            editor.putString(Constants.USER_IMG, Constants.USER_DEF_IMG);
                            editor.putString(Constants.USER_NAME, userName);
                            editor.apply();
                            RegisterActivity.this.finish();
                        }).addOnFailureListener(e -> Log.e("Exception", "Nebolo mozne pridat currentUser document: " + e.getLocalizedMessage()));

                    }).addOnFailureListener(e -> {
                        Log.e("Exception", "Nebolo mozne vytvorit uzivatela." + e.getLocalizedMessage());
                        Toast.makeText(this, "Nebolo možné vytvoriť používateľa skús to ešte raz", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
            });

        }else {
            progressBar.setVisibility(View.GONE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
            imm.showSoftInput(text, InputMethodManager.SHOW_IMPLICIT);

            if (email.equals("")){

                Toast.makeText(this, "Zabudol si vypniť email.", Toast.LENGTH_SHORT).show();
                text = view.findViewById(R.id.registerEmailTxt);
                text.requestFocus();
                return;
            }
            if (userName.equals("")){

                Toast.makeText(this, "Zabudol si vyplniť užívateľské meno.", Toast.LENGTH_SHORT).show();
                text = view.findViewById(R.id.registerUserNameTxt);
                text.requestFocus();
                return;
            }
            if (passwd1.length()<6){

                Toast.makeText(this, "Heslo musí obsahovať najmenej 6 znakov.", Toast.LENGTH_SHORT).show();
                text = view.findViewById(R.id.registerPasswordTxt1);
                text.requestFocus();
                return;
            }
            
            Toast.makeText(this, "Heslá sa nezhodujú.", Toast.LENGTH_SHORT).show();
            text = view.findViewById(R.id.registerPasswordTxt1);
            text.requestFocus();

        }
    }

    // Pri stlačení tlačidla na zrušenie registrácie sa ukončí aktivita
    void registerCancelClicked(View view){
        finish();
    }

    // Metóda na skrytie klávesnice
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);

        // Nájde aktuálne zobrazené view, takže možeme pracovať so správnym view
        View view = activity.getCurrentFocus();

        // Ak aktuálne view nie je zobrazené tak vytvorí nové view.
        if (view == null) {
            view = new View(activity);
        }

        // Skytie samotnej klávesnice
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
