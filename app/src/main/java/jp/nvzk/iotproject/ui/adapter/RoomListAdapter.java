package jp.nvzk.iotproject.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.Room;

/**
 * Created by amyu on 14/12/22.
 * コメントリストのAdapter
 */
public class RoomListAdapter extends BaseAdapter {

    /**
     * コメントリスト
     */
    private List<Room> roomList;

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
    public RoomListAdapter(Context context, List<Room> rooms) {
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
    public Room getItem(int position) {
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
        Room room = getItem(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_list, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_title);
            holder.id = (TextView) convertView.findViewById(R.id.item_sub);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(room.getName());
        holder.id.setText("ID: " + String.valueOf(room.getId()));

        return convertView;
    }


    //ViewHolder
    private static class ViewHolder {
        TextView name;
        TextView id;
    }

}
