package com.example.zolp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ItemFragment extends Fragment {
    private String location, target;
    private ViewPager2 pager;
    private RecommendViewAdapter adapter;

    private final ArrayList<RestaurantInfo> list = new ArrayList<RestaurantInfo>();
    private ArrayList<String> favoritesList, rejectionsList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users")
                .document(user.getUid());
        Task<QuerySnapshot> favoritesTask = docRef.collection("favorites").get();
        Task<QuerySnapshot> rejectionsTask = docRef.collection("rejections").get();
        Task<QuerySnapshot> locationsTask = docRef.collection("locations")
                .orderBy("imageCount", Query.Direction.DESCENDING)   //가장 많이 방문한 지역 1위 가져오기
                .limit(1)
                .get();
        Task<QuerySnapshot> keywordsTask = docRef.collection("keywords")
                .orderBy("imageCount", Query.Direction.DESCENDING)
                .limit(1)
                .get();
        Tasks.whenAllSuccess(favoritesTask, rejectionsTask, locationsTask, keywordsTask).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) { //4가지 쿼리 다 끝난후 웹크롤링
                QuerySnapshot q1 = (QuerySnapshot) objects.get(0), q2 = (QuerySnapshot) objects.get(1), q3 = (QuerySnapshot) objects.get(2), q4 = (QuerySnapshot) objects.get(3);
                if (!q1.isEmpty()) {
                    favoritesList = new ArrayList<>();
                    for (DocumentSnapshot i : q1.getDocuments()) {
                        favoritesList.add(i.getId());
                    }
                }
                if (!q2.isEmpty()) {
                    rejectionsList = new ArrayList<>();
                    for (DocumentSnapshot i : q2.getDocuments()) {
                        rejectionsList.add(i.getId());
                    }
                }
                if (!q3.isEmpty()) { //데이터 존재
                    location = q3.getDocuments().get(0).getId();
                } else {       //데이터 없으면 일단 비우기. 나중에 따로 메시지 띄울 필요
                    location = "";
                }
                if (!q4.isEmpty()) {
                    target = q4.getDocuments().get(0).getId();
                } else {
                    target = "";
                }
                openScrappingThread();
            }
        });

        View view = inflater.inflate(R.layout.fragment_item, container, false);

        pager = view.findViewById(R.id.view_pager);

        adapter = new RecommendViewAdapter(list);
        adapter.setOnItemClickListener(new RecommendViewAdapter.OnItemClickListener() {
            @Override
            public void itemClick(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adapter.getItem(position).webUrl));

                if (getActivity() != null) {
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void setFavorites(Button btn, int position) {
                RestaurantInfo info = adapter.getItem(position);
                if (info.isFavorites) {//좋아요 취소
                    docRef.collection("favorites").document(info.id).delete();
                    btn.setBackgroundResource(R.drawable.bookmark_before);
                    adapter.getItem(position).isFavorites = false;
                } else { //맛집 좋아요 등록
                    Map<String, String> newFavorites = new HashMap<>();
                    newFavorites.put("id", info.id);
                    newFavorites.put("name", info.name);
                    newFavorites.put("imageUrl", info.imageUrl);
                    newFavorites.put("location", info.location);
                    newFavorites.put("x", info.x);
                    newFavorites.put("y", info.y);
                    newFavorites.put("phoneNumber", info.phoneNumber);
                    newFavorites.put("webUrl", info.webUrl);
                    newFavorites.put("keyword", info.keyword);
                    newFavorites.put("date", new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date()));

                    docRef.collection("favorites").document(info.id).set(newFavorites);
                    btn.setBackgroundResource(R.drawable.bookmark_after);
                    adapter.getItem(position).isFavorites = true;
                }
            }

            @Override
            public void rejectItem(int position) {
                RestaurantInfo info = adapter.getItem(position);
                Map<String, String> newRejection = new HashMap<>();
                newRejection.put("id", info.id);
                newRejection.put("name", info.name);
                newRejection.put("imageUrl", info.imageUrl);
                newRejection.put("location", info.location);
                newRejection.put("phoneNumber", info.phoneNumber);
                newRejection.put("webUrl", info.webUrl);
                newRejection.put("keyword", info.keyword);
                newRejection.put("date", new SimpleDateFormat("yyyyMMddHHmmss", java.util.Locale.getDefault()).format(new Date()));

                docRef.collection("rejections").document(info.id).set(newRejection);
                adapter.deleteItem(position);
            }

            @Override
            public void route(int position, String type) {
                Intent intent = new Intent(getActivity(), MapActivity.class);

                String ex = adapter.getItem(position).x;
                String ey = adapter.getItem(position).y;
                String ename = adapter.getItem(position).name;

                intent.putExtra("ex", ex);
                intent.putExtra("ey", ey);
                intent.putExtra("ename", ename);
                intent.putExtra("type", type);

                startActivity(intent);
            }
        });

        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(3);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(60));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.85f + r * 0.15f);
            }
        });
        pager.setPageTransformer(compositePageTransformer);

        return view;
    }


    private void openScrappingThread() {
        try {
            Thread scrappingThread = new Thread() {
                @Override
                public void run() {
                    getRestaurantList();
                }
            };
            scrappingThread.start();
            scrappingThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pager.setAdapter(adapter);      //크롤링 끝나면 viewpager 업데이트
    }


    private void getRestaurantList() {
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
            ArrayList<JSONObject> favoritesFirstList = new ArrayList<>();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                if (key.contains("RestaurantListSummary")) {
                    JSONObject value = (JSONObject) jsonObject.get(key);
                    if (rejectionsList == null || !rejectionsList.contains((String) value.get("id"))) {      //차단 안된 맛집 필터링
                        if (favoritesList != null && favoritesList.contains((String) value.get("id"))) {        //좋아요 목록에 있는 맛집이면 따로 저장
                            value.put("favorites", true);
                            favoritesFirstList.add(value);
                        } else {
                            value.put("favorites", false);
                            jsonObjectsList.add(value);
                        }
                    }
                }
            }
            Collections.shuffle(jsonObjectsList);
            favoritesFirstList.addAll(jsonObjectsList);                     //좋아요 누른 맛집이 앞으로 오게 리스트 합침
            for (int i = 0; i < favoritesFirstList.size(); i++) {
                JSONObject value = favoritesFirstList.get(i);
                String id = (String) value.get("id");
                String name = (String) value.get("name");
                String category = (String) value.get("category");
                String address = (String) value.get("fullAddress");
                String x = (String) value.get("x");
                String y = (String) value.get("y");
                String phone = (String) value.get("phone");
                String imageUrl = (String) value.get("imageUrl");
                String routeUrl = (String) value.get("routeUrl");
                String visitorReviewScore = (String) value.get("visitorReviewScore");
                Boolean isFavorites = (Boolean) value.get("favorites");
                //list 대신 adapter로 직접 정보 전달
                adapter.addItem(new RestaurantInfo(id, name, imageUrl, address, x, y, phone, routeUrl, target, isFavorites));
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

}