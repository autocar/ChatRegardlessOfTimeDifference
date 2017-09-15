package alessandro.firebaseandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;



import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import alessandro.firebaseandroid.model.ChatModel;
import alessandro.firebaseandroid.model.UserModel;
import alessandro.firebaseandroid.view.LoginActivity;

public class User_list extends AppCompatActivity {


    static final String USER_REFERENCE = "usermode";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        final ListView listView = (ListView) findViewById(R.id.listView);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        // Assign adapter to ListView
        listView.setAdapter(adapter);

        // Connect to the Firebase database
        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        // Get a reference to the todoItems child items it the database
        final DatabaseReference myRef = database.getReference("usermode");

        myRef.addChildEventListener(new ChildEventListener(){

            // This function is called once for each child that exists
            // when the listener is added. Then it is called
            // each time a new child is added.
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                //String value = dataSnapshot.getValue(String.class);
                ChatModel user = dataSnapshot.getValue(ChatModel.class);
                //user.setName();
                String value = dataSnapshot.getKey();
                String time_HHMMSS = getTime_HHMMSS(user.getMessage());
                //boolean t = false;
             //   DatabaseReference Ref = database.getReferenceFromUrl("usermode").child(value).child("userModel").child("name");
                //String name = dataSnapshot.child(value).child("id").toString();

              //  String name2 = dataSnapshot.child(value).child("userModel").child("name").getValue().toString();
                String name = user.getId();
               //if(!name.isEmpty()) {
                    adapter.add(user.getUserModel().getName() + "     time now: "+ time_HHMMSS );
              //  }
            }

            // This function is called each time a child item is removed.
            public void onChildRemoved(DataSnapshot dataSnapshot){

                String value = dataSnapshot.getValue(String.class);
                adapter.remove(value);
            }

            // The following functions are also required in ChildEventListener implementations.
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName){}
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName){}

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("TAG:", "Failed to read value.", error.toException());
            }



        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                startActivity(new Intent(User_list.this, MainActivity.class));

                Query myQuery = myRef.orderByValue().equalTo((String)
                        listView.getItemAtPosition(position));

                myQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                         //   DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                           // firstChild.getRef().removeValue();
                       //     startActivity(new Intent(User_list.this, User_list.class));

                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }

                })
                ;}
        })
        ;}



    private String getTime_HHMMSS(String timeZone){


        Date date = new Date();

        SimpleDateFormat sdfZ = new SimpleDateFormat("HH:mm:ss", Locale.US);

        // System.out.println(sdfZ.format(date));
        sdfZ.setTimeZone(TimeZone.getTimeZone(timeZone));

        return sdfZ.format(date);
    }


}
