package com.panasonic.toughpad.android.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;


/**
 * An activity representing a list of ApiTests. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ApiTestDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ApiTestListFragment} and the item details
 * (if present) is a {@link ApiTestDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link ApiTestListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class ApiTestListActivity extends FragmentActivity
        implements ApiTestListFragment.Callbacks {

    public static final String ARG_ITEM_ID = "item_id";
    public static final String ACTION_ITEM_ID = ARG_ITEM_ID;
    
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Toughpad API Samples");
        setContentView(R.layout.activity_apitest_list);

        if (findViewById(R.id.apitest_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.
            ((ApiTestListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.apitest_list))
                    .setActivateOnItemClick(true);
        }

        // TODO: If exposing deep links into your app, handle intents here.
        if (getIntent().hasExtra(ACTION_ITEM_ID)) {
            onItemSelected(getIntent().getStringExtra(ACTION_ITEM_ID));
        }
    }

    /**
     * Callback method from {@link ApiTestListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            ApiTestDetailFragment fragment = ApiTestDetailFragment.API_TESTS_MAP.get(id);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.apitest_detail_container, fragment)
                    .commit();

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, ApiTestDetailActivity.class);
            detailIntent.putExtra(ApiTestListActivity.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
