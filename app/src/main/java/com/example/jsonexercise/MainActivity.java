package com.example.jsonexercise;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jsonexercise.databinding.ActivityMainBinding;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    ArrayList<String> userList;
    Handler mainHandler = new Handler();
    ArrayAdapter<String> listAdapter;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initializeUserlist();
        binding.fetchDataBttn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                new fetchData().start();
            }
        });
    }

    private void initializeUserlist() {
            userList = new ArrayList<>();
            listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1,
                    userList);
            binding.userList.setAdapter(listAdapter);
    }

    class fetchData extends Thread{

        String data = "";
        //String empty = null;

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
                    JSONArray users = new JSONArray(data);
                    userList.clear();
                    for (int i = 0; i<users.length(); i++){
                        JSONObject names = users.getJSONObject(i);
                        String name = names.getString("name");
                        String listId = names.getString("listId");
                        String id = names.getString("id");
                        if (!(name == null || name.isEmpty())) {
                            //condition name == null is always false
                            //name.equals(empty) creates added variable(s) does not support value
                            // initialization error
                            userList.add(name);
                        }
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