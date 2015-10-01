package jp.nvzk.iotproject.ui;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jp.nvzk.iotproject.Const;
import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.GPS;
import jp.nvzk.iotproject.model.Member;
import jp.nvzk.iotproject.model.MyData;
import jp.nvzk.iotproject.model.MyDevice;
import jp.nvzk.iotproject.model.Sensor;
import jp.nvzk.iotproject.ui.dialog.SimpleFragment;
import jp.nvzk.iotproject.ui.dialog.SingleFragment;
import jp.nvzk.iotproject.util.ProfileUtil;
import jp.nvzk.iotproject.util.SocketUtil;

/**
 * Created by user on 15/08/10.
 */
public class GameActivity extends AppCompatActivity {

    private Location currentLocation;
    private double tagRange = 10;

    private Socket socket;

    private final static int SDKVER_LOLLIPOP = 21;
    private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
    private final static int MESSAGE_NEW_SENDNUM = 1;
    private final static int REQUEST_ENABLE_BT = 100;

    private BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBleGattLeft;
    private BluetoothGatt mBleGattRight;
    private String mStrReceivedNum = "";
    private String mStrSendNum = "";

    private MemberFragment memberFragment;
    private MapFragment mapFragment;
    private SimpleFragment gpsFragment;
    private SimpleFragment deviceFragment;

    //TODO !!
    private boolean isConnect = false;
    private boolean connectRight;
    //TODO !!
    private boolean connectLeft = true;
    private boolean isStart = false;

    private int roomId;

    private String[] cannotSendMacAddress = new String[2];

    private mBleHandler mBleHandler = new mBleHandler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        roomId = getIntent().getIntExtra(Const.KEY.ROOM, 0);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        initView();

