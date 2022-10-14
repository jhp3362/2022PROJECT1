package com.example.zolp;

import android.view.View;
import android.widget.Button;

public interface OnItemClickListener {
    void itemClick(View view, int position);
    void setFavorites(Button btn, int position);
    void rejectItem(int position);
    void route(int position);
}