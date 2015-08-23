package jp.nvzk.iotproject.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jp.nvzk.iotproject.Const;
import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.ui.adapter.RoomListAdapter;
import jp.nvzk.iotproject.model.Room;

/**
 * Created by user on 15/08/09.
 */
public class RoomActivity extends AppCompatActivity {
    private Button newRoomBtn;
    private ListView roomListView;
    private List<Room> roomList = new ArrayList<>();
    private RoomListAdapter roomListAdapter;

    private AlertDialog gpsDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.title_room);

        initView();
    }

    @Override
    protected void onResume(){
        super.onResume();
        roomList.clear();
        roomListAdapter.notifyDataSetChanged();
        initList();
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

    private void initView(){
        newRoomBtn = (Button) findViewById(R.id.room_new_btn);
        newRoomBtn.setOnClickListener(mOnClickListener);
        newRoomBtn.setEnabled(false);

        roomListView = (ListView) findViewById(R.id.room_list_view);
        roomListAdapter = new RoomListAdapter(this, roomList);
        roomListView.setAdapter(roomListAdapter);
        roomListView.setOnItemClickListener(mOnItemClickListener);
    }

    private void initList(){
        //TODO APIから部屋リストを取得
        Room room = new Room();
        room.setName("開発用部屋");
        room.setId(000000);
        roomList.add(room);
        roomListAdapter.notifyDataSetChanged();
    }

    /**
     * GPSの確認
     */
    private boolean checkGPS(){
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
                            gpsDialog = null;
                        }
                    });
            if(gpsDialog == null) {
                gpsDialog = alertDialogBuilder.create();
                // 設定画面へ移動するかの問い合わせダイアログを表示
                gpsDialog.show();
            }

            return false;
        }
        if(gpsDialog != null){
            gpsDialog.dismiss();
            gpsDialog = null;
        }
        return true;
    }

    /**
     * bluetoothの準備確認
     */
    private boolean checkBluetooth(){
        BluetoothManager mBleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter mBleAdapter = mBleManager.getAdapter();

        // BluetoothがOffならインテントを表示する.
        if (mBleAdapter == null || !mBleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            return false;
        }
        else{
            return true;
        }
    }


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(!checkBluetooth() || !checkGPS()){
                return;
            }

            Room room = roomListAdapter.getItem(position);
            Intent intent = new Intent(RoomActivity.this, GameActivity.class);
            intent.putExtra(Const.KEY.ROOM, room.getId());
            startActivity(intent);
        }
    };


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO 新規に部屋作成
        }
    };
}
