package jp.nvzk.iotproject.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.Member;
import jp.nvzk.iotproject.ui.adapter.MemberListAdapter;

/**
 * Created by user on 15/09/19.
 */
public class MemberFragment extends Fragment {

    private LayoutInflater inflater;
    private View mView;

    private Button startBtn;
    private ListView memberListView;
    private List<Member> memberList = new ArrayList<>();
    private MemberListAdapter memberListAdapter;

    private onStartClickListener mListener;


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        this.inflater = inflater;
        mView = inflater.inflate(R.layout.fragment_member, container, false);
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
    }

    private void initView(){
        startBtn = (Button) mView.findViewById(R.id.member_start_btn);
        startBtn.setEnabled(true);
        startBtn.setOnClickListener(mOnClickListener);

        memberListView = (ListView) mView.findViewById(R.id.member_list_view);
        memberListAdapter = new MemberListAdapter(getActivity(), memberList);
        memberListView.setAdapter(memberListAdapter);
    }

    /**
     * メンバーリストにセット
     * @param members
     */
    public void setList(List<Member> members){
            //TODO ３人以上で開始 今回は何人でもOKとする
            if(members.size() > 2){
                startBtn.setEnabled(true);
            }
            else{
                startBtn.setEnabled(false);
            }
            //TODO 追々削除
            startBtn.setEnabled(true);

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

    /**
     * メンバーリストからメンバー削除
     * @param member
     */
    public void removeMember(Member member){
        for(Member localItem: memberList){
            if(member.getId().equals(localItem.getId())){
                memberList.remove(localItem);
                memberListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    /*---------------------
    Listener
    ---------------------- */

    public void setOnStartClickListener(onStartClickListener listener){
        mListener = listener;
    }

    public interface onStartClickListener {
        public void onStartClicked();
    }

    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(mListener != null){
                mListener.onStartClicked();
            }

        }
    };
}
