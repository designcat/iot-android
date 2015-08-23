package jp.nvzk.iotprojectandroid.ui;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.nvzk.iotprojectandroid.Const;
import jp.nvzk.iotprojectandroid.ui.adapter.DeviceListAdapter;
import jp.nvzk.iotprojectandroid.R;
import jp.nvzk.iotprojectandroid.model.Sensor;
import jp.nvzk.iotprojectandroid.util.ProfileUtil;


public class MainActivity extends AppCompatActivity {

    private final static int SDKVER_LOLLIPOP = 21;
    private final static int REQUEST_ENABLE_BT = 100;

    private BluetoothManager mBleManager;
    private BluetoothAdapter mBleAdapter;
    private BluetoothLeScanner mBleScanner;
    private BluetoothGatt mBleGattFirst;
    private BluetoothGatt mBleGattSecond;

    private Button reselectBtn;
    private Button decideBtn;
    private TextView selectedTextFirst;
    private TextView selectedTextSecond;

    private ListView deviceListViewFirst;
    private DeviceListAdapter deviceListFirstAdapter;
    private List<BluetoothDevice> deviceListFirst = new ArrayList<>();
    private ListView deviceListViewSecond;
    private DeviceListAdapter deviceListSecondAdapter;
    private List<BluetoothDevice> deviceListSecond = new ArrayList<>();

