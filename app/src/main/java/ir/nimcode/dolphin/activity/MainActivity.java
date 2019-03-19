package ir.nimcode.dolphin.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.maps.android.data.kml.KmlLayer;
import com.google.maps.android.geometry.Point;
import com.marcouberti.autofitbutton.AutoFitButton;
import com.mikepenz.fastadapter.IAdapter;
import com.mikepenz.fastadapter.listeners.OnClickListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.api.Pagination;
import ir.nimcode.dolphin.api.ResponseBase;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.model.Form;
import ir.nimcode.dolphin.model.FormSheet;
import ir.nimcode.dolphin.model.FormSheetProperty;
import ir.nimcode.dolphin.model.FormsUpdate;
import ir.nimcode.dolphin.model.KmlFile;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.PropertyValues;
import ir.nimcode.dolphin.model.Sheet;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.Utilities;
import retrofit2.Call;
import retrofit2.Response;

import static ir.nimcode.dolphin.model.Document.status.accept;
import static ir.nimcode.dolphin.model.Document.status.draft;
import static ir.nimcode.dolphin.model.Document.status.pending;
import static ir.nimcode.dolphin.model.Document.status.reject;

public class MainActivity extends FullAppCompatActivity
        implements
        Drawer.OnDrawerItemClickListener,
        OnClickListener<IDrawerItem>,
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private static final String TAG = "TAG_MainActivity";
    private static final int DOCUMENT_REQUEST_CODE = 1001;
    public static ProfileDrawerItem profileDrawerItem;
    public static AccountHeader headerResult;
    @BindView(R.id.add_new)
    FloatingActionButton addNew;
    @BindView(R.id.crossfade_content)
    FrameLayout crossfadeContent;
    @BindView(R.id.action_search)
    FloatingActionButton actionSearch;
    @BindView(R.id.action_history)
    FloatingActionButton actionHistory;
    @BindView(R.id.toolbar_title)
    TextView toolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.form_name)
    TextView formName;
    @BindView(R.id.info_label)
    TextView infoLabel;
    @BindView(R.id.info_value)
    TextView infoValue;
    @BindView(R.id.last_seen_date)
    TextView lastSeenDate;
    @BindView(R.id.details)
    AutoFitButton details;
    @BindView(R.id.map_bottom_sheet)
    LinearLayout mapBottomSheet;
    //    @BindView(R.id.filter)
//    ImageView filter;
    @BindView(R.id.my_location)
    ImageView myLocation;
    @BindView(R.id.map_style_mode)
    ImageView mapStyleMode;
    @BindView(R.id.direction)
    ImageView direction;
    //    @BindView(R.id.action_kml)
