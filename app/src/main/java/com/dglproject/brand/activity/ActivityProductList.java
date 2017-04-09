package com.dglproject.brand.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dglproject.brand.DglConstants;
import com.dglproject.brand.R;
import com.dglproject.brand.adapters.ProductListAdapter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ActivityProductList extends AppCompatActivity {

    ListView listMenu;
    ProgressBar prgLoading;
    //TextView txtTitle;
    EditText edtKeyword;
    ImageButton btnSearch;
    TextView txtAlert;

    JSONObject jsonObject;
    JSONArray jsonArrayProducts;

    ProductListAdapter productListAdapter;

    public static ArrayList<Long> Product_ID = new ArrayList<Long>();
    public static ArrayList<String> Product_name = new ArrayList<String>();
    public static ArrayList<Double> Product_price = new ArrayList<Double>();
    public static ArrayList<String> Product_image = new ArrayList<String>();

    String ProductService;
    int IOConnect = 0;
    long Category_ID;
    String Category_name;
    String Keyword;

    DecimalFormat formatData = new DecimalFormat("#.##");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
        listMenu = (ListView) findViewById(R.id.listMenu);
        edtKeyword = (EditText) findViewById(R.id.edtKeyword);
        btnSearch = (ImageButton) findViewById(R.id.btnSearch);
        txtAlert = (TextView) findViewById(R.id.txtAlert);

        Intent iGet = getIntent();
        Category_ID = iGet.getLongExtra("brand_id",0);
        Category_name = iGet.getStringExtra("brand_name");

        Log.d("","Brand ID: "+Category_ID);

        ProductService = DglConstants.ProductService+"?accesskey="+String.valueOf(DglConstants.generateAccessKey())+"&state=r&brand_id="+Category_ID;

        Log.d("","URL: "+ProductService);

        productListAdapter = new ProductListAdapter(ActivityProductList.this);

        new getDataTask().execute();



        getSupportActionBar().setTitle(Category_name);

        btnSearch.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                try {
                    Keyword = URLEncoder.encode(edtKeyword.getText().toString(), "utf-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                ProductService += "&keyword="+Keyword;
                IOConnect = 0;
                listMenu.invalidateViews();
                clearData();
                new getDataTask().execute();
            }
        });

        listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {

                Intent iDetail = new Intent(ActivityProductList.this, ActivityProductDetail.class);
                iDetail.putExtra("product_id", Product_ID.get(position));
                startActivity(iDetail);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category, menu);

//		final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//
//        	@Override
//            public boolean onQueryTextChange(String newText) {
//                return true;
//            }
//
//        	@Override
//            public boolean onQueryTextSubmit(String query) {
//        		try {
//					Keyword = URLEncoder.encode(query.toString(), "utf-8");
//				} catch (UnsupportedEncodingException e) {
//					e.printStackTrace();
//				}
//
//            	ProductApi += "&keyword="+Keyword;
// 				IOConnect = 0;
//     			listMenu.invalidateViews();
//     			clearData();
// 				new getDataTask().execute();
//
//                return true;
//            }
//        });
//
//        searchView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
//
//            @Override
//            public void onViewDetachedFromWindow(View arg0) {
//            	IOConnect = 0;
//    			listMenu.invalidateViews();
//    			clearData();
//    			new getDataTask().execute();
//            }
//
//            @Override
//            public void onViewAttachedToWindow(View arg0) {
//                // search was opened
//            }
//        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.cart:
                Intent iMyOrder = new Intent(ActivityProductList.this, ActivityCart.class);
                startActivity(iMyOrder);
                return true;

            case R.id.refresh:
                IOConnect = 0;
                listMenu.invalidateViews();
                clearData();
                new getDataTask().execute();
                return true;

            case android.R.id.home:
                this.finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void clearData(){
        Product_ID.clear();
        Product_name.clear();
        Product_price.clear();
        Product_image.clear();
    }

    public class getDataTask extends AsyncTask<Void, Void, Void>{

        getDataTask(){
            if(!prgLoading.isShown()){
                prgLoading.setVisibility(0);
                txtAlert.setVisibility(8);
            }
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            parseJSONData();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            prgLoading.setVisibility(8);

            if(Product_ID.size() > 0){
                listMenu.setVisibility(0);
                listMenu.setAdapter(productListAdapter);
            }else{
                txtAlert.setVisibility(0);
                Toast.makeText(getApplicationContext(), getString(R.string.no_product), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void parseJSONData(){

        clearData();

        try {
            HttpClient client = new DefaultHttpClient();
            HttpConnectionParams.setConnectionTimeout(client.getParams(), 15000);
            HttpConnectionParams.setSoTimeout(client.getParams(), 15000);
            HttpUriRequest request = new HttpGet(ProductService);
            HttpResponse response = client.execute(request);
            InputStream atomInputStream = response.getEntity().getContent();

            BufferedReader in = new BufferedReader(new InputStreamReader(atomInputStream));

            String line;
            String str = "";
            while ((line = in.readLine()) != null){
                str += line;
            }

            Log.d("","JSON: "+str);
            jsonObject = new JSONObject("{product=" + str+"}");
            jsonArrayProducts = jsonObject.getJSONArray("product");

            for (int i = 0; i < jsonArrayProducts.length(); i++) {

                Product_ID.add(jsonArrayProducts.getJSONObject(i).getLong("id"));
                Product_name.add(jsonArrayProducts.getJSONObject(i).getString("name"));
                Product_price.add(jsonArrayProducts.getJSONObject(i).getDouble("price"));
                Product_image.add(jsonArrayProducts.getJSONObject(i).getString("folder"));

            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        //productListAdapter.imageLoader.clearCache();
        listMenu.setAdapter(null);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
