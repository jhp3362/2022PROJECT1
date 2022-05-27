package com.example.zolp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private MainFragment mainFragment;
    public FirebaseAuth mAuth;
    LinearLayout userView;
    MaterialToolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userView = findViewById(R.id.user_View);
        toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
       /* mainFragment = new MainFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_content_main, mainFragment).commit();*/
    }
}