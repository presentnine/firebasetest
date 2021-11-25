package com.example.firebasetest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private Button showButton;
    private EditText message;
    private TextView textView;

    private String customToken;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseUser user;
    private FirebaseMessaging firebaseMessaging;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendButton = findViewById(R.id.sendButton);
        showButton = findViewById(R.id.showButton);
        message = findViewById(R.id.message);
        textView = findViewById(R.id.textView);

        //액티비티 생성되며 파이어베이스 인증권한 초기화
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCustomToken(customToken)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCustomToken:success");
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCustomToken:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        
        //사용자, firebase realtime Database, firebaseMessaging 초기화
        user = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance("https://sososhop-d7574-default-rtdb.firebaseio.com");
        firebaseMessaging = FirebaseMessaging.getInstance();

        ref = firebaseDatabase.getReference();

        //FcmId token 가져오기
        firebaseMessaging.getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        ref.child("FcmId").child(user.getUid()).setValue(token);
                        Toast.makeText(getApplicationContext(), token, Toast.LENGTH_SHORT).show();
                    }
                });

        DatabaseReference userInforRef = ref.child("users").child(user.getUid());

        //유저 접속 활성화
        userInforRef.child("connection").setValue(true);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInforRef.child("content").setValue(message.getText().toString());
            }
        });

        showButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userInforRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserInfor m = snapshot.getValue(UserInfor.class);
                        Toast.makeText(getApplicationContext(), m.content, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        DatabaseReference userInforRef = ref.child("users").child(user.getUid());

        //유저 접속 비활성화
        userInforRef.child("connection").setValue(false);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DatabaseReference userInforRef = ref.child("users").child(user.getUid());

        //유저 접속 비활성화
        userInforRef.child("connection").setValue(true);
    }
}