//    FloatingActionButton actionKml;
//    @BindView(R.id.search_map_progress)
//    ProgressBar searchMapProgress;
//    private MiniDrawer miniDrawer;
//    private Crossfader crossFader;
    private SweetAlertDialog progressDialog;
    private LatLng mCenterLatLong;
    private BottomSheetBehavior bottomSheetBehavior;
    private Drawer drawer;
    private SupportMapFragment mapFragment;
    private GoogleApiClient mGoogleApiClient;
    private GoogleMap mMap;
    private long lastBackButtonPressed;
    private Set<Marker> markers;

    private int lastPage = 1;

    private Logger log = LoggerFactory.getLogger(MainActivity.class);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        toolbarTitle.setText(getString(R.string.title_bar));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);

        markers = new HashSet<>();

        mMap = null;
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MainActivity.this);

        buildGoogleApiClient();

        profileDrawerItem = new ProfileDrawerItem()
                .withName(MyApplication.sp.getUser().fullname)
                .withEmail(String.format("%s %s", getString(R.string.edition_label), Utilities.getVersionName(MainActivity.this)))
                .withIcon(R.drawable.profile)
                .withIdentifier(11)
                .withTag("profile");
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header_background)
                .withTranslucentStatusBar(true)
                .withSelectionListEnabled(false)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        return false;
                    }
                })
                .addProfiles(
                        profileDrawerItem
                )
                .withSavedInstance(savedInstanceState)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withFullscreen(true)
                .withSelectedItem(-1)
                .withAccountHeader(headerResult) //set the AccountHeader we created earlier for the header
                .addDrawerItems(
                        new PrimaryDrawerItem().withName(getString(R.string.messages)).withIcon(getResources().getDrawable(R.drawable.ic_messages)).withIdentifier(1).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.history)).withIcon(getResources().getDrawable(R.drawable.ic_history)).withIdentifier(2).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.report)).withIcon(getResources().getDrawable(R.drawable.ic_report)).withIdentifier(3).withSelectable(false),
                        new DividerDrawerItem(),
                        new PrimaryDrawerItem().withName(getString(R.string.send_information)).withIcon(getResources().getDrawable(R.drawable.ic_upload)).withIdentifier(4).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.receive_information)).withIcon(getResources().getDrawable(R.drawable.ic_download)).withIdentifier(5).withSelectable(false),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName(R.string.kml_files).withIcon(getResources().getDrawable(R.drawable.ic_kml)).withIdentifier(6).withSelectable(false),
                        new PrimaryDrawerItem().withName(getString(R.string.about_us)).withIcon(R.mipmap.ic_launcher).withIdentifier(7).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.support).withIcon(getResources().getDrawable(R.drawable.ic_support)).withIdentifier(8).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.settings).withIcon(getResources().getDrawable(R.drawable.ic_settings)).withIdentifier(9).withSelectable(false),
                        new SecondaryDrawerItem().withName(R.string.logout).withIcon(getResources().getDrawable(R.drawable.ic_logout)).withIdentifier(10).withSelectable(false)
                ) // add the items we want to use with our Drawer
                .withOnDrawerItemClickListener(this)
//                .withGenerateMiniDrawer(true)
//                // build only the view of the Drawer (don't inflate it automatically in our layout which is done with .build())
//                .buildView();
                .withSavedInstance(savedInstanceState)
                .build();


//        //the MiniDrawer is managed by the Drawer and we just get it to hook it into the Crossfader
//        miniDrawer = drawer.getMiniDrawer()
//                .withOnMiniDrawerItemOnClickListener(this);
//
//        //get the widths in px for the first and second panel
//        int firstWidth = (int) UIUtils.convertDpToPixel(300, this);
//        int secondWidth = (int) UIUtils.convertDpToPixel(72, this);
//
//        //create and build our crossfader (see the MiniDrawer is also builded in here, as the build method returns the view to be used in the crossfader)
//        //the crossfader library can be found here: https://github.com/mikepenz/Crossfader
//        crossFader = new Crossfader()
//                .withContent(crossfadeContent)
//                .withGmailStyleSwiping()
//                .withFirst(drawer.getSlider(), firstWidth)
//                .withSecond(miniDrawer.build(this), secondWidth)
//                .withSavedInstance(savedInstanceState)
//                .build();
//
//        //define the crossfader to be used with the miniDrawer. This is required to be able to automatically toggle open / close
//        miniDrawer.withCrossFader(new CrossfadeWrapper(crossFader));
//
//        //define a shadow (this is only for normal LTR layouts if you have a RTL app you need to define the other one
//        crossFader.getCrossFadeSlidingPaneLayout().setShadowResourceRight(R.drawable.material_drawer_shadow_right);

        actionSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, SearchActivity.class), DOCUMENT_REQUEST_CODE);
            }
        });
        actionHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, HistoryActivity.class), DOCUMENT_REQUEST_CODE);
            }
        });
