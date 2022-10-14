package com.example.zolp;

import static android.app.Activity.RESULT_CANCELED;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;


public class GalleryFragment extends Fragment {
    private MainActivity main;
    private RecyclerView recyclerView;
    private ArrayList<Uri> uriList;
    private ArrayList<ImageInfo> infoList;
    private GalleryAdapter adapter;
    private LinearLayout noImageLayout;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
        if (main != null){
            main.inGallery = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_gallery, container, false);

        uriList = new ArrayList<>();
        infoList = new ArrayList<>();
        adapter = new GalleryAdapter(getContext());
        recyclerView = rootView.findViewById(R.id.gridView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        FirebaseUser user = main.checkAuth();
        noImageLayout = rootView.findViewById(R.id.noImagesLayout);


        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("images").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() { //DB에서 사진 정보 가져오기(위치, Storage에 저장된 이름)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().size() == 0){       //DB에 이미지 없을경우
                        noImageLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        final int[] index = {0};
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String location = document.getString("location");
                            String imageName = document.getId();
                            String keyword = document.getString("keywords");
                            float rating = Objects.requireNonNull(document.getDouble("rating")).floatValue();
                            infoList.add(new ImageInfo(location,imageName,keyword,rating));
                            FirebaseStorage.getInstance().getReference().child(user.getUid())
                                    .child(Objects.requireNonNull(imageName))  //DB에 저장된 파일명으로 storage에서 이미지 가져오기
                                    .getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            uriList.add(uri);
                                            if (index[0] == task.getResult().size() - 1) {
                                                Collections.sort(uriList);           //DB거는 날짜순으로 들어가는데 storage는 안되서 억지로 정렬
                                                adapter.setList(uriList,infoList);
                                                recyclerView.setAdapter(adapter);
                                            }
                                            index[0]++;
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "실패", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(getContext(),"실패" ,Toast.LENGTH_SHORT).show();
                }
            }
        });

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(View view, int position) {
                Intent intent = new Intent(getContext(), ImageActivity.class);
                intent.putExtra("index", position);
                intent.putParcelableArrayListExtra("uriList", adapter.getUriList());
                intent.putParcelableArrayListExtra("infoList", adapter.getInfoList());
                imageLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), adapter.getPairs()));
            }

            @Override
            public void setFavorites(Button btn, int position) {

            }

            @Override
            public void rejectItem(int position) {

            }

            public void route(int position) {}


        });
        return rootView;
    }


    private final ActivityResultLauncher<Intent> imageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getData()!=null) {
                        if(result.getResultCode()==RESULT_CANCELED){
                            ArrayList<Integer> removedIndexes = result.getData().getIntegerArrayListExtra("removedIndexes");
                            if (removedIndexes != null) {
                                for (int i : removedIndexes) {
                                    adapter.destroyItem(i);
                                    adapter.notifyItemRemoved(i);
                                }
                            }
                            if(result.getData().getParcelableArrayListExtra("infoList") != null){
                                infoList = result.getData().getParcelableArrayListExtra("infoList");
                                adapter.setInfoList(infoList);
                                recyclerView.setAdapter(adapter);
                            }
                            if (adapter.getItemCount() == 0) {
                                noImageLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
    });


    @Override
    public void onDetach() {
        main.inGallery = false;
        super.onDetach();
    }
}