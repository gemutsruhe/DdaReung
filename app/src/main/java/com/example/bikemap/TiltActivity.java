package com.example.bikemap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TiltActivity extends AppCompatActivity {
    boolean isHardMode = false; // 높은 경사도 체크 여부
    boolean isSave = false; // 정보 저장
    String str1, str2;
    double prevYellowGradient, yellowGradient;
    double prevRedGradient, redGradient;
    EditText yellowTilt, redTilt;
    CheckBox tiltCheck;
    Button set_button;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tilt);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initSetting();
    }

    private void initSetting() {
        yellowTilt = findViewById(R.id.yellow_tilt);
        redTilt = findViewById(R.id.red_tilt);
        tiltCheck = findViewById(R.id.tiltcheck);
        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Thread getData = new Thread() {
            public void run(){
                databaseReference.child("user").child(uid).child("YellowSlopeMinimum").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        prevYellowGradient = Double.parseDouble(String.valueOf(task.getResult().getValue().toString()));
                        yellowTilt.setText(String.valueOf(prevYellowGradient));
                        yellowTilt.setVisibility(View.VISIBLE);
                    }
                });
                databaseReference.child("user").child(uid).child("RedSlopeMinimum").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        prevRedGradient = Double.parseDouble(String.valueOf(task.getResult().getValue().toString()));
                        redTilt.setText(String.valueOf(prevRedGradient));
                        redTilt.setVisibility(View.VISIBLE);
                    }
                });
                databaseReference.child("user").child(uid).child("tiltCheck").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        tiltCheck.setChecked(Boolean.valueOf(task.getResult().getValue().toString()));
                        redTilt.setText(String.valueOf(prevRedGradient));
                        redTilt.setVisibility(View.VISIBLE);
                    }
                });
            }
        };
        getData.start();

        set_button = findViewById(R.id.save_btn);
        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                yellowGradient = Double.parseDouble(yellowTilt.getText().toString());
                redGradient = Double.parseDouble(redTilt.getText().toString());
                if(prevYellowGradient != yellowGradient) {
                    databaseReference.child("user").child(uid).child("YellowSlopeMinimum").setValue(yellowGradient);
                }
                if(prevRedGradient != redGradient) {
                    databaseReference.child("user").child(uid).child("RedSlopeMinimum").setValue(redGradient);
                }
                databaseReference.child("user").child(uid).child("tiltCheck").setValue(tiltCheck.isChecked());
                finish();
            }
        });
    }
}
