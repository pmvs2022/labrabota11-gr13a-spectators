package com.spectator.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.view.LayoutInflater;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spectator.R;

import java.util.zip.Inflater;

public class MapActivity extends AppCompatActivity
        implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * Request code for location permission request.
     *
     * @see #onRequestPermissionsResult(int, String[], int[])
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    /**
     * Flag indicating whether a requested permission has been denied after returning in {@link
     * #onRequestPermissionsResult(int, String[], int[])}.
     */
    private boolean permissionDenied = false;

    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        CameraUpdate point = CameraUpdateFactory.newLatLngZoom(
                new LatLng(53.8847186,27.5532006), 10.9f);
        map.moveCamera(point);

        LatLng bsu = new LatLng(53.89430250300721, 27.54684944898893);
        googleMap.addMarker(new MarkerOptions()
                .position(bsu)
                .title(getString(R.string.yik_prefix)+" 228"));

        LatLng sch30 = new LatLng(53.88337726790362, 27.553344925698838);
        googleMap.addMarker(new MarkerOptions()
                .position(sch30)
                .title(getString(R.string.yik_prefix)+" 30"));

        LatLng sch21 = new LatLng(53.913518778121556, 27.563816269357964);
        googleMap.addMarker(new MarkerOptions()
                .position(sch21)
                .title(getString(R.string.yik_prefix)+" 21"));

        LatLng sch44 = new LatLng(53.90846299575239, 27.54922505241031);
        googleMap.addMarker(new MarkerOptions()
                .position(sch44)
                .title(getString(R.string.yik_prefix)+" 44"));

        LatLng sch37 = new LatLng(53.93171452742694, 27.543731888523556);
        googleMap.addMarker(new MarkerOptions()
                .position(sch37)
                .title(getString(R.string.yik_prefix)+" 37"));

        LatLng sch57 = new LatLng(53.88068281604518, 27.60755836578476);
        googleMap.addMarker(new MarkerOptions()
                .position(sch57)
                .title(getString(R.string.yik_prefix)+" 57"));

        LatLng sch127 = new LatLng(53.88636925900216, 27.664538212163443);
        googleMap.addMarker(new MarkerOptions()
                .position(sch127)
                .title(getString(R.string.yik_prefix)+" 127"));

        LatLng sch165 = new LatLng(53.88560562072533, 27.441551684293895);
        googleMap.addMarker(new MarkerOptions()
                .position(sch165)
                .title(getString(R.string.yik_prefix)+" 165"));

        LatLng sch43 = new LatLng(53.91398763696401, 27.419842268989818);
        googleMap.addMarker(new MarkerOptions()
                .position(sch43)
                .title(getString(R.string.yik_prefix)+" 43"));

        LatLng sch125 = new LatLng(53.91553850647292, 27.477990066825093);
        googleMap.addMarker(new MarkerOptions()
                .position(sch125)
                .title(getString(R.string.yik_prefix)+" 125"));

        LatLng sch189 = new LatLng(53.874669753424115, 27.60365855516907);
        googleMap.addMarker(new MarkerOptions()
                .position(sch189)
                .title(getString(R.string.yik_prefix)+" 189"));


        map.setOnMyLocationButtonClickListener(this);
        map.setOnMyLocationClickListener(this);
        enableMyLocation();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // 1. Check if permissions are granted, if so, enable the my location layer
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
            return;
        }

        // 2. Otherwise, request location permissions from the user.
        PermissionUtils.requestLocationPermissions(this, LOCATION_PERMISSION_REQUEST_CODE, true);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        map.moveCamera(CameraUpdateFactory.zoomBy(3f));
        Log.v("LOC", "MyLocation button clicked");
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Log.v("LOC", "Current location:\n" + location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtils
                .isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Permission was denied. Display an error message
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (permissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            permissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

}
