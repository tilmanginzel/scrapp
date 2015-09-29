package de.medieninf.mobcomp.scrapp.view;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.medieninf.mobcomp.scrapp.R;
import de.medieninf.mobcomp.scrapp.rest.service.RestServiceHelper;
import de.medieninf.mobcomp.scrapp.util.Config;
import de.medieninf.mobcomp.scrapp.view.adapter.DrawerMenuItemAdapter;

/**
 * MainActivity which is the entry point for the app.
 * A NavigationDrawer is used switch its content with fragments.
 */
public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle drawerToggle;
    private ListView lvDrawerMenu;
    private DrawerMenuItemAdapter drawerMenuItemAdapter;

    private TextView tvRegistered;

    private RestServiceHelper restServiceHelper;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener sharedPreferencesListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restServiceHelper = new RestServiceHelper(this);
        sharedPreferences = getSharedPreferences(Config.USER_PREFERENCES, 0);

        // initialize toolbar
        initToolbar();

        // initialize navigation drawer
        initNavigationDrawer();

        sharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Config.PREF_KEY_IDENTITY_TOKEN)) {
                    setRegisteredSinceText();
                    restServiceHelper.getRules();
                }
            }
        };
        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferencesListener);

        // default page is "my subscriptions"
        if (savedInstanceState == null) {
            setFragment(1, SubscriptionFragment.class);
            getSupportActionBar().setTitle(R.string.menu_my_subscriptions);
        }

        // register user
        registerAppUser();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferencesListener);
    }

    /**
     * Init toolbar.
     */
    private void initToolbar() {
        // set the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    /**
     * Create navigation drawer.
     */
    private void initNavigationDrawer() {
        // find drawer and list view layout
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        lvDrawerMenu = (ListView) findViewById(R.id.lv_drawer_menu);

        // add the header to the list view
        RelativeLayout header = (RelativeLayout) View.inflate(this, R.layout.drawer_menu_header, null);
        lvDrawerMenu.addHeaderView(header);
        tvRegistered = (TextView) header.findViewById(R.id.tv_registered);

        // create menu items and set the adapter
        List<DrawerMenuItem> menuItems = createMenuItems();
        drawerMenuItemAdapter = new DrawerMenuItemAdapter(getApplicationContext(), menuItems);
        lvDrawerMenu.setAdapter(drawerMenuItemAdapter);
        lvDrawerMenu.setOnItemClickListener(this);

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer) {
            @Override
            public void onDrawerClosed(View view) {
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                invalidateOptionsMenu();
            }
        };
        drawerLayout.setDrawerListener(drawerToggle);
    }

    /**
     * Set the text for the 'registered since' TextView.
     */
    private void setRegisteredSinceText() {
        Date date = new Date(sharedPreferences.getLong(Config.PREF_KEY_REGISTERED_SINCE, 0));
        SimpleDateFormat format = new SimpleDateFormat("dd. MMM yyyy, HH:mm", Locale.GERMANY);

        String registeredSince = getResources().getString(R.string.registered_since);
        String formattedDate = format.format(date);
        String clock = getResources().getString(R.string.clock);
        tvRegistered.setText(registeredSince + ":\n" +  formattedDate + " " + clock);
    }

    /**
     * Registers the app user.
     */
    private void registerAppUser() {
        String identityToken = sharedPreferences.getString(Config.PREF_KEY_IDENTITY_TOKEN, null);

        if (identityToken == null) {
            restServiceHelper.createUser();
            tvRegistered.setText(R.string.registration_pending);
        } else {
            setRegisteredSinceText();
            restServiceHelper.getRules();
        }
    }

    /**
     * Create a list with all menu items.
     *
     * @return list with DrawerMenuItem objects
     */
    private List<DrawerMenuItem> createMenuItems() {
        List<DrawerMenuItem> items = new ArrayList<>();

        // my subscriptions
        DrawerMenuItem item1 = new DrawerMenuItem();
        item1.setIcon(R.drawable.ic_home_black_24dp);
        item1.setTitle(getResources().getString(R.string.menu_my_subscriptions));
        items.add(item1);

        // all rules
        DrawerMenuItem item2 = new DrawerMenuItem();
        item2.setIcon(R.drawable.ic_list_black_24dp);
        item2.setTitle(getResources().getString(R.string.menu_all_rules));
        items.add(item2);

        return items;
    }

    /**
     * Switches the current fragment with a new one.
     *
     * @param position menu position
     * @param fragmentClass fragment class
     */
    private void setFragment(int position, Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_container, fragment, fragmentClass.getSimpleName());
            fragmentTransaction.commit();

            lvDrawerMenu.setItemChecked(position, true);
            drawerLayout.closeDrawer(lvDrawerMenu);
            lvDrawerMenu.invalidateViews();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // ids start at 1 because the header has id 0
        switch(position) {
            case 1:
                setFragment(1, SubscriptionFragment.class);
                toolbar.setTitle(R.string.menu_my_subscriptions);
                break;
            case 2:
                setFragment(2, RulesFragment.class);
                toolbar.setTitle(R.string.menu_all_rules);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(lvDrawerMenu)) {
            drawerLayout.closeDrawer(lvDrawerMenu);
        }
        super.onBackPressed();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }
}
