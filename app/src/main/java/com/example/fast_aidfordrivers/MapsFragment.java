package com.example.fast_aidfordrivers;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.fast_aidfordrivers.R;
import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * TODO: Find a way to start RequestGPS Activity after GPS is turned off at any given moment
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private FirebaseFirestore database;

    private LatLng position;
    private boolean positionFound = false;
    private final HashMap<String, Object> mCurrentLocation = new HashMap<>();



    private BitmapDescriptor BitmapFromVector(Context context, int vectorResId) {
        // below line is use to generate a drawable.
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);

        // below line is use to set bounds to our vector drawable.
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        // below line is use to create a bitmap for our
        // drawable which we have added.
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        // below line is use to add bitmap in our canvas.
        Canvas canvas = new Canvas(bitmap);

        // below line is use to draw our
        // vector drawable in canvas.
        vectorDrawable.draw(canvas);

        // after generating our bitmap we are returning our bitmap.
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        database = FirebaseFirestore.getInstance();

        //database = FirebaseDatabase.getInstance(FirebaseApp.getInstance());
        //reference = database.getReference(FirebaseAuth.getInstance().getUid() + "/" + "CurrentLocation/");

        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requireActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        } else {
            //Required to triangulate last known position of the user. ONLY FOR UI PURPOSES
            if(position == null){
                LocationServices.getFusedLocationProviderClient(requireActivity())
                        .getLastLocation()
                        .addOnSuccessListener(loc -> {
                            try {
                                position = new LatLng(loc.getLatitude(), loc.getLongitude());
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 12));
                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        });


            }
            googleMap.setMyLocationEnabled(true);

            FusedLocationProviderClient mLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

            LocationRequest locationRequest = LocationRequest.create().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(100);
            locationRequest.setFastestInterval(100);
            locationRequest.setSmallestDisplacement(3);

            LocationCallback locationCallback = new LocationCallback() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    super.onLocationResult(locationResult);
                    position = new LatLng(locationResult.getLastLocation().getLatitude(), locationResult.getLastLocation().getLongitude());
                    if (!positionFound) {

                        //Animated zoom for maps
                        googleMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(position, 18), 300, new GoogleMap.CancelableCallback() {
                            @Override
                            public void onCancel() {
                                
                            }

                            @Override
                            public void onFinish() {

                            }
                        });

                        positionFound = true;

                    }
                    Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());

                    String hash = GeoFireUtils.getGeoHashForLocation(new GeoLocation(position.latitude, position.longitude));

                    //Updates address in Firestore every time locationCallback is called
                    mCurrentLocation.put("lat", position.latitude);
                    mCurrentLocation.put("lng", position.longitude);
                    mCurrentLocation.put("geohash", hash);
                    database.collection("Driver")
                            .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                            .set(mCurrentLocation);

                }
            };
            mLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        }
    }


}