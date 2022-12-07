package com.example.bikemap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TiltSetting extends AppCompatActivity {
    boolean isHardMode = false; // 높은 경사도 체크 여부
    boolean isSave = false; // 정보 저장
    String str1, str2;
    double yellow_gradient;
    double red_gradient;
    EditText min, max;
    Button set_button;

    private void initSetting() {

        SharedPreferences current_uid = getSharedPreferences("uid", Activity.MODE_PRIVATE);
        String uid = current_uid.getString("uid", null);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("user").child(uid).child("YellowSlopeMin").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                yellow_gradient = Double.valueOf(String.valueOf(task.getResult().getValue()));
            }
        });
        databaseReference.child("user").child(uid).child("RedSlopeMin").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                red_gradient = Double.valueOf(String.valueOf(task.getResult().getValue()));
            }
        });
        min = findViewById(R.id.tilt_min);
        min.setVisibility(View.VISIBLE);
        min.setText(String.valueOf(yellow_gradient));

        max = findViewById(R.id.tilt_max);
        max.setVisibility(View.VISIBLE);
        max.setText(String.valueOf(red_gradient));

        set_button = findViewById(R.id.tilt_button);
        set_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(yellow_gradient != Double.parseDouble(min.getText().toString())) {

                }
                if(red_gradient != Double.parseDouble(max.getText().toString())) {

                }
                finish();
            }
        });
    }
}
