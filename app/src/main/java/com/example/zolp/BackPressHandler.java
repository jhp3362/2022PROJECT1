package com.example.zolp;

import android.app.Activity;
import android.widget.Toast;

public class BackPressHandler {
    private Activity activity;
    private Toast toast;
    private long backPressedTime = 0;
    public BackPressHandler(Activity activity) {
        this.activity = activity;
    }
    public void onBackPressed(){
        if(System.currentTimeMillis() > backPressedTime + 2000){
            backPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(activity, "\'뒤로\' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
        else {
            toast.cancel();
            activity.finish();
        }
    }
}
