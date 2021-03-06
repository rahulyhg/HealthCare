package com.mlab.pes.healthcare;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity{
    //Declaring Buttons
    Button add, update, sync;
    //Declaring variables
    public static boolean check_internet = true;
    static TextView syncStatus;
    //Declaring Database
    static SQLiteDatabase db;
    private static MainActivity app;
    static SharedPreferences mPrefs;
    static String url="";
    public static boolean connected=false;
    public static ProgressDialog dialog;
    private Handler mHandler = new Handler();
    public static MainActivity get(){
        return app;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        app=this;

        mPrefs=getSharedPreferences("label", 0);

        //Checking if Internet is available
        if (check_internet && INTERNER_CHECK()) {
            showMessage("You have an Internet Connection", "Please Sync now",get());
        }
        //Making sure that it appears only once when the app is opened.
        check_internet = false;
        //Creating DB
        db = openOrCreateDatabase("healthcare", Context.MODE_PRIVATE, null);

        String image_table_query=
                "  child_id VARCHAR[10] ," +
                        "  photo_id VARCHAR[30] ," +
                        "  image TEXT" ;
        //creating image table
        db.execSQL("CREATE TABLE IF NOT EXISTS images( " + image_table_query + " )");

        String child_table_query=
                "  child_id VARCHAR[10] ," +
                        "  name VARCHAR[140] ," +
                        "  gender VARCHAR[10]," +
                        "  father_name VARCHAR[140]";
        //creating image table
        db.execSQL("CREATE TABLE IF NOT EXISTS child_references( " + child_table_query + " )");

        String school_table_query=
                "  school_id VARCHAR[10] ," +
                        "  name VARCHAR[140] " ;
        //creating image table
        db.execSQL("CREATE TABLE IF NOT EXISTS school_references( " + school_table_query + " )");

        /*
        String insert_query = "'" + "11111111" + "'," +
                "'" + "School Name" + "'";
        System.out.println(insert_query);
        //inserting into database
        MainActivity.db.execSQL("INSERT INTO school_references VALUES (" + insert_query + ")");

        String insert_query_child = "'" + "11111111111" + "'," +
                "'" + "Name" + "',"+
                "'" + "Male" + "',"+
                "'" + "Father Name" + "'";
        System.out.println(insert_query);
        //inserting into database
        MainActivity.db.execSQL("INSERT INTO child_references VALUES (" + insert_query_child + ")");*/

        syncStatus=(TextView) findViewById(R.id.syncStatus);

        String mString = mPrefs.getString("status", "Not Synced");
        syncStatus.setText(mString);


        //Button initialization
        add = (Button) findViewById(R.id.add);
        update = (Button) findViewById(R.id.update);
        sync = (Button) findViewById(R.id.sync);
    }
    //To Add Schools and Students(Changing Intent to SchoolAdd Activity)
    public void ADD(View view) {

        Intent intent = new Intent(this, studentDataEntry.class);
        startActivity(intent);
        //Transition Animation
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

    }
    //To Update Health Details of particular student(Changing Intent to Update Activity
    public void UPDATE(View view) {
        Intent i = new Intent(this, UpdateActivity.class);
        i.putExtra("caller", "MainActivity");
        startActivity(i);
        //Transition Animation
        overridePendingTransition(R.anim.push_left_in, R.anim.push_down_out);
    }
    //To Sync the details to the Cloud(Pushing Data to the Cloud)
    public void SYNC(View view) {
        boolean check = INTERNER_CHECK();
        if (check) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectorCheck connectorCheck=new connectorCheck();
                    connectorCheck.execute();

                    Intent intent = new Intent(MainActivity.this, syncingData.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);

                }
            });
            connected=false;
        } else {
            showMessage("Check Internet Connection", "Try again", get());
        }
        /*
        boolean check = INTERNER_CHECK();
        if (check) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                        //startProgressDialog("Connecting to Server");
                        //stopProgressDialog();

                        connectorCheck connectorCheck=new connectorCheck();

                        connectorCheck.execute();

                    int timeout=10500;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (connected) {
                                syncing a = new syncing();
                                a.SYNC();
                            } else {
                                showMessage("Warning", "Please check if server is connected to the internet. \nRestart the App and try again", MainActivity.get());
                            }
                        }
                    }, timeout);

                }
            });
        } else {
            showMessage("Check Internet Connection", "Try again", get());
        }
        connected=false;*/
    }
    public void RETRIEVE(View view) {
        boolean check = INTERNER_CHECK();
        if (check) {
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                        connectorCheck connectorCheck=new connectorCheck();

                        connectorCheck.execute();
                    int timeout=10500;
                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            if (connected) {
                                syncing a = new syncing();
                                a.retrieve_child_data();
                                a.retrieve_school_data();
                            } else {
                                showMessage("Warning", "Please check if server is connected to the internet. \nRestart the App and try again", MainActivity.get());
                            }
                        }
                    }, timeout);
                }
            });
        } else {
            showMessage("Check Internet Connection", "Try again", get());
        }

        connected=false;
    }
    /*
    public void emergency(View view) {
        BufferedReader reader=null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("faulty.csv")));
            String line;
            while((line = reader.readLine())!= null){
                db.execSQL("INSERT INTO images VALUES (" + line+")");
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        finally {
            if (reader!=null){
                try{
                    reader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }*/
    //Method to check internet connection
    public boolean INTERNER_CHECK() {
        boolean isInternetPresent;
        ConnectionDetector cd = new ConnectionDetector(getApplicationContext());
        isInternetPresent = cd.isConnectingToInternet();
        return isInternetPresent;
    }
    //Method to create the dialog box
    //@params title and message
    public static void showMessage(String title, String message,Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("OK", null);
        builder.setCancelable(false);
        builder.show();
    }
    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
}