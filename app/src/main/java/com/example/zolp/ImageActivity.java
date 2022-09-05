package com.example.zolp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ImageActivity extends AppCompatActivity {
    private File imageFile;
    private ViewPager2 pager;
    private StorageReference storageRef;
    private DocumentReference docRef;
    private FirebaseUser user;
    private ArrayList<Uri> uriList;
    private ArrayList<String> nameList;
    private ArrayList<ImageInfo> infoList;
    private ImageAdapter adapter;
    private boolean isRemoved = false;
    private ArrayList<Integer> removedIndexes = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            makeLoginAlert();
        }
        storageRef = FirebaseStorage.getInstance().getReference().child(user.getUid());
        docRef = FirebaseFirestore.getInstance().collection("users").document(user.getUid());
        /*byte[] byteArray = getIntent().getByteArrayExtra("image");
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.length);
        ImageView imageView = findViewById(R.id.image);
        imageView.setImageBitmap(bitmap);*/

        pager = findViewById(R.id.pager);
        Intent intent = getIntent();
        int index = intent.getIntExtra("index", 0);
        uriList = intent.getParcelableArrayListExtra("uriList");
        nameList = intent.getStringArrayListExtra("nameList");
        infoList = intent.getParcelableArrayListExtra("infoList");

        adapter = new ImageAdapter(getBaseContext(), uriList);
        pager.setAdapter(adapter);
        pager.setCurrentItem(index, false);
        pager.setOffscreenPageLimit(3);
        pager.setPageTransformer(new MarginPageTransformer(40));
        pager.setTransitionName("trans"+index);

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
    }

    private void saveInGallery(){
        ImageView imageView = pager.findViewWithTag(pager.getCurrentItem());
        try {
            imageFile = setImageFile();
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
                docRef.collection("images").whereEqualTo("imageName", name).get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (DocumentSnapshot document : task.getResult().getDocuments()) {
                                        document.getReference().delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(getBaseContext(),"삭제 완료" ,Toast.LENGTH_SHORT).show();

                                                isRemoved = true;
                                                int removedIdx = pager.getCurrentItem();
                                                removedIndexes.add(removedIdx);
                                                adapter.destroyItem(removedIdx);
                                                nameList.remove(removedIdx);
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
                                                locationRef.update("imageCount", FieldValue.increment(-1));

                                            }
                                        });
                                    }
                                } else {
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
                String name = nameList.get(pager.getCurrentItem());
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

    private void makeLoginAlert(){

    }
    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
    }

    @Override
    public void supportFinishAfterTransition() {
        Intent intent = new Intent();
        if(isRemoved) {
            setResult(RESULT_CANCELED, intent);
            intent.putIntegerArrayListExtra("removedIndexes", removedIndexes);
        }
        else {
            setResult(RESULT_OK, intent);
        }
        super.supportFinishAfterTransition();
    }
}