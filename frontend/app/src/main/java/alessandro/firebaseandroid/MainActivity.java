package alessandro.firebaseandroid;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.media.MediaRecorder;
import android.media.MediaPlayer;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.Date;

import alessandro.firebaseandroid.adapter.ChatFirebaseAdapter;
import alessandro.firebaseandroid.adapter.ClickListenerChatFirebase;
import alessandro.firebaseandroid.model.ChatModel;
import alessandro.firebaseandroid.model.FileModel;
import alessandro.firebaseandroid.model.MapModel;
import alessandro.firebaseandroid.model.UserModel;
import alessandro.firebaseandroid.util.Util;
import alessandro.firebaseandroid.view.FullScreenImageActivity;
import alessandro.firebaseandroid.view.LoginActivity;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.util.Locale;
import  android.app.AlertDialog;
import android.content.DialogInterface;
import 	android.app.Fragment;

import static android.R.attr.timeZone;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ClickListenerChatFirebase {

    private static final int IMAGE_GALLERY_REQUEST = 1;
    private static final int IMAGE_CAMERA_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 3;

    static final String TAG = MainActivity.class.getSimpleName();
    static final String CHAT_REFERENCE = "chatmodel";
    static final String USER_REFERENCE = "usermode";
    //Firebase and GoogleApiClient
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private GoogleApiClient mGoogleApiClient;
    private DatabaseReference mFirebaseDatabaseReference;
    FirebaseStorage storage = FirebaseStorage.getInstance();

    //CLass Model
    private UserModel userModel;
    //private String targetID = "tAfwfEJMGeRTbAS1SrH9XSfkXQg2";
    private String readID;
    private String readName = null;
    private String readTime = null;

    //Views UI
    private RecyclerView rvListMessage;
    private LinearLayoutManager mLinearLayoutManager;
    private ImageView btSendMessage,btEmoji;
    private EmojiconEditText edMessage;
    private View contentRoot;
    private EmojIconActions emojIcon;


    private ImageButton mRecordBtn;


    private MediaRecorder mRecorder;
    private AudioRecord mAudioRecord;


    private String mFileName = null;
    private String mFileName2 = null;
    //File
    private File filePathImageCamera;

    private static final String LOG_TAG = "Record Log";

    private StorageReference mStorage;
    private ProgressDialog mProgress;

    private MediaPlayer mMediaPlayer;

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private String token;
    private String id_send;


    private File audioFile;
    final int CHANNEL = AudioFormat.CHANNEL_CONFIGURATION_MONO;    //CHANNEL_CONFIGURATION_MONO   CHANNEL_IN_MONO
    final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    final int SAMPLE_RATE_CANDIDATES = 16000;
    private boolean isRecording=true, isPlaying=false;
    private RecordTask recorder;
    private PlayTask player;
    private File sampleDir;
    private File localFile;
    private byte[] buffer;


    private Date datePoint = new Date();
    private boolean isWarned = false;
    private long WARNTIME = 60000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        //mStorage = FirebaseStorage.getInstance().getReference();
        mStorage = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_AUD);

        mRecordBtn = (ImageButton) findViewById(R.id.buttonVoice);

        mProgress = new ProgressDialog(this);





        mRecordBtn.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent){
                if(motionEvent.getAction() == motionEvent.ACTION_DOWN)
                {
                    mProgress.setMessage("Recording Audio.....");
                    mProgress.show();
                    startRecording();


                }else if(motionEvent.getAction() == motionEvent.ACTION_UP)
                {


                    stopRecording();
                    mProgress.dismiss();
                    uploadAudio();
                }
                else
                {

                }


                return false;
            }
        });


        if (!Util.verificaConexao(this)){
            Util.initToast(this,"no internet");
            finish();
        }else{
            bindViews();
            verificaUsuarioLogado();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, this)
                    .addApi(Auth.GOOGLE_SIGN_IN_API)
                    .build();
        }
        if(userModel != null) {
            sendUserTimezone();
        }
        if(userModel != null) {
            token = FirebaseInstanceId.getInstance().getToken();
            Log.i(TAG,"get token **************   "+token);
            id_send = mFirebaseUser.getUid();
            Log.i(TAG,"get id ***************   "+id_send);
            //registerToken(token, mFirebaseUser.getUid());
            PostTask task = new PostTask();
            task.execute(token, id_send);
        }
        if(userModel != null) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference("usermode");
            myRef.addChildEventListener(new ChildEventListener(){

                // This function is called once for each child that exists
                // when the listener is added. Then it is called
                // each time a new child is added.
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

                    ChatModel user = dataSnapshot.getValue(ChatModel.class);
                    if(userModel.getId() != user.getId()){
                        readID = user.getId();
                        readTime = user.getMessage();
                        readName = user.getUserModel().getName();
                        Log.i(TAG,"read ID ###################################################################################   "+readTime);

                    }

                }

                // This function is called each time a child item is removed.
                public void onChildRemoved(DataSnapshot dataSnapshot){

                    ChatModel user = dataSnapshot.getValue(ChatModel.class);
                    if(userModel.getId() != user.getId()){
                        readID = user.getId();
                        readTime = user.getMessage();
                        readName = user.getUserModel().getName();
                        Log.i(TAG,"read ID ###################################################################################   "+readTime);
                    }

                }

                public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName){
                    ChatModel user = dataSnapshot.getValue(ChatModel.class);
                    if(userModel.getId() != user.getId()){
                        readID = user.getId();
                        readTime = user.getMessage();
                        readName = user.getUserModel().getName();
                        Log.i(TAG,"read ID ###################################################################################   "+readTime);
                    }
                }
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName){}

                @Override
                public void onCancelled(DatabaseError error) {
                    // Failed to read value
                    Log.w("TAG:", "Failed to read value.", error.toException());
                }



            });

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        StorageReference storageRef = storage.getReferenceFromUrl(Util.URL_STORAGE_REFERENCE).child(Util.FOLDER_STORAGE_IMG);

        if (requestCode == IMAGE_GALLERY_REQUEST){
            if (resultCode == RESULT_OK){
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null){
                    sendFileFirebase(storageRef,selectedImageUri);
                }else{
                    //URI IS NULL
                }
            }
        }else if (requestCode == IMAGE_CAMERA_REQUEST){
            if (resultCode == RESULT_OK){
                if (filePathImageCamera != null && filePathImageCamera.exists()){
                    StorageReference imageCameraRef = storageRef.child(filePathImageCamera.getName()+"_camera");
                    sendFileFirebase(imageCameraRef,filePathImageCamera);
                }else{
                    //IS NULL
                }
            }
        }else if (requestCode == PLACE_PICKER_REQUEST){
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                if (place!=null){
                    LatLng latLng = place.getLatLng();
                    MapModel mapModel = new MapModel(latLng.latitude+"",latLng.longitude+"");
                    ChatModel chatModel = new ChatModel(userModel,Calendar.getInstance().getTime().getTime()+"",mapModel);
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                }else{
                    //PLACE IS NULL
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.sendPhoto:
                verifyStoragePermissions();
                photoCameraIntent();
                break;
            case R.id.sendPhotoGallery:
                photoGalleryIntent();
                break;
            case R.id.sign_out:
                signOut();
                break;
            case R.id.time_there:
                String time_HHMMSS = getTime_HHMMSS(readTime);
                showTimeDialog(time_HHMMSS);
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Util.initToast(this,"Google Play Services error.");
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonMessage:
                if (!edMessage.getText().toString().isEmpty()){
                    sendMessageFirebase();
                    sendUserTimezone();
                }
                break;
        }
    }

    @Override
    public void clickImageChat(View view, int position,String nameUser,String urlPhotoUser,String urlPhotoClick) {
        Intent intent = new Intent(this,FullScreenImageActivity.class);
        intent.putExtra("nameUser",nameUser);
        intent.putExtra("urlPhotoUser",urlPhotoUser);
        intent.putExtra("urlPhotoClick",urlPhotoClick);
        startActivity(intent);
    }

    @Override
    public void clickImageMapChat(View view, int position,String latitude,String longitude) {
        String uri = String.format("geo:%s,%s?z=17&q=%s,%s", latitude,longitude,latitude,longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(intent);
    }

    @Override
    public void clickVoiceChat(View view, int position,String urlVoiceClick,String fileName) {
        //ChatModel model = new ChatModel(userModel, "Play Audio!", Calendar.getInstance().getTime().getTime() + "", null);
        //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);

        mProgress.setMessage("Downloading the Audio.....");
        mProgress.show();
        try{
            StorageReference gsReference = storage.getReferenceFromUrl(urlVoiceClick);
            sampleDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            //String sampleDir = mFileName = Environment.getExternalStorageDirectory().getAbsolutePath();
            Log.i(TAG,"create 3gp file");
            localFile = File.createTempFile(fileName + "_download", ".flac", sampleDir);
            Log.i(TAG,"created!");
            gsReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    // Local temp file has been created
                    Log.i(TAG,"onSuccess download file");
                    mProgress.dismiss();
                    player = new PlayTask();
                    player.execute();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG,"onFailure download file ");
                    // Handle any errors
                    mProgress.dismiss();
                }
            });
        } catch (IOException e){
            mProgress.dismiss();
            e.printStackTrace();
        }


    }

    @Override
    public void clickTrans(View view, int position,String urlVoiceClick,String fileName){
        Log.i(LOG_TAG,"get in clickTrans*******************************************************************");
        mProgress.setMessage("Transforming Speech to Text.....");
        mProgress.show();
        TransTask task = new TransTask();
        //String trans = task.execute(urlVoiceClick);
        task.execute(urlVoiceClick);

        //transAndShowDialog(trans);
    }


    private void uploadAudio()
    {
//        delFile.delete();
//        return false;************************************************************************************************************************
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ChatModel post = dataSnapshot.getValue(ChatModel.class);
                // ...
                if(isWarned == false) {
                    if (userModel.getId() != post.getId()) {
                        String date_time = getTime(post.getMessage());
                        String time_HHMMSS = getTime_HHMMSS(post.getMessage());

                        int hour = Integer.valueOf(date_time);
                        if (hour >= 23 || hour <= 8) {
                            createAndShowDialog_audio(time_HHMMSS, mFileName);
                        } else {
                            mProgress.setMessage("Uploading Audio.....");
                            mProgress.show();
                            Uri uri = Uri.fromFile(new File(mFileName));

                            StorageReference audioRef = mStorage.child(mFileName2);
                            UploadTask uploadTask = audioRef.putFile(uri);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                                    mProgress.dismiss();
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.i(TAG, "onSuccess sendFileFirebase");
                                    Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                    FileModel fileModel = new FileModel("voc", downloadUrl.toString(), mFileName2, "");
                                    ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                                    mProgress.dismiss();
                                    PushTask task = new PushTask();
                                    task.execute("Voice message from "+userModel.getName(), readID);
                                    //Log.i(TAG, "user ID############################################################"+userModel.getId());
                                }
                            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    Log.i(TAG, "ProgressListener*********************************************************************************");
                                    if (taskSnapshot.getBytesTransferred() == taskSnapshot.getTotalByteCount()) {
                                        Log.i(TAG, "finish upload audio file*********************************************************************************");
                                        File delFile = new File(mFileName);
                                        if (delFile.isFile() && delFile.exists()) {
                                            delFile.delete();
                                            //Log.i(TAG,"audio file exist");
                                        }
                                    }

                                }
                            });
                        }

                    }
                }else{
                    Date nowDate = new Date(System.currentTimeMillis());
                    long diff = nowDate.getTime() - datePoint.getTime();
                    if (diff > WARNTIME){
                        isWarned = false;
                    }
                    mProgress.setMessage("Uploading Audio.....");
                    mProgress.show();
                    Uri uri = Uri.fromFile(new File(mFileName));

                    StorageReference audioRef = mStorage.child(mFileName2);
                    UploadTask uploadTask = audioRef.putFile(uri);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                            mProgress.dismiss();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i(TAG, "onSuccess sendFileFirebase");
                            Uri downloadUrl = taskSnapshot.getDownloadUrl();
                            FileModel fileModel = new FileModel("voc", downloadUrl.toString(), mFileName2, "");
                            ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                            mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                            mProgress.dismiss();
                            PushTask task = new PushTask();
                            task.execute("Voice message from "+userModel.getName(), readID);
                        }
                    }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            Log.i(TAG, "ProgressListener*********************************************************************************");
                            if (taskSnapshot.getBytesTransferred() == taskSnapshot.getTotalByteCount()) {
                                Log.i(TAG, "finish upload audio file*********************************************************************************");
                                File delFile = new File(mFileName);
                                if (delFile.isFile() && delFile.exists()) {
                                    delFile.delete();
                                    //Log.i(TAG,"audio file exist");
                                }
                            }

                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mFirebaseDatabaseReference.child(USER_REFERENCE).child(readID).addListenerForSingleValueEvent(postListener);


    }



    private void sendFileFirebase(StorageReference storageReference, final Uri file){

        //*******************************************************************************************************************************************************
        final StorageReference s = storageReference;
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ChatModel post = dataSnapshot.getValue(ChatModel.class);
                // ...
                if(isWarned == false) {
                    if (userModel.getId() != post.getId()) {
                        String date_time = getTime(post.getMessage());
                        String time_HHMMSS = getTime_HHMMSS(post.getMessage());

                        int hour = Integer.valueOf(date_time);
                        if (hour >= 23 || hour <= 8) {
                            createAndShowDialog_uri(time_HHMMSS, s, file);
                        } else {
                            if (s != null) {
                                final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
                                StorageReference imageGalleryRef = s.child(name + "_gallery");
                                UploadTask uploadTask = imageGalleryRef.putFile(file);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Log.i(TAG, "onSuccess sendFileFirebase");
                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                                        ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                                        PushTask task = new PushTask();
                                        task.execute("A picture from "+userModel.getName(), readID);
                                    }
                                });
                            } else {
                                //IS NULL
                            }
                        }

                    }
                }else{
                    Date nowDate = new Date(System.currentTimeMillis());
                    long diff = nowDate.getTime() - datePoint.getTime();
                    if (diff > WARNTIME){
                        isWarned = false;
                    }
                    if (s != null) {
                        final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
                        StorageReference imageGalleryRef = s.child(name + "_gallery");
                        UploadTask uploadTask = imageGalleryRef.putFile(file);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.i(TAG, "onSuccess sendFileFirebase");
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                FileModel fileModel = new FileModel("img", downloadUrl.toString(), name, "");
                                ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                                mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                                PushTask task = new PushTask();
                                task.execute("A picture from "+userModel.getName(), readID);
                            }
                        });
                    } else {
                        //IS NULL
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mFirebaseDatabaseReference.child(USER_REFERENCE).child(readID).addListenerForSingleValueEvent(postListener);

    }


    private void sendFileFirebase(StorageReference storageReference, final File file){

        final StorageReference s = storageReference;

        //*******************************************************************************************************************************************************
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ChatModel post = dataSnapshot.getValue(ChatModel.class);
                // ...
                if(isWarned == false) {
                    if (userModel.getId() != post.getId()) {
                        String date_time = getTime(post.getMessage());
                        String time_HHMMSS = getTime_HHMMSS(post.getMessage());

                        int hour = Integer.valueOf(date_time);
                        if (hour >= 23 || hour <= 8) {
                            createAndShowDialog_file(time_HHMMSS, s, file);
                        } else {
                            if (s != null) {
                                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                        BuildConfig.APPLICATION_ID + ".provider",
                                        file);
                                UploadTask uploadTask = s.putFile(photoURI);
                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                                    }
                                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Log.i(TAG, "onSuccess sendFileFirebase");
                                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                        FileModel fileModel = new FileModel("img", downloadUrl.toString(), file.getName(), file.length() + "");
                                        ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                                        PushTask task = new PushTask();
                                        task.execute("A picture from "+userModel.getName(), readID);
                                    }
                                });
                            } else {
                                //IS NULL
                            }
                        }

                    }
                }else{
                    Date nowDate = new Date(System.currentTimeMillis());
                    long diff = nowDate.getTime() - datePoint.getTime();
                    if (diff > WARNTIME){
                        isWarned = false;
                    }
                    if (s != null) {
                        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                                BuildConfig.APPLICATION_ID + ".provider",
                                file);
                        UploadTask uploadTask = s.putFile(photoURI);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure sendFileFirebase " + e.getMessage());
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.i(TAG, "onSuccess sendFileFirebase");
                                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                                FileModel fileModel = new FileModel("img", downloadUrl.toString(), file.getName(), file.length() + "");
                                ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime() + "", fileModel);
                                mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                                PushTask task = new PushTask();
                                task.execute("A picture from "+userModel.getName(), readID);
                            }
                        });
                    } else {
                        //IS NULL
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mFirebaseDatabaseReference.child(USER_REFERENCE).child(readID).addListenerForSingleValueEvent(postListener);

    }


    private void locationPlacesIntent(){
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    private void photoCameraIntent(){
        String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
        filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), nomeFoto+"camera.jpg");
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                BuildConfig.APPLICATION_ID + ".provider",
                filePathImageCamera);
        it.putExtra(MediaStore.EXTRA_OUTPUT,photoURI);
        startActivityForResult(it, IMAGE_CAMERA_REQUEST);
    }

