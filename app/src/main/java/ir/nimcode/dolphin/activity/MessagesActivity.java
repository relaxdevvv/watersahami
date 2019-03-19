package ir.nimcode.dolphin.activity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.ldoublem.loadingviewlib.view.LVNews;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;

public class MessagesActivity extends FullAppCompatActivity {


    private static final String TAG = "TAG_MessagesActivity";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.progress)
    LVNews progress;

//    private MessagesAdapter messagesAdapter;
//    private ArrayList<Message> messages;
@BindView(R.id.noting_found_error_layout)
LinearLayout notingFoundErrorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        ButterKnife.bind(this);
        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(MessagesActivity.this, getString(R.string.messages)));
//        retry.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                getMessages();
//            }
//        });
//
//        settings.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                // go to network setting
//                Intent intent = new Intent();
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS);
//                startActivity(intent);
//            }
//        });
//
//        recyclerView.setHasFixedSize(true);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(MessagesActivity.this);
//        recyclerView.setLayoutManager(layoutManager);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//
//        messages = new ArrayList<>();
//        messagesAdapter = new MessagesAdapter(MessagesActivity.this, messages);
//        recyclerView.setAdapter(messagesAdapter);
//
//        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
//            @Override
//            public void onRefresh() {
//                getMessages();
//            }
//        });
//
//        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
//
//        messagesProgress.setViewColor(getResources().getColor(R.color.md_grey_600));
//        messagesProgress.startAnim(1000);
//
//        getMessages();
    }

//    private void getMessages() {
//
//        if (Utilities.isAvailableNetwork(MessagesActivity.this)) {
//
//            messagesProgress.startAnim(1000);
//            messagesProgress.setVisibility(View.VISIBLE);
//
//            messages.clear();
//            messagesAdapter.notifyDataSetChanged();
//            notingFoundErrorLayout.setVisibility(View.GONE);
//            errorLayout.setVisibility(View.GONE);
//            swipeRefreshLayout.setRefreshing(false);
//
//            Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").customerMessages(MyApplication.sp.getString("auth_token", null));
//            call.enqueue(new Callback<ResponseBody>() {
//
//                @Override
//                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull ResponseSingle<ResponseBody> response) {
//
//                    try {
//                        if (response.code() == 200) {
//                            String json = response.body().string();
//                            JSONObject responseData = new JSONObject(json);
//                            int status_code = responseData.getInt("status_code");
//                            if (status_code == 0) {
//                                Log.d(TAG, "getRequestsOfCustomerSuccess: ");
//
//                                JSONArray data = responseData.getJSONArray("data");
//                                for (int i = 0; i < data.length(); i++) {
//                                    JSONObject messageData = data.getJSONObject(i);
//                                    Message message = new Message();
//                                    message.setId(messageData.getInt("id"));
//                                    message.setMessage(messageData.getString("message"));
//                                    message.setVisited(messageData.getBoolean("visited"));
//                                    Calendar createdAt = Calendar.getInstance();
//                                    createdAt.setTimeInMillis(messageData.getLong("created_at"));
//                                    message.setCreatedTime(createdAt);
//
//                                    JSONObject requestJsonObject = messageData.getJSONObject("request");
//                                    Request request = new Request();
//                                    request.setId(requestJsonObject.getInt("id"));
//                                    request.setSource(new Location(requestJsonObject.getString("source_address"), requestJsonObject.getString("source_latLng")));
//                                    request.setDestination(new Location(requestJsonObject.getString("destination_address"), requestJsonObject.getString("destination_latLng")));
//                                    request.setOfferedPrice(requestJsonObject.getInt("offered_price"));
//                                    request.setVehicleType(new Pair(0, requestJsonObject.getString("vehicle_type")));
//                                    Calendar reservedTime = Calendar.getInstance();
//                                    reservedTime.setTimeInMillis(requestJsonObject.getLong("reserved_time"));
//                                    request.setReservedTime(reservedTime);
//                                    Calendar expiredTime = Calendar.getInstance();
//                                    expiredTime.setTimeInMillis(requestJsonObject.getLong("expired_time"));
//                                    request.setExpiredTime(expiredTime);
//
//                                    message.setRequest(request);
//
//                                    JSONObject suggestionJsonObject = messageData.getJSONObject("suggestion");
//                                    Suggestion suggestion = new Suggestion();
//                                    suggestion.setId(suggestionJsonObject.getInt("id"));
//                                    suggestion.setVehiclePhoto(suggestionJsonObject.getString("vehicle_photo"));
//                                    suggestion.setDriverPhoto(suggestionJsonObject.getString("driver_photo"));
//                                    suggestion.setDriverName(suggestionJsonObject.getString("driver_name"));
//                                    suggestion.setDriverScore((float) suggestionJsonObject.getDouble("driver_score"));
//                                    suggestion.setPrice(suggestionJsonObject.getInt("price"));
//                                    suggestion.setMessage(suggestionJsonObject.getString("message"));
//                                    Calendar suggestionCreatedAt = Calendar.getInstance();
//                                    suggestionCreatedAt.setTimeInMillis(suggestionJsonObject.getLong("created_at"));
//                                    suggestion.setCreatedTime(suggestionCreatedAt);
//
//                                    message.setSuggestion(suggestion);
//
//                                    messages.add(message);
//                                }
//                                new Handler().postDelayed(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        messagesProgress.stopAnim();
//                                        messagesProgress.setVisibility(View.INVISIBLE);
//                                        messagesAdapter.notifyDataSetChanged();
//                                        if (messages.isEmpty()) {
//                                            notingFoundErrorLayout.setVisibility(View.VISIBLE);
//                                        }
//                                    }
//                                }, 2000);
//
//
//                            }
//                        } else {
//                            if (BuildConfig.DEBUG) {
//                                String json = response.errorBody().string();
//                                Log.e(TAG, "getRequestsOfCustomerError");
//                                Intent intent = new Intent(MessagesActivity.this, ShowErrorActivity.class);
//                                intent.putExtra("errorBody", json);
//                                startActivity(intent);
//                            }
//                        }
//
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//
//                @Override
//                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
//                    Toasty.error(MessagesActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_LONG, true).show();
//                    messagesProgress.stopAnim();
//                    messagesProgress.setVisibility(View.GONE);
//                    errorLayout.setVisibility(View.VISIBLE);
//                }
//            });
//
//        } else {
//            errorLayout.setVisibility(View.VISIBLE);
//        }
//    }

}
