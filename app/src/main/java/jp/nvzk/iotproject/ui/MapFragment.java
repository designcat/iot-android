package jp.nvzk.iotproject.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.ui.IconGenerator;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import jp.nvzk.iotproject.Const;
import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.Member;
import jp.nvzk.iotproject.ui.dialog.SingleFragment;
import jp.nvzk.iotproject.util.DistanceUtil;
import jp.nvzk.iotproject.util.ProfileUtil;

/**
 * Created by user on 15/09/19.
 */
public class MapFragment extends Fragment {

    private LayoutInflater inflater;
    private View mView;

    private DateFormat sdf = new SimpleDateFormat("HH:mm:ss");

    private Timer readyTimer = new Timer(true);
    private Timer gameTimer = new Timer(true);

    private GoogleMap mMap;
    private final int zoomLevel = 18;
    private Map<String, Marker> markersList = new HashMap<>();
    private Marker currentMarker;
    private LocationManager locationManager;
    private Location currentLocation;
    private Location preLocation;
    private BitmapDescriptor currentIcon;

    private TextView statusText;
    private TextView pointText;
    private TextView timeText;
    private TextView readyText;
    private int point;

    private static final String READY_TIME = "READY_TIME";

    private boolean isTag = false;

    private onLocationChangeListener mListener;

    private FragmentManager mRetainedChildFragmentManager;

    private Handler mHandler = new Handler();


