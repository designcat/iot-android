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
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jp.nvzk.iotproject.Const;
import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.Sensor;
import jp.nvzk.iotproject.ui.adapter.DeviceListAdapter;
import jp.nvzk.iotproject.ui.dialog.SingleFragment;
import jp.nvzk.iotproject.util.ProfileUtil;


public class MainActivity extends AppCompatActivity {

    private final static int SDKVER_LOLLIPOP = 21;
    private final static int REQUEST_ENABLE_BT = 100;

    private static byte[] bleByteDataFirst;
    private static byte[] bleByteDataSecond;

    private static final int MESSAGE_FIRST = 0;
    private static final int MESSAGE_SECOND = 1;

    private BluetoothManager mBleManager;
    private static BluetoothAdapter mBleAdapter;
    private BluetoothLeScanner mBleScanner;
    private static BluetoothGatt mBleGattFirst;
    private static BluetoothGatt mBleGattSecond;

    private Button reselectBtn;
    private static Button decideBtn;
    private static TextView selectedTextFirst;
    private static TextView selectedTextSecond;

    private static ListView deviceListViewFirst;
    private DeviceListAdapter deviceListFirstAdapter;
    private List<BluetoothDevice> deviceListFirst = new ArrayList<>();
    private static ListView deviceListViewSecond;
    private DeviceListAdapter deviceListSecondAdapter;
    private List<BluetoothDevice> deviceListSecond = new ArrayList<>();

    private static boolean isSetLeft = false;
    private static boolean isSetRight = false;

