package com.example.zolp;

import static android.app.Activity.RESULT_CANCELED;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
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
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;


public class GalleryFragment extends Fragment {
    private MainActivity main;
    private RecyclerView recyclerView;
    private ArrayList<Uri> uriList;
    private ArrayList<String> nameList;
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
        nameList = new ArrayList<>();
        infoList = new ArrayList<>();
        adapter = new GalleryAdapter(getContext(), uriList, nameList);
        recyclerView = rootView.findViewById(R.id.gridView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        FirebaseUser user = main.checkAuth();
        noImageLayout = rootView.findViewById(R.id.noImagesLayout);

        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .collection("images").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        infoList.add(new ImageInfo(document.getString("location"), document.getString("name")));
                    }
                } else {
                    Toast.makeText(getContext(),"실패" ,Toast.LENGTH_SHORT).show();
                }
            }
        });
        FirebaseStorage.getInstance().getReference().child(user.getUid())
                .listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                List<StorageReference> list = listResult.getItems();
                if (list.size() == 0) {
                    noImageLayout.setVisibility(View.VISIBLE);
                } else {
                    final int[] index = {0};
                    for (StorageReference item : list) {
                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                adapter.addItem(uri, item.getName());
                                if (index[0] == list.size() - 1) {
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
            }
        });
        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void itemClick(View view, int position) {
                ViewCompat.setTransitionName(view,"trans"+position);
                Intent intent = new Intent(getContext(), ImageActivity.class);
                intent.putExtra("index", position);
                intent.putParcelableArrayListExtra("uriList", uriList);
                intent.putStringArrayListExtra("nameList", nameList);
                intent.putParcelableArrayListExtra("infoList", infoList);
                imageLauncher.launch(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), view, view.getTransitionName()));
            }
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
                            for (int i : removedIndexes){
                                adapter.destroyItem(i);
                                infoList.remove(i);
                            }
                            if (infoList.size() == 0) {
                                noImageLayout.setVisibility(View.VISIBLE);
                            }
                            recyclerView.setAdapter(adapter);
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