package com.example.bikemap;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    AppCompatActivity thisActivity;

    BottomNavigationView bottomNavigationView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        BottomNavigationView bottomNavigationMenu = (BottomNavigationView)findViewById(R.id.userBottomNavigationView);
        bottomNavigationMenu.setSelectedItemId(bottomNavigationMenu.getMenu().getItem(1).getItemId());

        thisActivity = this;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        TextView nickname_tv = (TextView) findViewById(R.id.nickname_tv);

        Button setting_tilt = (Button) findViewById(R.id.tilt_button);
        TextView pointView = (TextView) findViewById(R.id.point_num);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        Thread getUserData = new Thread(){
            @Override
            public void run(){
                mDatabase.child("user").child(uid).child("nick-name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        nickname_tv.setText(task.getResult().getValue().toString());
                    }
                });

                mDatabase.child("user").child(uid).child("point").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        pointView.setText(task.getResult().getValue().toString());
                    }
                });
            }
        };
        getUserData.start();

        setting_tilt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TiltActivity.class);
                startActivity(intent);	// intent 에 명시된 액티비티로 이동
            }
        });
        bottomNavigationView = findViewById(R.id.userBottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.navigation_user);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener(){
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.navigation_home:{
                        Intent intent = new Intent();
                        intent.putExtra("result", "trans");
                        setResult(RESULT_OK, intent);
                        thisActivity.finish();
                        return true;
                    }
                }
                return false;
            }
        });

        Button logout = (Button)findViewById(R.id.logout);
        logout.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                SharedPreferences.Editor loginEdit = auto.edit();
                loginEdit.clear();
                loginEdit.commit();
                Intent intent = new Intent();
                intent.putExtra("result", "logout");
                setResult(RESULT_OK, intent);
                intent = new Intent(UserActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                thisActivity.finish();
            }
        });
        Button signOut = (Button)findViewById(R.id.signout);
        signOut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        databaseReference.child("user").child(uid).setValue(null);
                        mAuth.signOut();
                        logout.performClick();
                    }
                });
            }
        });
        CheckBox autoLogin = (CheckBox) findViewById(R.id.autoLogin);
        if(getSharedPreferences("autoLogin", Activity.MODE_PRIVATE).getString("userEmail", null) != null) {
            autoLogin.setChecked(true);
        }
        autoLogin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(autoLogin.isChecked()) {
                    SharedPreferences lastLogin = getSharedPreferences("lastLogin", Activity.MODE_PRIVATE);
                    SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor loginEdit = auto.edit();
                    loginEdit.putString("userEmail", lastLogin.getString("userEmail", null));
                    loginEdit.putString("userPw", lastLogin.getString("userPw", null));
                    loginEdit.commit();
                } else {
                    SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                    SharedPreferences.Editor loginEdit = auto.edit();
                    loginEdit.clear();
                    loginEdit.commit();
                }
            }
        });
    }
}