    private AlertDialog gpsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // デバイスがBLEに対応しているかを確認する.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            // BLEに対応していない旨のToastやダイアログを表示する.
            SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_unavailable_bluetooth));
            dialog.setCloseListener(new SingleFragment.OnCloseListener() {
                @Override
                public void onClose() {
                    finish();
                }
            });
            dialog.show(getSupportFragmentManager(), "dialog");
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
        decideBtn.setEnabled(true);

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

        if(ProfileUtil.haveDeviceLeft()){
            deviceListFirst.add(ProfileUtil.getBluetoothDeviceLeft());
            deviceListFirstAdapter.notifyDataSetChanged();
            deviceListSecond.add(ProfileUtil.getBluetoothDeviceLeft());
            deviceListSecondAdapter.notifyDataSetChanged();
        }
        if(ProfileUtil.haveDeviceRight()){
            deviceListFirst.add(ProfileUtil.getBluetoothDeviceRight());
            deviceListFirstAdapter.notifyDataSetChanged();
            deviceListSecond.add(ProfileUtil.getBluetoothDeviceRight());
            deviceListSecondAdapter.notifyDataSetChanged();
        }


        checkBluetooth();
        checkGPS();
    }

    @Override
    protected void onPause(){
        reset();
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
                    finish();
                }
                break;
        }
    }

    private void reset(){
        deviceListViewFirst.setVisibility(View.VISIBLE);
        deviceListViewSecond.setVisibility(View.VISIBLE);
        selectedTextFirst.setVisibility(View.GONE);
        selectedTextSecond.setVisibility(View.GONE);
        isSetRight = false;
        isSetLeft = false;
        //TODO !!
        decideBtn.setEnabled(true);

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

        if(mBleGattFirst != null) {
            mBleGattFirst.close();
            mBleGattFirst = null;
        }

        if(mBleGattSecond != null) {
            mBleGattSecond.close();
            mBleGattSecond = null;
        }
    }


    /**
     * GPSの確認
     */
    private void checkGPS(){
        LocationManager nlLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!nlLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setMessage("GPSが有効になっていません。\n有効化しますか？")
                    .setCancelable(false)

                            //GPS設定画面起動用ボタンとイベントの定義
                    .setPositiveButton("GPS設定起動",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(callGPSSettingIntent);
                                    dialog.dismiss();
                                    gpsDialog = null;
                                }
                            });
            //キャンセルボタン処理
            alertDialogBuilder.setNegativeButton("キャンセル",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            finish();
                        }
                    });
            if(gpsDialog == null) {
                gpsDialog = alertDialogBuilder.create();
                // 設定画面へ移動するかの問い合わせダイアログを表示
                gpsDialog.show();
            }
        }
        else {
            if (gpsDialog != null) {
                gpsDialog.dismiss();
                gpsDialog = null;
            }
        }
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
                SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_connect_device));
                dialog.show(getSupportFragmentManager(), "connect");
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
                bleByteDataFirst = characteristic.getValue();

                if(bleByteDataFirst != null) {
                    // メインスレッドでTextViewに値をセットする.
                    mBleHandler.sendEmptyMessage(MESSAGE_FIRST);
                }
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
                SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_connect_device));
                dialog.show(getSupportFragmentManager(), "connect");
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
                bleByteDataSecond = characteristic.getValue();

                if(bleByteDataSecond != null) {
                    // メインスレッドでTextViewに値をセットする.
                    mBleHandler.sendEmptyMessage(MESSAGE_SECOND);
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
    private Handler mBleHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            Sensor sensor = new Sensor();
            try {
                sensor = sensor.getNewSensor(bleByteDataFirst);
                //sensor.setSensor(bleByteDataFirst);
                System.out.println(sensor.getSide());
            }
            catch (Exception e){
                return;
            }

            switch (msg.what){
                case MESSAGE_FIRST:

                    switch(sensor.getSide()){
                        case 0:
                            System.out.println("左");
                            if(!isSetLeft){
                                isSetLeft = true;
                                selectedTextFirst.setText(mBleGattFirst.getDevice().getName() + "\n" + getResources().getString(R.string.select_left));
                                ProfileUtil.setBluetoothDeviceLeft(mBleGattFirst.getDevice());
                            }
                            else {
                                SingleFragment check = (SingleFragment)getSupportFragmentManager().findFragmentByTag("already");
                                if(check == null) {
                                    SingleFragment dialog = SingleFragment.getInstance(getResources().getString(R.string.dialog_error_already_set));
                                    dialog.show(getSupportFragmentManager(), "already");
                                }
                                if(mBleGattFirst != null) {
                                    mBleGattFirst.close();
                                    //mBleGattFirst = null;
                                }
                                return;
                            }
                            break;
                        case 1:
                            System.out.println("右");
                            if(!isSetRight){
                                isSetRight = true;
                                selectedTextFirst.setText(mBleGattFirst.getDevice().getName() + "\n" + getString(R.string.select_right));
                                ProfileUtil.setBluetoothDeviceRight(mBleGattFirst.getDevice());
                            }
                            else{
                                SingleFragment check = (SingleFragment)getSupportFragmentManager().findFragmentByTag("already");
                                if(check == null) {
                                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_already_set));
                                    dialog.show(getSupportFragmentManager(), "already");
                                }
                                if(mBleGattFirst != null) {
                                    mBleGattFirst.close();
                                    //mBleGattFirst = null;
                                }
                                return;
                            }

                            break;
                        default:
                            SingleFragment check = (SingleFragment)getSupportFragmentManager().findFragmentByTag("device");
                            if(check == null) {
                                SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_unavailable_device));
                                dialog.show(getSupportFragmentManager(), "device");
                            }
                            if(mBleGattFirst != null) {
                                mBleGattFirst.close();
                                //mBleGattFirst = null;
                            }
                            return;
                    }
                    selectedTextFirst.setVisibility(View.VISIBLE);
                    deviceListViewFirst.setVisibility(View.GONE);

                    mBleGattFirst.close();
                    //mBleGattFirst = null;
                    break;
                case MESSAGE_SECOND:
                    try {
                        sensor.setSensor(bleByteDataSecond);
                    }
                    catch (Exception e){
                        return;
                    }
                    switch(sensor.getSide()){
                        case 0:
                            System.out.println("左");
                            if(!isSetLeft){
                                selectedTextSecond.setText(mBleGattSecond.getDevice().getName() + "\n" + getString(R.string.select_left));
                                isSetLeft = true;
                                ProfileUtil.setBluetoothDeviceLeft(mBleGattSecond.getDevice());
                            }
                            else {
                                SingleFragment check = (SingleFragment)getSupportFragmentManager().findFragmentByTag("already");
                                if(check == null) {
                                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_already_set));
                                    dialog.show(getSupportFragmentManager(), "already");
                                }
                                if(mBleGattSecond != null) {
                                    mBleGattSecond.close();
                                    //mBleGattSecond = null;
                                }
                                return;
                            }

                            break;
                        case 1:
                            System.out.println("右");
                            if(!isSetRight){
                                selectedTextSecond.setText(mBleGattSecond.getDevice().getName() + "\n" + getString(R.string.select_right));
                                isSetRight = true;
                                ProfileUtil.setBluetoothDeviceRight(mBleGattSecond.getDevice());
                            }
                            else{
                                SingleFragment check = (SingleFragment)getSupportFragmentManager().findFragmentByTag("already");
                                if(check == null) {
                                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_already_set));
                                    dialog.show(getSupportFragmentManager(), "already");
                                }
                                if(mBleGattSecond != null) {
                                    mBleGattSecond.close();
                                    //mBleGattSecond = null;
                                }
                                return;
                            }

                            break;
                        default:
                            SingleFragment check = (SingleFragment)getSupportFragmentManager().findFragmentByTag("device");
                            if(check == null) {
                                SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_unavailable_device));
                                dialog.show(getSupportFragmentManager(), "device");
                            }
                            if(mBleGattSecond != null) {
                                mBleGattSecond.close();
                                //mBleGattSecond = null;
                            }
                            return;

                    }
                    selectedTextSecond.setVisibility(View.VISIBLE);
                    deviceListViewSecond.setVisibility(View.GONE);

                    mBleGattSecond.close();
                    //mBleGattSecond = null;
                    break;
            }

            if(isSetLeft && isSetRight){
                decideBtn.setEnabled(true);
            }
        }
    };



    /**
     * デバイスリスト(上)から選択した時のリスナ
     */
    private AdapterView.OnItemClickListener onDeviceFirstItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(mBleGattFirst != null) {
                mBleGattFirst.close();
                mBleGattFirst = null;
            }

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
            if(mBleGattSecond != null) {
                mBleGattSecond.close();
                mBleGattSecond = null;
            }

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
            reset();
            onResume();
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