//    private void voiceRecordIntent() {
//       // String nomeFoto = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
//      //  filePathImageCamera = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), nomeFoto+"voice.jpg");
//    }





    private void photoGalleryIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture_title)), IMAGE_GALLERY_REQUEST);
    }



    private void sendUserTimezone(){

        ChatModel umodel = new ChatModel(userModel, getCurrentTimeZone(), Calendar.getInstance().getTime().getTime() + "", null);
        mFirebaseDatabaseReference.child(USER_REFERENCE).child(userModel.getId()).setValue(umodel);

    }

    private String getCurrentTimeZone(){
        TimeZone tz = TimeZone.getDefault();
        String strTz = tz.getDisplayName(false, TimeZone.SHORT);
        return strTz;
    }


    void createAndShowDialog(String time_HHMMSS) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warnning!");
        builder.setMessage("Maybe he/she is sleeping.\nCurrent time is " + time_HHMMSS + " there.");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isWarned = true;
                datePoint.setTime(System.currentTimeMillis());
                ChatModel model = new ChatModel(userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
                mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                PushTask task = new PushTask();
                task.execute(userModel.getName()+": " + edMessage.getText().toString(), readID);
                edMessage.setText(null);
            }
        });
        //builder.setNeutralButton("Delay", this);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //ChatModel model = new ChatModel(userModel, "Delay", Calendar.getInstance().getTime().getTime() + "", null);
                //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                edMessage.setText(null);
            }
        });
        builder.create().show();
    }

    void transAndShowDialog(String trans) {
        Log.i(LOG_TAG,"show dialog *******************************************************************");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Speech to Text");
        builder.setMessage(trans);
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        builder.create().show();
    }

    void showTimeDialog(String t) {
        Log.i(LOG_TAG,"show dialog *******************************************************************");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("His/Her Time Now");
        builder.setMessage(t);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {


            }
        });

        builder.create().show();
    }

    void createAndShowDialog_file(String time_HHMMSS, StorageReference storageReference, File f) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warnning!");
        builder.setMessage("Maybe he/she is sleeping.\nCurrent time is " + time_HHMMSS + " there.");
        final StorageReference s = storageReference;
        final File filePhoto = f;
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isWarned = true;
                datePoint.setTime(System.currentTimeMillis());
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        filePhoto);
                UploadTask uploadTask = s.putFile(photoURI);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG,"onSuccess sendFileFirebase");
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        FileModel fileModel = new FileModel("img",downloadUrl.toString(),filePhoto.getName(),filePhoto.length()+"");
                        ChatModel chatModel = new ChatModel(userModel,"",Calendar.getInstance().getTime().getTime()+"",fileModel);
                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                        PushTask task = new PushTask();
                        task.execute("A picture from "+userModel.getName(), readID);
                    }
                });
            }
        });
        //builder.setNeutralButton("Delay", this);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //ChatModel model = new ChatModel(userModel, "Delay", Calendar.getInstance().getTime().getTime() + "", null);
                //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                edMessage.setText(null);
            }
        });
        builder.create().show();
    }

    void createAndShowDialog_uri(String time_HHMMSS, StorageReference storageReference, Uri f) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warnning!");
        builder.setMessage("Maybe he/she is sleeping.\nCurrent time is " + time_HHMMSS + " there.");
        final StorageReference s = storageReference;
        final Uri filePhoto = f;

        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isWarned = true;
                datePoint.setTime(System.currentTimeMillis());
                final String name = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString();
                StorageReference imageGalleryRef = s.child(name+"_gallery");
                UploadTask uploadTask = imageGalleryRef.putFile(filePhoto);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG,"onSuccess sendFileFirebase");
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        FileModel fileModel = new FileModel("img",downloadUrl.toString(),name,"");
                        ChatModel chatModel = new ChatModel(userModel,"",Calendar.getInstance().getTime().getTime()+"",fileModel);
                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                        PushTask task = new PushTask();
                        task.execute("A picture from "+userModel.getName(), readID);
                    }
                });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //ChatModel model = new ChatModel(userModel, "Delay", Calendar.getInstance().getTime().getTime() + "", null);
                //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                edMessage.setText(null);
            }
        });
        builder.create().show();
    }


    void createAndShowDialog_audio(String time_HHMMSS, final String fileName) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Warnning!");
        builder.setMessage("Maybe he/she is sleeping.\nCurrent time is " + time_HHMMSS + " there.");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                isWarned = true;
                datePoint.setTime(System.currentTimeMillis());
                mProgress.setMessage("Uploading Audio.....");
                mProgress.show();
                Uri uri = Uri.fromFile(new File(fileName));
                StorageReference audioRef = mStorage.child(mFileName2);
                UploadTask uploadTask = audioRef.putFile(uri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure sendFileFirebase "+e.getMessage());
                        mProgress.dismiss();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG,"onSuccess sendFileFirebase");
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        FileModel fileModel = new FileModel("voc",downloadUrl.toString(),mFileName2,"");
                        ChatModel chatModel = new ChatModel(userModel, "", Calendar.getInstance().getTime().getTime()+"", fileModel);
                        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(chatModel);
                        mProgress.dismiss();
                        PushTask task = new PushTask();
                        task.execute("Voice message from "+userModel.getName(), readID);

                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG,"ProgressListener*********************************************************************************");
                        if(taskSnapshot.getBytesTransferred() == taskSnapshot.getTotalByteCount()){
                            Log.i(TAG,"finish upload audio file*********************************************************************************");
                            File delFile = new File(mFileName);
                            if (delFile.isFile() && delFile.exists()){
                                delFile.delete();
                                Log.i(TAG,"delete audio file*********************************************************************************");
                                //Log.i(TAG,"audio file exist");
                            }
                        }

                    }
                });
            }
        });
        //builder.setNeutralButton("Delay", this);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //ChatModel model = new ChatModel(userModel, "Delay", Calendar.getInstance().getTime().getTime() + "", null);
                //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                edMessage.setText(null);
                File delFile = new File(mFileName);
                if (delFile.isFile() && delFile.exists()){
                    delFile.delete();
                    //Log.i(TAG,"audio file exist");
                }
            }
        });
        builder.create().show();
    }



    private void sendMessageFirebase(){

        //String t = mFirebaseDatabaseReference.child(USER_REFERENCE).child(userModel.getId()).getKey();

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                ChatModel post = dataSnapshot.getValue(ChatModel.class);
                // ...
                if(isWarned == false) {

                    if (userModel.getId() != post.getId()) {
                        String date_time = getTime(post.getMessage());
                        String time_HHMMSS = getTime_HHMMSS(post.getMessage());

                        int hour = Integer.valueOf(date_time);
                        if (hour >= 23 || hour <= 8) {
                            createAndShowDialog(time_HHMMSS);
                        } else {
                            ChatModel model = new ChatModel(userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
                            mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                            PushTask task = new PushTask();
                            task.execute(userModel.getName()+": " + edMessage.getText().toString(), readID);
                            edMessage.setText(null);
                        }

//                    ChatModel model = new ChatModel(userModel,date_time, Calendar.getInstance().getTime().getTime()+"",null);
//                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
//                    edMessage.setText(null);
                    }
                }else{
                    Date nowDate = new Date(System.currentTimeMillis());
                    long diff = nowDate.getTime() - datePoint.getTime();
                    if (diff > WARNTIME){
                        isWarned = false;
                    }
                    ChatModel model = new ChatModel(userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
                    mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
                    PushTask task = new PushTask();
                    task.execute(userModel.getName()+": " + edMessage.getText().toString(), readID);
                    edMessage.setText(null);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mFirebaseDatabaseReference.child(USER_REFERENCE).child(readID).addListenerForSingleValueEvent(postListener);
        //ChatModel model = new ChatModel(userModel,t, Calendar.getInstance().getTime().getTime()+"",null);
        //mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
        //edMessage.setText(null);



//        ChatModel model = new ChatModel(userModel, edMessage.getText().toString(), Calendar.getInstance().getTime().getTime() + "", null);
//        mFirebaseDatabaseReference.child(CHAT_REFERENCE).push().setValue(model);
//        edMessage.setText(null);
    }

    private String getTime(String timeZone){


        Date date = new Date();

        SimpleDateFormat sdfZ = new SimpleDateFormat("HH", Locale.US);

        // System.out.println(sdfZ.format(date));
        sdfZ.setTimeZone(TimeZone.getTimeZone(timeZone));

        return sdfZ.format(date);
    }

    private String getTime_HHMMSS(String timeZone){


        Date date = new Date();

        SimpleDateFormat sdfZ = new SimpleDateFormat("HH:mm:ss", Locale.US);

        // System.out.println(sdfZ.format(date));
        sdfZ.setTimeZone(TimeZone.getTimeZone(timeZone));

        return sdfZ.format(date);
    }


    private void lerMessagensFirebase(){
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        final ChatFirebaseAdapter firebaseAdapter = new ChatFirebaseAdapter(mFirebaseDatabaseReference.child(CHAT_REFERENCE),userModel.getName(),this);
        firebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = firebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    rvListMessage.scrollToPosition(positionStart);
                }
            }
        });
        rvListMessage.setLayoutManager(mLinearLayoutManager);
        rvListMessage.setAdapter(firebaseAdapter);
    }


    private void verificaUsuarioLogado(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null){
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }else{
            userModel = new UserModel(mFirebaseUser.getDisplayName(), mFirebaseUser.getPhotoUrl().toString(), mFirebaseUser.getUid() );
            lerMessagensFirebase();
        }
    }


    private void bindViews(){
        contentRoot = findViewById(R.id.contentRoot);
        edMessage = (EmojiconEditText)findViewById(R.id.editTextMessage);
        btSendMessage = (ImageView)findViewById(R.id.buttonMessage);
        btSendMessage.setOnClickListener(this);
        btEmoji = (ImageView)findViewById(R.id.buttonEmoji);
        emojIcon = new EmojIconActions(this,contentRoot,edMessage,btEmoji);
        emojIcon.ShowEmojIcon();




        rvListMessage = (RecyclerView)findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
    }

    /**
     * Sign Out no login
     */
    private void signOut(){
        mFirebaseAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     */
    public void verifyStoragePermissions() {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }else{
            // we already have permission, lets go ahead and call camera intent
         //   photoCameraIntent();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode){
            case REQUEST_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    photoCameraIntent();
                }
                break;
        }
    }




    private class RecordTask extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... arg0) {
            isRecording = true;
            mFileName = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
            //String nameRcd = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString() + ".flac";
            mFileName2 = DateFormat.format("yyyy-MM-dd_hhmmss", new Date()).toString() + ".pcm";
            mFileName += mFileName2;
            audioFile = new File(mFileName);
            try {
                //开通输出流到指定的文件
                DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(audioFile)));
                //根据定义好的几个配置，来获取合适的缓冲大小
                int bufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE_CANDIDATES, CHANNEL, ENCODING);
                //实例化AudioRecord
                mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE_CANDIDATES, CHANNEL, ENCODING, bufferSize);
                //定义缓冲
                buffer = new byte[bufferSize];

                //开始录制
                mAudioRecord.startRecording();

                int r = 0; //存储录制进度
                //定义循环，根据isRecording的值来判断是否继续录制
                while(isRecording){
                    //从bufferSize中读取字节，返回读取的short个数
                    //这里老是出现buffer overflow，不知道是什么原因，试了好几个值，都没用，TODO：待解决
                    int bufferReadResult = mAudioRecord.read(buffer, 0, buffer.length);
                    //循环将buffer中的音频数据写入到OutputStream中
                    for(int i=0; i<bufferReadResult; i++){
                        dos.writeByte(buffer[i]);
                    }
                    publishProgress(new Integer(r)); //向UI线程报告当前进度
                    r++; //自增进度值
                }
                //录制结束
                mAudioRecord.stop();
                mAudioRecord.release();
                Log.v("The DOS available:", "::"+audioFile.length());
                dos.close();
                buffer = null;
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }


    }



    private class PlayTask extends AsyncTask<Void, Integer, Void>{
        @Override
        protected Void doInBackground(Void... arg0) {
            isPlaying = true;
            int bufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE_CANDIDATES, CHANNEL, ENCODING);
            byte[] buffer = new byte[bufferSize/4];
            try {
                //定义输入流，将音频写入到AudioTrack类中，实现播放
                DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(localFile)));
                //实例AudioTrack
                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE_CANDIDATES, CHANNEL, ENCODING, bufferSize, AudioTrack.MODE_STREAM);
                //开始播放
                track.play();
                //由于AudioTrack播放的是流，所以，我们需要一边播放一边读取
                while(isPlaying && dis.available()>0){
                    int i = 0;
                    while(dis.available()>0 && i<buffer.length){
                        buffer[i] = dis.readByte();
                        i++;
                    }
                    //然后将数据写入到AudioTrack中
                    track.write(buffer, 0, buffer.length);

                }

                //播放结束
                track.stop();
                dis.close();
                localFile.delete();
            } catch (Exception e) {
                // TODO: handle exception
            }
            return null;
        }


    }


    private void startRecording() {
        recorder = new RecordTask();
        recorder.execute();


    }

    private void stopRecording() {
//        mRecorder.stop();
//        mRecorder.release();
//        mRecorder = null;
        this.isRecording = false;
    }

    public class PostTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                registerToken(params[0], params[1]);
                return "true";
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

