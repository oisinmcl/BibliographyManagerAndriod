package uk.ac.mclaughlin_o9ulster.assignment;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Camera;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;
import java.security.Policy;

import static java.lang.Boolean.FALSE;

public class BarCodeScanActivity extends AppCompatActivity {
    private Button mybutton;
    private TextView myTextView;
    private SurfaceView mySurfaceView;
    private SurfaceHolder mySurfacholder;
    private Camera camera;
    private Policy.Parameters cameraParameters;
    private BarcodeDetector barcodeDetector;
    private CameraSource mCameraSource;
    private boolean barcodeFound;

    private String BARCODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_code_scan);
        barcodeFound = false;
        myTextView = (TextView) findViewById(R.id.txtContent);
        mySurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mybutton = (Button) findViewById(R.id.button);
        mybutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!barcodeFound){
                    Toast.makeText(getApplicationContext(), "No Barcode Found",
                           Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(BarCodeScanActivity.this, ViewBookActivity.class);
                    intent.putExtra("barcode", BARCODE);
                    startActivity(intent);
                }
            }
        });

        barcodeDetector = new BarcodeDetector
                .Builder(this)
                .setBarcodeFormats(Barcode.EAN_8|Barcode.EAN_13)
                .build();

        if (!barcodeDetector.isOperational()) {
            myTextView.setText("Could not set up the detector!");
            return;
        }

        mCameraSource = new CameraSource
                .Builder(this, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640, 480)
                .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
                .build();

        //parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);

        mySurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(BarCodeScanActivity.this, new String[]{Manifest.permission.CAMERA}, 1);
                }else {
                    try {
                        mCameraSource.start(mySurfaceView.getHolder());
                    } catch (IOException ie) {
                        Log.e("CAMERA SOURCE", ie.getMessage());
                    }
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

                mCameraSource.stop();
            }
        });


        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                //Toast.makeText(getApplicationContext(), "Bar Code Found!",
                //       Toast.LENGTH_LONG).show();
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {barcodeFound = false;}

                if (barcodes.size() != 0) {
                    barcodeFound = true;
                    myTextView.post(new Runnable() {    // Use the post method of the TextView
                        public void run() {
                            BARCODE = barcodes.valueAt(0).displayValue;
                            myTextView.setText(BARCODE);
                        }
                    });
                }
            }
        });
    }

}




