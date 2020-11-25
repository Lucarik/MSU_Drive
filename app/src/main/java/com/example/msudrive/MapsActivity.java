package com.example.msudrive;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.msudrive.directionhelpers.FetchURL;
import com.example.msudrive.directionhelpers.TaskLoadedCallback;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,TaskLoadedCallback {

    private GoogleMap mMap;
    MarkerOptions place1, place2;
    Polyline currentPolyLine;
    LatLng userLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Location permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            // If no permission return to main page
            openMain();
        }

        mMap.setMyLocationEnabled(true);

        // Get current location through a location change listener
        final int[] i = {0};
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location arg0) {
                LatLng location = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                // Only need starting location so use first current location as start of route
                if (i[0] < 1) {
                    // Current location
                    place1 = new MarkerOptions().position(location);

                    // Montclair State University location
                    place2 = new MarkerOptions().position(new LatLng(40.8643,-74.1986));

                    // Add markers
                    mMap.addMarker(place1.title("Current Location"));
                    mMap.addMarker(place2.title("Montclair University"));

                    // Set focus to current location
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,12));

                    // Send request to Directions API using both locations
                    String url = getUrl(place1.getPosition(), place2.getPosition(), "driving");
                    new FetchURL(MapsActivity.this).execute(url, "driving");


                }

                i[0]++;
            }
        });

    }

    // Method to format a request string to be sent through Google's Directions API
    private String getUrl(LatLng origin, LatLng dest, String directionMode) {
        // Origin
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        // Destination
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        // Mode
        String mode = "mode=" + directionMode;
        String param = str_origin + "&" + str_dest + "&" + mode;
        // format
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + param + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    // Opens main activity/ start page
    public void openMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    // Adds route line
    @Override
    public void onTaskDone(Object... values) {
        if (currentPolyLine != null)
            currentPolyLine.remove();
        currentPolyLine = mMap.addPolyline((PolylineOptions)values[0]);
    }
}