package com.example.adria.chatproject.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.adria.chatproject.Activities.LoginActivity;
import com.example.adria.chatproject.Activities.SearchFriendActivity;
import com.example.adria.chatproject.Adapters.FriendsAdapter;
import com.example.adria.chatproject.Model.Friend;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ProgressBarUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.Date;
import java.util.function.Consumer;

import static android.content.Context.MODE_PRIVATE;

public class ChatFragment extends Fragment {

    FirebaseAuth auth;
    ArrayList<Friend> friends = new ArrayList<>();
    ListenerRegistration friendsListener;
    FirebaseUser user;
    CollectionReference chatRef;
    String currentUserId;
    FriendsAdapter adapter;
    RecyclerView recyclerView;
    ProgressBar spinner;
    ProgressBarUtil progressBarUtil;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Context context = view.getContext();

        Button searchBtn = view.findViewById(R.id.main_fragment_search_bnt);
        spinner = view.findViewById(R.id.chatProgressBar);
        progressBarUtil = new ProgressBarUtil((Activity) context, spinner);

        chatRef = FirebaseFirestore.getInstance().collection(Constants.CHATS_REF);


        recyclerView = view.findViewById(R.id.friends_recyclerview);
        adapter = new FriendsAdapter(friends, view.getContext());
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(adapter);

        searchBtn.setOnClickListener(view1 -> {
            Intent searchIntent = new Intent(context, SearchFriendActivity.class);
            context.startActivity(searchIntent);

        });

        Log.i("LifeCycleOfFragment", "onCreateView");

        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onResume() {
        super.onResume();

        Log.i("LifeCycleOfFragment", "onResume");

            getAllFriends();

    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getAllFriends() {

        user = FirebaseAuth.getInstance().getCurrentUser();

        Log.i("LifeCycleOfFragment", "getAllFriends");

        if (user != null){

            progressBarUtil.setProgressBar();

            currentUserId = user.getUid();

            friendsListener = chatRef.orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
                    .addSnapshotListener((snapshots, e) -> {

                        if (e != null) Log.e("Exception", e.getLocalizedMessage());

                        Log.i("LifeCycleOfFragment", "friendsListener");


                        parseData(snapshots);

                    });
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    void parseData(QuerySnapshot snapshots){

        if (snapshots.size() != 0) {

            Log.i("LifeCycleOfFragment", "napshot size je vacsia ako nulla");

                    friends.clear();

            snapshots.forEach(snapshot -> {
                String docId = snapshot.getId();
                if (docId.contains(currentUserId)) {
                    String userId = (String) snapshot.get(Constants.USER1_ID);
                    String userName;
                    String userImg;
                    Date timeStamp = (Date) snapshot.get(Constants.TIMESTAMP);
                    String lastMessage = (String) snapshot.get(Constants.LAST_MESSAGE);

                    if (!currentUserId.equals(userId)) {
                        userName = (String) snapshot.get(Constants.USER1_NAME);
                        userImg = (String) snapshot.get(Constants.USER1_IMG);
                    } else {
                        userId = (String) snapshot.get(Constants.USER2_ID);
                        userName = (String) snapshot.get(Constants.USER2_NAME);
                        userImg = (String) snapshot.get(Constants.USER2_IMG);
                    }

                    friends.add(new Friend(userName, userImg, userId, lastMessage, timeStamp, docId));
                }

            });

            adapter.notifyDataSetChanged();
            progressBarUtil.dismisProgressBar();
        }

        if (snapshots.size() == 0) {
            friends.clear();
            adapter.notifyDataSetChanged();
        }
    }


}
