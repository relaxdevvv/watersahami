package ir.nimcode.dolphin.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import java.io.File;
import java.util.List;
import java.util.Locale;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.model.ImagePropertyValues;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.Sheet;
import ir.nimcode.dolphin.util.OnItemClickListener;

/**
 * Created by saeed on 2/22/18.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {


    private Context context;
    private List<Property> list;
    private int sheetPosition;
    private OnItemClickListener onItemClickListener;

    public ImageAdapter(Context context, List<Property> list, int sheetPosition, OnItemClickListener onItemClickListener) {
        this.context = context;
        this.list = list;
        this.sheetPosition = sheetPosition;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ImageAdapter.ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageAdapter.ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_image, parent, false));

    }

    @Override
    public void onBindViewHolder(final ImageAdapter.ImageViewHolder holder, final int position) {

        final Property property = list.get(position);
        final Sheet sheet = SheetActivity.sheets.get(sheetPosition - 1);

        holder.label.setText(property.getName_fa());

        if (property.getValue() != null) {
            try {
                ImagePropertyValues imagePropertyValues = new Gson().fromJson(property.getValue(), ImagePropertyValues.class);
                File imgFile = new File(imagePropertyValues.localLink);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    holder.image.setImageBitmap(myBitmap);
                    holder.image.setVisibility(View.VISIBLE);

                }
                holder.location.setText(String.format(new Locale("en"), "%f , %f", imagePropertyValues.longitude, imagePropertyValues.latitude));
                holder.locationLayout.setVisibility(View.VISIBLE);
                holder.distance.setText(String.format(new Locale("en"), "%d %s", imagePropertyValues.distanceFromSource, "متر"));
                holder.distanceLayout.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            holder.image.setImageBitmap(null);
            holder.image.setVisibility(View.GONE);
        }

        if (property.isEnabled()) {
            holder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, position);
                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {

        private TextView label;
        private TextView location;
        private LinearLayout locationLayout;
        private TextView distance;
        private LinearLayout distanceLayout;
        private ImageView image;
        private CardView rootView;

        public ImageViewHolder(View itemView) {
            super(itemView);

            this.label = itemView.findViewById(R.id.label);
            this.location = itemView.findViewById(R.id.location);
            this.locationLayout = itemView.findViewById(R.id.location_layout);
            this.distance = itemView.findViewById(R.id.distance);
            this.distanceLayout = itemView.findViewById(R.id.distance_layout);
            this.image = itemView.findViewById(R.id.image);
            this.rootView = itemView.findViewById(R.id.root_view);
        }
    }
}
