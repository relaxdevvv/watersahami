package ir.nimcode.dolphin.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.marcouberti.autofitbutton.AutoFitButton;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.adapter.PropertyAdapter;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.database.DatabaseHelper;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.Sheet;
import ir.nimcode.dolphin.util.Utilities;

/**
 * Created by saeed on 2/20/18.
 */

public class LocationFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    public final static String TAG = "TAG_SheetFragment";
    public static LatLng latLng;
    public Property utmZoneProperty;
    public Property utmXProperty;
    public Property utmYProperty;
    public DatabaseHelper mDatabaseHelper;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.update_location)
    AutoFitButton updateLocation;
    @BindView(R.id.save_location)
    AutoFitButton saveLocation;
    @BindView(R.id.utm_y)
    MaterialEditText utmY;
    @BindView(R.id.utm_x)
    MaterialEditText utmX;
    @BindView(R.id.utm_zone)
    MaterialEditText utmZone;
    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    @BindView(R.id.location_root_view)
    CardView locationRootView;
    private SweetAlertDialog alertDialog;
    private GoogleApiClient mGoogleApiClient;
    private Context context;
    private PropertyAdapter propertyAdapter;
    private List<Property> properties;
    private int position;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_location, container, false);
        rootView.setRotationY(180);

        ButterKnife.bind(this, rootView);

        context = getActivity();

        alertDialog = new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE);

        Bundle bundle = getArguments();

        updateLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMyLocation(false);
            }
        });

        saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoMyLocation(true);
            }
        });

        position = FragmentPagerItem.getPosition(bundle);

        properties = SheetActivity.sheetsProperties.get(position - 1);
        propertyAdapter = new PropertyAdapter(context, properties, position);

        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(propertyAdapter);

        utmZoneProperty = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("utm_zone").getId());
        utmXProperty = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("utm_x").getId());
        utmYProperty = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("utm_y").getId());
        if (utmZoneProperty == null || !utmZoneProperty.isEnabled()) {
            locationRootView.setVisibility(View.GONE);
        }

        propertyAdapter.notifyDataSetChanged();
        if (properties.isEmpty()) {
            notingFoundErrorLayout.setVisibility(View.VISIBLE);
        }

        buildGoogleApiClient();

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    @Override
    public void onConnected(Bundle bundle) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest mLocationRequest = new LocationRequest()
                .setInterval(10000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        gotoMyLocation(false);
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toasty.error(context, getString(R.string.connection_disconnect), Toast.LENGTH_SHORT, true).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void gotoMyLocation(boolean bind) {
        if (Utilities.isLocationAvailable(context)) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            if (location != null) {
                latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Utilities.UTM utm = Utilities.LatLngToUTM(latLng);
                utmZone.setText(String.valueOf(utm.getZone()));
                utmX.setText(String.format(new Locale("en"), "%6d", Math.round(utm.getX())));
                utmY.setText(String.format(new Locale("en"), "%7d", Math.round(utm.getY())));
                if (bind) {

                    Sheet sheet = SheetActivity.sheets.get(position - 1);
                    if (utmZoneProperty != null) {
                        utmZoneProperty.setValue(utmZone.getText().toString());
                        if (!utmZoneProperty.isChecked()) {
                            sheet.setProperties_checked_count(sheet.getProperties_checked_count() + 1);
                        }
                        utmZoneProperty.setChecked(true);
//                        propertyAdapter.notifyItemChanged(properties.indexOf(utmZoneProperty));
                    }
                    if (utmXProperty != null) {
                        utmXProperty.setValue(utmX.getText().toString());
                        if (!utmXProperty.isChecked()) {
                            sheet.setProperties_checked_count(sheet.getProperties_checked_count() + 1);
                        }
                        utmXProperty.setChecked(true);
//                        propertyAdapter.notifyItemChanged(properties.indexOf(utmXProperty));
                    }
                    if (utmYProperty != null) {
                        utmYProperty.setValue(utmY.getText().toString());
                        if (!utmYProperty.isChecked()) {
                            sheet.setProperties_checked_count(sheet.getProperties_checked_count() + 1);
                        }
                        utmYProperty.setChecked(true);
//                        propertyAdapter.notifyItemChanged(properties.indexOf(utmYProperty));
                    }
                    propertyAdapter.notifyDataSetChanged();

                }

            }
        } else {
            alertDialog.setTitleText(getString(R.string.error));
            alertDialog.setContentText(getString(R.string.no_gps_message));
            alertDialog.setCancelable(false);
            alertDialog.setConfirmText(getString(R.string.settings));
            alertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    alertDialog.dismiss();
                    Intent intent = new Intent();
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
                }
            });
            alertDialog.setCancelText(getString(R.string.retry));
            alertDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() != null) {
                propertyAdapter.notifyDataSetChanged();
            }
        }
    }
}