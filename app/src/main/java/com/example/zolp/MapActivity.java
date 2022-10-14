package com.example.zolp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class MapActivity extends AppCompatActivity {

    private WebView mWebView;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);

        mWebView = findViewById(R.id.webview);
        mWebView.setWebViewClient(new WebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        if (!checkLocationPermission())
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION});
        else {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        String sx = Double.toString(currentLocation.getLongitude());
        String sy = Double.toString(currentLocation.getLatitude());
        String sname = "현위치";

        String ex = getIntent().getStringExtra("ex");
        String ey = getIntent().getStringExtra("ey");
        String ename = getIntent().getStringExtra("ename");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss");
        long mNow = System.currentTimeMillis();
        Date mDate = new Date(mNow);
        String date = dateFormat.format(mDate);
        String time = timeFormat.format(mDate);
        String later = (Integer.parseInt(time.substring(0, 2)) < 6) ? "true" : "false";
        String url = "https://way-m.map.naver.com/quick-path/" + sx + "," + sy + "," + sname +
                "/" + ex + "," + ey + "," + ename + "/-/transit/0?departureTime=" + date + "T" + time + "&later=" + later;

        mWebView.loadUrl(url);
    }


    private boolean checkLocationPermission() {
        boolean locationStatus = false;
        if (ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(MapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationStatus = true;
        }
        return locationStatus;
    }

    private final ActivityResultLauncher<String[]> locationPermissionLauncher = registerForActivityResult(new ActivityResultContracts
                    .RequestMultiplePermissions(), new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Boolean fineLocationGranted = result.get(
                            Manifest.permission.ACCESS_FINE_LOCATION);
                    Boolean coarseLocationGranted = result.get(
                            Manifest.permission.ACCESS_COARSE_LOCATION);
                    if (fineLocationGranted != null && fineLocationGranted || coarseLocationGranted != null && coarseLocationGranted) {

                    } else {
                        Toast.makeText(getApplicationContext(), "위치 권한이 거부되었습니다. 설정에서 변경할 수 있습니다.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

}
