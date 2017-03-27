package com.example.nurro.tugasakhirlokasi;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static com.google.android.gms.internal.zzip.runOnUiThread;

/**
 * Created by nurro on 3/4/2017.
 */

public class GoogleAPIFragment extends Fragment {
    View myView;
    ListView myList;
    ArrayAdapter<String> adapter;
    ArrayList<String> data;
    Button searchAPIButton;
    GoogleAPITracker googleAPI;
    TextView textView;
    boolean enableButton = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        myView = inflater.inflate(R.layout.google_api_layout, container, false);
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitNetwork().build());
        textView = (TextView)myView.findViewById(R.id.resultAPI);
        myList = (ListView)myView.findViewById(R.id.listGPSAPISearch);
        data = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, data);
        myList.setAdapter(adapter);

        searchAPIButton = (Button)myView.findViewById(R.id.buttonSearchAPILocation);
        searchAPIButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(googleAPI == null) {
                    googleAPI = new GoogleAPITracker(myView.getContext(), textView, myList, data);
                }
                else {
                    if (!googleAPI.isGoogleApiClientConnected()) {
                        googleAPI.getLocation();
                    }
                    else {
                        Log.d("Finish", "Finish API");
                        googleAPI.stopUsingAPI();
                    }
                }
            }
        });


        return myView;
    }

    public void onBackPressed() {
        googleAPI.stopUsingAPI();
        if(getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
    }

    public void disableGPS() {
        if(googleAPI != null) {
            googleAPI.stopUsingAPI();
        }
    }


}