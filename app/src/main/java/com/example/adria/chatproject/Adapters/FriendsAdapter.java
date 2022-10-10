package com.example.adria.chatproject.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.nsd.NsdManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adria.chatproject.Activities.MessageActivity;
import com.example.adria.chatproject.Model.Friend;
import com.example.adria.chatproject.Model.User;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.Constants;
import com.example.adria.chatproject.Utilities.ImageUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> {

    private List<Friend> friends;
    private Context context;
    private SimpleDateFormat format;
    private String state;
    private String friendsImg;
    private ListenerRegistration stateListener;
    private CollectionReference usersRef = FirebaseFirestore.getInstance().collection(Constants.USERS_REF);


    public FriendsAdapter(List<Friend> friends, Context context){
        this.friends = friends;
        this.context = context;
        format = new SimpleDateFormat("kk:mm dd.MM", Locale.getDefault());
    }



    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.list_item_friends_layout, viewGroup, false);
        return new FriendsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int i) {
        holder.itemView.setTag(friends.get(i));

        format = new SimpleDateFormat("kk:mm dd.MM", Locale.getDefault());

        Friend friend = friends.get(i);

        try {
            Resources res = context.getResources();
            Bitmap src;

            if (friend.getFriendsImage().equals("default")){
                src = BitmapFactory.decodeResource(res, R.drawable.default_user_img);
            }else {
                byte[] decodedString = Base64.decode(friend.getFriendsImage(), Base64.DEFAULT);
                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            }
            holder.friendsImage.setImageDrawable(ImageUtils.roundedImage(context, src));
        }catch (Exception ex){
            Log.d("Exception", "Vynimka: " + ex);
        }


        stateListener = usersRef
                .document(friend.getFriendsId()).addSnapshotListener((documentSnapshot, e) -> {
                    state = (String) documentSnapshot.get(Constants.STATUS);
                    friendsImg = (String) documentSnapshot.get(Constants.USER_IMG);

                        if (state.equals(Constants.ONLINE)) {
                            holder.onlineImage.setVisibility(View.VISIBLE);
                        }else {
                            holder.onlineImage.setVisibility(View.GONE);
                        }

                        if (!friendsImg.equals(friend.getFriendsImage())){
                            friend.setFriendsImage(friendsImg);

                            try {

                                Bitmap src;
                                byte[] decodedString = Base64.decode(friend.getFriendsImage(), Base64.DEFAULT);
                                src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                holder.friendsImage.setImageDrawable(ImageUtils.roundedImage(context, src));

                            }catch (Exception ex){
                                Log.d("Exception", "Vynimka: " + ex);
                            }
                        }

        });



        holder.friendsName.setText(friend.getFriendsName());
        holder.lastMessage.setText(friend.getLastMessage());
        holder.timeStamp.setText(format.format(friend.getTimeStamp()));



    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    class FriendsViewHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{

        TextView friendsName;
        TextView lastMessage;
        TextView timeStamp;
        ImageView friendsImage;
        ImageView onlineImage;


        public FriendsViewHolder(View view){
            super(view);
            view.setOnClickListener(this);
            friendsName = view.findViewById(R.id.friend_item_name);
            friendsImage = view.findViewById(R.id.friend_item_image);
            lastMessage = view.findViewById(R.id.friend_item_message);
            timeStamp = view.findViewById(R.id.friend_item_time_stamp);
            onlineImage = view.findViewById(R.id.friend_item_online_img);
        }

        @Override
        public void onClick(View view) {
            Context context = view.getContext();
            int itemPosition = getAdapterPosition();
            Friend friend = friends.get(itemPosition);
            Intent messageActivityInten = new Intent(context, MessageActivity.class);
            messageActivityInten.putExtra(Constants.FRIENDS_NAME, friend.getFriendsName());
            messageActivityInten.putExtra(Constants.FRIENDS_IMG, friend.getFriendsImage());
            messageActivityInten.putExtra(Constants.FRIENDS_ID, friend.getFriendsId());
            messageActivityInten.putExtra(Constants.DOC_ID, friend.getDocId());
            context.startActivity(messageActivityInten);
        }
    }
}
