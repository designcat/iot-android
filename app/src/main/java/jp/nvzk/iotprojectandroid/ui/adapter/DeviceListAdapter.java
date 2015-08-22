package jp.nvzk.iotprojectandroid.ui.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import jp.nvzk.iotprojectandroid.R;

/**
 * Created by amyu on 14/12/22.
 * コメントリストのAdapter
 */
public class DeviceListAdapter extends BaseAdapter {

    /**
     * コメントリスト
     */
    private List<BluetoothDevice> deviceList;

    /**
     * LayoutInflate
     */
    private LayoutInflater mLayoutInflater;

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param devices 表示するコメントデータ
     */
    public DeviceListAdapter(Context context, List<BluetoothDevice> devices) {
        deviceList = devices;
        mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public int getCount() {
        return deviceList.size();
    }

    /**
     * {@inheritDoc}
     *
     * @param position
     * @return
     */
    @Override
    public BluetoothDevice getItem(int position) {
        return deviceList.get(position);
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
        BluetoothDevice device = getItem(position);
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.item_list, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.item_title);
            holder.address = (TextView) convertView.findViewById(R.id.item_sub);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());

        return convertView;
    }


    //ViewHolder
    private static class ViewHolder {
        TextView name;
        TextView address;
    }

}
