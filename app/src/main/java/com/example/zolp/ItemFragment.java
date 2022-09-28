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

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class ItemFragment extends Fragment {
    private String location, target;
    private boolean getLocationDone = false, getKeywordDone = false;
    private ViewPager2 pager;
    private RecommendViewAdapter adapter;

    private final ArrayList<RestaurantInfo> list = new ArrayList<RestaurantInfo>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid());
        docRef.collection("locations")
                .orderBy("imageCount", Query.Direction.DESCENDING)   //가장 많이 방문한 지역 1위 가져오기
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) { //데이터 존재
                            location = queryDocumentSnapshots.getDocuments().get(0).getId();
                        } else {       //데이터 없으면 일단 비우기. 나중에 따로 메시지 띄울 필요
                            location = "";
                        }
                        getLocationDone = true;
                        if (getKeywordDone) {     //키워드 가져오기도 끝났으면 크롤링. 안 끝났으면 밑 리스너에서 크롤링 실행.
                            openNewThread();
                        }
                    }
                });
        docRef.collection("keywords")
                .orderBy("imageCount", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            target = queryDocumentSnapshots.getDocuments().get(0).getId();
                        } else {
                            target = "";
                        }
                        getKeywordDone = true;
                        if (getLocationDone) {
                            openNewThread();
                        }
                    }
                });


        View view = inflater.inflate(R.layout.fragment_item, container, false);

        pager = view.findViewById(R.id.view_pager);

        adapter = new RecommendViewAdapter(list);
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

    private void openNewThread() {
        try {
            Thread scrappingThread = new Thread() {
                @Override
                public void run() {
                    GetRestaurantList();
                }
            };
            scrappingThread.start();
            scrappingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pager.setAdapter(adapter);      //크롤링 끝나면 viewpager 업데이트
    }

    private void GetRestaurantList() {
        try {
            String URL = "https://pcmap.place.naver.com/restaurant/list?query=";

            Document document = Jsoup.connect(URL + location + " " + target).get();
            String rawScript = document.getElementsByTag("script").get(2).toString();
            int start = rawScript.indexOf("window.__APOLLO_STATE__ = ") + "window.__APOLLO_STATE__ = ".length();
            int end = rawScript.indexOf("window.__PLACE_STATE__") - ";\n          ".length();

            String script = rawScript.substring(start, end);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(script);

            Iterator iter = jsonObject.keySet().iterator();
            ArrayList<JSONObject> jsonObjectsList = new ArrayList<>();

            while (iter.hasNext()) {
                String key = (String) iter.next();
                if (key.contains("RestaurantListSummary")) {
                    JSONObject value = (JSONObject) jsonObject.get(key);
                    jsonObjectsList.add(value);
                }
            }
            Collections.shuffle(jsonObjectsList);

            for (int i = 0; i < jsonObjectsList.size(); i++) {
                JSONObject value = jsonObjectsList.get(i);
                String name = (String) value.get("name");
                String category = (String) value.get("category");
                String address = (String) value.get("fullAddress");
                String phone = (String) value.get("phone");
                String imageUrl = (String) value.get("imageUrl");
                String routeUrl = (String) value.get("routeUrl");
                String visitorReviewScore = (String) value.get("visitorReviewScore");
                //list 대신 adapter로 직접 정보 전달
                adapter.addItems(new RestaurantInfo(0, name, imageUrl, address, phone, routeUrl, new String[]{target}));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}