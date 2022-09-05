package com.example.zolp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class ItemFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item, container, false);

        ViewPager2 pager = view.findViewById(R.id.view_pager);

        ArrayList<RestaurantInfo> list = new ArrayList<>();
        RestaurantInfo info = new RestaurantInfo(0, "hihi", "", "서울특별시 마포구", "010-1111-1111", "https://www.naver.com", new String[]{"파스타", "피자"});
        for(int i = 0;i<5;i++) {
            list.add(info);
        }
        RecommendViewAdapter adapter = new RecommendViewAdapter(list);
        adapter.setOnItemClickListener(new OnItemClickListener(){
            @Override
            public void itemClick(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adapter.getItem(position).webUrl));

                if(getActivity()!=null) {
                    getActivity().startActivity(intent);
                }
            }
        });
        pager.setAdapter(adapter);

        pager.setOffscreenPageLimit(3);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(80));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });
        pager.setPageTransformer(compositePageTransformer);
        RecyclerView rv = (RecyclerView)pager.getChildAt(0);
        rv.setPadding(30,0,30,0);
        rv.setClipToPadding(false);

        return view;
    }
}