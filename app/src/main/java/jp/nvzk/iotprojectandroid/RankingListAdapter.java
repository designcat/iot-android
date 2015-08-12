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
public class RankingListAdapter extends BaseAdapter {

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
    public RankingListAdapter(Context context, List<Member> rooms) {
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
            convertView = mLayoutInflater.inflate(R.layout.item_list_ranking, parent, false);
            holder = new ViewHolder();
            holder.rank = (TextView) convertView.findViewById(R.id.item_rank);
            holder.name = (TextView) convertView.findViewById(R.id.item_title);
            holder.point = (TextView) convertView.findViewById(R.id.item_point);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.rank.setText((position + 1) + " P");
        holder.name.setText(member.getName());
        holder.point.setText(member.getPoint());

        return convertView;
    }


    //ViewHolder
    private static class ViewHolder {
        TextView rank;
        TextView name;
        TextView point;
    }

}