//        actionKml.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(MainActivity.this, KmlFilesActivity.class));
//            }
//        });
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                startActivity(new Intent(MainActivity.this, FormsActivity.class));
            }
        });

        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));

        bottomSheetBehavior = BottomSheetBehavior.from(mapBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

//        filter.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mapPinFilter();
//            }
//        });
//        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//                if (BottomSheetBehavior.STATE_DRAGGING == newState) {
//                    addNew.animate().scaleX(0).scaleY(0).setDuration(300).start();
//                } else if (BottomSheetBehavior.STATE_COLLAPSED == newState) {
//                    addNew.animate().scaleX(1).scaleY(1).setDuration(300).start();
//                }
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//            }
//        });

//        carsh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Utilities.showOfflineMode(MainActivity.this, !Utilities.isAvailableNetwork(MainActivity.this));
        loadKmlLayers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DOCUMENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                changeMap(new LatLng(data.getDoubleExtra("latitude", 35.754969), data.getDoubleExtra("longitude", 51.420301)));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationRequest mLocationRequest = new LocationRequest()
                .setInterval(10000)
                .setFastestInterval(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, mLocationRequest, MainActivity.this);


//        gotoMyLocation();
        checkDocumentExtra();
    }


    public void checkDocumentExtra() {
        Intent intent = getIntent();
        if (intent != null && intent.getLongExtra("document_id", -1L) != -1) {
            Intent sheetIntent = new Intent(MainActivity.this, SheetActivity.class);
            sheetIntent.putExtra("document_id", intent.getLongExtra("document_id", -1L));
            sheetIntent.putExtra("form_id", intent.getLongExtra("form_id", -1L));
            startActivity(sheetIntent);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        this.mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (this.mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, MainActivity.this);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toasty.error(MainActivity.this, getString(R.string.connection_disconnect), Toast.LENGTH_SHORT, true).show();
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addConnectionCallbacks(MainActivity.this)
                .addOnConnectionFailedListener(MainActivity.this)
                .addApi(LocationServices.API)
                .build();
    }

    private void changeMap(Location location) {
        this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(17.0f).build()));
    }

    private void changeMap(LatLng location) {
        this.mMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(location.latitude, location.longitude)).zoom(17.0f).build()));
    }

    private void loadKmlLayers() {
        List<KmlFile> kmlFiles = MyApplication.database.kmlLayerDAO().getAll();
        for (KmlFile kmlFile : kmlFiles) {
            if (mMap != null && kmlFile.getUrl() != null && kmlFile.isVisibility()) {
                try {
                    KmlLayer kmlLayer = new KmlLayer(mMap, new FileInputStream(kmlFile.getUrl()), MainActivity.this);
                    kmlLayer.addLayerToMap();
                } catch (IOException | XmlPullParserException e) {
                    Crashlytics.logException(e);
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    public void getNearestMapPin(LatLng location) {
//        searchMapProgress.setVisibility(View.VISIBLE);

        int radius = MyApplication.sp.getMapLoadRadius();
        LatLng p1 = Utilities.calculateDerivedPosition(location, 1.1 * radius, 0);
        LatLng p2 = Utilities.calculateDerivedPosition(location, 1.1 * radius, 90);
        LatLng p3 = Utilities.calculateDerivedPosition(location, 1.1 * radius, 180);
        LatLng p4 = Utilities.calculateDerivedPosition(location, 1.1 * radius, 270);

        List<Form> mapPinFilterForms = new ArrayList<>();

        Form form = MyApplication.database.formDAO().get(MyApplication.sp.getFilterFormId());
        if (form != null) {
            mapPinFilterForms.clear();
            mapPinFilterForms.addAll(MyApplication.database.formDAO().getAll(form.getId()));
        }

        List<Document> documents = new ArrayList<>();
        if (mapPinFilterForms.isEmpty()) {
            documents.addAll(MyApplication.database.documentDAO().getNearest(p3.latitude, p1.latitude, p4.longitude, p2.longitude));
        } else {
            for (Form form1 : mapPinFilterForms) {
                documents.addAll(MyApplication.database.documentDAO().getNearestWithFormId(form1.getId(), p3.latitude, p1.latitude, p4.longitude, p2.longitude));
            }
        }
        if (!markers.isEmpty()) {
            for (Marker marker : markers) {
                marker.remove();
            }
        }
        for (Document document : documents) {
            setMapPin(new LatLng(document.getLatitude(), document.getLongitude()), document);
        }
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                searchMapProgress.setVisibility(View.INVISIBLE);
//            }
//        }, 1000);
    }

    private void setMapPin(LatLng latLng, Document document) {


        int icon_id = R.drawable.ic_map_new_pin;
        if (draft.getValue() == document.getStatus()) {
            icon_id = R.drawable.ic_map_new_pin_draft;
        } else if (accept.getValue() == document.getStatus()) {
            icon_id = R.drawable.ic_map_new_pin_accept;
        } else if (pending.getValue() == document.getStatus()) {
            icon_id = R.drawable.ic_map_new_pin_pendding;
        } else if (reject.getValue() == document.getStatus()) {
            icon_id = R.drawable.ic_map_new_pin_reject;
        }

        Bitmap bitmap = Utilities.getMarkerIconFromDrawable(MainActivity.this, getResources().getDrawable(icon_id), 40, 40);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Open Sans Bold.ttf"));
        Form form = MyApplication.database.formDAO().get(document.getForm_id());
        String mapPinText = form.getMap_pin_symbol();
        if (mapPinText != null) {
            if (mapPinText.length() == 1) {
                paint.setTextSize(Utilities.convertDpToPixel(15, MainActivity.this));
                canvas.drawText(mapPinText, Utilities.convertDpToPixel(13, MainActivity.this), Utilities.convertDpToPixel(21, MainActivity.this), paint);
            } else if (mapPinText.length() == 2) {
                paint.setTextSize(Utilities.convertDpToPixel(14, MainActivity.this));
                canvas.drawText(mapPinText, Utilities.convertDpToPixel(9, MainActivity.this), Utilities.convertDpToPixel(20, MainActivity.this), paint);
            } else if (mapPinText.length() == 3) {
                paint.setTextSize(Utilities.convertDpToPixel(11, MainActivity.this));
                canvas.drawText(mapPinText, Utilities.convertDpToPixel(8, MainActivity.this), Utilities.convertDpToPixel(20, MainActivity.this), paint);
            }
        }

        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        marker.setTag(new Point(document.getId(), document.getForm_id()));
        marker.setTitle(form.getName_fa());
        markers.add(marker);
    }

//    private void mapPinFilter() {
//
//        final Dialog dialog = new Dialog(MainActivity.this, R.style.DialogTheme);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        dialog.setContentView(R.layout.dialog_map_pin_filter);
//
//        List<String> formNames = new ArrayList<>();
//        final List<Form> forms = MyApplication.database.formDAO().getAllSubForms();
//        formNames.add("همه");
//        for (Form form : forms) {
//            formNames.add(MyApplication.database.formDAO().get(form.getParent_id()).getName_fa() + " > " + form.getName_fa());
//        }
//
//        final MaterialBetterSpinner formType = dialog.findViewById(R.id.form_type);
//        if (!mapPinFilterFormIds.isEmpty()) {
//            Form form=MyApplication.database.formDAO().get(mapPinFilterFormIds.get(0));
//            formType.setText(MyApplication.database.formDAO().get(form.getParent_id()).getName_fa() + " > " + form.getName_fa());
//
//        }
//
//        ArrayAdapter adapterFormType = new ArrayAdapter<>(this, R.layout.adapter_row_spinner, formNames.toArray(new String[formNames.size()]));
//        formType.setAdapter(adapterFormType);
//        formType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                mapPinFilterFormIds.clear();
//                if (position != 0) {
//                    mapPinFilterFormIds.add(forms.get(position - 1).getId());
//                }
//            }
//        });
//
//        dialog.show();
//    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mMap = googleMap;
        double lat = getIntent().getDoubleExtra("lat", 35.754969);
        double lon = getIntent().getDoubleExtra("lon", 51.420301);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(new LatLng(lat, lon)).zoom(15.0f).build()));

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        ((ImageView) mapFragment.getView().findViewById(Integer.parseInt("2"))).setImageDrawable(null);
        myLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (Utilities.isLocationAvailable(MainActivity.this)) {
                    gotoMyLocation();
                } else {
                    progressDialog.setTitleText(getString(R.string.error))
                            .setContentText(getString(R.string.no_gps_message))
                            .setConfirmText(getString(R.string.settings))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    progressDialog.dismiss();
                                    Intent intent = new Intent();
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            });
                    progressDialog.setCancelText(getString(R.string.retry));
                    progressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            progressDialog.dismiss();
                        }
                    });
                    progressDialog.setCancelable(false);
                    progressDialog.changeAlertType(SweetAlertDialog.ERROR_TYPE);
                    progressDialog.show();
                }
            }
        });
        mapStyleMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.setMapType(mMap.getMapType() == GoogleMap.MAP_TYPE_SATELLITE ? GoogleMap.MAP_TYPE_NORMAL : GoogleMap.MAP_TYPE_SATELLITE);
            }
        });
        direction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= 23 &&
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }

                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (location != null) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://maps.google.com/maps?saddr=" + location.getLatitude() + "," + location.getLongitude() + "&daddr=" + mCenterLatLong.latitude + "," + mCenterLatLong.longitude));
                    startActivity(intent);
                }

            }
        });
        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                mCenterLatLong = cameraPosition.target;
                getNearestMapPin(mCenterLatLong);
                if (direction.getVisibility() != View.INVISIBLE) {
                    direction.setVisibility(View.INVISIBLE);
                }
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    Point args = (Point) marker.getTag();
                    if (args != null) {
                        final long document_id = (long) args.x;
                        final long form_id = (long) args.y;
                        final Document document = MyApplication.database.documentDAO().get(document_id);
                        final List<DocumentSeries> documentSeries = MyApplication.database.documentSeriesDAO().getAll(document_id);
                        final Map<Long, PropertyValues> documentSeriesValues = documentSeries.get(documentSeries.size() - 1).getMapValues();
                        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                                    formName.setText(MyApplication.database.formDAO().get(document.getForm_id()).getName_fa());
                                    infoLabel.setText(getString(R.string.owner_code_label));
                                    if (documentSeriesValues.containsKey(21L)) {
                                        infoLabel.setText(getString(R.string.village_name_label));
                                        infoValue.setText(documentSeriesValues.get(21L).getVal());
                                    } else if (documentSeriesValues.containsKey(1L)) {
                                        infoValue.setText(documentSeriesValues.get(1L).getVal());
                                    }
                                    if (document.getLast_seen_date() != 0) {
                                        Calendar cal = Calendar.getInstance();
                                        cal.setTimeInMillis(document.getLast_seen_date());
                                        lastSeenDate.setText(new JalaliCalendar(cal).toString());
                                    } else {
                                        lastSeenDate.setText("-");
                                    }
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                                    direction.setVisibility(View.VISIBLE);
                                }
                            }
                        }, 600);

                        details.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(MainActivity.this, SheetActivity.class);
                                intent.putExtra("document_id", document_id);
                                intent.putExtra("form_id", form_id);
                                startActivity(intent);
                                Document document = MyApplication.database.documentDAO().get(document_id);
                                document.setLast_seen_date(new Date().getTime());
                                MyApplication.database.documentDAO().update(document);
                            }
                        });
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
                return false;
            }
        });
        loadKmlLayers();

        if (MyApplication.sp.getLastFormsUpdatedTime() == 0L) {
            downloadResourceFromServer(true);
        }
    }

    private void gotoMyLocation() {

        if (Build.VERSION.SDK_INT >= 23 &&
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);

        if (location != null) {
            changeMap(location);
        }
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
//        if (crossFader != null && crossFader.isCrossFaded()) {
        if (drawer != null && drawer.isDrawerOpen()) {
//            crossFader.crossFade();
            drawer.closeDrawer();
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        } else {
            if (lastBackButtonPressed + 2000 > System.currentTimeMillis()) {
                Utilities.finishApplication(MainActivity.this);
            } else {
                Toasty.info(MainActivity.this, getString(R.string.exit_message), Toast.LENGTH_LONG, true).show();
            }
            lastBackButtonPressed = System.currentTimeMillis();
        }
    }

    //on base drawer listener
    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        OnNavigationDrawerItemClick(view, drawerItem, position);
