package ch.crut.taxi.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.crut.taxi.ActivityMain;
import ch.crut.taxi.R;
import ch.crut.taxi.actionbar.ActionBarController;
import ch.crut.taxi.fragmenthelper.FragmentHelper;
import ch.crut.taxi.interfaces.ActionBarClickListener;
import ch.crut.taxi.querymaster.QueryMaster;
import ch.crut.taxi.utils.GoogleMapUtils;
import ch.crut.taxi.utils.LocationAddress;
import ch.crut.taxi.utils.NavigationPoint;
import ch.crut.taxi.utils.TaxiBookingHelper;
import pl.charmas.android.reactivelocation.ReactiveLocationProvider;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;

@EFragment(R.layout.fragment_from_where)
public class FragmentFromWhere extends Fragment implements ActionBarClickListener {

//    public static final String AUTO_LOCATION = "AUTO_LOCATION";

    private static final int FRAME_CONTAINER = R.id.fragmentFromWhereFrameLayout;

//    private boolean autoLocation;

    private GoogleMapUtils mapUtils;
    private LocationAddress locationAddress;
    private ActionBarController barController;
    private TaxiBookingHelper taxiBookingHelper;

    private LatLng selectedPosition;
    private SupportMapFragment mapFragment;


    public static FragmentFromWhere newInstance() {
        FragmentFromWhere fragmentFromWhere = new FragmentFromWhere_();
        Bundle bundle = new Bundle();
        fragmentFromWhere.setArguments(bundle);
        return fragmentFromWhere;
    }

    @ViewById(R.id.fragmentFromWhereAddress)
    protected EditText address;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

//        Bundle bundle = getArguments();

//        autoLocation = bundle.getBoolean(AUTO_LOCATION);

        final ActivityMain activityMain = (ActivityMain) getActivity();
        activityMain.replaceActionBarClickListener(this);

        barController = activityMain.getActionBarController();
        taxiBookingHelper = ((ActivityMain) activity).getTaxiBookingHelper();
        mapFragment = SupportMapFragment.newInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        barController.title(getString(R.string.elaboration));
        barController.cancelEnabled(true);
        barController.doneEnabled(true);

        FragmentHelper.add(getChildFragmentManager(), mapFragment, FRAME_CONTAINER);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                GoogleMap map = mapFragment.getMap();
                map.setOnMapClickListener(onMapClick);

                mapUtils = new GoogleMapUtils(map);
            }
        }, 100);

    }

    @Override
    public void onPause() {
        super.onPause();

        barController.cancelEnabled(false);
        barController.doneEnabled(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((ActivityMain) getActivity()).setActionBarDefaultListener();
    }

    private LocationAddress.OnCompleteListener onAddressFound = new LocationAddress.OnCompleteListener() {
        @Override
        public void complete(List<Address> addresses) {
            final Address userAddress = addresses.get(0);

            address.setText(userAddress.getAddressLine(0)
                    + ", " + userAddress.getAddressLine(1));
        }

        @Override
        public void error() {
            QueryMaster.toast(getActivity(), R.string.fail_getting_address);
        }
    };


    private GoogleMap.OnMapClickListener onMapClick = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng latLng) {
            selectedPosition = latLng;

            mapUtils.getMap().clear();
            mapUtils.me(latLng);

            locationAddress = new LocationAddress(getActivity(), latLng);
            locationAddress.setOnCompleteListener(onAddressFound);
            locationAddress.start();
        }
    };

    @Override
    public void clickSettings(View view) {

    }

    @Override
    public void clickBack(View view) {

    }

    @Override
    public void clickCancel(View view) {

    }

    @Override
    public void clickDone(View view) {
        NavigationPoint navigationPoint = new NavigationPoint();
        navigationPoint.address = this.address.getText().toString();
        navigationPoint.latLng = selectedPosition;

        taxiBookingHelper.original = navigationPoint;

        ((ActivityMain) getActivity()).initialScreen();
    }
}
