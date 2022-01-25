package com.duynn.memorableplaces;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.duynn.memorableplaces.databinding.ActivityMapsBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private LocationListener locationListener;
    private LocationManager locationManager;

    public void showMapOnLocation(Location location, String title){
        if(location != null){
            LatLng userLocation = new LatLng(location.getLatitude(),location.getLongitude());
            //mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLocation).title(title));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //check general permission
        if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            //check location permistion
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                //show location in fist view
                Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                showMapOnLocation(lastKnowLocation,"Your location");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //long press to mark location
        mMap.setOnMapLongClickListener(this);

        Intent intent = getIntent();
        Toast.makeText(this, ""+intent.getIntExtra("placeNumber",0), Toast.LENGTH_SHORT).show();

        int placeNumber = intent.getIntExtra("placeNumber",0);
        if( placeNumber == 0){
            //show user location
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    showMapOnLocation(location,"New mark");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }
            };
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListener,null);
                //show location in fist view
                Location lastKnowLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                showMapOnLocation(lastKnowLocation,"Your location");
            }else{
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
        }else{
            Location placeLocation = new Location(LocationManager.GPS_PROVIDER);
            placeLocation.setLatitude(MainActivity.locations.get(placeNumber).latitude);
            placeLocation.setLongitude(MainActivity.locations.get(placeNumber).longitude);

            showMapOnLocation(placeLocation,MainActivity.places.get(placeNumber));
        }


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address ="";
        try {
            List<Address> addressList = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);

            if(addressList != null && addressList.size() >0){
                if(addressList.get(0).getThoroughfare() != null){
                    if(addressList.get(0).getSubThoroughfare() != null){
                        address += addressList.get(0).getSubThoroughfare() + " ";
                    }
                    address += addressList.get(0).getThoroughfare() + " ";
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        if(address.equals("")){
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd-mm-yyyy");
            address+= sdf.format(new Date());
        }

        mMap.addMarker(new MarkerOptions().position(latLng).title(address));

        MainActivity.places.add(address);
        MainActivity.locations.add(latLng);

        MainActivity.arrayAdapter.notifyDataSetChanged();
        Toast.makeText(this,"Saved",Toast.LENGTH_LONG).show();
    }
}