//        if (crossFader != null && crossFader.isCrossFaded()) {
//            crossFader.crossFade();
//        }
        return false;
    }

    //on mini drawer listener
    @Override
    public boolean onClick(@NonNull View view, @NonNull IAdapter<IDrawerItem> adapter, @NonNull IDrawerItem drawerItem, int position) {
        OnNavigationDrawerItemClick(view, drawerItem, position);
        return false;
    }

    public void OnNavigationDrawerItemClick(View view, IDrawerItem drawerItem, int position) {
        switch ((int) drawerItem.getIdentifier()) {
            case 11: {
            }
            case 0: {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                break;
            }
            case 1: {
                startActivityForResult(new Intent(MainActivity.this, MessagesActivity.class), DOCUMENT_REQUEST_CODE);
                break;
            }
            case 2: {
                startActivityForResult(new Intent(MainActivity.this, HistoryActivity.class), DOCUMENT_REQUEST_CODE);
                break;
            }
            case 3: {
                startActivity(new Intent(MainActivity.this, ReportActivity.class));
                break;
            }
            case 4: {
                startActivity(new Intent(MainActivity.this, UploadDocumentsActivity.class));
                break;
            }
            case 5: {
                downloadResourceFromServer(false);
                break;
            }
            case 6: {
                startActivity(new Intent(MainActivity.this, KmlFilesActivity.class));
                break;
            }
            case 7: {
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            }
            case 8: {
                Intent in = new Intent(Intent.ACTION_DIAL);
                in.setData(Uri.parse(getString(R.string.support_tel)));
                startActivity(in);
                break;
            }
            case 9: {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            }
            case 10: {
                progressDialog.changeAlertType(SweetAlertDialog.WARNING_TYPE);
                progressDialog.setTitleText(getString(R.string.warning));
                progressDialog.setContentText(getString(R.string.logout_message));
                progressDialog.setCancelable(false);
                progressDialog.setConfirmText(getString(R.string.yes));
                progressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();

                        //remove app files
                        String[] profile_photo_files = MyApplication.directory.list();
                        for (String file : profile_photo_files) {
                            new File(MyApplication.directory, file).delete();
                        }

                        String serverAddress = MyApplication.sp.getServerAddress();
                        MyApplication.sp.clearAllCache();
                        MyApplication.sp.setServerAddress(serverAddress);

                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                });
                progressDialog.setCancelText(getString(R.string.no));
                progressDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();
                    }
                });
                progressDialog.show();
                break;
            }

        }
    }

    public void downloadResourceFromServer(final boolean isFirst) {

        // check internet is available
        if (!checkInternet()) {
            return;
        }

        progressDialog.setTitleText(getString(R.string.loading))
                .showContentText(false)
                .showCancelButton(false)
                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                getFormsUpdate();

                int currentPage = 1;
                long start;
                do {
                    start = System.currentTimeMillis();
                    getDocumentsUpdate(isFirst ? 0 : MyApplication.sp.getLastDocumentsUpdatedTime(), currentPage++);
                    long delay = System.currentTimeMillis() - start;
                    if (delay < 1000) {
                        try {
                            Thread.sleep(1000 - delay);
                        } catch (InterruptedException e) {
                            Crashlytics.logException(e);
                            log.error(e.getMessage(), e);
                            e.printStackTrace();
                        }
                    }
                } while (currentPage <= lastPage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (progressDialog.isShowing() && progressDialog.getAlerType() == SweetAlertDialog.PROGRESS_TYPE) {
                            showDialog(getString(R.string.alert), getString(R.string.download_information_success), SweetAlertDialog.SUCCESS_TYPE);

                        }
                    }
                });
            }
        }).start();
    }

    public void getFormsUpdate() {

        try {
            Call<ResponseBase<FormsUpdate>> call = APIBaseCreator.getAPIAdapter("server").getFormsUpdate("Bearer " + MyApplication.sp.getAuthToken(), MyApplication.sp.getLastFormsUpdatedTime());
            Response<ResponseBase<FormsUpdate>> response = call.execute();
            if (response.code() == 200) {

                ResponseBase<FormsUpdate> res = response.body();

                if (res.getStatusCode() == 0) {


                    FormsUpdate data = res.getData();

                    for (Form form : data.getForms()) {
                        if (MyApplication.database.formDAO().get(form.getId()) == null) {
                            MyApplication.database.formDAO().add(form);
                        } else {
                            MyApplication.database.formDAO().update(form);
                        }
                    }
                    for (Sheet sheet : data.getSheets()) {
                        if (MyApplication.database.sheetDAO().get(sheet.getId()) == null) {
                            MyApplication.database.sheetDAO().add(sheet);
                        } else {
                            MyApplication.database.sheetDAO().update(sheet);
                        }
                    }
                    for (FormSheet formSheet : data.getFormsSheets()) {
                        if (MyApplication.database.formSheetDAO().getById(formSheet.getId()) == null) {
                            MyApplication.database.formSheetDAO().add(formSheet);
                        } else {
                            MyApplication.database.formSheetDAO().update(formSheet);
                        }
                    }
                    for (Property property : data.getProperties()) {
                        if (MyApplication.database.propertyDAO().get(property.getId()) == null) {
                            MyApplication.database.propertyDAO().add(property);
                        } else {
                            MyApplication.database.propertyDAO().update(property);
                        }
                    }
                    for (FormSheetProperty formSheetProperty : data.getFormsSheetsProperties()) {
                        if (MyApplication.database.formSheetPropertyDAO().getById(formSheetProperty.getId()) == null) {
                            MyApplication.database.formSheetPropertyDAO().add(formSheetProperty);
                        } else {
                            MyApplication.database.formSheetPropertyDAO().update(formSheetProperty);
                        }
                    }

                    MyApplication.sp.setLastFormsUpdatedTime(System.currentTimeMillis());

                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            showDialog(getString(R.string.error), getString(R.string.download_information_fail), SweetAlertDialog.ERROR_TYPE);
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }

    }

    public void getDocumentsUpdate(long setLastDocumentsUpdatedTime, int page) {

        try {
            Call<ResponseBase<Pagination<Document>>> call = APIBaseCreator.getAPIAdapter("server").getDocumentsUpdate("Bearer " + MyApplication.sp.getAuthToken(), setLastDocumentsUpdatedTime, page);
            Response<ResponseBase<Pagination<Document>>> response = call.execute();
            if (response.code() == 200) {

                ResponseBase<Pagination<Document>> res = response.body();

                if (res.getStatusCode() == 0) {

                    lastPage = res.getData().getLastPage();

                    for (Document document : res.getData().getData()) {

                        Document oldDocument = MyApplication.database.documentDAO().getByGlobalId(document.getGlobal_id());
                        if (oldDocument == null) {
                            document.setId(0L);
                            long document_id = MyApplication.database.documentDAO().add(document);
                            document.setId(document_id);
                        } else {
                            document.setId(oldDocument.getId());
                            MyApplication.database.documentDAO().update(document);
                        }

                        for (DocumentSeries documentSeries : document.getDocument_series()) {
                            Map<Long, PropertyValues> valuesMap = new HashMap<>();
                            for (PropertyValues propertyValues : documentSeries.getValues()) {
                                valuesMap.put(propertyValues.getPid(), propertyValues);
                            }
                            documentSeries.setDate(0L);
                            documentSeries.setUpload(true);
                            documentSeries.setForm_id(document.getForm_id());
                            documentSeries.setDocument_id(document.getId());
                            documentSeries.setMapValues(valuesMap);
                            log.info(TAG + "getDocumentsUpdate: " + new Gson().toJson(documentSeries));
                            DocumentSeries oldDocumentSeries = MyApplication.database.documentSeriesDAO().get(document.getId(), documentSeries.getPosition());
                            if (oldDocumentSeries == null) {
                                MyApplication.database.documentSeriesDAO().add(documentSeries);
                            } else {
                                documentSeries.setId(oldDocumentSeries.getId());
                                MyApplication.database.documentSeriesDAO().update(documentSeries);
                            }
                        }
                    }

                    if (page == lastPage) {
                        MyApplication.sp.setLastDocumentsUpdatedTime(System.currentTimeMillis());
                    }

                }else{
                    lastPage=1;
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            showDialog(getString(R.string.error), getString(R.string.download_information_fail), SweetAlertDialog.ERROR_TYPE);
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public void showDialog(final String title, final String message, final int alertType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                progressDialog.setTitleText(title);
                progressDialog.setContentText(message);
                progressDialog.changeAlertType(alertType);

                progressDialog.setConfirmText(getString(R.string.ok));
                progressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    public boolean checkInternet() {
        if (Utilities.isAvailableNetwork(MainActivity.this)) {
            return true;
        } else {
            progressDialog.setTitleText(getString(R.string.error))
                    .showContentText(true)
                    .setContentText(getString(R.string.no_internet_message))
                    .setConfirmText(getString(R.string.settings))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setCancelText(getString(R.string.retry))
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            progressDialog.dismiss();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            progressDialog.show();
            return false;
        }
    }
}
