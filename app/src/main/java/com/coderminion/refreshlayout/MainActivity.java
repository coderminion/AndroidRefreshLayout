package com.coderminion.refreshlayout;


import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    public final String TAG = this.getClass().getSimpleName();

    ListView listView;
    ArrayList arrayList;
    ArrayAdapter arrayAdapter;
    SwipeRefreshLayout swipe_refresh_layout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        new GetJsonFromRemote().execute();
    }

    private void initView() {
        arrayList = new ArrayList();

        swipe_refresh_layout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        swipe_refresh_layout.setOnRefreshListener(this);

        listView = (ListView) findViewById(R.id.listview);
        arrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.list_item, R.id.textView, arrayList);
        listView.setAdapter(arrayAdapter);
    }

    @Override
    public void onRefresh() {
        new GetJsonFromRemote().execute();
        Toast.makeText(getApplicationContext(),"onRefresh Called",Toast.LENGTH_SHORT).show();
    }

    private class GetJsonFromRemote extends AsyncTask<JSONObject, JSONObject, JSONObject> {

        ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setTitle("Please wait...");
            progressDialog.setMessage("Loading...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(JSONObject... params) {

            JSONObject response = Webservice.getAndroidVersions();
            //TODO play with response
            return response;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            progressDialog.dismiss();

            //Stop refreshing
            swipe_refresh_layout.setRefreshing(false);
            if (ServerResponseHandler.jsonDataHandler(MainActivity.this, jsonObject)) { //EVERYTHING is alright

                try {
                    //Getting JSONArray
                    JSONArray flavoursJsonArray = jsonObject.getJSONArray("flavours");

                    for (int i = 0; i < flavoursJsonArray.length(); i++) {
                        //TODO play with data
                        // You can use corresponding data types like getInt(),getBoolean() and so on.
                        String name = "Name : " + flavoursJsonArray.getJSONObject(i).getString("name");
                        //optString() returns empty string if fall back is not provided(In this case fallback is : "No data found") and prevents throwing Jsonexception.
                        String version = "Version : " + flavoursJsonArray.getJSONObject(i).optString("version", "No data found");
                        String id = "id : " + flavoursJsonArray.getJSONObject(i).getString("id");
                        String support = "Support : " + flavoursJsonArray.getJSONObject(i).getString("support");

                        String name_version = name + "\n" + version;
                        // Add String
                        arrayList.add(name_version);

                        Log.e(TAG, "Added " + id + " " + flavoursJsonArray.getJSONObject(i).getString("name") + " " + support);
                    }
                    arrayAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}