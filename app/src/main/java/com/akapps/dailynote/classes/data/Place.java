package com.akapps.dailynote.classes.data;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;

public class Place extends RealmObject {

    private String placeName;
    private String addressString;
    private String placeId;
    private double latitude;
    private double longitude;

    public Place(){
        placeName = "";
        addressString = "";
        placeId = "";
        longitude = 0;
        latitude = 0;
    }

    public Place(String placeName, String addressString, String placeId, double latitude, double longitude) {
        this.placeName = placeName;
        this.addressString = addressString;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public String getPlaceId() {
        return placeId;
    }

    public void setPlaceId(String placeId) {
        this.placeId = placeId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}