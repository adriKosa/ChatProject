package com.example.adria.chatproject.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.nsd.NsdManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adria.chatproject.Adapters.MessageAdapter;
import com.example.adria.chatproject.Model.Message;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ImageUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageActivity extends AppCompatActivity {

    List<Message> messages = new ArrayList<>();
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;

    String friendsName;
    String friendsImg;
    String friendsId;
    String state;

    ListenerRegistration messagesListener;

    String documentId;
    ImageButton sendBtn;
    ImageView friendsStatus;
    TextView friendsNameTv;
    ImageView friendsImgView;
    EditText messageTxt;

    CollectionReference chatRef = FirebaseFirestore.getInstance().collection(Constants.CHATS_REF);
    CollectionReference usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);
    FirebaseUser currentUser;
    ListenerRegistration statusListener;
    String currentUserId;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Nastavenie Layoutu pre MessageActivity
        setContentView(R.layout.activity_message);

        // Skryje klávesnicu pri vytvorení Activity
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        // Získanie intentu
        // To je to čo posiela druhá Activita do tejto pri jej vytváraní
        Intent intent = getIntent();

        //Získanie Stringov z intentu
        friendsName = intent.getStringExtra(Constants.FRIENDS_NAME);
        friendsImg = intent.getStringExtra(Constants.FRIENDS_IMG);
        friendsId = intent.getStringExtra(Constants.FRIENDS_ID);
        documentId = intent.getStringExtra(Constants.DOC_ID);

        //Priradenie položiek z layoutu k objektom v tejto triede ImageView a TextView
        friendsImgView = findViewById(R.id.message_friends_img);
        friendsNameTv = findViewById(R.id.message_friends_name);
        friendsStatus = findViewById(R.id.message_friends_status);
        sendBtn = findViewById(R.id.message_send_btn);
        messageTxt = findViewById(R.id.message_edit_text);

        // Vytvorenie adaptéra a nśsledné jeho použitie v recyclerView
        messageAdapter = new MessageAdapter(this, messages, friendsImg, friendsName);
        recyclerView = findViewById(R.id.message_recycler_view);
        recyclerView.setAdapter(messageAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Zavolanie metódy na získanie všetkých správ
        getMessages();

        //Nastavenie obrazka pre mojho priatela
        try {
            Resources res = this.getResources();
            Bitmap src;

            if (friendsImg.equals("default")){
                src = BitmapFactory.decodeResource(res, R.drawable.default_user_img);
            }else {
                byte[] decodedString = Base64.decode(friendsImg, Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
            friendsImgView.setImageDrawable(ImageUtils.roundedImage(this, src));
        }catch (Exception e){
            Log.d("Exception", "Vynimka: " + e);
        }

        //Nastavenie mena mojho priatela
        friendsNameTv.setText(friendsName);

        //Kontrolovanie ci je moj kamarat online alebo nie
        friendsStatusCheck();

        // Nastavenie onClick Listeneru na tlaćidlo ktoré odosiela správu
        sendBtn.setOnClickListener(view -> {
            // Odosielanie danej správy
            Date currentTime = Calendar.getInstance().getTime();
            String message = messageTxt.getText().toString();
            messageTxt.setText("");
            MessageActivity.this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            Map<String, Object> messageData = new HashMap<>();
            messageData.put(Constants.MESSAGE_TEXT, message);
            messageData.put(Constants.RECIEVER_ID, friendsId);
            messageData.put(Constants.SENDER_ID, currentUserId);
            messageData.put(Constants.TIMESTAMP, currentTime);

            chatRef.document(documentId).collection(Constants.MESSAGES_REF)
                    .document()
                    .set(messageData);

            chatRef.document(documentId).update(Constants.TIMESTAMP, currentTime);
            chatRef.document(documentId).update(Constants.LAST_MESSAGE, message);
        });



    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Statusappky", "onResume");
        //Nastavý používateľovi online status pretože keď odišiel z MainActivity tak sa dal do offline
        // kvoli tomu že sa pausla MainActivity
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null){
            currentUserId = currentUser.getUid();
            usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);
            usersRef.document(currentUserId).update(Constants.STATUS, Constants.ONLINE);
        }
    }


    // Metóda na kontrolu či je môj chat kamarát online alebo nie
    private void friendsStatusCheck() {

        statusListener = usersRef.document(friendsId).addSnapshotListener((documentSnapshot, e) -> {

            state = (String) documentSnapshot.get(Constants.STATUS);
            if (state.equals(Constants.ONLINE)) {
                friendsStatus.setVisibility(View.VISIBLE);
            }else {
                friendsStatus.setVisibility(View.GONE);
            }

        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Statusappky", "onPause");

        // Keď používateľ odchádza z tejto Aktivity tak mu nastavý offline status
        if (currentUser != null){
            usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);
            usersRef.document(currentUserId).update(Constants.STATUS, Constants.OFFLINE);
        }

    }

    //Získa všetky správy medzi 2 používateľmi podľa ID dokumentu
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void getMessages() {
        messagesListener = chatRef.document(documentId).collection(Constants.MESSAGES_REF)
                .orderBy(Constants.TIMESTAMP, Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null)
                        Log.e("Exception", "Nebolo mozne nacitat thoughts: " + e.toString());

                    MessageActivity.this.parseData(snapshots);
                });
    }

    // Metóda parseData() pridá správy do ArrayListu ktorý obsahuje objekty inštancie triedy Message
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void parseData(QuerySnapshot snapshots) {

        if (snapshots != null){

            messages.clear();

            snapshots.forEach(snapshot -> {
                String messageText = (String) snapshot.get(Constants.MESSAGE_TEXT);
                String recieverId = (String) snapshot.get(Constants.RECIEVER_ID);
                String senderId = (String) snapshot.get(Constants.SENDER_ID);
                Date timeStamp = (Date) snapshot.get(Constants.TIMESTAMP);

                messages.add(new Message(messageText, recieverId, senderId, timeStamp));
            });

            messageAdapter.notifyDataSetChanged();
        }

        if (snapshots == null){
            messages.clear();
            messageAdapter.notifyDataSetChanged();
        }
    }

}
