//If you are reading this and are a judge for NASA spaceapps challenge, you are in the wrong branch
package com.spaceappschallenge.watertracker;

import android.content.Context;
import android.provider.Settings;
import android.os.Bundle;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spaceappschallenge.watertracker.dao.MappingDao;
import com.spaceappschallenge.watertracker.dao.MappingDaoImpl;
import com.spaceappschallenge.watertracker.model.DataPoint;

import android.location.LocationManager;
import android.location.Location;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class WaterTrackMap extends SupportMapFragment {
    private GoogleMap mMap;
    private int minute;
    private boolean tracking;
    private MappingDao mappingDao = new MappingDaoImpl();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //this.getActivity().setContentView(R.layout.fragment_navigation);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpMapIfNeeded();
    }

    @Override
    public void onResume() {
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
            mMap = this.getMap();
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
                updateMap();
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
                LatLng location = new LatLng(marker.getPosition().latitude, marker.getPosition().longitude);
                viewDetails(location);
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
            mMap.clear();
            populateMarkers();
        }
    }

    // Provides user with a popup box to choose map view mode
    // Options for map view mode are street view, street + earth, and terrain/elevation
    private void setMapViewMode(){
        AlertDialog mapView = new AlertDialog.Builder(this.getActivity())
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

    private void viewDetails(final LatLng location){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View thisView = inflater.inflate(R.layout.dialog_edit, null);
        final AlertDialog edit = new AlertDialog.Builder(this.getActivity())
                .setTitle("Edit Data Point")
                .setView(thisView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        DataPoint newPoint = new DataPoint();

                        Spinner category = (Spinner)thisView.findViewById(R.id.category);
                        if(category.getSelectedItem().toString().equals("Stream or Lake"))
                            newPoint.setCategory("Stream or Lake");
                        else if(category.getSelectedItem().toString().equals("Well"))
                            newPoint.setCategory("Well");
                        else if(category.getSelectedItem().toString().equals("Gov. Facility"))
                            newPoint.setCategory("Gov. Facility");
                        else if(category.getSelectedItem().toString().equals("Spring"))
                            newPoint.setCategory("Spring");

                        Spinner waterUse = (Spinner)thisView.findViewById(R.id.waterUse);
                        if(waterUse.getSelectedItem().toString().equals("Drinking"))
                            newPoint.setPurpose("Drinking");
                        else if(waterUse.getSelectedItem().toString().equals("Agriculture"))
                            newPoint.setPurpose("Agriculture");
                        else if(waterUse.getSelectedItem().toString().equals("Other"))
                            newPoint.setPurpose("Other");

                        newPoint.setLatitude(location.latitude);
                        newPoint.setLongitude(location.longitude);
                        int sample_id = mappingDao.addDataPoint(newPoint);
                        sendObservation(sample_id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .create();

        AlertDialog details = new AlertDialog.Builder(this.getActivity())
                .setTitle("Details")
                .setView(inflater.inflate(R.layout.dialog_details, null))
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        edit.show();
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .create();
        details.show();
    }
    // Gets locations and category of previous observations and prints them to the map
    private void populateMarkers(){
        double bounds[] = getBounds();
        DataPoint[] points = mappingDao.retrieveDataPoints(bounds[0], bounds[2], bounds[1], bounds[3]);
        for(int i=0; i< points.length; i++){
            if(points[i].getCategory().equals("Well"))
                mMap.addMarker(new MarkerOptions().position(new LatLng(points[i].getLatitude(), points[i].getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            else if(points[i].getCategory().equals("Stream or Lake"))
                mMap.addMarker(new MarkerOptions().position(new LatLng(points[i].getLatitude(), points[i].getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            else if(points[i].getCategory().equals("Gov. Facility"))
                mMap.addMarker(new MarkerOptions().position(new LatLng(points[i].getLatitude(), points[i].getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            else if(points[i].getCategory().equals("Spring"))
                mMap.addMarker(new MarkerOptions().position(new LatLng(points[i].getLatitude(), points[i].getLongitude())).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }

    private void logSample(final LatLng location){
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View thisView = inflater.inflate(R.layout.dialog_edit, null);
        double longitude = (double)Math.round(location.longitude * 10000) / 10000;
        double latitude = (double)Math.round(location.latitude * 10000) / 10000;
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(new LatLng(location.latitude, location.longitude)));
        final AlertDialog add = new AlertDialog.Builder(this.getActivity())
                .setTitle("Input details")
                .setView(thisView)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        DataPoint newPoint = new DataPoint();

                        Spinner category = (Spinner)thisView.findViewById(R.id.category);
                        if(category.getSelectedItem().toString().equals("Stream or Lake"))
                            newPoint.setCategory("Stream or Lake");
                        else if(category.getSelectedItem().toString().equals("Well"))
                            newPoint.setCategory("Well");
                        else if(category.getSelectedItem().toString().equals("Gov. Facility"))
                            newPoint.setCategory("Gov. Facility");
                        else if(category.getSelectedItem().toString().equals("Spring"))
                            newPoint.setCategory("Spring");

                        Spinner waterUse = (Spinner)thisView.findViewById(R.id.waterUse);
                        if(waterUse.getSelectedItem().toString().equals("Drinking"))
                            newPoint.setPurpose("Drinking");
                        else if(waterUse.getSelectedItem().toString().equals("Agriculture"))
                            newPoint.setPurpose("Agriculture");
                        else if(waterUse.getSelectedItem().toString().equals("Other"))
                            newPoint.setPurpose("Other");

                        newPoint.setLatitude(location.latitude);
                        newPoint.setLongitude(location.longitude);
                        int sample_id = mappingDao.addDataPoint(newPoint);
                        sendObservation(sample_id);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_map)
                .create();

        AlertDialog alert = new AlertDialog.Builder(this.getActivity())
                .setTitle("Log an observation at this location?")
                .setMessage("Lat: " + latitude + " Lon: " + longitude)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        add.show();
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
                .setIcon(android.R.drawable.ic_dialog_map)
                .create();
        alert.show();
    }

    // Gets Current Latitude and Longitude
    private LatLng getCurrentLocation(){
        try {
            if(mMap.getMyLocation() != null){
                return new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
            } else {
                LocationManager locationManager = (LocationManager) this.getActivity().getSystemService(Context.LOCATION_SERVICE);
                //Check to make sure location services are enabled
                boolean GPS_Enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean Network_Enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if (GPS_Enabled) { //Get location from GPS
                    return new LatLng(locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER).getLongitude());
                } else if (Network_Enabled) { //Get location from Network
                    return new LatLng(locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER).getLatitude(), locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER).getLongitude());
                } else {
                    AlertDialog alert = new AlertDialog.Builder(this.getActivity())
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

    private void sendObservation(final int sample_id){
        try{
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View thisView = inflater.inflate(R.layout.return_sample_id, null);
            TextView sample_id_view = (TextView)thisView.findViewById(R.id.sample_id);
            sample_id_view.setText(getString(sample_id));
            AlertDialog return_sample_id = new AlertDialog.Builder(this.getActivity())
                    .setTitle("ATTENTION!!!")
                    .setMessage("Observation was successfully logged to the Database.")
                    .setView(thisView)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .create();
            return_sample_id.show();
        } catch (Exception e) {
            AlertDialog err_msg = new AlertDialog.Builder(this.getActivity())
                    .setTitle("ERROR!!!")
                    .setMessage("An error occurred writing the observation to the database. Please consider reporting this error to the developers.")
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .create();
            err_msg.show();
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
    public double[] getBounds(){
        double maxLongitude, maxLatitude, minLongitude, minLatitude;
        maxLatitude = mMap.getProjection().getVisibleRegion().latLngBounds.northeast.latitude;
        maxLongitude = mMap.getProjection().getVisibleRegion().latLngBounds.northeast.longitude;
        minLatitude = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude;
        minLongitude = mMap.getProjection().getVisibleRegion().latLngBounds.southwest.longitude;
        return new double[] {maxLatitude, maxLongitude, minLatitude, minLongitude};
    }
}
