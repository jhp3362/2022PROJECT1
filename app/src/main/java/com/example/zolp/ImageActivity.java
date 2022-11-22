package com.example.zolp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageActivity extends AppCompatActivity {
    private ViewPager2 pager;
    private StorageReference storageRef;
    private DocumentReference docRef;
    private ArrayList<Uri> uriList;
    private ArrayList<ImageInfo> infoList;
    private ImageAdapter adapter;
    private boolean isRemoved = false, isRatingChanged = false;
    private final ArrayList<Integer> removedIndexes = new ArrayList<>();
    private float removedRating;
    private LinearLayout infoLayout;
    private Button infoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid());
        docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());

        pager = findViewById(R.id.pager);
        Intent intent = getIntent();
        int index = intent.getIntExtra("index", 0);
        uriList = intent.getParcelableArrayListExtra("uriList");
        infoList = intent.getParcelableArrayListExtra("infoList");

        adapter = new ImageAdapter(getBaseContext(), uriList);
        pager.setAdapter(adapter);
        pager.setCurrentItem(index, false);
        pager.setOffscreenPageLimit(3);
        pager.setPageTransformer(new MarginPageTransformer(40));
        pager.setTransitionName("trans"+index);

        infoLayout = findViewById(R.id.info_layout);
        infoBtn = findViewById(R.id.info_btn);
        infoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(infoLayout.getVisibility()==View.INVISIBLE) {
                    infoLayout.setVisibility(View.VISIBLE);
                    Log.d("asdad","11");
                }
                else{
                    infoLayout.setVisibility(View.INVISIBLE);
                    Log.d("asdad","21");
                }
            }
        });
        TextView keywordText = findViewById(R.id.keyword_txt);
        TextView locationText = findViewById(R.id.location_txt);
        TextView dateText = findViewById(R.id.date_txt);

        Button ratingBtn = findViewById(R.id.rating_btn);
        ImageInfo info = infoList.get(index);
        setRatingTxt(ratingBtn, info.getRating());   //선택 이미지에 따른 점수 표시
        keywordText.setText(info.getKeyword());
        locationText.setText(info.getLocation());
        dateText.setText(info.getDate());
        pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {  //페이지 넘길때마다 이미지별 점수 표시
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                ImageInfo info = infoList.get(position);
                float rating = info.getRating();
                setRatingTxt(ratingBtn, rating);
                keywordText.setText(info.getKeyword());
                locationText.setText(info.getLocation());
                dateText.setText(info.getDate());
            }
        });

        Button saveBtn = findViewById(R.id.save_btn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveInGallery();
            }
        });

        Button deleteBtn = findViewById(R.id.delete_btn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                makeDeleteAlert();
            }
        });

        LinearLayout ratingLayout = findViewById(R.id.rating_layout);
        RatingBar ratingBar = findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if(rating < 0.5F){
                    ratingBar.setRating(0.5F);   //최소 별점 0.5
                }
            }
        });

        ratingBtn.setOnClickListener(new View.OnClickListener() {      //평점 입력창 띄우기
            @Override
            public void onClick(View view) {
                if (infoLayout.getVisibility()==View.VISIBLE) {
                    infoLayout.setVisibility(View.INVISIBLE);
                }
                ratingLayout.setVisibility(View.VISIBLE);
                ratingLayout.bringToFront();

                int index = pager.getCurrentItem();
                float rating = infoList.get(index).getRating();
                if(rating == 0){
                    rating = 5;
                }
                ratingBar.setRating(rating);
            }
        });
        TextView okBtn = findViewById(R.id.ok_btn);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float rating = ratingBar.getRating();
                submitNewRating(rating);
                ratingLayout.setVisibility(View.INVISIBLE);
                ratingBtn.setText(String.valueOf(rating));
            }
        });
        TextView cancelBtn = findViewById(R.id.cancel_btn);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ratingLayout.setVisibility(View.INVISIBLE);
            }
        });

    }

    private void saveInGallery(){
        ImageView imageView = pager.findViewWithTag(pager.getCurrentItem());
        try {
            File imageFile = setImageFile();
            FileOutputStream stream = new FileOutputStream(imageFile);
            Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            stream.flush();
            stream.close();
            Toast.makeText(getBaseContext(), "갤러리 저장 완료", Toast.LENGTH_SHORT).show();
            MediaScannerConnection.scanFile(getBaseContext(), new String[]{imageFile.toString()}, null,null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File setImageFile() throws IOException { // 파일 생성
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss", java.util.Locale.getDefault()).format(new Date());
        String fileName = "zolp_" + timestamp;

        File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/zolp");
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        return File.createTempFile(fileName, ".jpg", imageDir);
    }

    private void deleteImage(String name){
        StorageReference imagesRef = storageRef.child(name);

        imagesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                docRef.collection("images").document(name).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(getBaseContext(),"삭제 완료" ,Toast.LENGTH_SHORT).show();

                                            isRemoved = true;
                                            int removedIdx = pager.getCurrentItem();
                                            removedRating = infoList.get(removedIdx).getRating();

                                            removedIndexes.add(removedIdx);
                                            adapter.destroyItem(removedIdx);
                                            infoList.remove(removedIdx);

                                            if(adapter.getItemCount()==0){
                                                supportFinishAfterTransition();
                                            }
                                            pager.setAdapter(adapter);
                                            if(removedIdx==adapter.getItemCount()) {
                                                pager.setCurrentItem(removedIdx-1, false);
                                            }
                                            else{
                                                pager.setCurrentItem(removedIdx, false);
                                            }
                                            DocumentReference locationRef = docRef.collection("locations").document(document.getString("location"));
                                            locationRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if(documentSnapshot.contains("imageCount")) {
                                                        Long count = documentSnapshot.getLong("imageCount");
                                                        if (count == 1) {
                                                            locationRef.delete();
                                                        } else {
                                                            locationRef.update("imageCount", FieldValue.increment(-1));
                                                        }
                                                    }
                                                }
                                            });

                                            DocumentReference keywordRef = docRef.collection("keywords").document(document.getString("keywords"));
                                            keywordRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                @Override
                                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                    if(documentSnapshot.contains("imageCount")) {
                                                        Long count = documentSnapshot.getLong("imageCount");
                                                        Double keywordRatingSum = documentSnapshot.getDouble("ratingSum");
                                                        if (count == 1) {
                                                            keywordRef.delete();
                                                        } else {
                                                            removedRating = (removedRating==0) ? 2.5F : removedRating;
                                                            keywordRef.update("imageCount", FieldValue.increment(-1), "ratingSum", keywordRatingSum - (removedRating-2.5));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }
                                else {
                                    Toast.makeText(getBaseContext(),"실패" ,Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getBaseContext(),"실패" ,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void makeDeleteAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("삭제");
        builder.setMessage("사진을 영구 삭제 하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = infoList.get(pager.getCurrentItem()).getName();
                deleteImage(name);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setRatingTxt(Button button, float rating){
        if(rating == 0){
            button.setText("별점입력");
        }
        else{
            button.setText(String.valueOf(rating));
        }
    }

    private void submitNewRating(float newRating){
        int index = pager.getCurrentItem();
        String keyword = infoList.get(index).getKeyword();
        float oldRating = infoList.get(index).getRating();
        if(newRating != oldRating){                 //기존 별점과 다르면 DB에 새로 저장
            String imageName = infoList.get(index).getName();
            docRef.collection("images").document(imageName).update("rating", newRating)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            infoList.get(index).setRating(newRating);
                            isRatingChanged = true;
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getBaseContext(),"오류" ,Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    });
            if(oldRating == 0){
                oldRating = 2.5F;
            }
            docRef.collection("keywords").document(keyword).update("ratingSum", FieldValue.increment(newRating-oldRating));
        }
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    @Override
    public void supportFinishAfterTransition() {
        pager.setTransitionName("");
        int index = pager.getCurrentItem();
        pager.findViewWithTag(index).setTransitionName("trans"+index);
        Intent intent = new Intent();
        if(isRemoved) {
            setResult(RESULT_CANCELED, intent);
            intent.putIntegerArrayListExtra("removedIndexes", removedIndexes);
        }
        else {
            setResult(RESULT_OK, intent);
        }
        if(isRatingChanged) {
            setResult(RESULT_CANCELED, intent);
            intent.putParcelableArrayListExtra("infoList", infoList);
        }


        super.supportFinishAfterTransition();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect rect = new Rect(), rect2 = new Rect();
        infoLayout.getGlobalVisibleRect(rect);
        infoBtn.getGlobalVisibleRect(rect2);
        rect.union(rect2);

        if (!rect.contains((int) ev.getX(), (int) ev.getY()) && ev.getAction() == MotionEvent.ACTION_UP) {
            if (infoLayout.getVisibility()==View.VISIBLE) {
                infoLayout.setVisibility(View.INVISIBLE);
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}