    public static MapFragment getInstance(int readyTime) {
        MapFragment fragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(READY_TIME, readyTime);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        mView = inflater.inflate(R.layout.fragment_map, container, false);
        return mView;
    }

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        initView();
        readyMap(savedInstanceState);
        //countReadyTimer(getArguments().getInt(READY_TIME));
    }

    private FragmentManager childFragmentManager() {//!!!Use this instead of getFragmentManager, support library from 20+, has a bug that doesn't retain instance of nested fragments!!!!
        if(mRetainedChildFragmentManager == null) {
            mRetainedChildFragmentManager = getChildFragmentManager();
        }
        return mRetainedChildFragmentManager;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (mRetainedChildFragmentManager != null) {
            //restore the last retained child fragment manager to the new
            //created fragment
            try {
                Field childFMField = Fragment.class.getDeclaredField("mChildFragmentManager");
                childFMField.setAccessible(true);
                childFMField.set(this, mRetainedChildFragmentManager);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    @Override
    public void onDestroy(){
        if(readyTimer != null){
            readyTimer.cancel();
        }
        if(gameTimer != null){
            gameTimer.cancel();
        }

        locationManager.removeUpdates(locationListener);
        super.onDestroy();
    }

    private void initView(){
        statusText = (TextView) mView.findViewById(R.id.game_status);
        pointText = (TextView) mView.findViewById(R.id.game_point);
        timeText = (TextView) mView.findViewById(R.id.game_time);
        readyText = (TextView) mView.findViewById(R.id.ready_timer);
    }

    /**
     * 地図準備
     * @param savedInstanceState
     */
    private void readyMap(Bundle savedInstanceState){
        // mapクラス関連の初期化
        SupportMapFragment mapFragment = (SupportMapFragment) childFragmentManager().findFragmentById(R.id.map_fragment);
        // 初期化
        /*if (savedInstanceState == null) {
            // 初期起動
            mapFragment.setRetainInstance(true);
        }*/
        mMap = mapFragment.getMap();

        setUpMapIfNeeded();
    }

    /**
     * 準備時間カウントダウン
     * @param time
     */
    private int tmpTime = 0;
    public void countReadyTimer(int time){
        tmpTime = time;
        readyText.setText(sdf.format(tmpTime));

        readyTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isAdded()) {
                    return;
                }

                if (tmpTime > 0) {
                    tmpTime -= 1000;
                } else if (readyTimer != null) {
                    readyTimer.cancel();
                    readyTimer = null;
                    //TODO サーバ準備できたら消す！
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            startGame(30000, false);
                        }
                    });
                }

                final int finalTmpTime = tmpTime;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        readyText.setText(sdf.format(finalTmpTime));
                    }
                });
            }
        }, 1000, 1000);
    }

    /**
     * ゲーム開始
     * @param time
     * @param isTag
     */
    private int gameTime = 0;
    public void startGame(int time, boolean isTag){
        gameTime = time;
        timeText.setText(sdf.format(gameTime));

        mView.findViewById(R.id.map_overlay_layout).setVisibility(View.GONE);

        if(!isTag){
            statusText.setText(getString(R.string.citizen));
        }
        else{
            statusText.setText(getString(R.string.oni));
            SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_your_turn));
            dialog.show(getActivity().getSupportFragmentManager(), "tag");
        }
        this.isTag = isTag;

        gameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(!isAdded()) {
                    return;
                }

                if (gameTime > 0) {
                    gameTime -= 1000;
                } else if (gameTimer != null) {
                    gameTimer.cancel();
                    gameTimer = null;
                    //TODO Socket通信により終了を受け取るので後で削除
                    ArrayList<Member> memberList = new ArrayList<Member>();
                    Member member = new Member();
                    member.setName(ProfileUtil.getUserName());
                    member.setPoint(point);
                    memberList.add(member);

                    Intent intent = new Intent(getActivity(), RankingActivity.class);
                    intent.putExtra(Const.KEY.MEMBERS, memberList);
                    startActivity(intent);
                    getActivity().finish();
                    //TODO ここまで全部
                }

                final int finalGameTime = gameTime;
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        timeText.setText(sdf.format(finalGameTime));
                    }
                });

                if (preLocation != null && currentLocation != null) {
                    double speed = DistanceUtil.getDistance(currentLocation.getLatitude(), currentLocation.getLongitude(), preLocation.getLatitude(), preLocation.getLongitude());
                    System.out.println(speed);
                    if (currentMarker != null) {
                        currentMarker.remove();
                    }
                    if (0.3d < speed) {
                        currentMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).icon(currentIcon).anchor(0.5f, 0.5f));
                        point += (int) (speed * 10d);
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                pointText.setText(String.valueOf(point));
                            }
                        });
                    }
                }
                preLocation = currentLocation;
            }
        }, 1000, 1000);
    }

    /**
     * メンバーの足跡表示
     * @param item
     */
    public void setFootPrint(Member item){

        if(markersList.containsKey(item.getId())) {
            Marker marker = markersList.get(item.getId());
            marker.remove();
        }

        if(item.getId().equals(ProfileUtil.getUserId())) {
            //TODO pointをサーバで計算する場合はここでセット
            //pointText.setText(String.valueOf(item.getPoint()));
        }
        else if(item.isMoving()) {
            IconGenerator iconGenerator = new IconGenerator(getActivity());
            Bitmap bmp;
            if(isTag) {
                bmp = iconGenerator.makeIcon(item.getName() + " " + item.getPoint() + " pt");
            }
            else{
                bmp = iconGenerator.makeIcon(item.getName());
            }
            Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(item.getGps().getLat(), item.getGps().getLng())).icon(BitmapDescriptorFactory.fromBitmap(bmp)));
            markersList.put(item.getId(), marker);
        }
    }

    /**
     * 鬼交代
     * @param fromUserId
     * @param toUserId
     */
    public void changeTag(String fromUserId, String toUserId){
        if(toUserId.equals(ProfileUtil.getUserId())) {
            statusText.setText(getString(R.string.oni));
            isTag = true;
            SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_your_turn));
            dialog.show(getActivity().getSupportFragmentManager(), "tag");
        }
        else if(fromUserId.equals(ProfileUtil.getUserId())) {
            statusText.setText(getString(R.string.citizen));
            isTag = false;
            SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_change_to_citizen));
            dialog.show(getActivity().getSupportFragmentManager(), "tag");
        }
    }


    /*-----------------
    地図
    ------------------- */

    /**
     * 地図の初期設定
     */
    private void setUpMapIfNeeded() {
        MapsInitializer.initialize(getActivity());
        if (MapsInitializer.initialize(getActivity()) != ConnectionResult.SUCCESS) {
            // Handle the error
            return;
        }

        /**
         * 前回終了時の位置に移動
         */
        double lat = 0;
        double lng = 0;
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE); // 位置マネージャ取得
        Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (loc == null)
            loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (loc != null) {
            lat = loc.getLatitude();
            lng = loc.getLongitude();
        }
        CameraPosition.Builder builder = new CameraPosition.Builder();

        LatLng center = new LatLng(lat, lng);
        builder.target(new LatLng(lat, lng));

        builder.zoom(zoomLevel);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

        //オーバーレイ
        GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                .image(BitmapDescriptorFactory.fromResource(R.drawable.map_overlay))
                .position(center, 10000f, 10000f)
                .transparency(0.7F);
        mMap.addGroundOverlay(newarkMap);

        setUpMap();
    }

    /**
     * 初期マップタイプ、リスナの設定
     */
    private void setUpMap(){
        try{
            mMap.setMyLocationEnabled(false);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(false);

            mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {

                @Override
                public boolean onMarkerClick(Marker marker) {
                    return true;
                }
            });
        }
        catch (Exception ignored){
        }

        IconGenerator iconGenerator = new IconGenerator(getActivity());
        Bitmap bmp;
        if(isTag) {
            bmp = iconGenerator.makeIcon(ProfileUtil.getUserName() + " " + point + " pt");
        }
        else{
            bmp = iconGenerator.makeIcon(ProfileUtil.getUserName());
        }
        currentIcon = BitmapDescriptorFactory.fromResource(R.drawable.green);
        //currentIcon = BitmapDescriptorFactory.fromBitmap(bmp);

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, //LocationManager.NETWORK_PROVIDER,
                1000, // 通知のための最小時間間隔（ミリ秒）
                0, // 通知のための最小距離間隔（メートル）
                locationListener
        );
    }


    /*------------------
    Listener
    ------------------- */

    public void setOnLocationChangeListener(onLocationChangeListener listener){
        mListener = listener;
    }

    public interface onLocationChangeListener {
        public void onLocationChanged(Location location);
    }

    /**
     * 位置情報変更リスナ
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(!isAdded()) {
                return;
            }

            if(mListener != null){
                mListener.onLocationChanged(location);
            }
            if(location != null) {
                currentLocation = location;
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                mMap.animateCamera(cameraUpdate);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


}
