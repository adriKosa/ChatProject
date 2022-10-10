package com.example.adria.chatproject.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ProgressBarUtil;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    SharedPreferences preferences;
    EditText text;
    FirebaseAuth auth;
    String password;
    String email;
    ProgressBarUtil progressBarUtil;
    ProgressBar spinner;
    Button loginBtn;
    Button registerBtn;


    //Vytvorenie Login Activity
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Priradenie layoutu k Activite
        setContentView(R.layout.activity_login);

        //Priradenie jednotlivych elementov layoutu do objetov
        loginBtn = findViewById(R.id.loginLoginBtn);
        registerBtn = findViewById(R.id.loginRegisterBtn);
        spinner = findViewById(R.id.login_progressBar);

        //Získanie inštancie pre autentifikaciu používateľa
        auth = FirebaseAuth.getInstance();

        //Získanie SharadPreferencies kde sú uložené používateľské dáta
        preferences = getSharedPreferences(Constants.MY_PREFS, MODE_PRIVATE);

        //Vytvorenie objektu ProgressBarUtil ktorý slúži na manipuláciu s ProgressBarom a klávesnicou
        progressBarUtil = new ProgressBarUtil(this, spinner);

        //Nastavenie onClick listeneru na login tlačidlo
        loginBtn.setOnClickListener(view -> {

            //Zavolanie metód getUserData() a loginUser()
            getUserData();
            loginUser();

        });

        //Nastevenie onClick listeneru na register tlačidlo
        registerBtn.setOnClickListener(view -> {

            //Vytvorenie nového Intentu pre RegisterActivity
            //a následné spustenie Activity pomocou tohoto intentu
            Intent registerIntent = new Intent(view.getContext(), RegisterActivity.class);
            startActivity(registerIntent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("STAVUSERA", "zapnutie login activity");

        //Ak sa v databáze nachádza používateľský email tak...
        if (!preferences.getString(Constants.LOGIN_EMAIL, "").equals("")){
            //zavolá sa metóda fillUserData()
            fillUserData();
        }

        //Ak je nejaký používateľ prihlásený na našu databázu tak
        // pri štartovaní tejto Aktivity ju rovno ukončí
        if (auth.getCurrentUser() != null){
            Log.i("STAVUSERA", "Vypnutie login activity");
            finish();
        }
    }

    //Metóda na naplnenie používateľského emailu ak bol pred tým nejaký používateľ prihlásený
    private void fillUserData() {

        //Priradenie elementu edit textu z layoutu do objektu text ktorý patrí triede EditText
        text = findViewById(R.id.loginEmailTxt);
        email = preferences.getString(Constants.LOGIN_EMAIL, "");
        text.setText(email);
    }

    private void getUserData(){

        //Priradenie elementu edit text to objektu text ktorý patrí triede EditText
        //a následné získanie stringu z tohoto elementu. Konkrétne používateľského emailu
        text = findViewById(R.id.loginEmailTxt);
        email = text.getText().toString();

        //To isté čo v predchádzajúcom prípade len teraz získavam
        //z druhého edit textu heslo a nie email
        text = findViewById(R.id.loginPasswordTxt);
        password = text.getText().toString();
    }

    //Metóda na prihlásenie používateľa do databázy
    private void loginUser() {

        //Kontrola či je niečo napísané v emaili a v hesle
        if (!email.isEmpty() && !password.isEmpty()){

            //Zavolanie metódy setProgressBar() objektu progressBarUtil
            progressBarUtil.setProgressBar();

            //Zavolanie metódy Firebasu na prihlásnie používateľa
            auth.signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener(authResult -> {

                        //Pri úspešnom prihlásení sa získa prihlásený používateľ
                        // následne pomocou jeho ID-čka sa získajú aj
                        // ostatné údaje z databázy a uložia sa lokálne

                        // Následne po tomto všetkom sa vypne progressBar
                        // a ukonči sa aktivita čiže použivateľ sa ocitne v chat Fragmente
                        FirebaseUser user = auth.getCurrentUser();
                        FirebaseFirestore.getInstance().collection(Constants.USERS_REF).
                                document(user.getUid()).get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String userImg;
                                userImg = documentSnapshot.getString(Constants.USER_IMG);
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(Constants.LOGIN_EMAIL, email);
                                editor.putString(Constants.LOGIN_PASSWD, password);
                                editor.putString(Constants.USER_NAME, user.getDisplayName());
                                editor.putString(Constants.USER_IMG, userImg);
                                editor.apply();
                                progressBarUtil.dismisProgressBar();
                                LoginActivity.this.finish();

                            }
                        }).addOnFailureListener(e -> {
                            Log.d("Exception", e.getLocalizedMessage());
                            //Ak sa nepodarí získať informácie o prihlásenom používateľovi
                            // z databázi tak sa len skryje progressBar a ukončí Aktivita
                            progressBarUtil.dismisProgressBar();
                            LoginActivity.this.finish();
                        });
                    })
                    .addOnFailureListener(e -> {
                        //Prihlasovanie nebolo úspešné
                        //Vypíše chybovú hlásku do Toastu a vypne skryje progressBar
                        Log.e("Exception", "Nebolo mozne sa prihlasit " + e.getLocalizedMessage());
                        Toast.makeText(LoginActivity.this, "Nepodarilo sa prihlásiť skús to ešte raz.", Toast.LENGTH_SHORT).show();
                        progressBarUtil.dismisProgressBar();
                    });
            //Ak je prázdny email alebo heslo tak tiež vypíše chybovú hlášku
        }else Toast.makeText(this, "Zabudol si vyplnit email alebo heslo", Toast.LENGTH_SHORT).show();

    }

    //Táto metóda sa zavolá keď používateľ stlačí tlačidlo späť
    @Override
    public void onBackPressed() {
        //Ak neni používateĺ prihlásený
        if (auth.getCurrentUser() == null){
            // zamedzí užívateľovi vrátit sa do fragmentov
            moveTaskToBack(true);
        }
    }


}
