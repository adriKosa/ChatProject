package com.example.adria.chatproject.Activities;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.adria.chatproject.Adapters.ViewPagerAdapter;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private ViewPagerAdapter viewPagerAdapter;
    ViewPager mViewPager;
    FirebaseUser currentUser;
    String currentUserId;
    CollectionReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nastavenie layoutu pre MainActivity
        setContentView(R.layout.activity_main);

        // Nastevenie toolbaru
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Vytvorí adapter ktorý budú uložené fragmenty ČETY A NASTAVENIA
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Nastavenie ViewPageru a pridanie adaptéru k nemu.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(viewPagerAdapter);

        // V tablayoute je uloźená hlavná navigácia
        TabLayout tabLayout = findViewById(R.id.tabs);

        // Zabezpečenie zmeny fragmentu pri kliknutí na hornú lištu alebo pri potiahnutí do strany
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Skrytie toolbaru
        toolbar.setVisibility(View.GONE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Statusappky", "onResume");

        // Priradenie požívateľa do objektu currentUser
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Ak sa podarí pridať používateľa tak získa jeho ID
        if (currentUser != null) currentUserId = currentUser.getUid();

        // Ak mamé používateľa a jeho id je odlišné ako to co máme v ife
        // tak nastavíme používateľa do online stavu
        if (currentUser != null && !currentUserId.equals("llMH5IUH51YSZgaR1ytvDLSSMXV2")){
            Log.i("Statusappky", "current user != null");
            usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);
            usersRef.document(currentUserId).update(Constants.STATUS, Constants.ONLINE);
        }else{
            //Inak spustí LoginActivitu
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Statusappky", "onPause");
        // Pri stopnutí appky alebo vypnutí appky nastavý používateľa do offline stavu
        if (currentUser != null){
            usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);
            usersRef.document(currentUserId).update(Constants.STATUS, Constants.OFFLINE);
        }
    }

}
