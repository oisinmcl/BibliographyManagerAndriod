package uk.ac.mclaughlin_o9ulster.assignment;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.vision.text.Text;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewReferenceActivity extends AppCompatActivity implements OnMapReadyCallback {
    Button btnNext;
    Button btnPrev;
    EditText txtTitle;
    EditText listAuthors;
    EditText txtPublishDate;
    EditText txtPublisher;
    ProgressBar progressBar;
    EditText txtISBN;
    TextView txtCount;
    TextView txtDate;
    TextView txtReport;

    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseAuth firebaseAuth;
    String userID;

    ArrayList<myReference> books;
    int index;

    //Maps Stuff
    final private int REQUEST_COARSE_ACCESS = 123;
    boolean permissionGranted = false;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_reference);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapView2);
        mapFragment.getMapAsync(this);


        index =0;

        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrev = (Button) findViewById(R.id.btnPrevious);
        txtTitle= (EditText) findViewById(R.id.bookTitle);
        listAuthors= (EditText) findViewById(R.id.authorsList);
        txtPublishDate= (EditText) findViewById(R.id.pubDate);
        txtPublisher= (EditText) findViewById(R.id.publisher);
        txtISBN= (EditText) findViewById(R.id.txtISBN);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);
        txtCount = (TextView) findViewById(R.id.txtCount);
        txtDate= (TextView) findViewById(R.id.txtDate);
        txtReport = (TextView) findViewById(R.id.txtReport);

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


        ref.child("books").child(userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressBar.setVisibility(View.VISIBLE);
                try {
                    if (snapshot.getChildrenCount() > 0) {
                        //ArrayList<myReference> books= new ArrayList<>();
                        books= new ArrayList<>();
                        for(DataSnapshot ds : snapshot.getChildren()) {
                            myReference tmpBook = ds.getValue(myReference.class);
                            books.add(tmpBook);
                        }
                        updateForm();
                    }else {
                        Toast.makeText(getBaseContext()
                                ,"No References Found!"
                                , Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
                    }
                }catch(Exception e){
                    Toast.makeText(getBaseContext()
                            , "Error Fetching Data: " + e.getMessage()
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

        btnPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index --;
                updateForm();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                index ++;
                updateForm();
            }
        });

    }

    private void updateForm(){
        if (index < 0) { index = books.size()-1;}
        if (index > books.size()-1) { index = 0;}
        try {
            txtTitle.setText(books.get(index).getTitle());
            listAuthors.setText(books.get(index).getAuthors());
            txtPublishDate.setText(books.get(index).getPublishedDate());
            txtPublisher.setText(books.get(index).getPublisher());
            txtISBN.setText(books.get(index).getISBN());
            txtDate.setText(books.get(index).getTimeStamp());
            txtCount.setText("Reference " + (index + 1) +  " / " + books.size() );
            txtReport.setText(books.get(index).getReport());
            UpdateMap();
        }catch(Exception e){
            Toast.makeText(getBaseContext()
                    , "Error Updating Form: " + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void UpdateMap(){
        List<Address> addresses = null;
        Geocoder geoCoder = new Geocoder(getBaseContext(), Locale.getDefault());
        String add = "";
        try {
            LatLng p = new LatLng(books.get(index).getLat(), books.get(index).getLong());
            try {
                addresses = geoCoder.getFromLocation(p.latitude, p.longitude, 1);
                Address address = addresses.get(0);
                if (addresses.size() > 0) {
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++)
                        add += address.getAddressLine(i) + "\n";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMap.clear();
            mMap.addMarker(new MarkerOptions()
                    .position(p)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    .title(add));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(p, 12.0f));
        }catch (Exception e){
            Toast.makeText(getBaseContext()
                    , "Location Error: " + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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






}
