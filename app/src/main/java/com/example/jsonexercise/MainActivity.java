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
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    //TreeMap same as dictionary in python
    ArrayList<String> userList;
    TreeMap<String, TreeMap<String, Item>> itemData = new TreeMap<String, TreeMap<String, Item>>();
    //TODO replace string key in second treemap with int
    //TODO parse and trim each name string to remove "item "
    //TODO convert name from string to int
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
            userList = new ArrayList();
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
                        Item item = new Item();
                        item.name = names.getString("name");
                        item.listid = names.getString("listId");
                        item.id = names.getString("id");

                        if (!(item.name == "null" || item.name.isEmpty())) {
                            loadData(item);
                        }
                    }

                    for(String listId: itemData.keySet()) {
                        TreeMap<String, Item> names = itemData.get(listId);
                        for (String name : names.keySet()) {
                            Item item = names.get(name);
                            userList.add("List Id: " + item.listid + " | Name:" + item.name);
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

    private void loadData(Item newitem) {
        TreeMap namemap = itemData.get(newitem.listid);
        if(namemap == null){
            //if the listid is not a key in treemap, then create a new treemap
            namemap = new TreeMap<String, Item>();
        }
        //item is name key, the value is treemap namemap
        namemap.put(newitem.name, newitem);

        itemData.put(newitem.listid, namemap);
    }
}