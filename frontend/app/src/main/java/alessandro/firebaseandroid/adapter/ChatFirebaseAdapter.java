package alessandro.firebaseandroid.adapter;

import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.util.Util;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import java.text.SimpleDateFormat;
import java.util.Date;

import alessandro.firebaseandroid.MainActivity;
import alessandro.firebaseandroid.R;
import alessandro.firebaseandroid.model.ChatModel;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;


public class ChatFirebaseAdapter extends FirebaseRecyclerAdapter<ChatModel,ChatFirebaseAdapter.MyChatViewHolder> {
    
    private static final int RIGHT_MSG = 0;
    private static final int LEFT_MSG = 1;
    private static final int RIGHT_MSG_IMG = 2;
    private static final int LEFT_MSG_IMG = 3;
    private static final int LEFT_VOC = 4;
    private static final int RIGHT_VOC = 5;

    static final String TAG = ChatFirebaseAdapter.class.getSimpleName();
    //static final String url = "https://firebasestorage.googleapis.com/v0/b/chat-f976b.appspot.com/o/Audio%2Fvoice.png?alt=media&token=bf531b9a-23e6-4634-a27c-ab8c94de6df5";

    private ClickListenerChatFirebase mClickListenerChatFirebase;

    private String nameUser;



    public ChatFirebaseAdapter(DatabaseReference ref, String nameUser,ClickListenerChatFirebase mClickListenerChatFirebase) {
        super(ChatModel.class, R.layout.item_message_left, ChatFirebaseAdapter.MyChatViewHolder.class, ref);
        this.nameUser = nameUser;
        this.mClickListenerChatFirebase = mClickListenerChatFirebase;
    }

