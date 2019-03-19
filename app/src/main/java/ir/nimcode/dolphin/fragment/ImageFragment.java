package ir.nimcode.dolphin.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.adapter.ImageAdapter;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.ImagePropertyValues;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.Sheet;
import ir.nimcode.dolphin.util.OnItemClickListener;
import ir.nimcode.dolphin.util.Utilities;

import static android.app.Activity.RESULT_OK;

/**
 * Created by saeed on 2/20/18.
 */

public class ImageFragment extends Fragment implements OnItemClickListener {

    public static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1002;
    public final static String TAG = "TAG_PropertyFragment";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    private Context context;
    private List<Property> properties;
    private ImageAdapter imageAdapter;
    private int sheetPosition;
    private int propertyPosition;
    private File imageFile;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_property, container, false);
        rootView.setRotationY(180);

        ButterKnife.bind(this, rootView);

        context = getActivity();

        Bundle bundle = getArguments();

        sheetPosition = FragmentPagerItem.getPosition(bundle);

        properties = SheetActivity.sheetsProperties.get(sheetPosition - 1);
        imageAdapter = new ImageAdapter(context, properties, sheetPosition, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();
        if (properties.isEmpty()) {
            notingFoundErrorLayout.setVisibility(View.VISIBLE);
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {

            Property property = properties.get(propertyPosition);
            if (property.getValue() != null) {
                File imgFile = new File(property.getValue());
                if (imgFile.exists()) {
                    imgFile.delete();
                }
            }

            ImagePropertyValues imagePropertyValues = new ImagePropertyValues();
            imagePropertyValues.localLink = imageFile.getAbsolutePath();
            imagePropertyValues.latitude = LocationFragment.latLng != null ? LocationFragment.latLng.latitude : 0;
            imagePropertyValues.longitude = LocationFragment.latLng != null ? LocationFragment.latLng.longitude : 0;
            LatLng currentLocation = new LatLng(imagePropertyValues.latitude, imagePropertyValues.longitude);
            Property utmZone = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("utm_zone").getId());
            Property utmX = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("utm_x").getId());
            Property utmY = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("utm_y").getId());
            LatLng DocumentLocation;
            if (utmZone != null && utmZone.getValue() != null && !utmZone.getValue().isEmpty() &&
                    utmX != null && utmX.getValue() != null && !utmX.getValue().isEmpty() &&
                    utmY != null && utmY.getValue() != null && !utmY.getValue().isEmpty()) {
                DocumentLocation = Utilities.UTMToLatLng(new Utilities.UTM(Double.parseDouble(utmX.getValue()), Double.parseDouble(utmY.getValue()), Integer.parseInt(utmZone.getValue()), 'N'));
            } else if (SheetActivity.document != null) {
                DocumentLocation = new LatLng(
                        SheetActivity.document.getLatitude(),
                        SheetActivity.document.getLongitude());
            } else {
                DocumentLocation = currentLocation;
            }
            imagePropertyValues.distanceFromSource = (long) Utilities.getDistanceBetweenTwoPoints(DocumentLocation, currentLocation);

            property.setValue(new Gson().toJson(imagePropertyValues));
            if (!property.isChecked()) {
                Sheet sheet = SheetActivity.sheets.get(sheetPosition - 1);
                sheet.setProperties_checked_count(sheet.getProperties_checked_count() + 1);
            }
            properties.get(propertyPosition).setChecked(true);

            compressImage();

            imageAdapter.notifyItemChanged(propertyPosition);
        }
    }

    @Override
    public void onItemClick(View view, int position) {
        this.propertyPosition = position;
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (pictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            //Create a file to store the image
            File root = new File(Environment.getExternalStorageDirectory() + MyApplication.APP_DIR + "Images");
            if (!root.exists()) {
                root.mkdirs();
            }
            imageFile = new File(root, System.currentTimeMillis() + ".jpg");
            if (imageFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(), "ir.nimcode.dolphin.provider", imageFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(pictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        }
    }

    public void compressImage() {

        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inJustDecodeBounds = true;
                Bitmap bmp = BitmapFactory.decodeFile(imageFile.getPath(), options);

                int actualHeight = options.outHeight;
                int actualWidth = options.outWidth;

                float maxHeight = 2064.0f;
                float maxWidth = 1548.0f;
                float imgRatio = actualWidth / actualHeight;
                float maxRatio = maxWidth / maxHeight;

                if (actualHeight > maxHeight || actualWidth > maxWidth) {
                    if (imgRatio < maxRatio) {
                        imgRatio = maxHeight / actualHeight;
                        actualWidth = (int) (imgRatio * actualWidth);
                        actualHeight = (int) maxHeight;
                    } else if (imgRatio > maxRatio) {
                        imgRatio = maxWidth / actualWidth;
                        actualHeight = (int) (imgRatio * actualHeight);
                        actualWidth = (int) maxWidth;
                    } else {
                        actualHeight = (int) maxHeight;
                        actualWidth = (int) maxWidth;

                    }
                }
                options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
                options.inJustDecodeBounds = false;
                options.inPurgeable = true;
                options.inInputShareable = true;
                options.inTempStorage = new byte[16 * 1024];
                bmp = BitmapFactory.decodeFile(imageFile.getPath(), options);

                Bitmap scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);

                float ratioX = actualWidth / (float) options.outWidth;
                float ratioY = actualHeight / (float) options.outHeight;
                float middleX = actualWidth / 2.0f;
                float middleY = actualHeight / 2.0f;

                Matrix scaleMatrix = new Matrix();
                scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

                Canvas canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

                ExifInterface exif = new ExifInterface(imageFile.getPath());

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Log.d("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.d("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.d("EXIF", "Exif: " + orientation);
                }
                scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                        scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                        true);

                FileOutputStream out = new FileOutputStream(imageFile);
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.close();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() != null) {
                imageAdapter.notifyDataSetChanged();
            }
        }
    }
}