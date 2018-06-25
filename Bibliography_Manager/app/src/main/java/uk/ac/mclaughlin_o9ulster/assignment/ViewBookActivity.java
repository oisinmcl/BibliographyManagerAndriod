package uk.ac.mclaughlin_o9ulster.assignment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewBookActivity extends AppCompatActivity implements SensorEventListener,OnMapReadyCallback {
    //Controls stuff
    Button btnSave;
    EditText txtTitle;
    EditText listAuthors;
    EditText txtPublishDate;
    EditText txtPublisher;
    ProgressBar progressBar;
    EditText txtISBN;
    Spinner spinner;

    //google books api stuff
    String apiurl;
    String fullReqUrl;
    String BARCODE;

    //firebase stuff
    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseAuth firebaseAuth;
    String userID;

    //Shake event stuff
    private static final float SHAKE_THRESHOLD_GRAVITY = 1.5f;
    private SensorManager sensorManager;
    private long lastUpdateTime;

    //Maps Stuff
    final private int REQUEST_COARSE_ACCESS = 123;
    boolean permissionGranted = false;
    double myLat;
    double myLong;

    private GoogleMap mMap;

    LocationManager lm;
    LocationListener locationListener;


    private InputStream OpenHttpConnection(String urlString) throws IOException {
        InputStream in = null;
        int response = -1;

        URL url = new URL(urlString);
        URLConnection conn = url.openConnection();

        if (!(conn instanceof HttpURLConnection))
            throw new IOException("Not an HTTP connection");
        try {
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            response = httpConn.getResponseCode();
            if (response == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
            }
        } catch (Exception ex) {
            Log.d("Networking", ex.getLocalizedMessage());
            throw new IOException("Error connecting");
        }
        return in;
    }
    public String readJSONFeed(String address) {
        URL url = null;
        try {
            url = new URL(address);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        StringBuilder stringBuilder = new StringBuilder();
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            InputStream content = new BufferedInputStream(urlConnection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(content));
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            urlConnection.disconnect();
        }
        return stringBuilder.toString();
    }
    private class ReadJSONFeedTask extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... urls) {
            return readJSONFeed(urls[0]);
        }

        protected void onPostExecute(String result) {
            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("items");
                //JSONObject volumeInfo = jsonObject.getJSONObject("volumeInfo");

                Log.i("JSON", "Number of books in feed: " + jsonArray.length());
                String str = "";

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject volumeInfo = jsonArray.getJSONObject(i).getJSONObject("volumeInfo");
                    JSONArray authors = volumeInfo.getJSONArray("authors");

                    txtTitle.setText(volumeInfo.getString("title"));
                    for (int a = 0; i < authors.length(); i++) {
                        str += authors.getString(a) + ", ";
                    }
                    listAuthors.setText(str);
                    txtPublishDate.setText(volumeInfo.getString("publishedDate"));
                    txtPublisher.setText(volumeInfo.getString("publisher"));
                    txtISBN.setText(BARCODE);

                }
                //updateView();
                progressBar.setVisibility(View.GONE);

            } catch (JSONException e) {
                Toast.makeText(getBaseContext()
                        , "JSON Error: " + e.getMessage()
                        , Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            catch (Exception e) {
                Toast.makeText(getBaseContext()
                        , "General Error: " + e.getMessage()
                        , Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_book);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            userID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        } catch(Exception e){
            Toast.makeText(getBaseContext()
                    , "Error Fetching User ID: " + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        myLat = 0;
        myLong= 0;

        btnSave = (Button) findViewById(R.id.btnAddReport);
        txtTitle= (EditText) findViewById(R.id.bookTitle);
        listAuthors= (EditText) findViewById(R.id.authorsList);
        txtPublishDate= (EditText) findViewById(R.id.pubDate);
        txtPublisher= (EditText) findViewById(R.id.publisher);
        txtISBN= (EditText) findViewById(R.id.txtISBN);
        spinner = (Spinner) findViewById(R.id.spinner);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.VISIBLE);

        apiurl = "https://www.googleapis.com/books/v1/volumes?q=isbn:";
        //get barcode from previous activity
        Bundle bundle = getIntent().getExtras();
        try{
            BARCODE = bundle.getString("barcode");
            //BARCODE = "9780470848708";
            //txtBarcode.setText(BARCODE);
        }catch (Exception e) {
            Toast.makeText(getBaseContext()
                    , "Error Getting Bar Code. Error: " + e.getMessage()
                    , Toast.LENGTH_LONG).show();
        }

        //full http api url
        fullReqUrl = apiurl + BARCODE;
        new ReadJSONFeedTask().execute(fullReqUrl);


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSave.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                saveToFirebase();
                progressBar.setVisibility(View.GONE);
                btnSave.setEnabled(true);
            }
        });
        try {
            sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            lastUpdateTime = System.currentTimeMillis();
        }catch(Exception e){
            Toast.makeText(getBaseContext()
                    , "Error in sensorManager: " + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        ref.child("reports").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
                try {
                    if (snapshot.getChildrenCount() > 0) {
                        ArrayList<String> reports = new ArrayList<>();

                        for (DataSnapshot reportSnapshot: snapshot.getChildren()) {
                            String reportName = reportSnapshot.child("title").getValue(String.class);
                            reports.add(reportName);
                        }
                        ArrayAdapter<String> reportsAdapter = new ArrayAdapter<>(ViewBookActivity.this
                                , android.R.layout.simple_spinner_item
                                , reports);
                        reportsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(reportsAdapter);
                    }else {
                        Toast.makeText(getBaseContext()
                                ,"No Reports Found!"
                                , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
                    }
                }catch(Exception e){
                    Toast.makeText(getBaseContext()
                            , "Error Fetching Reports: " + e.getMessage()
                            , Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    progressBar.setVisibility(View.GONE);

                }
                progressBar.setVisibility(View.GONE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getBaseContext()
                        , "Error Fetching Data: " + databaseError.getMessage()
                        , Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);

            }
        });

    }

    private void saveToFirebase(){
        if( txtTitle.getText().toString().trim().equals(""))
        {
            txtTitle.setError( "Title is required!" );
            txtTitle.setHint("Please enter a Title");
            return;
        }
        if( listAuthors.getText().toString().trim().equals(""))
        {
            listAuthors.setError( "An Author is required!" );
            listAuthors.setHint("Please enter an Author");
            return;
        }
        if( txtPublishDate.getText().toString().trim().equals(""))
        {
            txtPublishDate.setError( "A Publish Date is required!" );
            txtPublishDate.setHint("Please enter a Publish Date");
            return;
        }

        if( txtPublisher.getText().toString().trim().equals(""))
        {
            txtPublisher.setError( "A Publisher is required!" );
            txtPublisher.setHint("Please enter a Publisher");
            return;
        }
        if( txtISBN.getText().toString().trim().equals(""))
        {
            txtISBN.setError( "An ISBN is required!" );
            txtISBN.setHint("Please enter an ISBN");
            return;
        }
        if( myLat==0 || myLong==0)
        {
            Toast.makeText(getBaseContext(), "No Location Data.", Toast.LENGTH_LONG).show();
        }
        if( spinner.getSelectedItem().toString().trim().equals(""))
        {
            Toast.makeText(getBaseContext(), "Please select a report", Toast.LENGTH_LONG).show();
            return;
        }

        try{
            myReference book = new myReference(txtTitle.getText().toString()
                    ,listAuthors.getText().toString()
                    ,txtPublisher.getText().toString()
                    ,txtPublishDate.getText().toString()
                    ,txtISBN.getText().toString()
                    ,myLat
                    ,myLong
                    ,SimpleDateFormat.getInstance().format(System.currentTimeMillis())
                    ,spinner.getSelectedItem().toString());

            String key = ref.child("books").child(userID).push().getKey();
            ref.child("books").child(userID).child(key).setValue(book);

            Toast.makeText(getBaseContext(), "Book Saved to Database.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
        } catch(Exception e){
            Toast.makeText(getBaseContext()
                    , "Error Saving to Database: " + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    }

    private void getAccelerometer(SensorEvent event) {
        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        float gX = x / SensorManager.GRAVITY_EARTH;
        float gY = y / SensorManager.GRAVITY_EARTH;
        float gZ = z / SensorManager.GRAVITY_EARTH;

        // gForce will be close to 1 when there is no movement.
        float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

        long currentTime = System.currentTimeMillis();
        if (gForce >= SHAKE_THRESHOLD_GRAVITY) //
        {

            if (currentTime - lastUpdateTime < 200) {
                return;
            }
            lastUpdateTime = currentTime;
            Toast.makeText(this, "Device was shaken", Toast.LENGTH_SHORT).show();
            clearForm((ViewGroup) findViewById(R.id.LinearLayout));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);

        //----remove the location listener----
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_COARSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }
        if (permissionGranted) {
            lm.removeUpdates(locationListener);
        }
    }

    private void clearForm(ViewGroup group)
    {
        for (int i = 0, count = group.getChildCount(); i < count; ++i) {
            View view = group.getChildAt(i);
            if (view instanceof EditText) {
                ((EditText)view).setText("");
            }

            if(view instanceof ViewGroup && (((ViewGroup)view).getChildCount() > 0))
                clearForm((ViewGroup)view);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //---use the LocationManager class to obtain locations data---
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new ViewBookActivity.MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_COARSE_ACCESS);
            return;
        } else {
            permissionGranted = true;
        }
        if (permissionGranted) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_COARSE_ACCESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = true;
                } else {
                    permissionGranted = false;
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private class MyLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location loc) {
            if (loc != null) {
                Toast.makeText(getBaseContext(),
                        "Current Location : Lat: " + loc.getLatitude() +
                                " Lng: " + loc.getLongitude(), Toast.LENGTH_LONG).show();
                LatLng p = new LatLng(loc.getLatitude(), loc.getLongitude());
                myLat = loc.getLatitude();
                myLong= loc.getLongitude();
                Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
                List<Address> addresses = null;
                String add = "";
                try {
                    addresses = geoCoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                    Address address = addresses.get(0);

                    if (addresses.size() > 0) {
                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                            add += address.getAddressLine(i) + "\n";
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mMap.addMarker(new MarkerOptions()
                        .position(p)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(add));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 12.0f));

            }
        }



        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }


    }
}
