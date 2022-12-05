package com.example.bikemap;

import android.app.ProgressDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private static final String TAG = "RegisterActivity";

    String email;
    String pw;
    String pwCh;
    String pNumber;
    String nickName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        EditText idEdit = (EditText) findViewById(R.id.login_et_id) ;
        EditText pwEdit = (EditText) findViewById(R.id.login_et_pw);
        EditText pwChEdit =(EditText) findViewById(R.id.login_et_pw_ch);
        EditText eMailEdit = (EditText) findViewById(R.id.login_et_email);
        EditText nickNameEdit = (EditText) findViewById(R.id.login_et_nickname);
        Button joinButton = (Button) findViewById(R.id.joinBtn);
        Button checkEmail = (Button) findViewById(R.id.check_email_duplication);

        pwChEdit.addTextChangedListener(new TextWatcher(){

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(pwEdit.getText().toString().compareTo(pwChEdit.getText().toString()) == 0) {
                    pwChEdit.setTextColor(Color.BLACK);
                } else {
                    pwChEdit.setTextColor(Color.RED);
                }
            }
        });

        checkEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = idEdit.getText().toString();
                if(email != ""){
                    mAuth.fetchSignInMethodsForEmail(email).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                        @Override
                        public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                            boolean isNewUser = task.getResult().getSignInMethods().isEmpty();

                            if (isNewUser) {
                                Toast.makeText(RegisterActivity.this, "사용 가능한 아이디 입니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "이미 존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = idEdit.getText().toString().trim();
                pw = pwEdit.getText().toString().trim();
                pwCh= pwChEdit.getText().toString().trim();
                pNumber = eMailEdit.getText().toString().trim();
                nickName = nickNameEdit.getText().toString().trim();

                if(pw.equals(pwCh)){
                    final ProgressDialog mDialog = new ProgressDialog(RegisterActivity.this);
                    mDialog.setMessage("가입 진행 중입니다...");
                    mDialog.show();

                    mAuth.createUserWithEmailAndPassword(email, pw)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>()  {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        mDialog.dismiss();

                                        FirebaseUser user = mAuth.getCurrentUser();
                                        email = user.getEmail();
                                        String uid = user.getUid();

                                        HashMap userInfo = new HashMap<>();
                                        userInfo.put("e-mail", email);
                                        userInfo.put("phone-number", pNumber);
                                        userInfo.put("nick-name",nickName);
                                        userInfo.put("YellowSlopeMinimum", 4);
                                        userInfo.put("RedSlopeMinimum", 8);
                                        writeUser(uid, userInfo);

                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.putExtra("uid", uid);
                                        startActivity(intent);
                                        finish();
                                        Toast.makeText(RegisterActivity.this, "회원가입에 성공하셨습니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        mDialog.dismiss();
                                        Toast.makeText(RegisterActivity.this, "이미 존재하는 아이디 입니다.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                }
                            });
                }else{
                    Toast.makeText(RegisterActivity.this, "비밀번호가 틀렸습니다. 다시 입력해 주세요", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        });
    }

    private void writeUser(String uid, HashMap userInfo) {
        //User user = new User(email, pnumber,nickname);

        //데이터 저장
        System.out.println("TEST : " + mDatabase);
        mDatabase = mDatabase.child("user").child(uid);
        mDatabase.updateChildren(userInfo);
        /*mDatabase.child(uid).setValue(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() { //데이터베이스에 넘어간 이후 처리
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "저장을 완료했습니다", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "저장에 실패했습니다", Toast.LENGTH_LONG).show();
                    }
                });}*/
    }

}