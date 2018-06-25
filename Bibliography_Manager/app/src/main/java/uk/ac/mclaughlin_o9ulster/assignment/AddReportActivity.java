package uk.ac.mclaughlin_o9ulster.assignment;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

public class AddReportActivity extends AppCompatActivity implements SensorEventListener {
    EditText txtTitle;
    EditText txtModule;
    EditText txtdueDate;
    Switch boolReminder;
    Button btnAddReport;
    ProgressBar progressBar;

    FirebaseDatabase database;
    DatabaseReference ref;
    FirebaseAuth firebaseAuth;
    String userID;

    private static final float SHAKE_THRESHOLD_GRAVITY = 1.5f;
    private SensorManager sensorManager;
    private long lastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_report);

        database = FirebaseDatabase.getInstance();
        ref = database.getReference();

        //getting firebase auth object
        firebaseAuth = FirebaseAuth.getInstance();
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                //Go to login
            }
            else{
                userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            }
        }
        catch(Exception e){
            Toast.makeText(getBaseContext()
                    , "Error Fetching User ID: " + e.getMessage()
                    , Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        txtTitle=(EditText) findViewById(R.id.editReportTile);
        txtModule=(EditText) findViewById(R.id.editReportModule);
        txtdueDate=(EditText) findViewById(R.id.editReportDueDate);
        boolReminder=(Switch) findViewById(R.id.switchReminder);
        btnAddReport=(Button) findViewById(R.id.btnAddReport);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.GONE);

        btnAddReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnAddReport.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                saveToFirebase();
                progressBar.setVisibility(View.GONE);
                btnAddReport.setEnabled(true);
                startActivity(new Intent(getApplicationContext(), MainMenuActivity.class));
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
    }

    private void saveToFirebase(){

        if( txtTitle.getText().toString().trim().equals(""))
        {
            txtTitle.setError( "Title is required!" );
            txtTitle.setHint("Please enter a Title");
            return;
        }
        if( txtModule.getText().toString().trim().equals(""))
        {
            txtModule.setError( "An Module is required!" );
            txtModule.setHint("Please enter a Module");
            return;
        }
        if( txtdueDate.getText().toString().trim().equals(""))
        {
            txtdueDate.setError( "A Due Date is required!" );
            txtdueDate.setHint("Please enter a Due Date");
            return;
        }

        try{
            myReport report = new myReport(txtTitle.getText().toString()
                    ,txtModule.getText().toString()
                    ,txtdueDate.getText().toString()
                    ,boolReminder.isChecked());

            String key = ref.child("reports").child(userID).push().getKey();
            ref.child("reports").child(userID).child(key).setValue(report);
            Toast.makeText(getBaseContext(), "Report Saved to Database.", Toast.LENGTH_LONG).show();
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
}
