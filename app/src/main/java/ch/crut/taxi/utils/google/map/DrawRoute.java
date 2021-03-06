package ch.crut.taxi.utils.google.map;


import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import ch.crut.taxi.R;
import ch.crut.taxi.TaxiApplication;
import ch.crut.taxi.querymaster.QueryMaster;

public class DrawRoute implements QueryMaster.OnCompleteListener, QueryMaster.OnErrorListener {


    private static final String TAG = "DRAW_ROUTE";

    private static final String ROUTES = "routes";
    private static final String STEPS = "steps";
    private static final String LEGS = "legs";
    private static final String POLYLINE = "polyline";

    private Handler handlePoliline = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            DrawPolyline((ArrayList<LatLng>) msg.obj);
        }
    };
    private GoogleMapUtils googleMapUtils;
    private Context context;

    public DrawRoute(Context context, GoogleMapUtils googleMapUtils) {
        this.googleMapUtils = googleMapUtils;
        this.context = context;
    }

    public void draw(final LatLng origin, final LatLng destination) {
        DrawDirection(origin, destination, "driving");
    }

    public void DrawDirection(final LatLng origin, final LatLng destination, String mode) {

        final String url = "http://maps.googleapis.com/maps/api/directions/json?origin="
                + origin.latitude
                + ","
                + origin.longitude
                + "&destination="
                + destination.latitude
                + ","
                + destination.longitude
                + "&sensor=true&mode=" + mode;


        QueryMaster queryMaster = new QueryMaster(TaxiApplication.getRunningActivityContext(), url, QueryMaster.QUERY_GET);
        queryMaster.setProgressDialog();
        queryMaster.setOnCompleteListener(this);
        queryMaster.setOnErrorListener(this);
        queryMaster.start();

    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();

        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        Message msg = new Message();
        msg.obj = poly;

        handlePoliline.sendMessage(msg);

        return poly;
    }

    public void DrawPolyline(ArrayList<LatLng> latLngList) {
        PolylineOptions options = new PolylineOptions().width(5)
                .color(Color.BLUE).geodesic(false);
        for (LatLng latLng : latLngList) {
            options.add(latLng);
        }
        googleMapUtils.getMap().addPolyline(options);
    }

    @Override
    public void QMcomplete(JSONObject jsonObject) throws JSONException {
        JSONArray jRoutes = jsonObject.getJSONArray(ROUTES);

        if (jRoutes.length() > 0) {

            JSONObject jRoutesObj = jRoutes.getJSONObject(0);
            JSONArray jLegsArr = jRoutesObj.getJSONArray(LEGS);
            JSONObject jLegsObj = jLegsArr.getJSONObject(0);
            JSONArray jStepsArr = jLegsObj.getJSONArray(STEPS);
            final int length = jStepsArr.length();
            for (int i = 0; i < length; i++) {
                JSONObject jOneStep = jStepsArr.getJSONObject(i);
                JSONObject jPolylineObj = jOneStep
                        .getJSONObject(POLYLINE);

                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                decodePoly(jPolylineObj.getString("points"));
            }
        }
    }

    @Override
    public void QMerror(int errorCode) {
        if (errorCode == QueryMaster.QM_SERVER_ERROR) {
            QueryMaster.toast(context, R.string.error_server_connection);
        } else if (errorCode == QueryMaster.QM_NETWORK_ERROR) {
            QueryMaster.toast(context, R.string.error_network_unavailable);
        } else if (errorCode == QueryMaster.QM_INVALID_JSON) {
            QueryMaster.toast(context, R.string.error_invalid_json);
        }
    }
}
