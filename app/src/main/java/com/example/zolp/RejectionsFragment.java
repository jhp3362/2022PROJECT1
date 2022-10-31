package com.example.zolp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class RejectionsFragment extends Fragment {
    private MainActivity main;
    private ViewPager2 pager;
    private final ArrayList<RestaurantInfo> list = new ArrayList<RestaurantInfo>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
        if (main != null){
            main.inRejections = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_favorites, container, false);
        RejectionsAdapter adapter = new RejectionsAdapter(list);
        LinearLayout noImageLayout = rootView.findViewById(R.id.no_image_layout);
        TextView noRejectTxt = rootView.findViewById(R.id.no_item_txt);

        FirebaseUser user = main.checkAuth();
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("rejections").orderBy("date").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(queryDocumentSnapshots.size()==0){
                            noImageLayout.setVisibility(View.VISIBLE);
                            noRejectTxt.setText("차단한 식당이 없어요!");
                        }
                        else {
                            for(QueryDocumentSnapshot document : queryDocumentSnapshots){
                                String id = document.getString("id");
                                String name = document.getString("name");
                                String imageUrl = document.getString("imageUrl");
                                String location = document.getString("location");
                                String phoneNumber = document.getString("phoneNumber");
                                String webUrl = document.getString("webUrl");
                                String keyword = document.getString("keyword");
                                RestaurantInfo info = new RestaurantInfo(id, name, imageUrl, location, "","",phoneNumber,webUrl,keyword,false);
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

        TextView rejectionsTxt = rootView.findViewById(R.id.rejections_txt);
        rejectionsTxt.setVisibility(View.VISIBLE);

        adapter.setOnItemClickListener(new RejectionsAdapter.OnItemClickListener() {
            @Override
            public void itemClick(View view, int position) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(adapter.getItem(position).webUrl));

                if (getActivity() != null) {
                    getActivity().startActivity(intent);
                }
            }

            @Override
            public void deleteRejection(int position) {
                String id = adapter.getItem(position).id;
                FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                        .collection("rejections").document(id).delete();
                adapter.deleteItem(position);
                if(adapter.getItemCount()==0){
                    noImageLayout.setVisibility(View.VISIBLE);
                    noRejectTxt.setText("차단한 식당이 없어요!");
                }
            }
        });

        pager = rootView.findViewById(R.id.pager);
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

        return rootView;
    }

    @Override
    public void onDetach() {
        main.inRejections = false;
        super.onDetach();
    }
}