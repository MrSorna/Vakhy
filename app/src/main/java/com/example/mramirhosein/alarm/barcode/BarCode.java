package com.example.mramirhosein.alarm.barcode;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.mramirhosein.alarm.R;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class BarCode extends Activity implements
        DecoratedBarcodeView.TorchListener {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageView switchFlashlightButton;
    private boolean tmp=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);


            setContentView(R.layout.method_barcode);
            barcodeScannerView = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
            barcodeScannerView.setTorchListener(this);
            switchFlashlightButton = (ImageView) findViewById(R.id.switch_flashlight);
            switchFlashlightButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(tmp){
                        barcodeScannerView.setTorchOff();
                    }else{
                        barcodeScannerView.setTorchOn();
                    }
                    tmp = !tmp;
                }
            });
            // if the device does not have flashlight in its camera,
            // then remove the switch flashlight button...t
            if (!hasFlash()) {
                switchFlashlightButton.setVisibility(View.GONE);
            }

            capture = new CaptureManager(this, barcodeScannerView);
            capture.initializeFromIntent(getIntent(), savedInstanceState);
            capture.decode();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    @Override
    public void onTorchOn() {
        switchFlashlightButton.setImageResource(R.drawable.flashlight_on);
    }

    @Override
    public void onTorchOff() {
        switchFlashlightButton.setImageResource(R.drawable.flashlight_off);

    }
}
