package ir.nimcode.dolphin.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.adapter.PropertyAdapter;
import ir.nimcode.dolphin.model.Property;

/**
 * Created by saeed on 2/19/18.
 */

public class PropertyFragment extends Fragment {

    public final static String TAG = "TAG_PropertyFragment";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    private PropertyAdapter propertyAdapter;
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_property, container, false);
        rootView.setRotationY(180);

        ButterKnife.bind(this, rootView);

        context = getActivity();

        Bundle bundle = getArguments();

        int position = FragmentPagerItem.getPosition(bundle);

        List<Property> properties = SheetActivity.sheetsProperties.get(position - 1);
        propertyAdapter = new PropertyAdapter(context, properties, position);
        propertyAdapter.setHasStableIds(true);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(propertyAdapter);
        propertyAdapter.notifyDataSetChanged();
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
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (getActivity() != null) {
                propertyAdapter.notifyDataSetChanged();
            }
        }
    }
}