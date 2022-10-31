package com.example.zolp;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private WebView mWebView;
    private LinearLayout needPermissionLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mWebView = findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                super.onGeolocationPermissionsShowPrompt(origin, callback);
                callback.invoke(origin, true, false);
            }
        });

        needPermissionLayout = findViewById(R.id.permission_layout);
        Button permission = findViewById(R.id.permission);
        permission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                locationSettingLauncher.launch(intent);
            }
        });

        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            loadWebView();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {//한번 거부했었으면 직접 설정가서 허용해야함
                needPermissionLayout.setVisibility(View.VISIBLE);
                mWebView.setVisibility(View.INVISIBLE);
            } else {//첫 이용 시 권한 요청
                locationPermissionLauncher.launch(new String[]{
                        Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
            }
        }
    }

    private void loadWebView() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getBaseContext());

        LocationRequest locationRequest = LocationRequest.create();     //위치 정보 설정 과정
        locationRequest.setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest).setAlwaysShow(false);
        SettingsClient settingsClient = LocationServices.getSettingsClient(getBaseContext());
        settingsClient.checkLocationSettings(builder.build()).addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {//위치 정보 가져오기 성공
                if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                LocationCallback locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        Location location = locationResult.getLastLocation();
                        String sx = Double.toString(location.getLongitude());
                        String sy = Double.toString(location.getLatitude());
                        String sname = "현위치";

                        String ex = getIntent().getStringExtra("ex");
                        String ey = getIntent().getStringExtra("ey");
                        String ename = getIntent().getStringExtra("ename");
                        String type = getIntent().getStringExtra("type");

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
                        long mNow = System.currentTimeMillis();
                        Date mDate = new Date(mNow);
                        String date = dateFormat.format(mDate);
                        String time = timeFormat.format(mDate);
                        String later = (Integer.parseInt(time.substring(0, 2)) < 6) ? "true" : "false";
                        String url = "https://way-m.map.naver.com/quick-path/" + sx + "," + sy + "," + sname +
                                "/" + ex + "," + ey + "," + ename + "/-/" + type + "/0?departureTime=" + date + "T" + time + "&later=" + later;
                        Log.d("aaaaaaa", url);
                        mWebView.loadUrl(url);
                        fusedLocationClient.removeLocationUpdates(this);
                    }
                };

                fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) { //위치기능 꺼진 경우
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(((ResolvableApiException) e).getResolution()).build();
                    locationLauncher.launch(intentSenderRequest);
                } else {
                    e.printStackTrace();
                }
            }
        });
    }

    private final ActivityResultLauncher<IntentSenderRequest> locationLauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(),
            new ActivityResultCallback<ActivityResult>() {//gps 키라는 알림
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        needPermissionLayout.setVisibility(View.INVISIBLE);
                        mWebView.setVisibility(View.VISIBLE);
                        loadWebView();
                    } else {
                        makeLocationAlert();
                    }
                }
            });

    private void makeLocationAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("위치 기능 꺼짐");
        builder.setMessage("서비스 이용 시 위치 기능이 필요합니다.");
        builder.setPositiveButton("위치 기능 켜기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                locationOnOffLauncher.launch(intent);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();      //취소나 그냥 뒤로가기 누를경우 액티비티 종료
            }
        });
    }

    private final ActivityResultLauncher<Intent> locationOnOffLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    loadWebView();
                }
            });

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts
                    .RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {//권한 허용 하라는 알림
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Boolean fineLocationGranted = result.get(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(
                            Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {
                        needPermissionLayout.setVisibility(View.INVISIBLE);
                        mWebView.setVisibility(View.VISIBLE);
                        loadWebView();
                    } else {
                        Toast.makeText(getApplicationContext(), "서비스 이용 시 위치 권한이 필요합니다. 설정에서 변경할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        needPermissionLayout.setVisibility(View.VISIBLE);
                        mWebView.setVisibility(View.INVISIBLE);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> locationSettingLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {//설정 창 띄우는 알림
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        needPermissionLayout.setVisibility(View.INVISIBLE);
                        mWebView.setVisibility(View.VISIBLE);
                        loadWebView();
                    } else {
                        Toast.makeText(getBaseContext(), "서비스 이용 시 위치 권한이 필요합니다. 설정에서 변경할 수 있습니다.", Toast.LENGTH_SHORT).show();
                        needPermissionLayout.setVisibility(View.VISIBLE);
                        mWebView.setVisibility(View.INVISIBLE);
                    }
                }
            });
}
