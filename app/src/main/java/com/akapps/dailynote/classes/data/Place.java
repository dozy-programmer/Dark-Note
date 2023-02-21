package com.akapps.dailynote.classes.data;

import com.google.android.gms.maps.model.LatLng;

import io.realm.RealmObject;

public class Place extends RealmObject {

    private String placeName;
    private String addressString;
    private String placeId;

    public Place(){
        placeName = "";
        addressString = "";
        placeId = "";
    }

    public Place(String placeName, String addressString, String placeId) {
        this.placeName = placeName;
        this.addressString = addressString;
        this.placeId = placeId;
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
}