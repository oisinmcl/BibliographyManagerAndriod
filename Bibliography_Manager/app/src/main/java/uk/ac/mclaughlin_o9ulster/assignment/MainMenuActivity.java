package uk.ac.mclaughlin_o9ulster.assignment;

import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainMenuActivity extends AppCompatActivity {
    Button btnscanBook;
    Button btnAddreport;
    Button btnViewRef;
    Button buttonLogout;

    //firebase auth object
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        firebaseAuth = FirebaseAuth.getInstance();

        btnscanBook = (Button)findViewById(R.id.Scanbook);
        btnAddreport = (Button)findViewById(R.id.AddNewReport);
        btnViewRef  = (Button)findViewById(R.id.ViewReferences);
        buttonLogout = (Button)findViewById(R.id.LogOut);

        btnscanBook.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainMenuActivity.this, new String[]{android.Manifest.permission.CAMERA}, 1);
                }
                startActivity(new Intent(MainMenuActivity.this, BarCodeScanActivity.class));
            }
        });
        btnAddreport.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainMenuActivity.this, AddReportActivity.class));
            }
        });
        btnViewRef.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startActivity(new Intent(MainMenuActivity.this, ViewReferenceActivity.class));
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //logging out the user
                firebaseAuth.signOut();
                //closing activity
                finish();
                //starting login activity
                startActivity(new Intent(MainMenuActivity.this, LoginActivity.class));
            }
        });

        //if the user is not logged in
        //that means current user will return null
        if(firebaseAuth.getCurrentUser() == null){
            //closing this activity
            finish();
            //starting login activity
            startActivity(new Intent(this, LoginActivity.class));
        }

    }

}
