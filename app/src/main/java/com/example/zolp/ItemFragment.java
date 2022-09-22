package com.example.zolp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class ItemFragment extends Fragment {

    private final ArrayList<RestaurantInfo> list = new ArrayList<RestaurantInfo>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        new Thread() {
            public void run() {
                GetRestaurantList();
            }
        }.start();

        try {
            Thread.sleep(3000); // 크롤링 하는 동안 원래 쓰레드는 잠시 대기. 이부분 콜백함수로 개선 필요함
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        View view = inflater.inflate(R.layout.fragment_item, container, false);

        ViewPager2 pager = view.findViewById(R.id.view_pager);

        RecommendViewAdapter adapter = new RecommendViewAdapter(list);
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adapter.getItem(position).webUrl));

                if (getActivity() != null) {
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
        RecyclerView rv = (RecyclerView) pager.getChildAt(0);
        rv.setPadding(30, 0, 30, 0);
        rv.setClipToPadding(false);

        return view;
    }

    private synchronized void GetRestaurantList() {
        try {
            String URL = "https://pcmap.place.naver.com/restaurant/list?query=";

            String location = "홍대";
            String target = "파스타";

            Document document = Jsoup.connect(URL + location + " " + target).get();
            String rawScript = document.getElementsByTag("script").get(2).toString();
            int start = rawScript.indexOf("window.__APOLLO_STATE__ = ") + "window.__APOLLO_STATE__ = ".length();
            int end = rawScript.indexOf("window.__PLACE_STATE__") - ";\n          ".length();

            String script = rawScript.substring(start, end);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(script);

            Iterator iter = jsonObject.keySet().iterator();
            
            int i = 0;
            while (iter.hasNext() && i < 5) {
                String key = (String) iter.next();
                if (key.contains("RestaurantListSummary")) {
                    JSONObject value = (JSONObject) jsonObject.get(key);
                    assert value != null;
                    String name = (String) value.get("name");
                    String category = (String) value.get("category");
                    String address = (String) value.get("fullAddress");
                    String phone = (String) value.get("phone");
                    String imageUrl = (String) value.get("imageUrl");
                    String routeUrl = (String) value.get("routeUrl");
                    String visitorReviewScore = (String) value.get("visitorReviewScore");
                    list.add(new RestaurantInfo(0, name, imageUrl, address, phone, routeUrl, new String[]{target}));
                    i++;
                }
            }

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}