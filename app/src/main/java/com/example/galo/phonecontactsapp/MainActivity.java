package com.example.galo.phonecontactsapp;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.galo.phonecontactsapp.controllers.AppController;


public class MainActivity extends AppCompatActivity {
    Button button;
    final static String CONTACTS_WS = "";
    final static HashMap<String, String> contacts = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.button = (Button) findViewById(R.id.button);
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Contacts().execute(MainActivity.this);
                for (String name : contacts.keySet()) {
                    String phone = contacts.get(name);
                    addNewContact(name, phone);
                }
                Toast.makeText(MainActivity.this, "New contacts added successfully!", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void addNewContact(String DisplayName, String MobileNumber){
        ArrayList < ContentProviderOperation > ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation.newInsert(
                ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());

        //------------------------------------------------------ Names
        if (DisplayName != null) {
            ops.add(ContentProviderOperation.newInsert(
                    ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            DisplayName).build());
        }

        //------------------------------------------------------ Mobile Number
        if (MobileNumber != null) {
            ops.add(ContentProviderOperation.
                    newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, MobileNumber)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        // Asking the Contact provider to create a new contact
        try {
            getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this.getApplicationContext(), "Exception: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private class Contacts extends AsyncTask<Context, Void, Void> {
        Context context;
        @Override
        protected Void doInBackground(Context... ctxs) {
            context = ctxs[0];
                JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                        CONTACTS_WS, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArrContacts = response.getJSONArray("contacts");
                            int numContacts = jsonArrContacts.length();
                            for (int i = 0; i < numContacts; i++) {
                                JSONObject jsonObj = (JSONObject) jsonArrContacts.get(i);
                                String phoneNumber = jsonObj.getString("phone");
                                String name = jsonObj.getString("name");
                                contacts.put(name, phoneNumber);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context, "Error on loading data!", Toast.LENGTH_LONG).show();
                        } finally {
                            System.gc();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("tag", "Error: " + error.getMessage());
                        Toast.makeText(context, "HTTP Error", Toast.LENGTH_SHORT).show();
                    }
                });
                AppController.getInstance(context).addToRequestQueue(jsonObjReq);
            return null;
        }
    }
}
