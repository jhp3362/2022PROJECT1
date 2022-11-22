package com.example.zolp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {
    private MainActivity main;
    private ViewPager2 pager;
    private final ArrayList<RestaurantInfo> list = new ArrayList<RestaurantInfo>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
        if (main != null){
            main.inFavorites = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favor_reject, container, false);
        FavoritesAdapter adapter = new FavoritesAdapter(list);
        LinearLayout noImageLayout = rootView.findViewById(R.id.no_image_layout);
        TextView noFavorTxt = rootView.findViewById(R.id.no_item_txt);

        FirebaseUser user = main.checkAuth();
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("favorites").orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()==0){
                            noImageLayout.setVisibility(View.VISIBLE);
                            noFavorTxt.setText("찜 해놓은 맛집이 없어요!");
                        }
                        else {
                            for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                                String id = document.getString("id");
                                String name = document.getString("name");
                                String imageUrl = document.getString("imageUrl");
                                String location = document.getString("location");
                                String x = document.getString("x");
                                String y = document.getString("y");
                                String phoneNumber = document.getString("phoneNumber");
                                String webUrl = document.getString("webUrl");
                                String keyword = document.getString("keyword");
                                RestaurantInfo info = new RestaurantInfo(id, name, imageUrl, location, x, y,phoneNumber,webUrl,keyword,false);
                                adapter.addItem(info);
                            }
                            pager.setAdapter(adapter);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "실패", Toast.LENGTH_SHORT).show();
                    }
                });

        TextView favoritesTxt = rootView.findViewById(R.id.favorites_txt);
        favoritesTxt.setVisibility(View.VISIBLE);

        adapter.setOnItemClickListener(new FavoritesAdapter.OnItemClickListener() {
            @Override
            public void itemClick(int position) {
                Intent intent = new Intent(getActivity(), ItemDetailActivity.class);

                String id = adapter.getItem(position).id;
                intent.putExtra("id", id);
                startActivity(intent);
            }

            @Override
            public void deleteFavorite(int position) {
                String id = adapter.getItem(position).id;
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .collection("favorites").document(id).delete();
                adapter.deleteItem(position);
                if(adapter.getItemCount()==0){
                    noImageLayout.setVisibility(View.VISIBLE);
                    noFavorTxt.setText("찜 해놓은 맛집이 없어요!");
                }
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

        pager = rootView.findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(1);
        pager.setPageTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY(0.7f + r * 0.3f);
            }
        });


        return rootView;
    }

    @Override
    public void onDetach() {
        main.inFavorites = false;
        super.onDetach();
    }
}