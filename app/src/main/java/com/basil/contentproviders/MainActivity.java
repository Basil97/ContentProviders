package com.basil.contentproviders;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final int CODE = 152;

    private static final String TAG = "Working";

    ListView list;
    ArrayList<String> contacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list = findViewById(R.id.list);
        contacts = new ArrayList<>();

        Log.i(TAG, "on create");

        //boolean access = mRequestPermission();
        if (mRequestPermission()) {
            getAllContacts();
        }
    }

    private boolean mRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(READ_CONTACTS)) {
                    requestPermissions(new String[] {READ_CONTACTS}, CODE);
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getAllContacts();
            }
        }else
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getAllContacts() {
        Log.i(TAG, "getting all contacts");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "running in background");
                Uri uri = ContactsContract.Contacts.CONTENT_URI; //URI Data = content://com.android.contacts/contacts
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {

                    Log.i(TAG, "got the cursor");
                    StringBuilder output;
                    cursor.moveToFirst();
                    do {
                        output = new StringBuilder();

                        String CONTACTS_ID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                        if (hasPhoneNumber > 0) {
                            output.append("First Name: ").append(name);

                            Cursor numbers = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, //URI Data = content://com.android.contents/data/phones
                                    null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                    new  String[] {CONTACTS_ID},
                                    null);
                            if (numbers != null) {
                                numbers.moveToFirst();
                                do {
                                    String number = numbers.getString(numbers.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                    output.append("\nPhone Number:").append(number);
                                }while (numbers.moveToNext());
                                numbers.close();
                            }
                        }
                        contacts.add(output.toString());
                        Log.i(TAG, "Still Getting contacts");
                    }while (cursor.moveToNext());
                    cursor.close();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        list.setAdapter(new ArrayAdapter<>(getApplicationContext(),
                                android.R.layout.simple_list_item_1,
                                android.R.id.text1,
                                contacts));
                    }
                });
            }
        }).start();
    }
}
