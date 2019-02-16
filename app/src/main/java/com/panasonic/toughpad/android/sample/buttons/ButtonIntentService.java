package com.panasonic.toughpad.android.sample.buttons;

import android.app.IntentService;
import android.content.Intent;
import com.panasonic.toughpad.android.api.appbtn.AppButtonManager;
import com.panasonic.toughpad.android.sample.ApiTestListActivity;

public class ButtonIntentService extends IntentService {

    public ButtonIntentService() {
        super("Button Intent Handler Thread");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!intent.getAction().equals(AppButtonManager.ACTION_APPBUTTON)) {
            // Ignore..
            return;
        }

        if (ButtonTestFragment.getInstance() != null) {
            ButtonTestFragment.getInstance().updateButtonState(intent);
        } else {
            Intent launchIntent = new Intent(getBaseContext(), ApiTestListActivity.class);
            launchIntent.setAction(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            launchIntent.putExtra(ApiTestListActivity.ACTION_ITEM_ID, "buttons");
            getApplication().startActivity(launchIntent);
        }
    }
}
