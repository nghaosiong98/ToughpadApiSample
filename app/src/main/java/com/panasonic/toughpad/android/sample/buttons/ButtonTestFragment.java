package com.panasonic.toughpad.android.sample.buttons;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.panasonic.toughpad.android.api.ToughpadApi;
import com.panasonic.toughpad.android.api.ToughpadApiListener;
import com.panasonic.toughpad.android.api.appbtn.AppButtonManager;
import com.panasonic.toughpad.android.sample.ApiTestDetailFragment;
import com.panasonic.toughpad.android.sample.R;

public class ButtonTestFragment extends ApiTestDetailFragment implements ToughpadApiListener {

    private boolean isLoaded = false;
    private TextView txtA1, txtA2, txtA3, txtUser, txtSide;
    private static ButtonTestFragment instance;
    
    public static ButtonTestFragment getInstance() {
        return instance;
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
    
    public void updateButtonState(Intent buttonIntent) {
        if (buttonIntent.getAction().equals(AppButtonManager.ACTION_APPBUTTON)) {
            int buttonId = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_BUTTON, 0);
            boolean down = buttonIntent.getIntExtra(AppButtonManager.EXTRA_APPBUTTON_STATE, 0) == AppButtonManager.EXTRA_APPBUTTON_STATE_DOWN;
            updateButtonState(buttonId, down);
        }
    }
    
    private void updateButtonState(final int buttonId, final boolean newState) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                int newBackground;
                if (newState) {
                    newBackground = R.drawable.btn_pressed;
                } else {
                    newBackground = R.drawable.btn_released;
                }
                
                switch (buttonId) {
                    case AppButtonManager.BUTTON_A1:
                        txtA1.setBackgroundResource(newBackground);
                        break;
                    case AppButtonManager.BUTTON_A2:
                        txtA2.setBackgroundResource(newBackground);
                        break;
                    case AppButtonManager.BUTTON_A3:
                        txtA3.setBackgroundResource(newBackground);
                        break;
                    case AppButtonManager.BUTTON_USER:
                        txtUser.setBackgroundResource(newBackground);
                        break;
                    case AppButtonManager.BUTTON_SIDE:
                        txtSide.setBackgroundResource(newBackground);
                        break;
                }
            }
        });
    }
    
    public void onApiConnected(int version) {
        instance = this;
        isLoaded = true;

        try {
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_A1)) {
                txtA1.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_A2)) {
                txtA2.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_A3)) {
                txtA3.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_USER)) {
                txtUser.setVisibility(View.GONE);
            }
            if (!AppButtonManager.isButtonAvailable(AppButtonManager.BUTTON_SIDE)) {
                txtSide.setVisibility(View.GONE);
            }
            // Check if we have control
            if (!AppButtonManager.hasButtonControl()) {
                // We do not have control, request it.
                Intent reconfigureApp = new Intent(Intent.ACTION_MAIN);
                reconfigureApp.addCategory(Intent.CATEGORY_LAUNCHER);
                reconfigureApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                reconfigureApp.setComponent(new ComponentName("com.panasonic.toughpad.android.service",
                                                              "com.panasonic.toughpad.android.appbuttondelegator.ConfigActivity"));
                startActivity(reconfigureApp);
            }
        } catch (final Exception ex) {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setIcon(android.R.drawable.ic_dialog_alert);
                    builder.setMessage(ex.getMessage());
                    builder.setTitle("API Error");
                    builder.show();
                }
            });
        }
    }

    public void onApiDisconnected() {
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apitest_buttons, container, false);
        
        txtA1 = (TextView)view.findViewById(R.id.txtA1);
        txtA2 = (TextView)view.findViewById(R.id.txtA2);
        txtA3 = (TextView)view.findViewById(R.id.txtA3);
        txtUser = (TextView)view.findViewById(R.id.txtUser);
        txtSide = (TextView)view.findViewById(R.id.txtSide);
        
        ToughpadApi.initialize(getActivity(), this);
        
        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        ToughpadApi.destroy();
        
        instance = null;
        isLoaded = false;
    }
}
