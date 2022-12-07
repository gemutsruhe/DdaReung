package com.example.bikemap;

import android.app.Activity;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class LoginOrRegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_register);

        Button chooseLoginButton = (Button) findViewById(R.id.choose_login_btn);
        Button chooseRegisterButton = (Button) findViewById(R.id.choose_register_btn);
        SharedPreferences auto = getSharedPreferences("autoLogin", Activity.MODE_PRIVATE);
        String userId = auto.getString("userEmail", null);
        String userPw = auto.getString("userPw", null);

        if(userId != null && userPw != null){
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        }

        chooseLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        chooseRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        copyDatabase();
    }

    private void copyDatabase() {

        String DB_PATH = "/data/data/" + getApplicationContext().getPackageName() + "/databases/";
        String DB_NAME = "CrossRoadCoord.db";

        try{
            // 디렉토리가 없으면, 디렉토리를 먼저 생성한다.
            File fDir = new File( DB_PATH );
            if( !fDir.exists() ) { fDir.mkdir(); }

            String strOutFile = DB_PATH + DB_NAME;
            InputStream inputStream = getApplicationContext().getAssets().open( DB_NAME );
            OutputStream outputStream = new FileOutputStream( strOutFile );

            byte[] mBuffer = new byte[1024];
            int mLength;
            while( ( mLength = inputStream.read( mBuffer) ) > 0 ) {
                outputStream.write( mBuffer, 0, mLength );
            }

            outputStream.flush();
            outputStream.close();
            inputStream.close();

        }catch( Exception e ) {
            e.printStackTrace();
        }

    }
}
