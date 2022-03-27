package com.example.jsonexercise;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<String> userList;
    Handler mainHandler = new Handler();
    ArrayAdapter<String> listAdapter;
    ProgressDialog progressDialog;

        @Override
        public void flush() {

        }

        @Override
        public void close() throws SecurityException {

        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(R.layout.activity_main);
        initializeUserlist();
        binding.fetchDataBttn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new fetchData().start();
            }
        })
    }

    private void initializeUserlist() {
            userList = new ArrayList<>();
            listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, userList);
            binding.userList.setAdapter(listAdapter);
    }

    class fetchData extends Thread{

        String data = "";

        @Override
        public void run(){

            mainHandler.post(new Runnable() {
                @Override
                public void run(){
                    progressDialog = new ProgressDialog(MainActivity.this);
                    progressDialog.setMessage("Fetching Data");
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                }
            });
            try {
                URL url = new URL("https://fetch-hiring.s3.amazonaws.com/hiring.json");
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = httpURLConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;

                while((line = bufferedReader.readLine()) != null){
                    data = data + line;
                }

                if(!data.isEmpty()){
                    JSONObject jsonObject = new JSONObject(data);
                    //there is no parent node in the json from URL
                    JSONArray users = jsonObject.getJSONArray("hiring");
                    userList.clear();
                    for (int i = 0; i<users.length(); i++){
                        JSONObject names = users.getJSONObject(i);
                        String name = names.getString("name");
                        userList.add(name);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            mainHandler.post(new Runnable(){
                @Override
                public void run(){
                    if (progressDialog.isShowing()){
                        progressDialog.dismiss();
                        listAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }
}