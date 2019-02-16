package com.panasonic.toughpad.android.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.panasonic.toughpad.android.sample.bcr.BarcodeTestFragment;
import com.panasonic.toughpad.android.sample.buttons.ButtonTestFragment;
import com.panasonic.toughpad.android.sample.msr.MagstripeTestFragment;
import com.panasonic.toughpad.android.sample.serial.SerialPortTestFragment;

import java.util.HashMap;

public class ApiTestDetailFragment extends Fragment {
    
	private static final String API_TEST_ID = "test_id";
	private static final String NAME_STR_ID = "name_str_id";
	private static final String ICON_RES_ID = "icon_res_id";
	
    public static final ApiTestDetailFragment[] API_TESTS = new ApiTestDetailFragment[] {
    	newInstance(BarcodeTestFragment.class, "bcr", R.drawable.barcode, R.string.sample_barcode),
    	newInstance(MagstripeTestFragment.class, "msr", R.drawable.magstripe, R.string.sample_magstripe),
    	newInstance(ButtonTestFragment.class, "buttons", R.drawable.button, R.string.sample_appbutton),
    	newInstance(SerialPortTestFragment.class, "serialport", R.drawable.serial, R.string.sample_serialport)
    };

    /*
    newInstance(UsbGadgetTestFragment.class, "ports", R.drawable.ports, R.string.sample_ports),
    newInstance(CradleTestFragment.class, "cradle", R.drawable.device, R.string.sample_cradle),
    */
    
    public static final HashMap<String, ApiTestDetailFragment> API_TESTS_MAP = new HashMap<String, ApiTestDetailFragment>();
    
    static {
        for (ApiTestDetailFragment fragment : API_TESTS) {
            API_TESTS_MAP.put(fragment.getApiTestId(), fragment);
        }
    }
    
    public ApiTestDetailFragment(){
    }
    
    public static ApiTestDetailFragment newInstance(Class<? extends ApiTestDetailFragment> subclass, String testId, int iconResId, int nameResId) {
    	ApiTestDetailFragment instance;
		try {
			instance = subclass.newInstance();
		} catch (Exception ex) {
			// Should never happen.
			throw new RuntimeException(ex);
		}
    	Bundle bundle = new Bundle();
    	bundle.putString(API_TEST_ID, testId);
    	bundle.putInt(ICON_RES_ID, iconResId);
    	bundle.putInt(NAME_STR_ID, nameResId);
    	instance.setArguments(bundle);
    	return instance;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        TextView view = new TextView(inflater.getContext());
        view.setText(inflater.getContext().getString(R.string.lbl_under_construction));
        return view;
    }
    
    public int getNameStringId() {
    	return getArguments().getInt(NAME_STR_ID);
    }

    public int getIconResourceId() {
    	return getArguments().getInt(ICON_RES_ID);
    }

    public String getApiTestId() {
    	return getArguments().getString(API_TEST_ID);
    }
}
