package com.example.zolp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private Snackbar snackbar;
    private Uri imageUri;
    private File imageFile;
    private Address address;
    private String storageImagePath;
    View rootView;
    MainActivity main;
    FirebaseUser user;
    FirebaseStorage storage;
    FirebaseFirestore db;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_main, container, false);
        main.toolbar.setTitle("");
        storage = FirebaseStorage.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        LinearLayout Camera = rootView.findViewById(R.id.button_Camera);
        Button Logout = main.findViewById(R.id.logout_Btn);
        TextView userNameView = main.findViewById((R.id.user_Name));

        user = main.mAuth.getCurrentUser();
        if (user != null) {
            main.userView.setVisibility(View.VISIBLE);
            userNameView.setText(user.getDisplayName() + " 님 ");
        }

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkCameraPermission()) openCamera();
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
                } else snackbar.setText("카메라 권한이 필요합니다. 확인을 누르면 설정 창으로 이동합니다.");
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

        File imageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/zolp");
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

    private void saveAtFBStorage() {
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child(user.getUid()).child(imageFile.getName());
        storageImagePath = imagesRef.getPath();
        UploadTask uploadTask = imagesRef.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Storage Error", e.toString());
                Toast.makeText(getContext(), "사진 백업 실패", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getContext(), "사진 백업 성공", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean checkLocationPermission() {
        boolean locationStatus = false;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationStatus = true;
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                saveAtFBDBnoLocation();
                if (snackbar == null) {
                    snackbar = Snackbar.make(getView(), "위치 권한이 거부되어 있습니다. 확인을 누르면 설정 창으로 이동합니다.", Snackbar.LENGTH_SHORT);
                    snackbar.setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
                            startActivity(intent);
                        }
                    });
                } else snackbar.setText("위치 권한이 거부되어 있습니다. 확인을 누르면 설정 창으로 이동합니다.");
                snackbar.show();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }

        return locationStatus;
    }

    public String createAddressString(Address address){
        String str="";
        if(address.getAdminArea()!=null){
            str = address.getAdminArea();
        }
        if(address.getSubAdminArea()!=null){
            str = str + " " + address.getSubAdminArea();
        }
        if(address.getLocality()!=null){
            str = str + " " + address.getLocality();
        }
        if(address.getSubLocality()!=null){
            str = str + " " + address.getSubLocality();
        }
        if(address.getThoroughfare()!=null){
            str = str + " " + address.getThoroughfare();
        }
        return str;

    }

    private void saveAtFBDB() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(false);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(builder.build()).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (!checkLocationPermission()) return;

                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        Location location = locationResult.getLastLocation();
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addressList = null;
                        try {
                            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(addressList != null) {
                            address = addressList.get(0);
                            Log.d("aaaaaaaaaaaa",address.getAddressLine(0));

                            String addressStr = createAddressString(address);
                            Map<String,Object> image = new HashMap<>();
                            image.put("imagePath", storageImagePath);

                            DocumentReference docRef = db.collection("users").document(user.getUid())
                                    .collection("locations").document(addressStr);
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Nullable
                                @Override
                                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot snapshot = transaction.get(docRef);
                                    transaction.set(docRef.collection("images").document(), image);
                                    Object count = snapshot.get("imageCount");
                                    if(count==null) {
                                        Map<String, Integer> newCount = new HashMap<>();
                                        newCount.put("imageCount", 1);
                                        transaction.set(docRef, newCount);
                                    }
                                    else {
                                        transaction.update(docRef, "imageCount", FieldValue.increment(1));
                                    }

                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getContext(), "DB 저장 성공", Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), "DB 저장 실패", Toast.LENGTH_SHORT).show();
                                }
                            });

                        }
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                };

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) { //위치기능 꺼진 경우
                    if (!checkLocationPermission()) return;
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build();
                    locationLauncher.launch(intentSenderRequest);

                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    public void saveAtFBDBnoLocation(){
        Map<String,Object> image = new HashMap<>();
        image.put("imagePath", storageImagePath);

        db.collection("users").document(user.getUid())
                .collection("locations").document("null")
                .collection("images").add(image).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                Toast.makeText(getContext(), "DB 저장 성공", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "DB 저장 실패", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final ActivityResultLauncher<IntentSenderRequest> locationLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        saveAtFBDB();
                    }
                }
            });

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ImageView image = rootView.findViewById(R.id.imageView3);
                        image.setImageURI(imageUri); //임시로 화면에 띄움
                        
                        //갤러리 갱신
                        MediaScannerConnection.scanFile(getContext(), new String[]{imageFile.toString()}, null,null);
                        //파이어베이스 스토리지 저장
                        saveAtFBStorage();
                        //위치정보 가져오기
                        saveAtFBDB();
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
    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts
                    .RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Boolean fineLocationGranted = result.get(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(
                            Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {
                        saveAtFBDB();
                    }
                    else {
                        Toast.makeText(getContext(),"위치 권한이 거부되었습니다. 설정에서 변경할 수 있습니다.",Toast.LENGTH_SHORT).show();
                        saveAtFBDBnoLocation();
                    }
                }
            }
    );

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
