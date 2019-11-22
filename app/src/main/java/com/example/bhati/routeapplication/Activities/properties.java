package com.example.bhati.routeapplication.Activities;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class properties
{
    public static JSONArray jsonArrayLocs  = new JSONArray();
    public static HashMap<Integer,String> locdata =  new HashMap<Integer, String>();
    //public static HashMap<Integer,String> colors =  new HashMap<Integer, String>();
    public static HashMap<String,String> colorstr =  new HashMap<String, String>();
    public static List<String> colorsdata = new ArrayList<String>();
    public static Double loclat=0.0;
    public static Double loclog=0.0;
    public static Double locang=0.0;
    public static Double locdir=0.0;
    public static Double locrol=0.0;
    public static HashMap<String,String> audiodata =  new HashMap<String, String>();
    public static String Server_IP="192.168.43.205";
    public static ArrayList<LatLng> firstCoordinatesOfPolylines = new ArrayList<>();
    // interval for frame extraction
    public static int REGULAR_FRAME_INTERVAL_MILLIS = 5000;

    public static String email = "admin@admin.com";
    public static String password = "adminadmin";

    public static String webViewUrl = "http://geovisuals.cs.kent.edu/";

}
