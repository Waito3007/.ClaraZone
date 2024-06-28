package com.hoyo.cz.Activity;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.hoyo.cz.R;
import android.content.Intent;
import android.os.Handler;
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Sử dụng Handler để chuyển sang màn hình chính sau một khoảng thời gian ngắn
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Chuyển sang màn hình chính của ứng dụng
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 2000); // Thời gian hiển thị splash screen, ở đây là 2000 milliseconds (2 giây)
    }
}