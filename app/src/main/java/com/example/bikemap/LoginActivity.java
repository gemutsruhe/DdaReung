package com.example.bikemap;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.normal.TedPermission;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private CheckBox autoLogin;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tedPermission();

        EditText logViewEmail = (EditText) findViewById(R.id.login_view_email);
        EditText logViewPw = (EditText)findViewById(R.id.login_view_pw);
        autoLogin = (CheckBox)findViewById(R.id.auto_login_checkbox);
        Button doLoginBtn = (Button)findViewById(R.id.do_login_btn);

        mAuth = FirebaseAuth.getInstance();

        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        String userId = auto.getString("userEmail", null);
        String userPw = auto.getString("userPw", null);

        if(userId != null && userPw != null){
            logViewEmail.setText(userId);
            logViewPw.setText(userPw);
            login(userId, userPw);
        }else{
            doLoginBtn.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View view) {
                    String inputEmail = logViewEmail.getText().toString().trim();
                    String inputPw = logViewPw.getText().toString().trim();
                    login(inputEmail, inputPw);
                }
            });
        }
    }

    private void login(String inputEmail, String inputPw){
        mAuth.signInWithEmailAndPassword(inputEmail, inputPw)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            if(autoLogin.isChecked()) {
                                SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor loginEdit = auto.edit();
                                loginEdit.putString("userEmail", inputEmail);
                                loginEdit.putString("userPw", inputPw);
                                loginEdit.commit();
                            }
                            FirebaseUser user = mAuth.getCurrentUser();
                            System.out.println("TEST : " + user.getUid());
                            SharedPreferences uid = getSharedPreferences("uid", Activity.MODE_PRIVATE);
                            SharedPreferences.Editor uidEdit = uid.edit();
                            uidEdit.putString("uid", user.getUid());
                            uidEdit.commit();
                            Intent intent = new Intent(LoginActivity.this, MapsActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

    }

    //위치 사용 권한 요청
    private void tedPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                // 권한 요청 성공
                //Toast.makeText(LoginActivity.this, "권한 성공", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {

            }
        };

        TedPermission.create()
                .setPermissionListener(permissionListener)
                .setRationaleMessage("앱을 사용하기 위해서는 위치 접근 권한이 필요합니다.")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다.")
                .setPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
                .check();
    }
}