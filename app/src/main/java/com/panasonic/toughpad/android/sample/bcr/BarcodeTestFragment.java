package com.panasonic.toughpad.android.sample.bcr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ProgressBar;
import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.barcode.BarcodeData;
import com.panasonic.toughpad.android.api.barcode.BarcodeException;
import com.panasonic.toughpad.android.api.barcode.BarcodeListener;
import com.panasonic.toughpad.android.api.barcode.BarcodeReader;
import com.panasonic.toughpad.android.api.barcode.BarcodeReaderManager;
import com.panasonic.toughpad.android.sample.ApiTestDetailFragment;
import com.panasonic.toughpad.android.sample.R;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class BarcodeTestFragment extends ApiTestDetailFragment implements ToughpadApiListener, 
                                                                          View.OnClickListener, 
                                                                          CompoundButton.OnCheckedChangeListener,
                                                                          Spinner.OnItemSelectedListener,
                                                                          BarcodeListener {
    
    private Spinner spnBcrList;
    private CompoundButton btnEnable;
    private Button btnSwTrigger;
    private CompoundButton chkHwTrigger;
    private TextView txtBcrLog;
    private ScrollView sclBcrLog;
    
    private TextView lblBatteryCharge;
    private TextView txtIsCharging;
    private ProgressBar prgBatteryCharge;
    private TextView txtBatteryCharge;
    private TextView txtDeviceInfo;
    
    private List<BarcodeReader> readers;
    private BarcodeReader selectedReader;
    
    public BarcodeTestFragment() {
    }
    
    private class EnableReaderTask extends AsyncTask<BarcodeReader, Void, Boolean> {
        
        private ProgressDialog dialog;
        
        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(getActivity());
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.dlg_enabling_bcr));
            dialog.show();
        }
        @Override
        protected Boolean doInBackground(BarcodeReader... params) {
            try {
                params[0].enable(10000);
                params[0].addBarcodeListener(BarcodeTestFragment.this);
                return true;
            } catch (BarcodeException ex) {
                handleError(ex);
                return false;
            } catch (TimeoutException ex) {
                handleError(ex);
                return false;
            }
        }
        @Override
        protected void onPostExecute(Boolean result) {
            dialog.dismiss();
            
            if (result) {
                printLogText(getString(R.string.lbl_enabled_device) + " " + selectedReader.getDeviceName());
                onBarcodeEnabled();
            }
        }
    }
    
    private void handleError(final Exception ex) {
        final String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        ex.printStackTrace();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(message);
                builder.setTitle(getString(R.string.title_bcr_error));
                builder.show();
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // NOTE: Reset state from previous invocations
        readers = null;
        selectedReader = null;
        
        View view = inflater.inflate(R.layout.fragment_apitest_barcode, container, false);
        
        spnBcrList = (Spinner)view.findViewById(R.id.spnBcrList);
        btnEnable = (CompoundButton)view.findViewById(R.id.btnEnable);
        btnSwTrigger = (Button)view.findViewById(R.id.btnSwTrigger);
        chkHwTrigger = (CompoundButton)view.findViewById(R.id.chkHwTrigger);
        txtBcrLog = (TextView)view.findViewById(R.id.txtBcrLog);
        sclBcrLog = (ScrollView)view.findViewById(R.id.sclBcrLog);
        
        lblBatteryCharge = (TextView)view.findViewById(R.id.lblBatteryCharge);
        txtIsCharging = (TextView)view.findViewById(R.id.txtIsCharging);
        txtBatteryCharge = (TextView)view.findViewById(R.id.txtBatteryCharge);
        txtDeviceInfo = (TextView)view.findViewById(R.id.txtDeviceInfo);
        prgBatteryCharge = (ProgressBar)view.findViewById(R.id.prgBatteryCharge);
        
        txtBcrLog.setText("");
        onBarcodeDisabled();
        
        spnBcrList.setOnItemSelectedListener(this);
        btnEnable.setOnCheckedChangeListener(this);
        btnSwTrigger.setOnClickListener(this);
        chkHwTrigger.setOnCheckedChangeListener(this);
        
        ToughpadApi.initialize(getActivity(), this);
        
        return view;
    }
    
    public void onApiConnected(int version) {
        readers = BarcodeReaderManager.getBarcodeReaders();
        
        List<String> readerNames = new ArrayList<String>();
        for (BarcodeReader reader : readers) {
            readerNames.add(reader.getDeviceName());
        }
        
        spnBcrList.setAdapter(new ArrayAdapter<String>(getActivity(),
                              android.R.layout.simple_list_item_activated_1,
                              android.R.id.text1,
                              readerNames));
    }
    
    public void onApiDisconnected() {
    }
    
    public void onRead(BarcodeReader bsObj, final BarcodeData result) {
        // Read barcode data
        printLogText(String.format(getString(R.string.log_bcr_scanned), bsObj.getDeviceName(), result.getSymbology()));
        printLogText(result.getTextData());
        
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String formatted = String.format(getString(R.string.dlg_bcr_scanned),
                                                 result.getSymbology(), result.getEncoding(), result.getTextData());
                builder.setMessage(formatted);
                builder.setTitle(R.string.title_bcr_scanned);
                builder.setCancelable(true);
                builder.show();
            }
        });
    }
    
    public void onClick(View v) {
        if (v == btnSwTrigger) {
            try {
                selectedReader.pressSoftwareTrigger(true);
            } catch (BarcodeException ex) {
                handleError(ex);
            }
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (selectedReader == null) {
            return;
        }
        
        if (buttonView == chkHwTrigger) {
            try {
                selectedReader.setHardwareTriggerEnabled(isChecked);
            } catch (BarcodeException ex) {
                handleError(ex);
            }
        } else if (buttonView == btnEnable) {
            if (selectedReader.isEnabled() && !isChecked) {
                try {
                    selectedReader.disable();
                    selectedReader.clearBarcodeListener();
                    printLogText(getString(R.string.lbl_disabled_device) + " " + selectedReader.getDeviceName());
                    onBarcodeDisabled();
                } catch (BarcodeException ex) {
                    handleError(ex);
                }
            } else if (!selectedReader.isEnabled() && isChecked) {
                EnableReaderTask task = new EnableReaderTask();
                task.execute(selectedReader);
            }
        }
    }
    
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedReader = readers.get(position);
        
        if (selectedReader.isEnabled()) {
            onBarcodeEnabled();
        } else {
            onBarcodeDisabled();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        selectedReader = null;
        // Should never happen ???
    }
    
    private void printLogText(final String text) { 
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                txtBcrLog.append(text + "\n");
                sclBcrLog.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    
    private void onBarcodeEnabled() {
        btnEnable.setChecked(true);
        btnSwTrigger.setEnabled(true);
        
        if (selectedReader.isHardwareTriggerAvailable()) {
            chkHwTrigger.setEnabled(true);
            chkHwTrigger.setChecked(selectedReader.isHardwareTriggerEnabled());
        } else {
            chkHwTrigger.setEnabled(false);
        }
        
        txtDeviceInfo.setEnabled(true);
        
        String deviceType = getString(R.string.bcr_type_unknown);
        switch (selectedReader.getBarcodeType()) {
            case BarcodeReader.BARCODE_TYPE_CAMERA:
                deviceType = getString(R.string.bcr_type_cam); break;
            case BarcodeReader.BARCODE_TYPE_ONE_DIMENSIONAL:
                deviceType = getString(R.string.bcr_type_1d); break;
            case BarcodeReader.BARCODE_TYPE_TWO_DIMENSIONAL:
                deviceType = getString(R.string.bcr_type_2d); break;
        }
        
        // Fill in device info
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.lbl_firmware_version, selectedReader.getDeviceFirmwareVersion())).append("\n");
            sb.append(getString(R.string.lbl_serial_number, selectedReader.getDeviceSerialNumber())).append("\n");
            sb.append(getString(R.string.lbl_external_device, getString(selectedReader.isExternal() ? R.string.lbl_is_charging_yes : R.string.lbl_is_charging_no))).append("\n");
            sb.append(getString(R.string.lbl_bcr_type, deviceType));
            txtDeviceInfo.setText(sb.toString());
        } catch (BarcodeException ex) {
            handleError(ex);
        }
        
        lblBatteryCharge.setEnabled(false);
        txtIsCharging.setEnabled(false);
        txtBatteryCharge.setEnabled(false);
        prgBatteryCharge.setEnabled(false);
        
        prgBatteryCharge.setProgress(0);
        txtBatteryCharge.setText(R.string.lbl_charge_na);
        
        if (selectedReader.isExternal()) {
            try {
                int charge = selectedReader.getBatteryCharge();
                if (charge != -1) {
                    lblBatteryCharge.setEnabled(true);
                    txtIsCharging.setEnabled(true);
                    txtBatteryCharge.setEnabled(true);
                    prgBatteryCharge.setEnabled(true);
        
                    // Battery Info Available
                    prgBatteryCharge.setProgress(charge);
                    txtBatteryCharge.setText(String.format(getString(R.string.lbl_charge_percent), charge));
                    txtIsCharging.setText(getString(R.string.lbl_is_charging, 
                                                    selectedReader.isBatteryCharging() ? 
                                                        getString(R.string.lbl_is_charging_yes) :
                                                        getString(R.string.lbl_is_charging_no)));
                }
            } catch (BarcodeException ex) {
                handleError(ex);
            }
        }
    }
    
    private void onBarcodeDisabled() {
        btnEnable.setChecked(false);
        btnSwTrigger.setEnabled(false);
        chkHwTrigger.setEnabled(false);
        
        lblBatteryCharge.setEnabled(false);
        txtIsCharging.setEnabled(false);
        txtIsCharging.setText("");
        txtBatteryCharge.setEnabled(false);
        txtDeviceInfo.setEnabled(false);
        txtDeviceInfo.setText("");
        prgBatteryCharge.setEnabled(false);
        prgBatteryCharge.setProgress(0);
        txtBatteryCharge.setText("");
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ToughpadApi.destroy();
    }
}
