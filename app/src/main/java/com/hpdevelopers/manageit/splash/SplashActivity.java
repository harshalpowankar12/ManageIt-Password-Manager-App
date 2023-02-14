package com.hpdevelopers.manageit.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.hpdevelopers.manageit.R;
import com.hpdevelopers.manageit.auth.LoginActivity;
import com.hpdevelopers.manageit.passwords.PasswordActivity;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {


    //Declare
    TextView appName, appHeadline;
    private static final long SPLASH_TIME_OUT = 2000;
    Animation animationAN, animationHL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //FlagFullScreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        //Initialize
        appName = findViewById(R.id.tvSplAppName);
        appHeadline = findViewById(R.id.tvSplAppHeadline);

        //Set Animation
        animationAN = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
        animationHL = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
        appName.startAnimation(animationAN);
        appHeadline.startAnimation(animationAN);

        //Handler Delayed Action
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //Stay Logged In if User Is Available
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent intent;
                if (user != null) {
                    intent = new Intent(SplashActivity.this, PasswordActivity.class);
                    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                } else {
                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);

    }
}