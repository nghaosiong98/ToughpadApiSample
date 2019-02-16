package com.panasonic.toughpad.android.sample.serial;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.serial.SerialPort;
import com.panasonic.toughpad.android.api.serial.SerialPortManager;
import com.panasonic.toughpad.android.sample.ApiTestDetailFragment;
import com.panasonic.toughpad.android.sample.R;
import java.io.InputStream;

public class SerialPortTestFragment extends ApiTestDetailFragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener,
        Spinner.OnItemSelectedListener,
        ToughpadApiListener,
        Runnable {

    private ViewGroup grpCfg1, grpCfg2, grpCfg3;
    private CompoundButton btnEnable;
    private Button btnSend;
    private Spinner spnPortList;
    private Spinner spnBaudRate, spnDataBits, spnStopBits, spnParity, spnFlowControl;
    private EditText txtInput, txtOutput;
    private List<SerialPort> serialPorts;
    private SerialPort selectedPort;
    private Thread inputThread;

    public SerialPortTestFragment() {
    }

    private void handleError(final Exception ex) {
        ex.printStackTrace();

        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                builder.setMessage(ex.getMessage());
                builder.setTitle(getString(R.string.title_port_error));
                builder.show();
            }
        });
    }

    private void printLogText(final String text) { 
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                txtInput.append(text);
            }
        });
    }
    
    private void disableEnableControls(ViewGroup vg, boolean enable) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            View child = vg.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                disableEnableControls((ViewGroup) child, enable);
            }
        }
    }
    
    public void run() {
        try {
            InputStream in = selectedPort.getInputStream();
            byte[] buf = new byte[1024];
            while (true) {
                try {
                    int read = in.read(buf);
                    if (read == -1) {
                        // disconnected
                        Log.i(SerialPortTestFragment.class.getName(), "serial port disconnect");
                        break;
                    }
                    // Decode ISO-8859-1
                    String str = new String(buf, 0, read, "ISO-8859-1");
                    printLogText(str);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }
            }
        } catch (IllegalStateException ex) {
            handleError(ex);  
            return;
        }
    }

    private void onSerialPortEnabled() {
        disableEnableControls(grpCfg1, false);
        disableEnableControls(grpCfg2, false);
        disableEnableControls(grpCfg3, false);
        txtInput.setEnabled(true);
        txtOutput.setEnabled(true);
        
        inputThread = new Thread(this, "Serial Port Input");
        inputThread.start();
    }

    private void onSerialPortDisabled() {
        disableEnableControls(grpCfg1, true);
        disableEnableControls(grpCfg2, true);
        disableEnableControls(grpCfg3, true);
        
        txtInput.setEnabled(false);
        txtOutput.setEnabled(false);
        txtInput.setText("");
        txtOutput.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Make sure activity content is panned when the keyboard opens
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // NOTE: Reset state from previous invocations
        serialPorts = null;
        selectedPort = null;

        View view = inflater.inflate(R.layout.fragment_apitest_serial, container, false);

        grpCfg1 = (ViewGroup) view.findViewById(R.id.grpCfg1);
        grpCfg2 = (ViewGroup) view.findViewById(R.id.grpCfg2);
        grpCfg3 = (ViewGroup) view.findViewById(R.id.grpCfg3);

        spnPortList = (Spinner) view.findViewById(R.id.spnPortList);
        btnEnable = (CompoundButton) view.findViewById(R.id.btnEnable);
        btnSend = (Button) view.findViewById(R.id.btnSend);

        spnBaudRate = (Spinner) view.findViewById(R.id.spnBaudRate);
        spnBaudRate.setSelection(6); // 9600b
        
        spnDataBits = (Spinner) view.findViewById(R.id.spnDataBits);
        spnDataBits.setSelection(3); // 8 data bits
        
        // The rest keep defaults at 0 value
        spnStopBits = (Spinner) view.findViewById(R.id.spnStopBits);
        spnParity = (Spinner) view.findViewById(R.id.spnParity);
        spnFlowControl = (Spinner) view.findViewById(R.id.spnFlowCtrl);

        txtInput = (EditText) view.findViewById(R.id.txtInput);
        txtOutput = (EditText) view.findViewById(R.id.txtOutput);
        txtInput.setTypeface(Typeface.MONOSPACE);
        txtOutput.setTypeface(Typeface.MONOSPACE);

        spnPortList.setOnItemSelectedListener(this);
        btnEnable.setOnCheckedChangeListener(this);
        btnSend.setOnClickListener(this);

        onSerialPortDisabled();

        ToughpadApi.initialize(getActivity(), this);

        return view;
    }

    public void onApiConnected(int version) {
        serialPorts = SerialPortManager.getSerialPorts();

        List<String> portNames = new ArrayList<String>();
        for (SerialPort port : serialPorts) {
            portNames.add(port.getDeviceName());
        }

        spnPortList.setAdapter(new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                portNames));
    }
    
    public void onApiDisconnected() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (selectedPort != null && selectedPort.isEnabled()) {
            selectedPort.disable();
        }
        ToughpadApi.destroy();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedPort = serialPorts.get(position);

        try {
            if (selectedPort.isEnabled()) {
                if (selectedPort.getInputStream() == null) {
                    throw new IllegalStateException("Not supposed to happen");
                }
                onSerialPortEnabled();
            } else {
                onSerialPortDisabled();
            }
        } catch (IllegalStateException ex) {
            handleError(ex);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        selectedPort = null;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean checked) {
        if (selectedPort == null) {
            return;
        }

       if (selectedPort.isEnabled() && !checked) {
            try {
                selectedPort.disable();
                onSerialPortDisabled();
            } catch(IllegalStateException ex) {
                handleError(ex);      
            }
        } else if (!selectedPort.isEnabled() && checked) {
            // Get params
            int baudRate;
            switch (Integer.parseInt((String) spnBaudRate.getSelectedItem())) {
                case 300:
                    baudRate = SerialPort.BAUDRATE_300;
                    break;
                case 600:
                    baudRate = SerialPort.BAUDRATE_600;
                    break;
                case 1200:
                    baudRate = SerialPort.BAUDRATE_1200;
                    break;
                case 1800:
                    baudRate = SerialPort.BAUDRATE_1800;
                    break;
                case 2400:
                    baudRate = SerialPort.BAUDRATE_2400;
                    break;
                case 4800:
                    baudRate = SerialPort.BAUDRATE_4800;
                    break;
                case 9600:
                    baudRate = SerialPort.BAUDRATE_9600;
                    break;
                case 19200:
                    baudRate = SerialPort.BAUDRATE_19200;
                    break;
                case 38400:
                    baudRate = SerialPort.BAUDRATE_38400;
                    break;
                case 57600:
                    baudRate = SerialPort.BAUDRATE_57600;
                    break;
                case 115200:
                    baudRate = SerialPort.BAUDRATE_115200;
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized baud rate");
            }

            int dataBits;
            switch (Integer.parseInt((String) spnDataBits.getSelectedItem())) {
                case 5:
                    dataBits = SerialPort.DATASIZE_5;
                    break;
                case 6:
                    dataBits = SerialPort.DATASIZE_6;
                    break;
                case 7:
                    dataBits = SerialPort.DATASIZE_7;
                    break;
                case 8:
                    dataBits = SerialPort.DATASIZE_8;
                    break;
                default:
                    throw new UnsupportedOperationException("Unrecognized data bits");
            }

            int stopBits;
            if (spnStopBits.getSelectedItem().equals("1")) {
                stopBits = SerialPort.STOPBITS_1;
            } else if (spnStopBits.getSelectedItem().equals("2")) {
                stopBits = SerialPort.STOPBITS_2;
            } else {
                throw new UnsupportedOperationException("Unrecognized stop bits");
            }

            int parity;
            String parityStr = (String) spnParity.getSelectedItem();
            if (parityStr.equals("None")) {
                parity = SerialPort.PARITY_NONE;
            } else if (parityStr.equals("Even")) {
                parity = SerialPort.PARITY_EVEN;
            } else if (parityStr.equals("Odd")) {
                parity = SerialPort.PARITY_ODD;
            } else {
                throw new UnsupportedOperationException("Unrecognized parity");
            }

            int flowCtrl;
            String flowCtrlStr = (String) spnFlowControl.getSelectedItem();
            if (flowCtrlStr.equals("None")) {
                flowCtrl = SerialPort.FLOWCONTROL_NONE;
            } else if (flowCtrlStr.equals("RTS/CTS")) {
                flowCtrl = SerialPort.FLOWCONTROL_RTS_CTS;
            } else if (flowCtrlStr.equals("XON/XOFF")) {
                flowCtrl = SerialPort.FLOWCONTROL_XON_XOFF;
            } else {
                throw new UnsupportedOperationException("Unrecognized flow control");
            }

            try {
                selectedPort.enable(baudRate, dataBits, parity, stopBits, flowCtrl);
                onSerialPortEnabled();
            } catch (IOException ex) {
                handleError(ex);
            } catch (IllegalArgumentException ex) {
                handleError(ex);
            } catch (IllegalStateException ex) {
                handleError(ex);      
            } catch (Exception ex) {
                handleError(ex);
            }
        }
    }

    @Override
    public void onClick(View buttonView) {

        try {
            OutputStream out = selectedPort.getOutputStream();
            // Convert to ISO-8859-1
            byte[] latin1 = txtOutput.getText().toString().getBytes("ISO-8859-1");
            out.write(latin1);
        } catch (UnsupportedEncodingException ex) {
            handleError(ex);
        } catch (IOException ex) {
            handleError(ex);
        } catch (IllegalStateException ex) {
            handleError(ex);
        }
    }
}
