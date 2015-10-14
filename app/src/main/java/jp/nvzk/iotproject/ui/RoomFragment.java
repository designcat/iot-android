package jp.nvzk.iotproject.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

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

import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.Room;
import jp.nvzk.iotproject.ui.adapter.RoomListAdapter;
import jp.nvzk.iotproject.ui.dialog.NewRoomFragment;
import jp.nvzk.iotproject.util.ProfileUtil;
import jp.nvzk.iotproject.util.SocketUtil;

/**
 * Created by user on 15/08/09.
 */
public class RoomFragment extends Fragment {
    private Button newRoomBtn;
    private ListView roomListView;
    private List<Room> roomList = new ArrayList<>();
    private RoomListAdapter roomListAdapter;

    private Socket socket;

    private LayoutInflater inflater;
    private View mView;

    private SelectRoomListener mSelectRoomListener;



    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        mView = inflater.inflate(R.layout.activity_room, container, false);
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
        initView();
        initSocket();
    }

    @Override
    public void onDestroy(){
        stopSocket();
        super.onDestroy();
    }

    public void stopSocket(){
        if(socket != null) {
            socket.disconnect();
        }
    }

    private void initView(){
        newRoomBtn = (Button) mView.findViewById(R.id.room_new_btn);
        newRoomBtn.setOnClickListener(mOnClickListener);
        newRoomBtn.setEnabled(false);

        roomListView = (ListView) mView.findViewById(R.id.room_list_view);
        roomListAdapter = new RoomListAdapter(getActivity(), roomList);
        roomListView.setAdapter(roomListAdapter);
        roomListView.setOnItemClickListener(mOnItemClickListener);
    }

    /**
     * socket.ioの準備
     */
    private void initSocket(){

        socket = SocketUtil.getRoomSocket();

        socket.on("connect", onConnect
        ).on("member", onChangeMember
        ).on("disconnect", onDisconnect);

        socket.connect();
    }



    /**
     *
     * @return 開発用部屋を返す
     */
    private Room getDefaultRoom(){
        Room room = new Room();
        room.setRoomName("開発用部屋");
        room.setUserId("");
        room.setUserName("admin");
        room.setMaxMember(20);
        return room;
    }


    /*----------------------
    リスナ
    ------------------------ */

    /**
     * リストから選択時
     */
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Room room = roomListAdapter.getItem(position);
            if(mSelectRoomListener != null){
                mSelectRoomListener.onSelectRoom(room.getUserId());
            }
        }
    };



    /**
     * 接続時
     */
    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            newRoomBtn.setEnabled(true);
        }
    };

    /**
     * 部屋情報受信時
     */
    private Emitter.Listener onChangeMember = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    Gson gson = new Gson();
                    Type collectionType = new TypeToken<Collection<Room>>() {
                    }.getType();
                    List<Room> rooms = gson.fromJson(new Gson().toJson(data), collectionType);
                    rooms.add(0, getDefaultRoom());
                    roomList.clear();
                    roomList.addAll(rooms);
                    roomListAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    /**
     * 退室時
     */
    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            getActivity().finish();
        }
    };


    /**
     * 新規部屋作成ボタンリスナ
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            NewRoomFragment newRoomFragment = new NewRoomFragment();
            newRoomFragment.setOkListener(onNewRoomCreateListener);
            newRoomFragment.show(getChildFragmentManager(), "room");
        }
    };

    /**
     * 部屋作成時
     */
    private NewRoomFragment.OnOkListener onNewRoomCreateListener = new NewRoomFragment.OnOkListener() {
        @Override
        public void onOK(String roomName, int maxMember) {
            Room room = new Room();
            room.setRoomName(roomName);
            room.setMaxMember(maxMember);
            room.setUserId(ProfileUtil.getUserId());
            Gson gson = new Gson();
            String str = gson.toJson(room);
            try {
                JSONObject jsonObject = new JSONObject(str);
                socket.emit("room", jsonObject);

                if(mSelectRoomListener != null){
                    mSelectRoomListener.onSelectRoom(room.getUserId());
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    public interface SelectRoomListener {
        void onSelectRoom(String id);
    }

    public void setCreateRoomListener(SelectRoomListener listener){
        mSelectRoomListener = listener;
    }
}
