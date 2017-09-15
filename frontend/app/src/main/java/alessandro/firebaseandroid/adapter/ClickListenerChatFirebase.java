package alessandro.firebaseandroid.adapter;

import android.view.View;

public interface ClickListenerChatFirebase {


    void clickImageChat(View view, int position,String nameUser,String urlPhotoUser,String urlPhotoClick);


    void clickImageMapChat(View view, int position,String latitude,String longitude);

    void clickVoiceChat(View view, int position,String urlVoiceClick,String fileName);

    void clickTrans(View view, int position,String urlVoiceClick,String fileName);

}
