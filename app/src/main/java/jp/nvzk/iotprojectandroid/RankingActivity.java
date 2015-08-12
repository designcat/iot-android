package jp.nvzk.iotprojectandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jp.nvzk.iotprojectandroid.model.Member;

/**
 * Created by user on 15/08/09.
 */
public class RankingActivity extends AppCompatActivity {
    private Button finishBtn;
    private ListView memberListView;
    private List<Member> getMemberList = new ArrayList<>();
    private List<Member> memberList = new ArrayList<>();
    private RankingListAdapter rankingListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.title_finish);

        getMemberList = (ArrayList<Member>) getIntent().getSerializableExtra(Const.KEY.MEMBERS);

        initView();
    }

    private void initView(){
        finishBtn = (Button) findViewById(R.id.ranking_finish_btn);
        finishBtn.setOnClickListener(mOnClickListener);

        memberList.add(getMemberList.get(0));
        for(int i = 1; i < getMemberList.size(); i++){
            for(int j = 0; j < memberList.size(); j++){
                if(getMemberList.get(i).getPoint() > memberList.get(j).getPoint()){
                    memberList.add(j, getMemberList.get(i));
                    break;
                }
            }
            memberList.add(getMemberList.get(i));
        }

        memberListView = (ListView) findViewById(R.id.ranking_list_view);
        rankingListAdapter = new RankingListAdapter(this, memberList);
        memberListView.setAdapter(rankingListAdapter);

    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            finish();
        }
    };
}