    @Override
    public MyChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        if (viewType == RIGHT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right,parent,false);
            return new MyChatViewHolder(view);
        }else if (viewType == LEFT_MSG){
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left,parent,false);
            return new MyChatViewHolder(view);
        }else if (viewType == RIGHT_MSG_IMG){
            Log.i(TAG,"item_message_right_img");
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_right_img,parent,false);
            return new MyChatViewHolder(view);
        }else if (viewType == LEFT_MSG_IMG){
            Log.i(TAG,"item_message_left_img");
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_left_img,parent,false);
            return new MyChatViewHolder(view);
        }
        else if (viewType == RIGHT_VOC){
            Log.i(TAG,"item_voice_right");
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voice_right,parent,false);
            return new MyChatViewHolder(view);
        }
        else{
            Log.i(TAG,"item_voice_left");
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_voice_left,parent,false);
            return new MyChatViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        ChatModel model = getItem(position);
        if (model.getMapModel() != null){
            if (model.getUserModel().getName().equals(nameUser)){
                return RIGHT_MSG_IMG;
            }else{
                return LEFT_MSG_IMG;
            }
        }else if (model.getFile() != null){
            if (model.getFile().getType().equals("img")){
                Log.i(TAG,"get img type");
                if (model.getUserModel().getName().equals(nameUser)){
                    return RIGHT_MSG_IMG;
                }
                else{
                    return LEFT_MSG_IMG;
                }
            }
            else{
                Log.i(TAG,"get voc type");
                if (model.getUserModel().getName().equals(nameUser)){
                    return RIGHT_VOC;
                }
                else{
                    return LEFT_VOC;
                }
            }
        }else if (model.getUserModel().getName().equals(nameUser)){
            return RIGHT_MSG;
        }else{
            return LEFT_MSG;
        }
    }

    @Override
    protected void populateViewHolder(MyChatViewHolder viewHolder, ChatModel model, int position) {
        viewHolder.setIvUser(model.getUserModel().getPhoto_profile());
        viewHolder.setTxtMessage(model.getMessage());
        viewHolder.setTvTimestamp(model.getTimeStamp());
        viewHolder.tvIsLocation(View.GONE);
        if (model.getFile() != null){
            if (model.getFile().getType().equals("img")) {
                Log.i(TAG,"populate: img");
                viewHolder.tvIsLocation(View.GONE);
                viewHolder.setIvChatPhoto(model.getFile().getUrl_file());
            }
            else{
                Log.i(TAG,"populate: voc");
                viewHolder.tvIsLocation(View.GONE);
                viewHolder.setIvChatVoice();
            }
        }else if(model.getMapModel() != null){
            viewHolder.setIvChatPhoto(alessandro.firebaseandroid.util.Util.local(model.getMapModel().getLatitude(),model.getMapModel().getLongitude()));
            viewHolder.tvIsLocation(View.VISIBLE);
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvTimestamp,tvLocation;
        EmojiconTextView txtMessage;
        ImageView ivUser,ivChatPhoto;
        ImageButton ivChatVoice;

        public MyChatViewHolder(View itemView) {
            super(itemView);
            tvTimestamp = (TextView)itemView.findViewById(R.id.timestamp);
            txtMessage = (EmojiconTextView)itemView.findViewById(R.id.txtMessage);
            tvLocation = (TextView)itemView.findViewById(R.id.tvLocation);
            ivChatPhoto = (ImageView)itemView.findViewById(R.id.img_chat);
            ivChatVoice = (ImageButton) itemView.findViewById(R.id.voc_chat);
            ivUser = (ImageView)itemView.findViewById(R.id.ivUserChat);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            ChatModel model = getItem(position);
            if (model.getMapModel() != null){
                mClickListenerChatFirebase.clickImageMapChat(view,position,model.getMapModel().getLatitude(),model.getMapModel().getLongitude());
            }else{
                if(model.getFile().getType().equals("img")){
                    mClickListenerChatFirebase.clickImageChat(view,position,model.getUserModel().getName(),model.getUserModel().getPhoto_profile(),model.getFile().getUrl_file());
                }
                else{

                }
            }
        }

        public void setTxtMessage(String message){
            if (txtMessage == null)return;
            txtMessage.setText(message);
        }

        public void setIvUser(String urlPhotoUser){
            if (ivUser == null)return;
            Glide.with(ivUser.getContext()).load(urlPhotoUser).centerCrop().transform(new CircleTransform(ivUser.getContext())).override(40,40).into(ivUser);
        }

        public void setTvTimestamp(String timestamp){
            if (tvTimestamp == null)return;
            tvTimestamp.setText(converteTimestamp(timestamp));
        }

        public void setIvChatPhoto(String url){
            if (ivChatPhoto == null)return;
            Glide.with(ivChatPhoto.getContext()).load(url)
                    .override(100, 100)
                    .fitCenter()
                    .into(ivChatPhoto);
            ivChatPhoto.setOnClickListener(this);
        }

        public void setIvChatVoice(){
            if (ivChatVoice == null)return;
            ivChatVoice.setOnTouchListener(new View.OnTouchListener(){

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date curDate = new Date();
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent){
                    int position = getAdapterPosition();
                    ChatModel model = getItem(position);
                    if(motionEvent.getAction() == motionEvent.ACTION_DOWN)
                    {
                        curDate.setTime(System.currentTimeMillis());

                       // mClickListenerChatFirebase.clickVoiceChat(view, position, model.getFile().getUrl_file(), model.getFile().getName_file());
                    }else if(motionEvent.getAction() == motionEvent.ACTION_UP)
                    {
                        Date endDate = new Date(System.currentTimeMillis());
                        long diff = endDate.getTime() - curDate.getTime();
                        if (diff >800)
                        {
                            Log.i(TAG,"trans*******************************************************************************"+model.getFile().getUrl_file());
                            //mClickListenerChatFirebase.clickVoiceChat(view, position, model.getFile().getUrl_file(), model.getFile().getName_file());
                            mClickListenerChatFirebase.clickTrans(view, position, model.getFile().getUrl_file(), model.getFile().getName_file());


                        }
                        else{
                            mClickListenerChatFirebase.clickVoiceChat(view, position, model.getFile().getUrl_file(), model.getFile().getName_file());
                        }

                    }
                    else
                    {

                    }

                    return false;
                }
            });
        }

        public void tvIsLocation(int visible){
            if (tvLocation == null)return;
            tvLocation.setVisibility(visible);
        }

    }

    private CharSequence converteTimestamp(String mileSegundos){
        return DateUtils.getRelativeTimeSpanString(Long.parseLong(mileSegundos),System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS);
    }

}
