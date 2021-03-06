package notfastjustfurious.epam.smartparking.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

import notfastjustfurious.epam.smartparking.R;
import notfastjustfurious.epam.smartparking.adapter.OpenParkingSlotListAdapter;
import notfastjustfurious.epam.smartparking.entity.OpenParkingSlot;
import notfastjustfurious.epam.smartparking.webio.URLHelper;
import notfastjustfurious.epam.smartparking.webio.JSONHelper;

public class NormalUserHome extends AppCompatActivity {

    private ListView openSlotListView;
    private ProgressDialog progress;
    private SwipeRefreshLayout pullToRefresh;
    private TextView avergaeFillTimeTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_user_home);
        pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateOpenSlotListView(); // your code
                pullToRefresh.setRefreshing(false);
            }
        });
        openSlotListView = findViewById(R.id.openSlotListView);
        avergaeFillTimeTV = findViewById(R.id.averageFillTimeTextView);
        progress= new ProgressDialog(this);
        progress.setTitle("Fetching Open Slots");
        progress.setMessage("Wait while fetching the data...");
        progress.setCancelable(false); // disable dismiss by tapping outside of the dialog

        connectToVacationAPI();

        populateOpenSlotListView();


    }

    private void connectToVacationAPI() {
        progress.show();
        StringRequest request = new StringRequest(Request.Method.POST, URLHelper.VACATION_API, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                if(string.equals("Success")) {
                    Log.d("VacationAPI", "Connected to Vacations API");
                }
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(NormalUserHome.this, "Some error occurred!!", Toast.LENGTH_SHORT).show();
                Log.d("JSON Error", "Error occured while JSON Request");
                Log.e("Volley Error", volleyError.getMessage());
                progress.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(NormalUserHome.this);
        rQueue.add(request);
    }

    private void displayAverageFillTime() {
        StringRequest request = new StringRequest(Request.Method.POST, URLHelper.GET_SLOT_FILL_TIME, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                avergaeFillTimeTV.setText("Predicted time to fill the parking area: "+ string);
                avergaeFillTimeTV.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(NormalUserHome.this, "Some error occurred!!", Toast.LENGTH_SHORT).show();
                Log.d("JSON Error", "Error occured while JSON Request");
                Log.e("Volley Error", volleyError.getMessage());
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(NormalUserHome.this);
        rQueue.add(request);
    }

    private void populateOpenSlotListView() {
        progress.show();
        displayAverageFillTime();
        StringRequest request = new StringRequest(Request.Method.POST, URLHelper.GET_AVAILABLE_SLOT_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String string) {
                Log.d("JSON Success", string);
                try {
                    JSONArray jsonArray = new JSONArray(string);
                    if(string.equals("[]") || jsonArray == null) {
                        findViewById(R.id.noSlotAvailableTextView).setVisibility(View.VISIBLE);
                        progress.dismiss();
                        return;
                    }
                    List<OpenParkingSlot> openParkingSlotList = JSONHelper.getOpenParkingSlots(jsonArray);
                    if (openParkingSlotList != null) {
                        openSlotListView.setAdapter(new OpenParkingSlotListAdapter(NormalUserHome.this, openParkingSlotList));
                        setListViewHeightBasedOnChildren(openSlotListView);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Toast.makeText(NormalUserHome.this, "Some error occurred!!", Toast.LENGTH_SHORT).show();
                Log.d("JSON Error", "Error occured while JSON Request");
                Log.e("Volley Error", volleyError.getMessage());
                progress.dismiss();
            }
        });

        RequestQueue rQueue = Volley.newRequestQueue(NormalUserHome.this);
        rQueue.add(request);

    }



    public void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().toString().equalsIgnoreCase("logout")) {
            //TODO Logout
            SharedPreferences sharedPreferences = getSharedPreferences("loginInfo", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("loginStatus", false + "");
            editor.commit();
            startActivity(new Intent(NormalUserHome.this, Login.class));
        } else {
            Toast.makeText(this, "Under Development..!", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        finishAffinity();
    }
}