    private boolean isSetLeft = false;
    private boolean isSetRight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // デバイスがBLEに対応しているかを確認する.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // BLEに対応していない旨のToastやダイアログを表示する.
            //TODO ダイアログ
            finish();
        }

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.title_bluetooth);

        deviceListViewFirst = (ListView)findViewById(R.id.main_list_view1);
        deviceListFirstAdapter = new DeviceListAdapter(this, deviceListFirst);
        deviceListViewFirst.setAdapter(deviceListFirstAdapter);
        deviceListViewFirst.setOnItemClickListener(onDeviceFirstItemClickListener);

        deviceListViewSecond = (ListView)findViewById(R.id.main_list_view2);
        deviceListSecondAdapter = new DeviceListAdapter(this, deviceListSecond);
        deviceListViewSecond.setAdapter(deviceListSecondAdapter);
        deviceListViewSecond.setOnItemClickListener(onDeviceSecondItemClickListener);

        reselectBtn = (Button) findViewById(R.id.main_reselect_btn);
        reselectBtn.setOnClickListener(mOnReselectClickListener);
        decideBtn = (Button) findViewById(R.id.main_decide_btn);
        decideBtn.setOnClickListener(mOnDecideClickListener);
        decideBtn.setEnabled(false);

        selectedTextFirst = (TextView)findViewById(R.id.main_select_text1);
        selectedTextSecond = (TextView)findViewById(R.id.main_select_text2);

    }

    @Override
    protected void onResume(){
        super.onResume();

        deviceListFirst.clear();
        deviceListFirstAdapter.notifyDataSetChanged();
        deviceListSecond.clear();
        deviceListSecondAdapter.notifyDataSetChanged();

        checkBluetooth();
        checkGPS();
    }

    @Override
    protected void onPause()
    {
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

        // 画面遷移時は通信を切断する.
        if(mBleGattFirst != null) {
            mBleGattFirst.close();
            mBleGattFirst = null;
        }
        if(mBleGattSecond != null) {
            mBleGattSecond.close();
            mBleGattSecond = null;
        }
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent date){
        switch (requestCode){
            case REQUEST_ENABLE_BT:
                if(resultCode == RESULT_OK){
                    scanNewDevice();
                }
                else{
                    //TODO リタイヤとみなす
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
     * TODO GPSの確認
     */
    private void checkGPS(){

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
        // デバイスの検出
        mBleScanner.startScan(mScanCallbackUp = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                BluetoothDevice device = result.getDevice();

                if(device.getName() == null){
                    return;
                }
                //TODO 限定する
                for(BluetoothDevice item: deviceListFirst) {
                    if(item.getAddress().equals(device.getAddress())){
                        return;
                    }
                }

                deviceListFirst.add(device);
                deviceListFirstAdapter.notifyDataSetChanged();
                deviceListSecond.add(device);
                deviceListSecondAdapter.notifyDataSetChanged();
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

                    //TODO 限定
                    for(BluetoothDevice item: deviceListFirst) {
                        if(item.getAddress().equals(device.getAddress())){
                            return;
                        }
                    }
                    deviceListFirst.add(device);
                    deviceListFirstAdapter.notifyDataSetChanged();
                    deviceListSecond.add(device);
                    deviceListSecondAdapter.notifyDataSetChanged();
                }
            });
        }
    };


    /**
     * 接続のコールバックを得る(上)
     */
    private final BluetoothGattCallback mGattCallbackFirst = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索する.
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続が切れたらGATTを空にする.
                if (mBleGattFirst != null)
                {
                    mBleGattFirst.close();
                    mBleGattFirst = null;
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
                        mBleGattFirst = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト.
                        boolean registered = mBleGattFirst.setCharacteristicNotification(mBleCharacteristic, true);

                        // Characteristic の Notificationを有効化する.
                        BluetoothGattDescriptor descriptor = mBleCharacteristic.getDescriptor(
                                UUID.fromString(Const.CLIENT_CHARACTERISTIC_CONFIG));

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBleGattFirst.writeDescriptor(descriptor);

                        //デバイス決定、保存
                        final BluetoothDevice device = gatt.getDevice();
                        final byte[] bleByteData = mBleCharacteristic.getValue();
                        Handler mHandler = new Handler();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Sensor sensor = new Sensor();
                                sensor.setSensor(bleByteData);

                                switch(sensor.getSide()){
                                    case 0:
                                        if(isSetLeft) {
                                            //TODO 既に設定されています。
                                            return;
                                        }
                                        selectedTextFirst.setText(device.getName() + "\n" + getString(R.string.select_left));
                                        isSetLeft = true;
                                        ProfileUtil.setBluetoothDeviceLeft(device);
                                        break;
                                    case 1:
                                        if(isSetRight){
                                            //TODO 既に設定されています。
                                            return;
                                        }
                                        selectedTextFirst.setText(device.getName() + "\n" + getString(R.string.select_right));
                                        isSetRight = true;
                                        ProfileUtil.setBluetoothDeviceRight(device);
                                        break;
                                }
                                selectedTextFirst.setVisibility(View.VISIBLE);
                                deviceListViewFirst.setVisibility(View.GONE);

                                if(isSetLeft && isSetRight){
                                    decideBtn.setEnabled(true);
                                }
                            }
                        });

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
            if (Const.UUID_BLESERIAL_RX.toUpperCase().equals(characteristic.getUuid().toString().toUpperCase())){
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }
    };

    /**
     * 接続のコールバックを得る(下)
     */
    private final BluetoothGattCallback mGattCallbackSecond = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            // 接続状況が変化したら実行.
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // 接続に成功したらサービスを検索する.
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // 接続が切れたらGATTを空にする.
                if (mBleGattSecond != null)
                {
                    mBleGattSecond.close();
                    mBleGattSecond = null;
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
                        mBleGattSecond = gatt;

                        // キャラクタリスティックが見つかったら、Notificationをリクエスト.
                        boolean registered = mBleGattSecond.setCharacteristicNotification(mBleCharacteristic, true);

                        // Characteristic の Notificationを有効化する.
                        BluetoothGattDescriptor descriptor = mBleCharacteristic.getDescriptor(
                                UUID.fromString(Const.CLIENT_CHARACTERISTIC_CONFIG));

                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBleGattSecond.writeDescriptor(descriptor);

                        //デバイス決定、保存
                        final BluetoothDevice device = gatt.getDevice();
                        final byte[] bleByteData = mBleCharacteristic.getValue();
                        Handler mHandler = new Handler();
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Sensor sensor = new Sensor();
                                sensor.setSensor(bleByteData);

                                switch(sensor.getSide()){
                                    case 0:
                                        if(isSetLeft) {
                                            //TODO 既に設定されています。
                                            return;
                                        }
                                        selectedTextSecond.setText(device.getName() + "\n" + getString(R.string.select_left));
                                        isSetLeft = true;
                                        ProfileUtil.setBluetoothDeviceLeft(device);
                                        break;
                                    case 1:
                                        if(isSetRight){
                                            //TODO 既に設定されています。
                                            return;
                                        }
                                        selectedTextSecond.setText(device.getName() + "\n" + getString(R.string.select_right));
                                        isSetRight = true;
                                        ProfileUtil.setBluetoothDeviceRight(device);
                                        break;
                                }
                                selectedTextSecond.setVisibility(View.VISIBLE);
                                deviceListViewSecond.setVisibility(View.GONE);

                                if(isSetLeft && isSetRight){
                                    decideBtn.setEnabled(true);
                                }
                            }
                        });

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
            if (Const.UUID_BLESERIAL_RX.toUpperCase().equals(characteristic.getUuid().toString().toUpperCase())){
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {

        }
    };



    /**
     * デバイスリスト(上)から選択した時のリスナ
     */
    private AdapterView.OnItemClickListener onDeviceFirstItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceListFirst.get(position);
            mBleGattFirst = device.connectGatt(getApplicationContext(), false, mGattCallbackFirst);
        }
    };

    /**
     * デバイスリスト(下)から選択した時のリスナ
     */
    private AdapterView.OnItemClickListener onDeviceSecondItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice device = deviceListSecond.get(position);
            mBleGattSecond = device.connectGatt(getApplicationContext(), false, mGattCallbackSecond);
        }
    };


    /**
     * 再設定のボタンリスナ
     */
    private View.OnClickListener mOnReselectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            deviceListViewFirst.setVisibility(View.VISIBLE);
            deviceListViewSecond.setVisibility(View.VISIBLE);
            selectedTextFirst.setVisibility(View.GONE);
            selectedTextSecond.setVisibility(View.GONE);
            isSetRight = false;
            isSetLeft = false;
            decideBtn.setEnabled(false);

        }
    };

    /**
     * 決定ボタンのリスナ
     */
    private View.OnClickListener mOnDecideClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    };
}
