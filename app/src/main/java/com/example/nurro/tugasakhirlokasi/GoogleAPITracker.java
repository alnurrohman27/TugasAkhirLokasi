package com.example.nurro.tugasakhirlokasi;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.ConnectionResult;

import com.google.android.gms.common.GooglePlayServicesUtil;

import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationListener;

import com.google.android.gms.location.LocationRequest;

import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.w3c.dom.Text;

import static com.google.android.gms.internal.zzip.runOnUiThread;


/**
 * Created by nurro on 3/3/2017.
 */

public class GoogleAPITracker extends Service implements LocationListener {
    //Variable for Context
    private Context context;

    //Variable for TextView
    private TextView textView;

    //Variable for ListView
    private ListView myList;
    private ArrayList<String>data;
    private ArrayAdapter<String> arrayAdapter;

    //GoogleAPI Client
    private GoogleApiClient googleApiClient;

    //Variable for Attribute GPS
    private String providerName;
    private LocationRequest locationRequest;
    private double latitude;
    private double longitude;
    private double accuracy;
    private double altitude;

    // The minimum distance to change Updates in meters
    private static final long minDistance = 0; // 10 meters

    // The minimum time between updates in milliseconds
    private static final long minTime = 1000 * 1 * 1; // 1 menit

    // Max iteration
    private int iteration = 0;

    // Connection to SERVER
    private String link = "http://alnurrohman27.000webhostapp.com/TA/select.php?command=location";
    private String[][] dataPlaces;
    private InputStream inputStream = null;
    private String line;
    private String result;
    private int dataLength;
    private String[][] dataIteration;

    private void setContext(Context context) {
        this.context = context;
    }
    private void setTextView(TextView textView) {
        this.textView = textView;
    }
    private void setMyListAdapter(ListView myList) {
        this.myList = myList;
        this.arrayAdapter = (ArrayAdapter<String>)this.myList.getAdapter();
    }
    private void setArrayAdapter(Context context) {
        this.context = context;
    }
    private void setData(ArrayList<String> data) {
        this.data = data;
    }

