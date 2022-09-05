package com.example.zolp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AfterCaptureFragment extends Fragment {
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private Address address;
    private String storageImageName;
    private Uri imageUri;
    private File imageFile;
    private MainActivity main;
    private FirebaseUser user;
    private Boolean doneDB = false, doneStorage = false, doneSetting = false;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_after_capture, container, false);
        if(getArguments() != null) {
            Bundle bundle = getArguments();
            imageUri = bundle.getParcelable("imageUri");
            imageFile = (File) bundle.getSerializable("imageFile");
            ImageView imageView = rootView.findViewById(R.id.capture_image);
            imageView.setImageURI(imageUri);
            imageLabeling();

            user = main.mAuth.getCurrentUser();
            storage = FirebaseStorage.getInstance();
            db = FirebaseFirestore.getInstance();
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

            Button saveBtn = rootView.findViewById(R.id.okay_btn);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //위치정보 가져오기
                    saveAtFBDB();
                    //파이어베이스 스토리지 저장
                    saveAtFBStorage();
                }
            });
            Button cancelBtn = rootView.findViewById(R.id.cancel_btn);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(imageFile.delete()){
                        imageFile = null;
                        imageUri = null;
                    }
                    main.onBackPressed();
                }
            });
        }

        return rootView;
    }

    private void saveAtFBStorage() {
        StorageReference storageRef = storage.getReference();
        StorageReference imagesRef = storageRef.child(user.getUid()).child(imageFile.getName());
        storageImageName = imageFile.getName();
        UploadTask uploadTask = imagesRef.putFile(imageUri);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Storage Error", e.toString());
                Toast.makeText(getContext(), "서버 저장 오류", Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                doneStorage = true;
            }
        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(doneDB && !doneSetting){
                    Toast.makeText(getContext(), "서버 저장 성공", Toast.LENGTH_SHORT).show();
                    if(imageFile.delete()){
                        imageFile = null;
                        imageUri = null;
                    }
                    main.onBackPressed();
                }
            }
        });
    }

    private boolean checkLocationPermission() {
        boolean locationStatus = false;
        if (ContextCompat.checkSelfPermission(main, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(main, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationStatus = true;
        } else {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                makeLocationAlert();
            } else {
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }

        return locationStatus;
    }

    private void makeLocationAlert(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("권한 허용");
        builder.setMessage("위치 권한이 허용되지 않았습니다.");
        builder.setPositiveButton("위치 정보 없이 저장", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveAtFBDBnoLocation();
            }
        });
        builder.setNegativeButton("위치 권한 허용", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doneSetting = true;
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName()));
                locationSettingLauncher.launch(intent);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void saveAtFBDB() {
        if (!checkLocationPermission()) return;
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(false);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getContext());
        settingsClient.checkLocationSettings(builder.build()).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                if (ContextCompat.checkSelfPermission(main, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(main, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;

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

                            String addressStr = createAddressString(address);
                            Map<String,Object> image = new HashMap<>();
                            image.put("imageName", storageImageName);
                            image.put("location", addressStr);
                            //image.put("date", Date.)
                            //image.put("keywords", )
                            DocumentReference docRef = db.collection("users").document(user.getUid());
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Nullable
                                @Override
                                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentReference locationRef = docRef.collection("locations").document(addressStr);
                                    DocumentSnapshot snapshot = transaction.get(locationRef);
                                    Object count = snapshot.get("imageCount");
                                    if(count==null) {
                                        Map<String, Integer> newCount = new HashMap<>();
                                        newCount.put("imageCount", 1);
                                        transaction.set(locationRef, newCount);
                                    }
                                    else {
                                        transaction.update(locationRef, "imageCount", FieldValue.increment(1));
                                    }
                                    transaction.set(docRef.collection("images").document(), image);
                                    return null;
                                }
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    doneDB = true;
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(doneStorage && !doneSetting){
                                        Toast.makeText(getContext(), "서버 저장 성공", Toast.LENGTH_SHORT).show();
                                        if(imageFile.delete()){
                                            imageFile = null;
                                            imageUri = null;
                                        }
                                        main.onBackPressed();
                                        Log.d("aaaaaaaaa","hihihihihihhih22");
                                    }
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

    public void saveAtFBDBnoLocation(){
        Map<String,Object> image = new HashMap<>();
        image.put("imageName", storageImageName);
        image.put("location", "null");

        db.collection("users").document(user.getUid())
                .collection("images").add(image).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
            @Override
            public void onSuccess(DocumentReference documentReference) {
                doneDB = true;
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "서버 저장 오류", Toast.LENGTH_SHORT).show();
            }
        }).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if(doneStorage && !doneSetting){
                    Toast.makeText(getContext(), "서버 저장 성공", Toast.LENGTH_SHORT).show();
                    if(imageFile.delete()){
                        imageFile = null;
                        imageUri = null;
                    }
                    Log.d("aaaaaaaaa","hihihihihihhih11");
                    main.onBackPressed();
                }
            }
        });
    }


    private void imageLabeling() {
        FirebaseVisionImage fbImage;
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

        try {
            fbImage = FirebaseVisionImage.fromFilePath(getContext(), imageUri); // 이미지 uri를 가지고 fbimage로 변환

            labeler.processImage(fbImage)
                    .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
                        @Override
                        public void onSuccess(List<FirebaseVisionImageLabel> labels) {
                            // Task completed successfully
                            for (FirebaseVisionImageLabel label: labels) {
                                float confidence = label.getConfidence();
                                if(confidence < 0.8) break;
                                String text = label.getText();
                                System.out.println(text);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Task failed with an exception
                            // ...
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final ActivityResultLauncher<IntentSenderRequest> locationLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        saveAtFBDB();
                    }
                    else{
                        saveAtFBDBnoLocation();
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

    private final ActivityResultLauncher<Intent> locationSettingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    doneSetting = false;
                    if(ContextCompat.checkSelfPermission(main, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(main, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        saveAtFBDB();
                    }
                    else {
                        Toast.makeText(getContext(),"위치 권한이 거부되었습니다. 설정에서 변경할 수 있습니다.",Toast.LENGTH_SHORT).show();
                        saveAtFBDBnoLocation();
                    }
                }
            });


}