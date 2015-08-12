package jp.nvzk.iotprojectandroid;

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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import jp.nvzk.iotprojectandroid.model.Member;
import jp.nvzk.iotprojectandroid.model.MyData;
import jp.nvzk.iotprojectandroid.model.Sensor;
import jp.nvzk.iotprojectandroid.util.ProfileUtil;
import jp.nvzk.iotprojectandroid.util.SocketUtil;

/**
 * Created by user on 15/08/10.
 */
public class MapActivity extends AppCompatActivity {
    private Socket socket;
    private boolean startFlag;
    private MyData myData;
    private Sensor sensor;

    private final static int SDKVER_LOLLIPOP = 21;
    private final static int MESSAGE_NEW_RECEIVEDNUM = 0;
    private final static int MESSAGE_NEW_SENDNUM = 1;
    private final static int REQUEST_ENABLE_BT = 100;

    private BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBleGatt;
    private BluetoothGattCharacteristic mBleCharacteristic;
    private String mStrReceivedNum = "";
    private byte[] bleByteData;
    private String mStrSendNum = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initView();
        initSocket();

        checkGPS();
        checkBluetooth();
    }

    private void initView(){

    }

    @Override
    protected void onDestroy()
    {
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP)
        {
            if(mBleScanner != null) {
                mBleScanner.stopScan(mScanCallbackUp);
            }
        }
        else
        {
            if(mBleAdapter != null && mBleAdapter.isEnabled()) {
                mBleAdapter.stopLeScan(mScanCallbackUnder);
            }
        }

        // 画面遷移時は通信を切断する.
        if(mBleGatt != null) {
            mBleGatt.close();
            mBleGatt = null;
        }
        super.onDestroy();
    }

    /**
     * socket.ioの準備
     */
    private void initSocket(){

        socket = SocketUtil.getSocket();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                //TODO readyの合図を送る
                MyData data = new MyData();
                data.setId(ProfileUtil.getUserId());
                sendSocket(data);
            }

        }).on("timer", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //TODO timer START

            }
        }).on("start", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
               startFlag = true;
            }
        }).on("event", onReceive
        ).on("finish", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //TODO finishを受け取ったら、ゲーム終了
                JSONObject data = (JSONObject) args[0];
                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<Member>>() {
                }.getType();
                ArrayList<Member> members = gson.fromJson(new Gson().toJson(data), collectionType);
                Intent intent = new Intent(MapActivity.this, RankingActivity.class);
                intent.putExtra(Const.KEY.MEMBERS, members);
                startActivity(intent);
                finish();
            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                //TODO 接続がきれましたダイアログ
                Intent intent = new Intent(MapActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        });

        socket.connect();

    }


    private void sendSocket(MyData myData){
        //Gson gson = new Gson();
        socket.emit("event", myData);
    }

    private Emitter.Listener onReceive = new Emitter.Listener() {
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
                    //TODO 地図表示
                }
            });
        }
    };

    /**
     * GPSの確認
     */
    private void checkGPS(){

    }

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
        else
        {
            myData = new MyData();
            myData.setId(ProfileUtil.getUserId());
            sensor = new Sensor();
            // BLEが使用可能ならスキャン開始.
            scanNewDevice();
            //デバイスと接続開始
            mBleGatt = ProfileUtil.getBluetoothDevice().connectGatt(getApplicationContext(), false, mGattCallback);
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


    private ScanCallback mScanCallbackUp;
    @TargetApi(SDKVER_LOLLIPOP)
    private void startScanByBleScanner()
    {
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
                //TODO rssiによって判定
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
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(device.getName() == null){
                        return;
                    }
                    //TODO rssiによって判定
                }
            });
        }
    };


    /**
     * 接続のコールバックを得る
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索する.
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続が切れたらGATTを空にする.
                /*if (mBleGatt != null)
                {
                    mBleGatt.close();
                    mBleGatt = null;
                }*/
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
                    mBleCharacteristic = service.getCharacteristic(UUID.fromString(Const.UUID_BLESERIAL_RX));

                    if (mBleCharacteristic != null) {
                        // Service, CharacteristicのUUIDが同じならBluetoothGattを更新する.
                        mBleGatt = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト.
                        boolean registered = mBleGatt.setCharacteristicNotification(mBleCharacteristic, true);

                        // Characteristic の Notificationを有効化する.
                        BluetoothGattDescriptor descriptor = mBleCharacteristic.getDescriptor(
                                UUID.fromString(Const.CLIENT_CHARACTERISTIC_CONFIG));

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBleGatt.writeDescriptor(descriptor);

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
                bleByteData = characteristic.getValue();
                // Peripheralで値が更新されたらNotificationを受ける.
                mStrReceivedNum = characteristic.getStringValue(0);
                // メインスレッドでTextViewに値をセットする.
                mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);

                sensor.setSensor(bleByteData);

                //TODO GPSも　あとでこれごとGPSのchangeListenerに持っていく
                if(startFlag) {
                    myData.setSensor(sensor);
                    sendSocket(myData);
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
    private Handler mBleHandler = new Handler()
    {
        public void handleMessage(Message msg)
        {
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

}