    public GoogleAPITracker(final Context context2, final TextView textView2, final ListView myList2, final ArrayList<String> data2) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setContext(context2);
                setTextView(textView2);
                setMyListAdapter(myList2);
                setData(data2);
                getPlaces();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        getLocation();
                    }
                });
            }
        }).start();
    }

    private boolean checkPlayServices() {
        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.context);
        if (result != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(result)) {
                Toast.makeText(this.context,
                        "This device is supported. Please download Google Play Services", Toast.LENGTH_LONG)
                        .show();
            }
            else {
                Toast.makeText(this.context,
                        "This device is not supported", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }

    public boolean isGoogleApiClientConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }

    private void startAPI() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this.context).addApi(LocationServices.API)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks(){
                        @Override

                        public void onConnectionSuspended(int cause) {

                        }

                        @Override

                        public void onConnected(Bundle connectionHint) {

                        }

                    }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                        }

                    }).build();
            googleApiClient.connect();
        }
        else {
            googleApiClient.connect();
        }
    }

    private void requestUpdateLocation(final LocationListener listener) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(minTime);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, listener);
                }
                catch (SecurityException se) {
                    Log.d("Security Exception", se.toString());
                    se.printStackTrace();
                }
                catch (Exception ex){
                    Log.d("Ex Request", ex.toString());
                    ex.printStackTrace();
                }
            }
        }, minTime);
    }



    public void getLocation() {
        try {
            if (checkPlayServices()) {
                Toast.makeText(this.context, "Pencarian Dimulai", Toast.LENGTH_SHORT).show();
                dataIteration = new String[10][8];
                this.iteration = 0;
                clearListData();
                startAPI();
                requestUpdateLocation(this);
            }
        }
        catch (Exception ex) {
            Log.d("Error Google API", ex.toString());
        }
    }

    private void clearListData() {
        this.textView.setText("Hasil Lokasi");
        this.data.clear();
        arrayAdapter.notifyDataSetChanged();
    }

    //Get Latitude Value
    public double getLatitude() {
        return latitude;
    }

    //Get Longitude Value
    public double getLongitude() {
        return longitude;
    }

    //Get Altitude Value
    public double getAltitude() {
        return altitude;
    }

    //Get Accuracy Value
    public double getAccuracy() {
        return accuracy;
    }

    public void stopUsingAPI() {
        Toast.makeText(this.context, "Pencarian Selesai", Toast.LENGTH_SHORT).show();
        if (googleApiClient != null) {
            googleApiClient.disconnect();
        }
    }

    private void getPlaces() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        line = null;
        try {
            URLConnection url = new URL(link).openConnection();

            HttpURLConnection con = (HttpURLConnection)url;
            //con.setRequestMethod("GET");

            inputStream = new BufferedInputStream(con.getInputStream());

            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder sb = new StringBuilder();

            while((line = br.readLine()) != null){
                sb.append(line+"\n");
            }
            inputStream.close();
            result = sb.toString();

        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            JSONArray jsonArray = new JSONArray(result);
            dataLength = jsonArray.length();
            dataPlaces = new String[jsonArray.length()][7];
            for(int i = 0; i < dataLength; i++){
                int j=0;
                dataPlaces[i][j] = jsonArray.getJSONObject(i).getString("id");
                j++;
                dataPlaces[i][j] = jsonArray.getJSONObject(i).getString("name");
                j++;
                dataPlaces[i][j] = jsonArray.getJSONObject(i).getString("latitude");
                j++;
                dataPlaces[i][j] = jsonArray.getJSONObject(i).getString("longitude");
                j++;
                dataPlaces[i][j] = jsonArray.getJSONObject(i).getString("range");
                j++;
                dataPlaces[i][j] = jsonArray.getJSONObject(i).getString("altitude");
                j++;
                dataPlaces[i][j] = "0";
            }

        }

        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void KNN() {
        double tempDistance, tempAltitude;
        String[][] result = new String[10][3];
        int counter = 0;
        String[] tempData;
        Location loc1, loc2;

        for (int i = 0; i < iteration; i++) {
            tempData = new String[3];
            loc1 = new Location("");
            loc1.setLongitude(Double.parseDouble(dataIteration[i][1]));
            loc1.setLatitude(Double.parseDouble(dataIteration[i][2]));
            tempAltitude = Double.parseDouble(dataIteration[i][3]);
            for (int j = 0; j < dataLength; j++) {
                loc2 = new Location("");
                loc2.setLatitude(Double.parseDouble(dataPlaces[j][2]));
                loc2.setLongitude(Double.parseDouble(dataPlaces[j][3]));
                tempDistance = loc1.distanceTo(loc2);
                if (tempData[0] == null && tempAltitude != 0 && tempAltitude < Double.parseDouble(dataPlaces[j][5])) {
                    tempData[0] = Double.toString(tempDistance);
                    tempData[1] = dataPlaces[j][0];
                    tempData[2] = dataPlaces[j][1];
                }
                else if (tempData[0] != null && tempAltitude != 0 && tempAltitude < Double.parseDouble(dataPlaces[j][5])) {
                    if (tempDistance < Double.parseDouble(tempData[0])) {
                        tempData[0] = Double.toString(tempDistance);
                        tempData[1] = dataPlaces[j][0];
                        tempData[2] = dataPlaces[j][1];
                    }
                }
                else {
                    dataIteration[i][5] = Integer.toString(0);
                    dataIteration[i][6] = Integer.toString(0);
                    dataIteration[i][7] = Integer.toString(0);
                }
            }
            if (tempData[0] != null) {
                dataIteration[i][5] = tempData[0];
                dataIteration[i][6] = tempData[1];
                dataIteration[i][7] = tempData[2];
            }

            boolean available = false;
            for (int j = 0; j < counter; j++) {
                if (result[j][0].equals(dataIteration[i][6])) {
                    int tempCounter = Integer.parseInt(result[j][2])+1;
                    result[j][2] = Integer.toString(tempCounter);
                    available = true;
                    Log.d("Result " + Integer.toString(i + 1), result[j][1] + " n: " + result[j][2]);
                    Log.d("Data Iteration " + Integer.toString(i + 1), dataIteration[i][6]);
                    break;
                }
            }
            if (!available) {
                result[counter][0] = dataIteration[i][6];
                result[counter][1] = dataIteration[i][7];
                result[counter][2] = Integer.toString(1);
                Log.d("Result " + Integer.toString(i + 1), result[counter][1] + " n: " + result[counter][2]);
                Log.d("Data Iteration " + Integer.toString(i + 1), dataIteration[i][6]);
                counter++;
            }
        }
        int temp1 = 0, temp2 = 0;
        int idResult = 0;
        temp1 = Integer.parseInt(result[0][2]);
        idResult = Integer.parseInt(result[0][0]);
        String placeResult = result[0][1];
        for (int i = 1; i < counter; i++) {
            temp2 = Integer.parseInt(result[i][2]);
            if (temp2 > temp1) {
                temp1 = Integer.parseInt(result[i][2]);
                idResult = Integer.parseInt(result[i][0]);
                placeResult = result[i][1];
            }
        }
        Log.d("ID: " + idResult, placeResult);
        if (idResult == 0) {
            setListDataAndResult("Tidak Diketahui");
        }
        else {
            setListDataAndResult(placeResult);
        }
    }

    private void setListDataAndResult(String result) {
        for (int i = 0; i < dataIteration.length; i++) {
            data.add("ID: " + dataIteration[i][0] + "\nLongitude: " + dataIteration[i][1] + "\nLatitude: " +
                    dataIteration[i][2] + "\nAltitude: " + dataIteration[i][3] + "\nAccuracy: " + dataIteration[i][4] +
                    "\nDistance: " + dataIteration[i][5] + "\nPlace: " + dataIteration[i][7]);
        }
        arrayAdapter.notifyDataSetChanged();
        textView.setText(result);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.accuracy = location.getAccuracy();
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = location.getAltitude();
        Log.d("Data", "Longitude: " + getLongitude() + "\nLatitude: " +
                getLatitude() + "\nAccuracy: " + getAccuracy() + "\nAltitude: " + getAltitude());
        if(this.accuracy <= 10) {
            if (iteration < 10) {
                int i = 0;
                dataIteration[iteration][i] = Integer.toString(iteration + 1);
                i++;
                dataIteration[iteration][i] = Double.toString(getLongitude());
                i++;
                dataIteration[iteration][i] = Double.toString(getLatitude());
                i++;
                dataIteration[iteration][i] = Double.toString(getAltitude());
                i++;
                dataIteration[iteration][i] = Double.toString(getAccuracy());
                iteration++;
            }
            else {
                KNN();
                stopUsingAPI();
            }
        }
    }
}
