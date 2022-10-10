package com.example.adria.chatproject.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.adria.chatproject.Model.Message;
import com.example.adria.chatproject.R;
import com.example.adria.chatproject.Utilities.DateParse;
import com.example.adria.chatproject.Utilities.ImageUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter {
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private Context context;
    private List<Message> messages;
    DateParse dateParse = new DateParse();
    SimpleDateFormat format = new SimpleDateFormat("kk:mm", Locale.getDefault());
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

    private String recieverUserName;
    private String recieverUserImg;

    public MessageAdapter(Context context, List<Message> messages, String recieverUserImg, String recieverUserName){
        this.context = context;
        this.messages = messages;
        this.recieverUserName = recieverUserName;
        this.recieverUserImg = recieverUserImg;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_message_recieved, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        Message message = (Message) messages.get(i);

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);

        if (message.getSenderId().equals(currentUser.getUid())) {
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

        class ReceivedMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText, nameText;
            ImageView profileImage;

            ReceivedMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
                nameText = itemView.findViewById(R.id.text_message_name);
                profileImage = itemView.findViewById(R.id.image_message_profile);
            }

            void bind(Message message) {
                messageText.setText(message.getMessageText());

                // Formátuje dátum na String a následne nastavý tento String pre timeText.
                timeText.setText(format.format(message.getTimeStamp()));

                nameText.setText(recieverUserName);

                // Vloži sa obrázok môjho kamaráta ktorý sme dostali z konštruktora
                try {
                    Resources res = context.getResources();
                    Bitmap src;

                    if (recieverUserImg.equals("default")){
                        src = BitmapFactory.decodeResource(res, R.drawable.default_user_img);
                    }else {
                        byte[] decodedString = Base64.decode(recieverUserImg, Base64.DEFAULT);
                        src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    }
                    profileImage.setImageDrawable(ImageUtils.roundedImage(context, src));
                }catch (Exception e){
                    Log.d("Exception", "Vynimka: " + e);
                }
            }
        }

        class SentMessageHolder extends RecyclerView.ViewHolder {
            TextView messageText, timeText;

            SentMessageHolder(View itemView) {
                super(itemView);

                messageText = itemView.findViewById(R.id.text_message_body);
                timeText = itemView.findViewById(R.id.text_message_time);
            }

            void bind(Message message) {
                messageText.setText(message.getMessageText());
                timeText.setText(format.format(message.getTimeStamp()));
            }
        }

}
