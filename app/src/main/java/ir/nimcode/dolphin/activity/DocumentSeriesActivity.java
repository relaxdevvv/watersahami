package ir.nimcode.dolphin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.github.clans.fab.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.adapter.DocumentSeriesAdapter;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.OnItemClickListener;
import ir.nimcode.dolphin.util.Utilities;

public class DocumentSeriesActivity extends FullAppCompatActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.add_new)
    FloatingActionButton addNew;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_series);
        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.document_series)));
        Intent intent = getIntent();
        final long form_id = intent.getLongExtra("form_id", -1);
        final long document_id = intent.getLongExtra("document_id", -1);
        DocumentSeriesAdapter documentSeriesAdapter = new DocumentSeriesAdapter(DocumentSeriesActivity.this, SheetActivity.documentDocumentSeries, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(DocumentSeriesActivity.this, SheetActivity.class);
                intent.putExtra("document_series_position", position);
                intent.putExtra("form_id", form_id);
                intent.putExtra("document_id", document_id);
                Log.d("TEST", "onCreate: " + form_id + " " + document_id + " document_series_position " + position);
                startActivity(intent);
                finish();
            }
        });


        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(DocumentSeriesActivity.this));
        recyclerView.setAdapter(documentSeriesAdapter);

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DocumentSeriesActivity.this, SheetActivity.class);
                intent.putExtra("new_document_series", true);
                Log.d("TEST", "onCreate: " + form_id + " " + document_id + " new_document_series " + true);
                intent.putExtra("form_id", form_id);
                intent.putExtra("document_id", document_id);
                startActivity(intent);
                finish();
            }
        });
    }

}
