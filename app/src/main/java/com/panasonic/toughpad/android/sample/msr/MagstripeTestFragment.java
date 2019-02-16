package com.panasonic.toughpad.android.sample.msr;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ProgressBar;
import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.magstripe.MagStripeData;
import com.panasonic.toughpad.android.api.magstripe.MagStripeException;
import com.panasonic.toughpad.android.api.magstripe.MagStripeListener;
import com.panasonic.toughpad.android.api.magstripe.MagStripeReader;
import com.panasonic.toughpad.android.api.magstripe.MagStripeReaderManager;
import com.panasonic.toughpad.android.api.magstripe.magtek.MagTekMagStripeData;
import com.panasonic.toughpad.android.sample.ApiTestDetailFragment;
import com.panasonic.toughpad.android.sample.R;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class MagstripeTestFragment extends ApiTestDetailFragment implements ToughpadApiListener, 
                                                                            CompoundButton.OnCheckedChangeListener,
                                                                            Spinner.OnItemSelectedListener,
                                                                            MagStripeListener {
    
    private Spinner spnMsrList;
    private CompoundButton btnEnable;
    private TextView txtMsrLog;
    private ScrollView sclMsrLog;
    
    private TextView lblBatteryCharge;
    private TextView txtIsCharging;
    private ProgressBar prgBatteryCharge;
    private TextView txtBatteryCharge;
    private TextView txtDeviceInfo;
    
    private List<MagStripeReader> readers;
    private MagStripeReader selectedReader;
    
    public MagstripeTestFragment() {
    }

    private class EnableReaderTask extends AsyncTask<MagStripeReader, Void, Boolean> {
        
        private ProgressDialog dialog;
        
        @Override
        protected void onPreExecute(){
            dialog =  new ProgressDialog(getActivity());
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.setMessage(getString(R.string.dlg_enabling_msr));
            dialog.show();
        }
        @Override
        protected Boolean doInBackground(MagStripeReader... params) {
            try {
                params[0].enable(10000);
                params[0].addMagStripeListener(MagstripeTestFragment.this);
                return true;
            } catch (MagStripeException ex) {
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
                onMagStripeEnabled();
            }
        }
    }
    
    private void handleError(final Exception ex) {
        ex.printStackTrace();
        
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(ex.getMessage());
                builder.setTitle(getString(R.string.title_msr_error));
                builder.show();
            }
        });
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // NOTE: Reset state from previous invocations
        readers = null;
        selectedReader = null;
        
        View view = inflater.inflate(R.layout.fragment_apitest_magstripe, container, false);
        
        spnMsrList = (Spinner)view.findViewById(R.id.spnMsrList);
        btnEnable = (CompoundButton)view.findViewById(R.id.btnEnable);
        txtMsrLog = (TextView)view.findViewById(R.id.txtMsrLog);
        sclMsrLog = (ScrollView)view.findViewById(R.id.sclMsrLog);
        
        lblBatteryCharge = (TextView)view.findViewById(R.id.lblBatteryCharge);
        txtIsCharging = (TextView)view.findViewById(R.id.txtIsCharging);
        txtBatteryCharge = (TextView)view.findViewById(R.id.txtBatteryCharge);
        txtDeviceInfo = (TextView)view.findViewById(R.id.txtDeviceInfo);
        prgBatteryCharge = (ProgressBar)view.findViewById(R.id.prgBatteryCharge);
        
        txtMsrLog.setText("");
        onMagStripeDisabled();
        
        spnMsrList.setOnItemSelectedListener(this);
        btnEnable.setOnCheckedChangeListener(this);
        
        ToughpadApi.initialize(getActivity(), this);
        
        return view;
    }

    //new fn initReader() Crash fix VT 12/1/2015
    private void initReader() {
        readers = MagStripeReaderManager.getMagStripeReaders();
        
        List<String> readerNames = new ArrayList<String>();
        for (MagStripeReader reader : readers) {
            readerNames.add(reader.getDeviceName());
        }
        
        spnMsrList.setAdapter(new ArrayAdapter<String>(getActivity(),
                              android.R.layout.simple_list_item_activated_1,
                              android.R.id.text1,
                              readerNames));
    }
    
    public void onApiConnected(int version) {
	initReader(); //moved below code to above fn VT 12/1/2015
	/*
        readers = MagStripeReaderManager.getMagStripeReaders();
        
        List<String> readerNames = new ArrayList<String>();
        for (MagStripeReader reader : readers) {
            readerNames.add(reader.getDeviceName());
        }
        
        spnMsrList.setAdapter(new ArrayAdapter<String>(getActivity(),
                              android.R.layout.simple_list_item_activated_1,
                              android.R.id.text1,
                              readerNames));
	*/
    }

    public void onApiDisconnected() {
    }
    
    @Override
    public void onRead(MagStripeReader reader, final MagStripeData result) {
        printLogText(String.format(getString(R.string.log_msr_scanned), reader.getDeviceName()));
        
        printLogText("Masked Track 1: " + result.getMaskedTrackData(1));
        printLogText("Masked Track 2: " + result.getMaskedTrackData(2));
        printLogText("Masked Track 3: " + result.getMaskedTrackData(3));
        printLogText("Track 1: " + result.getTrackData(1));
        printLogText("Track 2: " + result.getTrackData(2));
        printLogText("Track 3: " + result.getTrackData(3));

        
        if(result instanceof MagTekMagStripeData) {
            MagTekMagStripeData magTekData = (MagTekMagStripeData) result;
            printLogText("_____MagTekMagStripeData_____");
            printLogText("Track 1: " + magTekData.getTrackData(1));
            printLogText("Track 2: " + magTekData.getTrackData(2)); 
            printLogText("Track 3: " + magTekData.getTrackData(3)); 
            printLogText("Good Swipe: " + magTekData.isGoodSwipe());
            printLogText("Data Encrypted: " + magTekData.isTrackDataEncrypted());
            printLogText("Card Name: " + magTekData.getCardName());
            printLogText("Card Service Code: " + magTekData.getCardServiceCode());
            printLogText("Card Status: " + magTekData.getCardStatus());
            printLogText("Encrypted Count: " + magTekData.getEncryptedCount()); 
            printLogText("Encryption Status: " + magTekData.getEncryptionStatus());
            printLogText("Experation Date: " + magTekData.getExperationDate());
            printLogText("Firmware: " + magTekData.getFirmware());
            printLogText("IIN: " + magTekData.getIIN());
            printLogText("KSN: " + magTekData.getKSN()); 
            printLogText("Last 4 Digits: " + magTekData.getLast4Digits());
            printLogText("MSR Serial Number: " + magTekData.getMSRSerialNumber());
            printLogText("Magne Print: " + magTekData.getMagnePrint());
            printLogText("Magne Print Status: " + magTekData.getMagnePrintStatus());
            printLogText("PAN: " + magTekData.getMaskedPAN());
            printLogText("Session ID: " + magTekData.getSessionId());
            printLogText("Track Decode Status: " + magTekData.getTrackDecodeStatus());
        }
        
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                String formatted;
                if(result instanceof MagTekMagStripeData) {
                    MagTekMagStripeData magTekData = (MagTekMagStripeData) result;
                    formatted = String.format(getString(R.string.dlg_msr_scanned), 
                                            "MagTek - " +
                                            "Track 1: " + magTekData.getTrackData(1) + 
                                            "Track 2: " + magTekData.getTrackData(2) + 
                                            "Track 3: " + magTekData.getTrackData(3) + 
                                            "Good Swipe: " + magTekData.isGoodSwipe() +
                                            "Data Encrypted: " + magTekData.isTrackDataEncrypted() +
                                            "Card Name: " + magTekData.getCardName() +
                                            "Card Service Code: " + magTekData.getCardServiceCode() + 
                                            "Card Status: " + magTekData.getCardStatus() + 
                                            "Encrypted Count: " + magTekData.getEncryptedCount() + 
                                            "Encryption Status: " + magTekData.getEncryptionStatus() +
                                            "Experation Date: " + magTekData.getExperationDate() + 
                                            "Firmware: " + magTekData.getFirmware() + 
                                            "IIN: " + magTekData.getIIN() + 
                                            "KSN: " + magTekData.getKSN() + 
                                            "Last 4 Digits: " + magTekData.getLast4Digits() +
                                            "MSR Serial Number: " + magTekData.getMSRSerialNumber() + 
                                            "Magne Print: " + magTekData.getMagnePrint() + 
                                            "Magne Print Status: " + magTekData.getMagnePrintStatus() + 
                                            "PAN: " + magTekData.getMaskedPAN() +
                                            "Session ID: " + magTekData.getSessionId() + 
                                            "Track Decode Status: " + magTekData.getTrackDecodeStatus());
                }
                else {
                    formatted = String.format(getString(R.string.dlg_msr_scanned), 
                                                               "Not MagTek - " + result.getTrackData(1) + result.getTrackData(2) + result.getTrackData(3));
                }
                
                builder.setMessage(formatted);
                builder.setTitle(R.string.title_msr_scanned);
                builder.setCancelable(true);
                builder.show();
            }
        });
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (selectedReader == null) {
            return;
        }
        
        if (buttonView == btnEnable) {
            if (selectedReader.isEnabled() && !isChecked) {
                try {
                    selectedReader.disable();
                    selectedReader.clearMagStripeListeners();
                    printLogText(getString(R.string.lbl_disabled_device) + " " + selectedReader.getDeviceName());
                    onMagStripeDisabled();
		    initReader(); //Sample Crash fix VT 12/1/2015
                } catch (MagStripeException ex) {
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
            onMagStripeEnabled();
        } else {
            onMagStripeDisabled();
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        selectedReader = null;
        // Should never happen ???
    }
    
    private void printLogText(final String text) { 
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                txtMsrLog.append(text + "\n");
                sclMsrLog.fullScroll(View.FOCUS_DOWN);
            }
        });
    }
    
    private void onMagStripeEnabled() {
        btnEnable.setChecked(true);
        txtDeviceInfo.setEnabled(true);
        
        // Fill in device info
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(getString(R.string.lbl_firmware_version, selectedReader.getDeviceFirmwareVersion())).append("\n");
            sb.append(getString(R.string.lbl_serial_number, selectedReader.getDeviceSerialNumber())).append("\n");
            sb.append(getString(R.string.lbl_external_device, getString(selectedReader.isExternal() ? R.string.lbl_is_charging_yes : R.string.lbl_is_charging_no))).append("\n");
            txtDeviceInfo.setText(sb.toString());
        } catch (MagStripeException ex) {
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
            } catch (MagStripeException ex) {
                handleError(ex);
            }
        }
    }
    
    private void onMagStripeDisabled() {
        btnEnable.setChecked(false);
        
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
