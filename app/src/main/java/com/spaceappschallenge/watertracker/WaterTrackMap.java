package com.spaceappschallenge.watertracker;

import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.LocationManager;
import android.location.Location;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.util.Calendar;

public class WaterTrackMap extends FragmentActivity {
    private GoogleMap mMap;
    private int minute;
    private boolean tracking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_water_track_map);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMyLocationEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    // Sets up map location and listeners/handlers for various events
    private void setUpMap() {
        minute = Calendar.getInstance().MINUTE;
        tracking = true;
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location){
                updateMap();
            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mMap.clear();
                populateMarkers();
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                logSample(latLng);
            }
        });
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setMapViewMode();
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Display additional info
                viewDetails(/*marker identifier*/);
                return false;
            }
        });

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(getCurrentLocation(), 18));
        populateMarkers();
    }

    /* Function: updateMap
     * Parameters: none
     * Return: none
     * Description: Obtains GPS coordinates every minute and updates the map
     */
    private void updateMap() {
        if(Calendar.getInstance().MINUTE - minute >= 1 && tracking) {
            minute = Calendar.getInstance().MINUTE;
            mMap.moveCamera(CameraUpdateFactory.newLatLng(getCurrentLocation()));
        }
    }

    // Provides user with a popup box to choose map view mode
    // Options for map view mode are street view, street + earth, and terrain/elevation
    private void setMapViewMode(){
        AlertDialog mapView = new AlertDialog.Builder(this)
                .setTitle("Choose Map View")
                .setCancelable(true)
                .setPositiveButton("Street", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        dialog.dismiss();
                    }
                })
                .setNeutralButton("Earth", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Elevation", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        mapView.show();
    }

    private void viewDetails(/*marker identifier*/){
        AlertDialog details = new AlertDialog.Builder(this)
                .setTitle("Details")
                .setMessage("Details...")
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        details.show();
    }
    // Gets locations and category of previous observations and prints them to the map
    private void populateMarkers(){
        mMap.addMarker(new MarkerOptions().position(new LatLng(getCurrentLocation().latitude+1, getCurrentLocation().longitude+1)));
        /*double bounds[] = getBounds();
        // Query database
        for(int i=0; i<sizeof(); i++){
            if(array[i].category == "well")
                mMap.addMarker(new MarkerOptions().position(new LatLng(array[i].latitude, array[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            else if(array[i].category == "stream")
                mMap.addMarker(new MarkerOptions().position(new LatLng(array[i].latitude, array[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            else if(array[i].category == "government")
                mMap.addMarker(new MarkerOptions().position(new LatLng(array[i].latitude, array[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            else if(array[i].category == "spring")
                mMap.addMarker(new MarkerOptions().position(new LatLng(array[i].latitude, array[i].longitude)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }*/
    }

    private void logSample(final LatLng location){
        double longitude = (double)Math.round(location.longitude * 10000) / 10000;
        double latitude = (double)Math.round(location.latitude * 10000) / 10000;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        AlertDialog alert = new AlertDialog.Builder(this)
                .setTitle("Log an observation at this location?")
                .setMessage("Lat: " + latitude + " Lon: " + longitude)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Add logic here
                        mMap.clear();
                        populateMarkers();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.clear();
                        populateMarkers();
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .create();
        alert.show();
    }

    // Gets Current Latitude and Longitude
    private LatLng getCurrentLocation(){
        try {
            if(mMap.getMyLocation() != null){
                return new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            } else {
                LocationManager locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
                //Check to make sure location services are enabled
                boolean GPS_Enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean Network_Enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (GPS_Enabled) { //Get location from GPS
                    return new LatLng(locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER).getLongitude());
                } else if (Network_Enabled) { //Get location from Network
                    return new LatLng(locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER).getLongitude());
                } else {
                    AlertDialog alert = new AlertDialog.Builder(this)
                            .setTitle("Geolocation Failed")
                            .setMessage("We were unable to find your location. Please make sure location services are enabled on your phone and restart the application.")
                            .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .create();
                    alert.show();
                    return new LatLng(0, 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new LatLng(0,0);
        }
    }

    // Public getter for tracking boolean
    public boolean getTracking(){
        return tracking;
    }

    // Public setter for tracking boolean to turn map tracking on and off
    public void setTracking(boolean setTracking){
        tracking = setTracking;
    }

    // Gets the latitude and longitude of the four sides of the viewable area
    private double[] getBounds(){
        double maxLongitude, maxLatitude, minLongitude, minLatitude;
        maxLatitude = mMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude;
        maxLongitude = mMap.getProjection().getVisibleRegion().latLngBounds.northeast.longitude;
        minLatitude = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;
        minLongitude = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.longitude;
        return new double[] {maxLatitude, maxLongitude, minLatitude, minLongitude};
    }
}
