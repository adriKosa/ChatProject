package com.example.adria.chatproject.Activities;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.SearchView;

import com.example.adria.chatproject.Adapters.UsersAdapter;
import com.example.adria.chatproject.Model.User;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ProgressBarUtil;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javax.annotation.Nullable;

public class SearchFriendActivity extends AppCompatActivity {

    public static ArrayList<User> users;
    ArrayList<String> friendsIds;
    RecyclerView userRecyclerView;
    static CollectionReference usersRef;
    static CollectionReference chatRef;
    public static UsersAdapter usersAdapter;
    static FirebaseUser currentUser;
    static String currentUserId;
    ProgressBar spinner;
    ProgressBarUtil progressBarUtil;
    SearchView searchView;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_friend);
        users = new ArrayList<>();
        friendsIds = new ArrayList<>();
        spinner = findViewById(R.id.search_progressBar);
        searchView = findViewById(R.id.searchView);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                usersAdapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                usersAdapter.filter(newText);
                return true;
            }
        });

        progressBarUtil = new ProgressBarUtil(this, spinner);

        usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);
        chatRef = FirebaseFirestore.getInstance().collection(Constants.CHATS_REF);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = currentUser.getUid();



            usersAdapter = new UsersAdapter(users, this);
            userRecyclerView = findViewById(R.id.search_friend_recycle_view);
            userRecyclerView.setAdapter(usersAdapter);
            userRecyclerView.setLayoutManager(new LinearLayoutManager(this));



        getAllUsers();



    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getAllUsers() {

        progressBarUtil.setProgressBar();

        users.clear();

        usersRef.orderBy(Constants.USER_NAME, Query.Direction.ASCENDING).get().addOnSuccessListener(userSnapshots -> {


            if (userSnapshots.size() != 0) {
                chatRef.get().addOnSuccessListener(chatSnapshots -> {


                    AtomicInteger i = new AtomicInteger();
                    userSnapshots.forEach(userSnap -> {

                        String userId = userSnap.getId();
                        Log.i("POCET", "Listuje uzivatelmi");

                        Iterator<QueryDocumentSnapshot> iterator = chatSnapshots.iterator();

                        while (iterator.hasNext()) {
                            String chatId = iterator.next().getId();
                            Log.i("POCET", "Hlada zhodu");
                            if (chatId.equals(userId + currentUserId) || chatId.equals(currentUserId + userId) || currentUserId.equals(userId)) {
                                i.set(1);
                                Log.i("POCET", "Nasla sa zhoda a nemalo by to dalej pokracovat");
                                break;
                            }
                        }


                        if (i.get() == 0) {
                            String userName = (String) userSnap.get(Constants.USER_NAME);
                            String userImg = (String) userSnap.get(Constants.USER_IMG);
                            Log.i("POCET", "pridanie pouzivatela");
                            users.add(new User(userName, userImg, userId));
                        }
                        i.set(0);
                    });

                    usersAdapter.notifyDataSetChanged();
                    progressBarUtil.dismisProgressBar();

                });

            }

            if (userSnapshots.size() == 0) {
                progressBarUtil.dismisProgressBar();
                users.clear();
                usersAdapter.notifyDataSetChanged();
            }

        });

    }
}
