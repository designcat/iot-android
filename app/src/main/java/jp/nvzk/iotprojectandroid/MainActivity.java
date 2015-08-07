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
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    //BLESerial サービスUUID
    public static String UUID_BLESERIAL_SERVICE = "569a1101-b87F-490c-92cb-11ba5ea5167c";
    //　BLESerial　受信UUID （Notify)
    public static String UUID_BLESERIAL_RX = "569a2000-b87F-490c-92cb-11ba5ea5167c";
    // BLESerial 送信UUID （write without response)
    public static String UUID_BLESERIAL_TX = "569a2001-b87F-490c-92cb-11ba5ea5167c";
    //　キャラクタリスティック設定UUID
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

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
    private String mStrSendNum = "";

    private Socket socket;

    private ListView deviceListView;
    private DeviceListAdapter deviceListAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // デバイスがBLEに対応しているかを確認する.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // BLEに対応していない旨のToastやダイアログを表示する.
            finish();
        }

        deviceListView = (ListView)findViewById(R.id.main_list_view);
        deviceListAdapter = new DeviceListAdapter(this, deviceList);
        deviceListView.setAdapter(deviceListAdapter);
        deviceListView.setOnItemClickListener(onDeviceItemClickListener);

        checkBluetooth();
    }

    @Override
    protected void onDestroy()
    {
        if (Build.VERSION.SDK_INT >= SDKVER_LOLLIPOP)
        {
            mBleScanner.stopScan(mScanCallbackUp);
        }
        else
        {
            mBleAdapter.stopLeScan((BluetoothAdapter.LeScanCallback) this);
        }

        // 画面遷移時は通信を切断する.
        if(mBleGatt != null) {
            mBleGatt.close();
            mBleGatt = null;
        }
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent date){
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){
                    scanNewDevice();
                }
                else{
                    finish();
                }
                break;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * bluetoothの準備確認
     */
    private void checkBluetooth(){
        mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBleAdapter = mBleManager.getAdapter();

        // BluetoothがOffならインテントを表示する.
        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            // Intentでボタンを押すとonActivityResultが実行されるので、第二引数の番号を元に処理を行う.
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        else
        {
            // BLEが使用可能ならスキャン開始.
            scanNewDevice();
        }
    }

    /**
     * スキャンスタート
     */
    private void scanNewDevice()
    {
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

    @Override
    public void onPause(){
        super.onPause();
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
                System.out.println(result.getDevice().getName());
                //TODO BLESerialに限定
                for(BluetoothDevice item: deviceList) {
                    if(item.getAddress().equals(device.getAddress())){
                        return;
                    }
                }
                deviceList.add(device);
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
                    System.out.println(device.getName());
                    //TODO BLESerialに限定
                    for(BluetoothDevice item: deviceList) {
                        if(item.getAddress().equals(device.getAddress())){
                            return;
                        }
                    }
                    deviceList.add(device);
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
                if (mBleGatt != null)
                {
                    mBleGatt.close();
                    mBleGatt = null;
                }
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            // Serviceが見つかったら実行.
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // UUIDが同じかどうかを確認する.
                BluetoothGattService service = gatt.getService(UUID.fromString(UUID_BLESERIAL_SERVICE));
                if (service != null)
                {
                    // 指定したUUIDを持つCharacteristicを確認する.
                    mBleCharacteristic = service.getCharacteristic(UUID.fromString(UUID_BLESERIAL_RX));

                    if (mBleCharacteristic != null) {
                        // Service, CharacteristicのUUIDが同じならBluetoothGattを更新する.
                        mBleGatt = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト.
                        boolean registered = mBleGatt.setCharacteristicNotification(mBleCharacteristic, true);

                        // Characteristic の Notificationを有効化する.
                        BluetoothGattDescriptor descriptor = mBleCharacteristic.getDescriptor(
                                UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));

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
            System.out.println(characteristic.getUuid().toString().toUpperCase());
            // キャラクタリスティックのUUIDをチェック(getUuidの結果が全て小文字で帰ってくるのでUpperCaseに変換)
            if (UUID_BLESERIAL_RX.equals(characteristic.getUuid().toString().toUpperCase()))
            {
                // Peripheralで値が更新されたらNotificationを受ける.
                mStrReceivedNum = characteristic.getStringValue(0);
                // メインスレッドでTextViewに値をセットする.
                mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);
            }
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
                    TextView receiveText = (TextView)findViewById(R.id.main_receive);
                    receiveText.setText(mStrReceivedNum);
                    break;
                case MESSAGE_NEW_SENDNUM:
                    break;
            }
        }
    };

    /**
     *
     */
    private void initSocket(){
        try {
            socket = IO.socket("http://localhost");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                socket.emit("foo", "hi");
                socket.disconnect();
            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        });
        socket.connect();
    }

    private void sendSocket(){
        // Sending an object
        JSONObject obj = new JSONObject();
        try {
            obj.put("hello", "server");
            obj.put("binary", new byte[42]);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit("foo", obj);
    }

    private void receiveSocket(){
        // Receiving an object
        socket.on("foo", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject obj = (JSONObject)args[0];
            }
        });
    }


    /**
     * デバイスリストから選択した時のリスナ
     */
    private AdapterView.OnItemClickListener onDeviceItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceList.get(position);
            mBleGatt = device.connectGatt(getApplicationContext(), false, mGattCallback);
        }
    };
}
