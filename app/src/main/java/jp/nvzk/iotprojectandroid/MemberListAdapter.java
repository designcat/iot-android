package jp.nvzk.iotprojectandroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import jp.nvzk.iotprojectandroid.model.Member;

/**
 * Created by amyu on 14/12/22.
 * コメントリストのAdapter
 */
public class MemberListAdapter extends BaseAdapter {

    /**
     * コメントリスト
     */
    private List<Member> roomList;

    /**
     * LayoutInflate
     */
    private LayoutInflater mLayoutInflater;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param rooms 表示するコメントデータ
     */
    public MemberListAdapter(Context context, List<Member> rooms) {
        roomList = rooms;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int getCount() {
        return roomList.size();
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @return
     */
    @Override
    public Member getItem(int position) {
        return roomList.get(position);
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        Member member = getItem(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_list_single, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_title);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(member.getName());

        return convertView;
    }


    //ViewHolder
    private static class ViewHolder {
        TextView name;
    }

}
