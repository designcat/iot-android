package jp.nvzk.iotprojectandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jp.nvzk.iotprojectandroid.Const;
import jp.nvzk.iotprojectandroid.R;
import jp.nvzk.iotprojectandroid.ui.adapter.RoomListAdapter;
import jp.nvzk.iotprojectandroid.model.Room;

/**
 * Created by user on 15/08/09.
 */
public class RoomActivity extends AppCompatActivity {
    private Button newRoomBtn;
    private ListView roomListView;
    private List<Room> roomList = new ArrayList<>();
    private RoomListAdapter roomListAdapter;

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
        //TODO 接続チェック　だめだったらやり直し
        roomList.clear();
        roomListAdapter.notifyDataSetChanged();
        initList();
    }

    private void initView(){
        newRoomBtn = (Button) findViewById(R.id.room_new_btn);
        newRoomBtn.setOnClickListener(mOnClickListener);

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


    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
