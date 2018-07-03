package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.service.carrier.CarrierMessagingService;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

// COMPLETED (1) Create a Geofencing class with a Context and GoogleApiClient constructor that
// initializes a private member ArrayList of Geofences called mGeofenceList

public class Geofencing implements ResultCallback {

    private static final String TAG = Geofencing.class.getSimpleName();

    private static final int GEOFENCE_TIMEOUT = 24 * 60 * 60 * 60 * 1000; // 24 hours in milliseconds
    private static final int GEOFENCE_RADIUS = 50; // 50 meters

    private Context mContext;
    private GoogleApiClient mClient;
    private PendingIntent mGeofencePendingIntent;
    private List<Geofence> mGeoFenceList;

    public Geofencing(Context context, GoogleApiClient client) {
        this.mContext = context;
        this.mClient = client;
        mGeofencePendingIntent = null;
        mGeoFenceList = new ArrayList<>();
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, String.format("Error adding|removing geofence : %s",
                result.getStatus().toString()));
    }

    // COMPLETED (2) Inside Geofencing, implement a public method called updateGeofencesList that
    // given a PlaceBuffer will create a Geofence object for each Place using Geofence.Builder
    // and add that Geofence to mGeofenceList

    public void updateGeoFenceList(PlaceBuffer places) {
        mGeoFenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) { return; }
        for (Place place : places) {
            String placeUid = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUid)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
            mGeoFenceList.add(geofence);
        }
    }

    // COMPLETED (3) Inside Geofencing, implement a private helper method called getGeofencingRequest that
    // uses GeofencingRequest.Builder to return a GeofencingRequest object from the Geofence list

    private GeofencingRequest getGeofencingRequest() {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(mGeoFenceList)
                .build();
    }

    // COMPLETED (5) Inside Geofencing, implement a private helper method called getGeofencePendingIntent that
    // returns a PendingIntent for the com.example.android.shushme.GeofenceBroadcastReceiver class

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent == null) {
            Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
            mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return mGeofencePendingIntent;
    }

    // COMPLETED (6) Inside Geofencing, implement a public method called registerAllGeofences that
    // registers the GeofencingRequest by calling LocationServices.GeofencingApi.addGeofences
    // using the helper functions getGeofencingRequest() and getGeofencePendingIntent()

    public void registerAllGeofences() {
        if (mClient == null || !mClient.isConnected() ||
                mGeoFenceList == null || mGeoFenceList.size() == 0) { return; }
        try {
            LocationServices.GeofencingApi.addGeofences(mClient, getGeofencingRequest(),
                    getGeofencePendingIntent()).setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }


    // COMPLETED (7) Inside Geofencing, implement a public method called unRegisterAllGeofences that
    // unregisters all geofences by calling LocationServices.GeofencingApi.removeGeofences
    // using the helper function getGeofencePendingIntent()

    public void unRegisterAllGeofences() {
        if (mClient == null || !mClient.isConnected()) { return; }
        try {
            LocationServices.GeofencingApi.removeGeofences(mClient, getGeofencePendingIntent())
                    .setResultCallback(this);
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage());
        }
    }
}