        checkGPS();
        checkBluetooth();
    }

    @Override
    protected void onDestroy(){
        if(socket != null) {
            socket.disconnect();
        }

        stopScan();

        // 画面遷移時は通信を切断する.
        if(mBleGattLeft != null) {
            mBleGattLeft.close();
            mBleGattLeft = null;
        }
        if(mBleGattRight != null) {
            mBleGattRight.close();
            mBleGattRight = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction()==KeyEvent.ACTION_DOWN) {
            switch (event.getKeyCode()) {
                case KeyEvent.KEYCODE_BACK:
                    finish();
                    return false;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public void initView(){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        memberFragment = new MemberFragment();
        fragmentTransaction.add(R.id.game_root_layout, memberFragment);
        fragmentTransaction.commit();
        memberFragment.setOnStartClickListener(mOnStartClickListener);

        gpsFragment = SimpleFragment.getInstance(getString(R.string.dialog_ready_gps));
        gpsFragment.setCancelable(false);
        gpsFragment.show(getSupportFragmentManager(), "gps");
    }

    /**
     * GPSの確認
     */
    private void checkGPS(){
        LocationManager nlLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!nlLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            finish();
        }
    }


    /*------------------------
    Socket
    ------------------------- */

    /**
     * socket.ioの準備
     */
    private void initSocket(){
        if(!connectRight || !connectLeft){
            return;
        }
        if(deviceFragment != null) {
            deviceFragment.dismiss();
            deviceFragment = null;
        }

        socket = SocketUtil.getSocket();

        socket.on("connected", onConnected
        ).on("startTimer", onStartTimer
        ).on("start", onStart
        ).on("disconnect", onDisconnectMember
        ).on("login", onReceiveMemberList
        ).on("footprint", onReceiveSensor
        ).on("tag", onReceiveTag
        ).on("finish", onFinishSocket
        ).on("disconnected", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                finish();
            }

        });

        socket.connect();

    }

    /**
     * 接続時
     */
    private Emitter.Listener onConnected = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //入室した部屋IDとUserIdを送信
            MyData data = new MyData();
            data.setUserId(ProfileUtil.getUserId());
            data.setName(ProfileUtil.getUserName());
            data.setRoomId(roomId);
            socket.emit("room", data);

            isConnect = true;
        }
    };

    /**
     * メンバー入室時
     */
    private Emitter.Listener onReceiveMemberList = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson gson = new Gson();
                    Type collectionType = new TypeToken<Collection<Member>>() {
                    }.getType();
                    List<Member> members = gson.fromJson(new Gson().toJson(data), collectionType);
                    if(memberFragment != null) {
                        memberFragment.setList(members);
                    }

                }
            });
        }
    };

    /**
     * メンバー退室時
     */
    private Emitter.Listener onDisconnectMember = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson gson = new Gson();
                    Member member = gson.fromJson(new Gson().toJson(data), Member.class);
                    if(memberFragment != null) {
                        memberFragment.removeMember(member);
                    }
                }
            });
        }
    };

    /**
     * スタートボタンが押されたとき
     */
    private Emitter.Listener onStartTimer = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    JSONObject data = (JSONObject) args[0];
                    int time = 30000;
                    try {
                        time = data.getInt("time");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    mapFragment = MapFragment.getInstance(time);
                    fragmentTransaction.add(R.id.game_root_layout, mapFragment);
                    fragmentTransaction.commit();

                    mapFragment.setOnLocationChangeListener(mOnLocationChangeListener);

                    memberFragment = null;
                }
            });
        }
    };

    /**
     * ゲーム開始時
     */
    private Emitter.Listener onStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //鬼を振り分けた結果をもらう
                    JSONObject data = (JSONObject) args[0];

                    try {
                        int gameTime = data.getInt("gameTime");
                        boolean isTag = data.getBoolean("isTag");
                        mapFragment.startGame(gameTime, isTag);
                        isStart = true;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    };

    /**
     * 他メンバーのfootprint受信時
     */
    private Emitter.Listener onReceiveSensor = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isStart) {
                        JSONObject data = (JSONObject) args[0];
                        Gson gson = new Gson();
                        Member item = gson.fromJson(new Gson().toJson(data), Member.class);

                        mapFragment.setFootPrint(item);
                    }
                }
            });
        }
    };

    /**
     * 鬼交代情報受信時
     */
    private Emitter.Listener onReceiveTag = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(isStart) {
                        JSONObject data = (JSONObject) args[0];
                        try {
                            String fromUserId = data.getString("fromUserId");
                            String toUserId = data.getString("toUserId");
                            if (toUserId.equals(ProfileUtil.getUserId())) {
                                String addressLeft = data.getString("fromUserMacAddressLeft");
                                String addressRight = data.getString("fromUserMacAddressRight");
                                cannotSendMacAddress[0] = addressLeft;
                                cannotSendMacAddress[1] = addressRight;
                                scanNewDevice();
                            } else {
                                cannotSendMacAddress = new String[2];
                            }

                            mapFragment.changeTag(fromUserId, toUserId);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        }
    };

    /**
     * ゲーム終了受信時
     */
    private Emitter.Listener onFinishSocket = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];
                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<Member>>() {}.getType();
                ArrayList<Member> members = gson.fromJson(new Gson().toJson(data), collectionType);
                Intent intent = new Intent(GameActivity.this, RankingActivity.class);
                intent.putExtra(Const.KEY.MEMBERS, members);
                startActivity(intent);
                finish();
        }
    };


    /*-------------------------
    Bleutooth
    ----------------------- */

    /**
     * bluetoothの準備確認
     */
    private void checkBluetooth(){
        mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();

        // BluetoothがOffならリタイヤ.
        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            finish();
        }
        else {
            // BLEが使用可能ならスキャン開始.
            scanNewDevice();
            //TODO デバイスと接続開始 左右のセンサがセットされていないとエラーになるのでとりあえずコメントアウト
            //mBleGattLeft = ProfileUtil.getBluetoothDeviceLeft().connectGatt(getApplicationContext(), false, mGattCallbackLeft);
            //mBleGattRight = ProfileUtil.getBluetoothDeviceRight().connectGatt(getApplicationContext(), false, mGattCallbackRight);
            deviceFragment = SimpleFragment.getInstance(getString(R.string.dialog_ready_device));
            deviceFragment.setCancelable(false);
            deviceFragment.show(getSupportFragmentManager(), "ble");
        }
    }

    /**
     * スキャンスタート
     */
    private void scanNewDevice(){

        // OS ver.5.0以上ならBluetoothLeScannerを使用する.
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP)
        {
            startScanByBleScanner();
        }
        else
        {
            // デバイスの検出.
            mBleAdapter.startLeScan(mScanCallbackUnder);
        }
    }

    /**
     * スキャンストップ
     */
    private void stopScan(){
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP)
        {
            if(mBleAdapter != null && mBleAdapter.isEnabled() && mBleScanner != null) {
                mBleScanner.stopScan(mScanCallbackUp);
            }
        }
        else
        {
            if(mBleAdapter != null && mBleAdapter.isEnabled()) {
                mBleAdapter.stopLeScan(mScanCallbackUnder);
            }
        }
    }


    private ScanCallback mScanCallbackUp;
    @TargetApi(SDKVER_LOLLIPOP)
    private void startScanByBleScanner(){
        mBleScanner = mBleAdapter.getBluetoothLeScanner();
        // デバイスの検出.
        mBleScanner.startScan(mScanCallbackUp = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();

                if(device.getName() == null){
                    return;
                }
                if(result.getRssi() < tagRange && !device.getAddress().equals(cannotSendMacAddress[0]) && !device.getAddress().equals(cannotSendMacAddress[1])) {
                    socket.emit("tag", device.getAddress());
                    stopScan();
                }
            }
            @Override
            public void onScanFailed(int intErrorCode)
            {
                super.onScanFailed(intErrorCode);
            }
        });
    }

    /**
     * スキャンのコールバックを得る
     */
    private final BluetoothAdapter.LeScanCallback mScanCallbackUnder = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(device.getName() == null){
                        return;
                    }
                    if(rssi < tagRange && !device.getAddress().equals(cannotSendMacAddress[0]) && !device.getAddress().equals(cannotSendMacAddress[1])) {
                        socket.emit("tag", device.getAddress());
                        stopScan();
                    }
                }
            });
        }
    };


    /**
     * 接続のコールバックを得る（左）
     */
    private final BluetoothGattCallback mGattCallbackLeft = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索する.
                gatt.discoverServices();
                if(!connectLeft) {
                    connectLeft = true;
                    initSocket();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                System.out.println("unConnect");
                // 接続が切れたらGATTを空にする.
                if (mBleGattLeft != null)
                {
                    mBleGattLeft.close();
                    mBleGattLeft = null;
                }
                if(!connectLeft){
                    //接続失敗
                    if(deviceFragment != null){
                        deviceFragment.dismiss();
                        deviceFragment = null;
                    }
                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_connect_device));
                    dialog.setCloseListener(new SingleFragment.OnCloseListener() {
                        @Override
                        public void onClose() {
                            finish();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "connect");
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            // Serviceが見つかったら実行.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // UUIDが同じかどうかを確認する.
                BluetoothGattService service = gatt.getService(UUID.fromString(Const.UUID_BLESERIAL_SERVICE));
                if (service != null)
                {
                    // 指定したUUIDを持つCharacteristicを確認する.
                    BluetoothGattCharacteristic mBleCharacteristic = service.getCharacteristic(UUID.fromString(Const.UUID_BLESERIAL_RX));

                    if (mBleCharacteristic != null) {
                        // Service, CharacteristicのUUIDが同じならBluetoothGattを更新する.
                        mBleGattLeft = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト.
                        boolean registered = mBleGattLeft.setCharacteristicNotification(mBleCharacteristic, true);

                        // Characteristic の Notificationを有効化する.
                        BluetoothGattDescriptor descriptor = mBleCharacteristic.getDescriptor(
                                UUID.fromString(Const.CLIENT_CHARACTERISTIC_CONFIG));

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBleGattLeft.writeDescriptor(descriptor);

                        if (registered) {
                            // Characteristics通知設定が成功
                        } else {
                            // Characteristics通知設定が失敗
                        }
                    }
                }
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            // キャラクタリスティックのUUIDをチェック(getUuidの結果が全て小文字で帰ってくるのでUpperCaseに変換)
            if (Const.UUID_BLESERIAL_RX.toUpperCase().equals(characteristic.getUuid().toString().toUpperCase()))
            {
                byte[] bleByteData = characteristic.getValue();
                System.out.println(bleByteData[1]);
                // Peripheralで値が更新されたらNotificationを受ける.
                mStrReceivedNum = characteristic.getStringValue(0);
                // メインスレッドでTextViewに値をセットする.
                mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);

                if(isConnect && currentLocation != null) {
                    Sensor sensor = new Sensor();
                    try {
                        sensor.setSensor(bleByteData);
                    }
                    catch (Exception e) {
                        return;
                    }
                    sendFootprint(sensor, 0);
                }

            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }
    };


    /**
     * 接続のコールバックを得る（右）
     */
    private final BluetoothGattCallback mGattCallbackRight = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {

            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                System.out.println("Connect");
                // 接続に成功したらサービスを検索する.
                gatt.discoverServices();
                if(!connectRight) {
                    connectRight = true;
                    initSocket();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                System.out.println("unConnect");
                // 接続が切れたらGATTを空にする.
                if (mBleGattRight != null) {
                    mBleGattRight.close();
                    mBleGattRight = null;
                }
                if(!connectRight){
                    //接続失敗
                    if(deviceFragment != null){
                        deviceFragment.dismiss();
                        deviceFragment = null;
                    }
                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_connect_device));
                    dialog.setCloseListener(new SingleFragment.OnCloseListener() {
                        @Override
                        public void onClose() {
                            finish();
                        }
                    });
                    dialog.show(getSupportFragmentManager(), "connect");
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            // Serviceが見つかったら実行.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // UUIDが同じかどうかを確認する.
                BluetoothGattService service = gatt.getService(UUID.fromString(Const.UUID_BLESERIAL_SERVICE));
                if (service != null)
                {
                    System.out.println("discoverd");
                    // 指定したUUIDを持つCharacteristicを確認する.
                    BluetoothGattCharacteristic mBleCharacteristic = service.getCharacteristic(UUID.fromString(Const.UUID_BLESERIAL_RX));

                    System.out.println(mBleCharacteristic);

                    if (mBleCharacteristic != null) {
                        // Service, CharacteristicのUUIDが同じならBluetoothGattを更新する.
                        mBleGattRight = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト.
                        boolean registered = mBleGattRight.setCharacteristicNotification(mBleCharacteristic, true);

                        // Characteristic の Notificationを有効化する.
                        BluetoothGattDescriptor descriptor = mBleCharacteristic.getDescriptor(
                                UUID.fromString(Const.CLIENT_CHARACTERISTIC_CONFIG));

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBleGattRight.writeDescriptor(descriptor);

                        if (registered) {
                            // Characteristics通知設定が成功
                        } else {
                            // Characteristics通知設定が失敗
                        }
                    }
                }
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
        {
            // キャラクタリスティックのUUIDをチェック(getUuidの結果が全て小文字で帰ってくるのでUpperCaseに変換)
            if (Const.UUID_BLESERIAL_RX.toUpperCase().equals(characteristic.getUuid().toString().toUpperCase()))
            {
                byte[] bleByteData = characteristic.getValue();
                // Peripheralで値が更新されたらNotificationを受ける.
                mStrReceivedNum = characteristic.getStringValue(0);
                // メインスレッドでTextViewに値をセットする.
                mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);

                if(isConnect) {

                    Sensor sensor = new Sensor();
                    try {
                        sensor.setSensor(bleByteData);
                    }
                    catch (Exception e) {
                        return;
                    }
                    sendFootprint(sensor, 1);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }
    };



    /**
     * キャラクタリスティックの受信に応じてUIスレッド処理
     */
    private class mBleHandler extends Handler {
        public void handleMessage(Message msg){
            // UIスレッドで実行する処理.
            switch (msg.what)
            {
                case MESSAGE_NEW_RECEIVEDNUM:
                    //TODO 特にないかも

                    break;
                case MESSAGE_NEW_SENDNUM:
                    break;
            }
        }
    };

    /**
     * footprint情報を送信
     * @param sensor
     * @param side
     */
    private void sendFootprint(Sensor sensor, int side){
        GPS gps = new GPS();
        gps.setLocation(currentLocation);
        MyDevice myDevice = new MyDevice();
        myDevice.setSide(side);
        myDevice.setSensor(sensor);
        myDevice.setGps(gps);
        Gson gson = new Gson();
        String str = gson.toJson(myDevice);
        System.out.println(str);
        try {
            JSONObject jsonObject = new JSONObject(str);
            System.out.println(jsonObject.get("gps").toString());
            socket.emit("footprint", jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }



    /*--------------------
    Listener
    ---------------------- */


    /**
     * スタートボタンクリックリスナ
     */
    private MemberFragment.onStartClickListener mOnStartClickListener = new MemberFragment.onStartClickListener() {
        @Override
        public void onStartClicked() {
            //TODO なにかしら開始合図を送る
            socket.emit("startTimer", 1);
        }
    };

    /**
     * GPS変更時
     */
    private MapFragment.onLocationChangeListener mOnLocationChangeListener = new MapFragment.onLocationChangeListener() {
        @Override
        public void onLocationChanged(Location location) {
            if(gpsFragment != null){
                gpsFragment.dismiss();
                gpsFragment = null;
            }
            currentLocation = location;
        }
    };


}
