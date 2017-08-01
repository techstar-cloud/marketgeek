package com.dglproject.brand.activity;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.dglproject.brand.R;
import com.dglproject.brand.fragments.BrandFragment;
import com.dglproject.brand.utilities.DGLConstants;
import com.dglproject.brand.utilities.PrefManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import android.widget.AdapterView.OnItemSelectedListener;
/**
 * Author: Tortuvshin Byambaa.
 * Project: DglBrand
 * URL: https://www.github.com/tortuvshin
 */
public class ActivityBrandAdd extends AppCompatActivity implements OnItemSelectedListener{

    private static final String TAG = BrandFragment.class.getSimpleName();

    EditText name, description, phone, email, image;
    private Handler mHandler;
    PrefManager prefManager;
    private Spinner catSpinner;
    private Spinner subCatSpinner;
    private ArrayAdapter<String> catAdapter;
    private ArrayAdapter<String> subCatAdapter;
    JSONObject category;
    JSONArray catItems;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand_add);
        getSupportActionBar().setTitle(getString(R.string.company_add));
        getSupportActionBar().setHomeButtonEnabled(true);

        name = (EditText)findViewById(R.id.bName);
        description = (EditText)findViewById(R.id.bDescription);
        phone = (EditText)findViewById(R.id.bPhone);
        email = (EditText)findViewById(R.id.bEmail);
        Button add = (Button)findViewById(R.id.btnBrandAdd);
        catSpinner = (Spinner) findViewById(R.id.catSpinner);
        subCatSpinner = (Spinner) findViewById(R.id.subCatSpinner);
        catSpinner.setOnItemSelectedListener(this);
        subCatSpinner.setOnItemSelectedListener(this);

        prefManager = new PrefManager(this);
        mHandler = new Handler(Looper.getMainLooper());
        getCategory();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), String.valueOf(prefManager.getUserId()), Toast.LENGTH_LONG).show();
                create(name.getText().toString(),
                        description.getText().toString(),
                        prefManager.getUserId(),
                        1,
                        prefManager.getLanguage(),
                        phone.getText().toString(),
                        email.getText().toString(),
                        ""
                );
            }
        });
    }

    private void create (String name, String desc, int userId, int catId, String lang, String mobile, String email, String address) {

        RequestBody formBody = new FormBody.Builder()
                .add("state", "c")
                .add("name", name)
                .add("description", desc)
                .add("ui", String.valueOf(userId))
                .add("categoryId", String.valueOf(catId))
                .add("language", lang)
                .add("mobile", mobile)
                .add("email", email)
                .add("address", address)
                .build();

        String uri = DGLConstants.BrandService;
        Log.e("Exection: ", uri + " ");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(uri)
                .post(formBody)
                .build();

        Log.e(TAG, request.toString());

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Login failed : " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                final String res = response.body().string();

                Log.e(TAG, res);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray ob = new JSONArray(String.valueOf(res));
                            Log.e(TAG, ob.toString());
                            String success = "";
                            for (int i = 0; i < ob.length(); i++) {
                                success = ob.getJSONObject(i).getString("success");
                            }

                            if (success == "1") {
                                Toast.makeText(ActivityBrandAdd.this, getString(R.string.success), Toast.LENGTH_LONG)
                                        .show();
                                finish();
                            } else {
                                Toast.makeText(ActivityBrandAdd.this, getString(R.string.error), Toast.LENGTH_LONG)
                                        .show();
                            }
                            
                        } catch (JSONException e) {
                            Toast.makeText(ActivityBrandAdd.this, getString(R.string.error)+e.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                            e.printStackTrace();
                            Log.e("ERROR : ", e.getMessage() + " ");
                        }
                    }
                });
            }
        });
    }

    private void getCategory() {
        String uri = DGLConstants.CategoryService;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(uri)
                .build();
        Log.e(TAG,request.toString());
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String res = response.body().string();
                mHandler.post(() -> {
                    try {
                        category = new JSONObject(String.valueOf("{category="+res+"}"));
                        catItems = category.getJSONArray("category");
                        List<String> categories = new ArrayList<String>();
                        for (int i = 0; i < catItems.length(); i++){
                            if(!catItems.getJSONObject(i).getString("level").equalsIgnoreCase("3"))
                                categories.add(catItems.getJSONObject(i).getString("name"));
                        }
                        catAdapter = new ArrayAdapter<String>(ActivityBrandAdd.this, android.R.layout.simple_spinner_item, categories);
                        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        catSpinner.setAdapter(catAdapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }
        });

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch (parent.getId()) {
            case R.id.catSpinner:
                List<String> subCats = new ArrayList<String>();
                try {
                    String catId = catItems.getJSONObject(position).getString("id");
                    Log.e(TAG, catItems.getJSONObject(position).getString("id")+" "+catItems.getJSONObject(position).getString("name"));

                    for (int i = 0; i < catItems.length(); i++){

                        String parentId = catItems.getJSONObject(i).getString("parent_id");
                        if (catId.equalsIgnoreCase(parentId)){
                            Log.e(TAG, "Parent id: "+parentId+" name: "+catItems.getJSONObject(i).getString("name"));
                            subCats.add(catItems.getJSONObject(i).getString("name"));
                        } else {
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                subCatAdapter = new ArrayAdapter<String>(ActivityBrandAdd.this, android.R.layout.simple_spinner_item, subCats);
                subCatAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subCatSpinner.setAdapter(subCatAdapter);

                break;
            case R.id.subCatSpinner:
                try {
                    Log.e(TAG, "Дэд ангилал сонгосон: "
                            +catItems.getJSONObject(position).getInt("id")+" нэр: "
                            +catItems.getJSONObject(position).getInt("name"));
                    prefManager.setCat(catItems.getJSONObject(position).getInt("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return false;
    }
}
