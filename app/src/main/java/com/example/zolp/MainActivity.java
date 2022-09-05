package com.example.zolp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    public FirebaseAuth mAuth;
    private TextView userName;
    private LinearLayout userMenu, userView;
    public MaterialToolbar toolbar;
    private FirebaseUser user;
    private boolean isMenuOpened = false;
    private Animation downMenu, upMenu;
    public boolean inGallery = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.user_name);
        userView = findViewById(R.id.user_view);
        toolbar = findViewById(R.id.toolbar);
        userMenu = findViewById(R.id.user_menu);
        Button gallery = findViewById(R.id.galley_btn);
        Button logout = findViewById(R.id.logout_btn);
        mAuth = FirebaseAuth.getInstance();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        toolbar.setBackgroundColor(getResources().getColor(R.color.white));


        downMenu = AnimationUtils.loadAnimation(this, R.anim.animation_down);
        upMenu = AnimationUtils.loadAnimation(this, R.anim.animation_up);

        upMenu.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                userMenu.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isMenuOpened) {
                    userMenu.startAnimation(upMenu);
                }
                else{
                    userMenu.setVisibility(View.VISIBLE);
                    userMenu.bringToFront();
                    userMenu.startAnimation(downMenu);
                }

                isMenuOpened = !isMenuOpened;
            }
        });


        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMenu.setVisibility(View.INVISIBLE);
                isMenuOpened = !isMenuOpened;
                logoutProcess();
            }
        });


        findViewById(R.id.fragment_content_main).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });



        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userMenu.startAnimation(upMenu);
                isMenuOpened = !isMenuOpened;
                if (!inGallery) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new GalleryFragment()).addToBackStack(null).commit();
                }
            }
        });

    }

    public FirebaseUser checkAuth(){
        user = mAuth.getCurrentUser();
        if(user != null){ //로그인 중
            userView.setVisibility(View.VISIBLE);
            userName.setText(user.getDisplayName() + " 님");
            toolbar.setTitle("");
        }
        else {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new LoginFragment()).commit();
            userView.setVisibility(View.INVISIBLE);
            toolbar.setTitle("로그인");
        }
        return user;
    }

    private void logoutProcess(){
        user = mAuth.getCurrentUser();
        if(user != null) {
            mAuth.signOut();
        }
        checkAuth();
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect rect = new Rect(), rect2 = new Rect();
        userMenu.getGlobalVisibleRect(rect);
        userView.getGlobalVisibleRect(rect2);
        rect.union(rect2);
        if (!rect.contains((int) ev.getX(), (int) ev.getY()) && ev.getAction() == MotionEvent.ACTION_UP) {
            if (isMenuOpened) {
                userMenu.startAnimation(upMenu);
                isMenuOpened = !isMenuOpened;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard(){
        if(getCurrentFocus()!=null) {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getCurrentFocus().clearFocus();
        }
    }


    @Override
    public void onActivityReenter(int resultCode, Intent data) {
        if(resultCode == RESULT_OK && data != null){
            int endIndex = data.getIntExtra("end_index", -1);
        }
    }


}