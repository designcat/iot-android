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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import jp.nvzk.iotprojectandroid.Const;
import jp.nvzk.iotprojectandroid.ui.adapter.MemberListAdapter;
import jp.nvzk.iotprojectandroid.R;
import jp.nvzk.iotprojectandroid.model.Member;
import jp.nvzk.iotprojectandroid.model.MyData;
import jp.nvzk.iotprojectandroid.model.MyDevice;
import jp.nvzk.iotprojectandroid.model.Sensor;
import jp.nvzk.iotprojectandroid.util.ProfileUtil;
import jp.nvzk.iotprojectandroid.util.SocketUtil;

/**
 * Created by user on 15/08/10.
 */
public class GameActivity extends AppCompatActivity {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private long readyTime = 30000;
    private long gameTime = 10 * 60 * 1000;

    private Timer readyTimer = new Timer(true);
    private Timer gameTimer = new Timer(true);

    private Socket socket;
    private boolean startFlag;
    private Sensor sensor;

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

    private Button startBtn;
    private ListView memberListView;
    private List<Member> memberList = new ArrayList<>();
    private MemberListAdapter memberListAdapter;

    private TextView statusText;
    private TextView pointText;
    private TextView timeText;
    private TextView readyText;

    private boolean isTag = false;

    private int roomId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        roomId = getIntent().getIntExtra(Const.KEY.ROOM, 0);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.title_member);

        initView();
        initSocket();

        checkGPS();
        checkBluetooth();
    }

    @Override
    protected void onResume(){
        super.onResume();
        //TODO 接続確認 だめだったら戻る
    }

    @Override
    protected  void onDestroy(){
        if(readyTimer != null){
            readyTimer.cancel();
        }
        if(gameTimer != null){
            gameTimer.cancel();
        }

        socket.disconnect();

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

    public void initView(){
        startBtn = (Button) findViewById(R.id.member_start_btn);
        memberListView = (ListView) findViewById(R.id.member_list_view);
        memberListAdapter = new MemberListAdapter(this, memberList);
        memberListView.setAdapter(memberListAdapter);

        statusText = (TextView) findViewById(R.id.map_status);
        pointText = (TextView) findViewById(R.id.map_point);
        timeText = (TextView) findViewById(R.id.map_time);
        readyText = (TextView) findViewById(R.id.ready_timer);

    }

    /**
     * GPSの確認
     */
    private void checkGPS(){

    }

    /*------------------------
    Socket
    ------------------------- */

    /**
     * socket.ioの準備
     */
    private void initSocket(){

        socket = SocketUtil.getSocket();

        socket.on("connected", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                //入室した部屋IDとUserIdを送信
                MyData data = new MyData();
                data.setUserId(ProfileUtil.getUserId());
                data.setRoomId(roomId);
                socket.emit("room", data);
            }

        }).on("startTimer", onStartTimer
        ).on("start", onStart
        ).on("disconnect", onDisconnectMember
        ).on("login", onReceiveMember
        ).on("event", onReceive
        ).on("finish", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //TODO finishを受け取ったら、ゲーム終了
                JSONObject data = (JSONObject) args[0];
                Gson gson = new Gson();
                Type collectionType = new TypeToken<Collection<Member>>() {
                }.getType();
                ArrayList<Member> members = gson.fromJson(new Gson().toJson(data), collectionType);
                Intent intent = new Intent(GameActivity.this, RankingActivity.class);
                intent.putExtra(Const.KEY.MEMBERS, members);
                startActivity(intent);
                finish();
            }
        }).on("disconnected", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                finish();
            }

        });

        socket.connect();

    }


    private void sendSocket(MyDevice myDevice){
        //Gson gson = new Gson();
        socket.emit("event", myDevice);
    }


    private Emitter.Listener onReceiveMember = new Emitter.Listener() {
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
                    if(members.size() != 1){
                        startBtn.setEnabled(true);
                        startBtn.setOnClickListener(mOnClickListener);
                    }
                    else{
                        startBtn.setEnabled(false);
                    }

                    for(Member item: members){
                        for(Member localItem: memberList){
                            if(item.getId().equals(localItem.getId())){
                                break;
                            }
                        }
                        memberList.add(item);
                        memberListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnectMember = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson gson = new Gson();
                    Member member = gson.fromJson(new Gson().toJson(data), Member.class);
                    for(Member localItem: memberList){
                        if(member.getId().equals(localItem.getId())){
                            memberList.remove(localItem);
                            memberListAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            });
        }
    };

    private Emitter.Listener onStartTimer = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.member_layout).setVisibility(View.GONE);

                    readyTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (readyTime > 0) {
                                readyTime -= 1000;
                            }
                            else if(readyTimer != null) {
                                readyTimer.cancel();
                                readyTimer = null;
                            }
                            readyText.setText(sdf.format(readyTime));
                        }
                    }, 0, 1000);
                }
            });
        }
    };

    private Emitter.Listener onStart = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.map_overlay_layout).setVisibility(View.GONE);
                    startFlag = true;

                    //鬼を振り分けた結果をもらう
                    JSONObject data = (JSONObject) args[0];
                    Gson gson = new Gson();
                    Type collectionType = new TypeToken<Collection<Member>>() {
                    }.getType();
                    List<Member> members = gson.fromJson(new Gson().toJson(data), collectionType);
                    for(Member item: members){
                        if(item.getId().equals(ProfileUtil.getUserId())){
                            switch (item.getStatus()){
                                case 0:
                                    statusText.setText("市民");
                                    break;
                                case 1:
                                    statusText.setText("鬼");
                                    isTag = true;
                                    //TODO あなたが鬼ですダイアログ
                                    break;
                            }
                            break;
                        }
                    }
                    gameTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (gameTime > 0) {
                                gameTime -= 1000;
                            }
                            else if(gameTimer != null) {
                                gameTimer.cancel();
                                gameTimer = null;
                            }
                            timeText.setText(sdf.format(gameTime));
                        }
                    }, 0, 1000);
                }
            });
        }
    };

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
                    //TODO 鬼になったかどうかの判定は別のタグから受信する？とりあえずここで
                    for(Member item: members){
                        if(item.getId().equals(ProfileUtil.getUserId())){
                            switch (item.getStatus()){
                                case 0:
                                    if(isTag) {
                                        //TODO 市民になりました
                                        statusText.setText("市民");
                                        isTag = false;
                                    }
                                    break;
                                case 1:
                                    if(!isTag) {
                                        statusText.setText("鬼");
                                        isTag = true;
                                        //TODO あなたが鬼ですダイアログ
                                    }
                                    break;
                            }
                            break;
                        }
                    }
                    //TODO 地図表示
                }
            });
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
        else
        {
            sensor = new Sensor();
            // BLEが使用可能ならスキャン開始.
            scanNewDevice();
            //デバイスと接続開始
            mBleGattLeft = ProfileUtil.getBluetoothDeviceLeft().connectGatt(getApplicationContext(), false, mGattCallbackLeft);
            mBleGattRight = ProfileUtil.getBluetoothDeviceLeft().connectGatt(getApplicationContext(), false, mGattCallbackRight);
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
                // Peripheralで値が更新されたらNotificationを受ける.
                mStrReceivedNum = characteristic.getStringValue(0);
                // メインスレッドでTextViewに値をセットする.
                mBleHandler.sendEmptyMessage(MESSAGE_NEW_RECEIVEDNUM);

                sensor.setSensor(bleByteData);

                //TODO GPSも　あとでこれごとGPSのchangeListenerに持っていく
                if(startFlag) {
                    MyDevice myDevice = new MyDevice();
                    myDevice.setSide(0);
                    myDevice.setSensor(sensor);
                    sendSocket(myDevice);
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
                    BluetoothGattCharacteristic mBleCharacteristic = service.getCharacteristic(UUID.fromString(Const.UUID_BLESERIAL_RX));

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

                sensor.setSensor(bleByteData);

                //TODO GPSも　あとでこれごとGPSのchangeListenerに持っていく
                if(startFlag) {
                    MyDevice myDevice = new MyDevice();
                    myDevice.setSide(1);
                    myDevice.setSensor(sensor);
                    sendSocket(myDevice);
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

    /*---------------------
    Listener
    ---------------------- */

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO 始めたら知らせる
            socket.emit("startTimer", 1);
        }
    };
}
