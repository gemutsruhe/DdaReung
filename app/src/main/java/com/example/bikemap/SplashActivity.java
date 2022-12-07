package com.example.bikemap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(
                    new ActivityResultContracts.RequestMultiplePermissions(),
                    result -> {
                        Boolean fineLocationGranted = result.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        Boolean coarseLocationGranted = result.get(Manifest.permission.ACCESS_COARSE_LOCATION);

                        if (fineLocationGranted != null && fineLocationGranted) {
                        } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        } else {
                            Toast.makeText(this,
                                    "Unable to launch app because location permissions are denied.",
                                    Toast.LENGTH_SHORT
                            ).show();
                            finish();
                        }
                    }
            );
        }
        moveMain(2);
    }

    private void moveMain(int sec) {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                //new Intent(현재 context, 이동할 activity)

                Intent intent = new Intent(getApplicationContext(), LoginOrRegisterActivity.class);

                startActivity(intent);	// intent 에 명시된 액티비티로 이동

                finish();	// 액티비티 종료
            }
        }, 2000 * sec); // 2초 딜레이를 준 후 시작
    }

}