//        protected void onPostExecute(String getResponse) {
//            System.out.println(getResponse);
//        }

        private void registerToken(String token, String id) {
            Log.i(LOG_TAG,"sending token*******************************************************************");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("Token",token)
                    .add("ID", id)
                    .build();

            Request request = new Request.Builder()
                    .url("http://chat-166522.appspot.com/")
                    .post(body)
                    .build();

            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                Log.e(LOG_TAG,"error  cant send*******************************************************************");
                e.printStackTrace();
            }
            Log.i(LOG_TAG,"sent token*******************************************************************");
        }
    }


    public class PushTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                pushNotification(params[0], params[1]);
                return "true";
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

//        protected void onPostExecute(String getResponse) {
//            System.out.println(getResponse);
//        }

        private void pushNotification(String msg, String id) {
           // Log.i(LOG_TAG,"sending token*******************************************************************");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("MSG",msg)
                    .add("ID", id)
                    .build();

            Request request = new Request.Builder()
                    .url("http://chat-166522.appspot.com/push")
                    .post(body)
                    .build();

            try {
                client.newCall(request).execute();
            } catch (IOException e) {
                Log.e(LOG_TAG,"error  cant send*******************************************************************");
                e.printStackTrace();
            }
            //Log.i(LOG_TAG,"sent token*******************************************************************");
        }
    }



    public class TransTask extends AsyncTask<String, Void, String> {
        private Exception exception;

        protected String doInBackground(String... params) {
            try {
                Log.i(LOG_TAG,"to post trans*******************************************************************");
                String getResponse = trans2t(params[0]);
                Log.i(LOG_TAG,"transed *******************************************************************"+getResponse);
                //transAndShowDialog(getResponse);
                //createAndShowDialog(getResponse);
                return getResponse;
            } catch (Exception e) {
                this.exception = e;
                return null;
            }
        }

        protected void onPostExecute(String getResponse) {
            mProgress.dismiss();
            transAndShowDialog(getResponse);
            //return result;
        }

//        protected void onPostExecute(String getResponse) {
//            System.out.println(getResponse);
//        }

        private String trans2t(String url) {
            //Log.i(LOG_TAG,"sending token*******************************************************************");
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("URL",url)
                    .build();

            Request request = new Request.Builder()
                    .url("http://chat-166522.appspot.com/stt")
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();

                String res = response.body().string();
                Log.i(LOG_TAG,"response*******************************************************************"+res);
                return res;
            } catch (IOException e) {
                Log.e(LOG_TAG,"error  cant send*******************************************************************");
                e.printStackTrace();
                return null;
            }
            // Log.i(LOG_TAG,"sent token*******************************************************************");
        }
    }





}
