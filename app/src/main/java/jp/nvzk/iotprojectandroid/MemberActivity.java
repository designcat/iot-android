package jp.nvzk.iotprojectandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.nvzk.iotprojectandroid.model.Member;
import jp.nvzk.iotprojectandroid.model.MyData;
import jp.nvzk.iotprojectandroid.util.ProfileUtil;
import jp.nvzk.iotprojectandroid.util.SocketUtil;

/**
 * Created by user on 15/08/10.
 */
public class MemberActivity extends AppCompatActivity {
    private Button startBtn;
    private ListView memberListView;
    private List<Member> memberList = new ArrayList<>();
    private MemberListAdapter memberListAdapter;

    private Socket socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member);

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
    }

    @Override
    protected void onResume(){
        super.onResume();
        //TODO 接続確認 だめだったら戻る
    }

    public void initView(){
        startBtn = (Button) findViewById(R.id.member_start_btn);
        memberListView = (ListView) findViewById(R.id.member_list_view);
        memberListAdapter = new MemberListAdapter(this, memberList);
        memberListView.setAdapter(memberListAdapter);

    }

    /**
     * socket.ioの準備
     */
    private void initSocket(){

        socket = SocketUtil.getSocket();

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                MyData data = new MyData();
                data.setId(ProfileUtil.getUserId());
                sendSocket(data);
            }

        }).on("start", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                //TODO startを受け取ったら、ゲーム開始
                socket.disconnect();
                Intent intent = new Intent(MemberActivity.this, MapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();

            }
        }).on("event", onReceive
        ).on("finish", new Emitter.Listener() {
            @Override
            public void call(Object... args) {

            }
        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                //TODO 接続がきれましたダイアログ
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
                    if(members.size() == 1){
                        startBtn.setClickable(true);
                        startBtn.setOnClickListener(mOnClickListener);
                    }
                    else{
                        startBtn.setClickable(false);
                    }
                    if(memberList.size() < members.size()) {
                        memberList.add(members.get(members.size() - 1));
                        memberListAdapter.notifyDataSetChanged();
                    }
                }
            });
        }
    };

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //TODO 始めたら知らせる
            socket.emit("start", 1);
        }
    };
}
