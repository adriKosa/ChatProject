package com.example.adria.chatproject.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.adria.chatproject.Activities.SearchFriendActivity;
import com.example.adria.chatproject.Model.Friend;
import com.example.adria.chatproject.Model.User;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ImageUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersViewHolder> {

    private List<User> users;
    private List<User> usersCopy;
    private Context context;
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    String currentUserId = currentUser.getUid();
    private SharedPreferences prefs;
    private String currentUserName;
    private String currentUserImg;

    public UsersAdapter(List<User> users, Context context){
        this.users = users;
        this.usersCopy = new ArrayList<>(users);
        this.context = context;
        prefs = context.getSharedPreferences(Constants.MY_PREFS, Context.MODE_PRIVATE);
        currentUserName = prefs.getString(Constants.USER_NAME, "Adri");
        currentUserImg = prefs.getString(Constants.USER_IMG, "default");

    }

    public void filter(String text) {

        if (users.size() > usersCopy.size()){
            usersCopy.addAll(users);
        }

        users.clear();

        if(text.isEmpty()){
            users.addAll(usersCopy);
        } else{
            text = text.toLowerCase();
            for(User user: usersCopy){
                if(user.getUserName().toLowerCase().contains(text)){
                    users.add(user);
                }
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UsersViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_users_layout, viewGroup, false);
        return new UsersAdapter.UsersViewHolder(v);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull UsersViewHolder holder, int i) {
        holder.itemView.setTag(users.get(i));

        User user = users.get(i);

        try {
            Resources res = context.getResources();
            Bitmap src;

            if (user.getUserImage().equals("default")){
                src = BitmapFactory.decodeResource(res, R.drawable.default_user_img);
            }else {
                byte[] decodedString = Base64.decode(user.getUserImage(), Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
            holder.userImage.setImageDrawable(ImageUtils.roundedImage(context, src));
        }catch (Exception e){
            Log.d("Exception", "Vynimka: " + e);
        }


        holder.userName.setText(user.getUserName());


        holder.addFriend.setOnClickListener(view -> {

            Date currentTime = Calendar.getInstance().getTime();


            CollectionReference chatRef = FirebaseFirestore.getInstance()
                    .collection(Constants.CHATS_REF);


            String docId = currentUserId + user.getUserId();

            Map<String, Object> chatData = new HashMap<>();
            chatData.put(Constants.USER1_ID, currentUserId);
            chatData.put(Constants.USER1_NAME, currentUserName);
            chatData.put(Constants.USER1_IMG, currentUserImg);

            chatData.put(Constants.USER2_ID, user.getUserId());
            chatData.put(Constants.USER2_NAME, user.getUserName());
            chatData.put(Constants.USER2_IMG, user.getUserImage());

            chatData.put(Constants.TIMESTAMP, currentTime);
            chatData.put(Constants.LAST_MESSAGE, currentUserName + " si ťa pridal ako priateľa");
            chatRef.document(docId).set(chatData);

            // VYTVORENIE PRVEJ SPRAVY MEDZI PRIATELMI
            Map<String, Object> messageData = new HashMap<>();
            messageData.put(Constants.MESSAGE_TEXT, currentUserName + " si ťa pridal ako priateľa");
            messageData.put(Constants.RECIEVER_ID, user.getUserId());
            messageData.put(Constants.SENDER_ID, currentUserId);
            messageData.put(Constants.TIMESTAMP, currentTime);
            chatRef.document(docId).collection(Constants.MESSAGES_REF)
                    .document()
                    .set(messageData)
                    .addOnSuccessListener(runnable1 -> {

                        // DOCASTNE RIESENIE
                        //SearchFriendActivity.getAllUsers();

                        Toast.makeText(context, "Pridal si nového priateľa", Toast.LENGTH_SHORT).show();
                        SearchFriendActivity.users.remove(i);
                        SearchFriendActivity.usersAdapter.notifyItemRemoved(i);


                    }).addOnFailureListener(e -> {
                Log.i("Vynimka", e.getLocalizedMessage());
                Toast.makeText(context, "Nepodarilo sa pridať nového priateľa", Toast.LENGTH_SHORT).show();
            });


        });

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UsersViewHolder extends RecyclerView.ViewHolder{

        TextView userName;
        ImageView userImage;
        Button addFriend;

        public UsersViewHolder(View view){
            super(view);
            userName = view.findViewById(R.id.friend_item_name);
            userImage = view.findViewById(R.id.friend_item_image);
            addFriend = view.findViewById(R.id.user_item_btn);
        }
    }
}
