package com.example.zolp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainFragment extends Fragment {
    private Snackbar snackbar;
    private Uri imageUri;
    private File imageFile;
    View rootView;
    MainActivity main;
    FirebaseUser user;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView =  inflater.inflate(R.layout.fragment_main, container, false);
        LinearLayout Camera = rootView.findViewById(R.id.button_Camera);
        Button Logout = main.findViewById(R.id.logout_Btn);
        TextView userNameView = main.findViewById((R.id.user_Name));

        user = main.mAuth.getCurrentUser();
        if(user!=null){
            main.userView.setVisibility(View.VISIBLE);
            userNameView.setText(user.getDisplayName() + " 님");
        }

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutProcess();
            }
        });
        return rootView;
    }

    private File setImageFile() throws IOException { //임시 파일 생성
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss", java.util.Locale.getDefault()).format(new Date());
        String fileName = "ARImage_" + timestamp;
        File imageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        
        return File.createTempFile(fileName,".jpg",imageDir);
    }
    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }
        else {
            if(shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                if(snackbar == null) {
                    snackbar = Snackbar.make(getView(), "카메라 권한이 필요합니다. 확인을 누르면 설정 창으로 이동합니다.", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
                            startActivity(intent);
                        }
                    });
                }
                snackbar.show();
            }
            //ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.CAMERA},0);
            else {
                permissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }
    }
    private void openCamera(){     //카메라 촬영
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = setImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(imageFile != null){
            imageUri = FileProvider.getUriForFile(getContext(),"com.example.zolp.fileprovider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            launcher.launch(intent);
        }
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ImageView image = rootView.findViewById(R.id.imageView3);
                        image.setImageURI(imageUri); //임시로 화면에 띄움
                        imageFile.delete();
                    }
                    else{
                        if(imageFile.delete()){
                            imageFile = null;
                        }
                    }
                }
            });

    private final ActivityResultLauncher<String> permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result){
                        openCamera();
                    }
                    else{
                        Toast.makeText(getContext(),"앱 사용 시 권한이 필요합니다",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void logoutProcess(){
        if(user != null) {
            main.mAuth.signOut();
            main.findViewById(R.id.user_View).setVisibility(View.INVISIBLE);
        }
        main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new LoginFragment()).commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        main = null;
    }
}
