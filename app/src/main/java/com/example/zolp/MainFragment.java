package com.example.zolp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainFragment extends Fragment {
    private Snackbar snackbar;
    private Uri imageUri;
    private File imageFile;
    private MainActivity main;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();

        BackPressHandler handler = new BackPressHandler(main);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handler.onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        LinearLayout Camera = rootView.findViewById(R.id.button_camera);
        LinearLayout RecommendList = rootView.findViewById(R.id.button_interest);

        main.checkAuth();

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) openCamera();
            }
        });

        RecommendList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new ItemFragment()).addToBackStack(null).commit();
            }
        });

        return rootView;
    }


    private boolean checkCameraPermission() {
        boolean cameraStatus = false;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraStatus = true;
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                if (snackbar == null) {
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
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        }

        return cameraStatus;
    }



    private File setImageFile() throws IOException { // 파일 생성
        String timestamp = new SimpleDateFormat("yyyyMMddhhmmss", java.util.Locale.getDefault()).format(new Date());
        String fileName = "zolp_" + timestamp;

        File imageDir = requireContext().getCacheDir();
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        return File.createTempFile(fileName, ".jpg", imageDir);
    }

    private void openCamera() {     //카메라 촬영
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = setImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageFile != null) {
            imageUri = FileProvider.getUriForFile(getContext(), "com.example.zolp.fileprovider", imageFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);
        }
    }


    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("imageUri", imageUri);
                        bundle.putSerializable("imageFile", imageFile);

                        AfterCaptureFragment fragment = new AfterCaptureFragment();
                        fragment.setArguments(bundle);
                        main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, fragment).addToBackStack(null).commit();

                    }
                    else{
                        if(imageFile.delete()){
                            imageFile = null;
                            imageUri = null;
                        }
                    }
                }
            });


    private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean result) {
                    if(result) {
                        openCamera();
                    }
                    else {
                        Toast.makeText(getContext(),"앱 사용 시 카메라 권한이 필요합니다",Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onDetach() {
        super.onDetach();
        main = null;
    }
}
