package jp.nvzk.iotproject.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import jp.nvzk.iotproject.R;


/**
 * Created by jlm on 2014/12/11.
 * ダウンロード完了後に出てくるDialog
 */
public class NewRoomFragment extends DialogFragment {

    private Dialog mDialog;
    private OnOkListener mListener;

    private EditText roomEditText;
    private NumberPicker memberNumberPicker;


    /**
     * インスタンスの取得
     *
     * @return このクラスのインスタンス
     */
    public static NewRoomFragment getInstance() {
        NewRoomFragment fragment = new NewRoomFragment();
        return fragment;
    }

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     * @return
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        initDialog();
        initView();

        return mDialog;
    }


    /**
     * Dialogの初期化
     */
    private void initDialog() {
        mDialog = new Dialog(getActivity(), R.style.FragmentDialog);
        mDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        mDialog.setContentView(R.layout.fragment_dialog_room);
        mDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Viewの初期化
     */
    private void initView() {
        Button cancelButton = (Button) mDialog.findViewById(R.id.dialog_cancel_btn);
        Button okButton = (Button) mDialog.findViewById(R.id.dialog_create_btn);
        cancelButton.setOnClickListener(mCancelClickListener);
        okButton.setOnClickListener(mOkClickListener);
        roomEditText = (EditText)mDialog.findViewById(R.id.dialog_room_name);
        memberNumberPicker = (NumberPicker)mDialog.findViewById(R.id.dialog_member_number);
        memberNumberPicker.setMinValue(2);
        memberNumberPicker.setMaxValue(30);
        memberNumberPicker.setValue(10);
    }

    public interface OnOkListener {
        void onOK(String roomName, int maxMember);
    }

    public void setOkListener(OnOkListener listener){
        mListener = listener;
    }



    /**
     * ***********************************************************************************************
     * Listener
     * ************************************************************************************************
     */

    /**
     * キャンセルボタンのListener
     */
    private View.OnClickListener mCancelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };

    /**
     * OKボタンのListener
     */
    private View.OnClickListener mOkClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String roomName = roomEditText.getText().toString();
            int maxMember = memberNumberPicker.getValue();
            if(roomName.isEmpty()){
                return;
            }

            if(mListener != null){
                mListener.onOK(roomName, maxMember);
            }
            dismiss();
        }
    };

}
