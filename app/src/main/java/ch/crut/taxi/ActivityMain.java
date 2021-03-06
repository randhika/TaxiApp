package ch.crut.taxi;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;

import org.androidannotations.annotations.App;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.WindowFeature;

import java.util.ArrayList;

import ch.crut.taxi.fragmenthelper.FragmentHelper;
import ch.crut.taxi.fragments.FragmentTaxiBooking;
import ch.crut.taxi.interfaces.OnPlaceSelectedListener;
import ch.crut.taxi.querymaster.QueryMaster;
import ch.crut.taxi.utils.NavigationPoint;
import ch.crut.taxi.utils.TaxiBookingHelper;
import ch.crut.taxi.utils.UserLocation;
import ch.crut.taxi.utils.WishObjects;
import ch.crut.taxi.utils.actionbar.NBConnector;
import ch.crut.taxi.utils.actionbar.NBController;
import ch.crut.taxi.utils.actionbar.NBItemSelector;
import ch.crut.taxi.utils.actionbar.NBItems;
import ch.crut.taxi.utils.actionbar.UINotificationBar;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


@WindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY)
@EActivity(R.layout.activity_main)
public class ActivityMain extends FragmentActivity implements OnPlaceSelectedListener, QueryMaster.OnErrorListener,
        NBConnector, NBItemSelector {


    //    public static final int MAP_CONTAINER = R.id.activityMainMap;
    public static final int FRAME_CONTAINER = R.id.activityMainContainer;


    private FragmentManager fragmentManager = getSupportFragmentManager();
    //    private SupportMapFragment supportMapFragment;
    private NBController NBController;
    private FragmentTaxiBooking fragmentTaxiBooking;
    private TaxiBookingHelper taxiBookingHelper;

    private UserLocation userLocation;

    private ArrayList<WishObjects> userWishes;

    // getters
//    public NBController getNBController() {
//        return NBController;
//    }

    public TaxiBookingHelper getTaxiBookingHelper() {
        return taxiBookingHelper;
    }

    @App
    protected TaxiApplication taxiApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UINotificationBar uiNotificationBar = new UINotificationBar(getActionBar());

        NBController = new NBController(uiNotificationBar);
//        setActionBarDefaultListener();
//
        fragmentTaxiBooking = FragmentTaxiBooking.newInstance();
//        supportMapFragment = SupportMapFragment.newInstance();
        taxiBookingHelper = new TaxiBookingHelper();


        userWishes = new ArrayList<WishObjects>();

//        FragmentHelper.add(fragmentManager, FragmentAuthorization.newInstance(), FRAME_CONTAINER);
//        FragmentHelper.add(fragmentManager, supportMapFragment, MAP_CONTAINER);
        FragmentHelper.add(fragmentManager, fragmentTaxiBooking, FRAME_CONTAINER);
    }

    @Override
    protected void onResume() {
        super.onResume();
        taxiApplication.setCurrentActivity(this);

        userLocation = new UserLocation(this);
        userLocation.update();
    }

    @Override
    protected void onPause() {
        clearReferences();
        userLocation.destroy();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

//    @Override
//    public void onBackPressed() {
//        if (FragmentHelper.pop(fragmentManager, 2))
//            super.onBackPressed();
//    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(new CalligraphyContextWrapper(newBase));
    }

    public void add(Fragment fragment) {
        FragmentHelper.add(fragmentManager, fragment, FRAME_CONTAINER);
    }

    public void addNoReplace(Fragment fragment) {
        FragmentHelper.addNoReplace(fragmentManager, fragment, FRAME_CONTAINER);
    }

    public void initialScreen() {
        FragmentHelper.clear(fragmentManager, 1);
    }

    @Override
    public void placeSelected(NavigationPoint navigationPoint, PlaceSelectedKeys selectedKey) {
        if (selectedKey == PlaceSelectedKeys.DESTINATION) {
            taxiBookingHelper.destination = navigationPoint;
        } else {
            taxiBookingHelper.original = navigationPoint;
        }
    }

    private void clearReferences() {
        Activity currActivity = taxiApplication.getCurrentActivity();
        if (currActivity != null && currActivity.equals(this))
            taxiApplication.setCurrentActivity(null);
    }

    @Override
    public void QMerror(int errorCode) {
        if (errorCode == QueryMaster.QM_SERVER_ERROR) {
            QueryMaster.toast(this, R.string.error_server_connection);
        } else if (errorCode == QueryMaster.QM_NETWORK_ERROR) {
            QueryMaster.toast(this, R.string.error_network_unavailable);
        } else if (errorCode == QueryMaster.QM_INVALID_JSON) {
            QueryMaster.toast(this, R.string.error_invalid_json);
        }
    }

    @Override
    public NBController nbConnected() {
        return NBController;
    }

    @Override
    public void NBItemSelected(int id) {
        switch (id) {
            case NBItems.BACK:
                FragmentHelper.pop(fragmentManager);
                break;
        }
//        QueryMaster.toast(this, ActivityMain.this.toString() + ", " + String.valueOf(id));
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View view = activity.getCurrentFocus();

        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    public void setUserWishes(ArrayList<WishObjects> userWishes) {
        this.userWishes = userWishes;
    }

    public ArrayList<WishObjects> getUserWishes() {
        return userWishes;
    }

}

