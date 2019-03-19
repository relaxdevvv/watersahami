package ir.nimcode.dolphin.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.adapter.SheetAdapter;
import ir.nimcode.dolphin.model.Sheet;

/**
 * Created by saeed on 2/19/18.
 */

public class SheetFragment extends Fragment {

    public final static String TAG = "TAG_SheetFragment";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    //    @BindView(R.id.send_document)
//    AutoFitButton sendDocument;
//    @BindView(R.id.document_series)
//    AutoFitButton documentSeries;
    private Context context;
    private SheetAdapter sheetAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sheet, container, false);
        rootView.setRotationY(180);

        ButterKnife.bind(this, rootView);

        final Bundle bundle = getArguments();

        context = getActivity();
        List<Sheet> sheets = SheetActivity.sheets;
        sheetAdapter = new SheetAdapter(context, sheets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(sheetAdapter);
        sheetAdapter.notifyDataSetChanged();
        if (sheets.isEmpty()) {
            notingFoundErrorLayout.setVisibility(View.VISIBLE);
        }
//        setDocumentSeriesButtonEnabled(!bundle.getBoolean("new_document", false));
//        documentSeries.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, DocumentSeriesActivity.class);
//                intent.putExtra("form_id", bundle.getLong("form_id"));
//                intent.putExtra("document_id", bundle.getLong("document_id"));
//                getActivity().startActivity(intent);
//                getActivity().finish();
//            }
//        });
//
//        sendDocument.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((SheetActivity) getActivity()).save(true);
//            }
//        });

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
                sheetAdapter.notifyDataSetChanged();
            }
        }
    }

//    public  void setDocumentSeriesButtonEnabled(boolean enabled) {
//        documentSeries.setEnabled(enabled);
//        if (enabled) {
//            documentSeries.setText(getString(R.string.document_series));
//            documentSeries.setBackground(getResources().getDrawable(R.drawable.selector_accent_rounded_10dp));
//        } else {
//            documentSeries.setText(R.string.new_document_series);
//            documentSeries.setBackground(getResources().getDrawable(R.drawable.selector_disabled_rounded_10dp));
//        }
//    }
}