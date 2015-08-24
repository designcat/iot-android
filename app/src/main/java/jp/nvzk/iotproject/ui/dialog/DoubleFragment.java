package jp.nvzk.iotproject.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import jp.nvzk.iotproject.Const;
import jp.nvzk.iotproject.R;


/**
 * Created by jlm on 2014/12/11.
 * ダウンロード完了後に出てくるDialog
 */
public class DoubleFragment extends DialogFragment {

    private Dialog mDialog;
    private OnOkListener mListener;


    /**
     * インスタンスの取得
     *
     * @return このクラスのインスタンス
     */
    public static DoubleFragment getInstance(String message) {
        DoubleFragment fragment = new DoubleFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Const.KEY.MESSAGE, message);
        fragment.setArguments(bundle);
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
        mDialog.setContentView(R.layout.fragment_dialog_double);
        mDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Viewの初期化
     */
    private void initView() {
        Button cancelButton = (Button) mDialog.findViewById(R.id.dialog_cancel_btn);
        Button okButton = (Button) mDialog.findViewById(R.id.dialog_ok_btn);
        cancelButton.setOnClickListener(mCancelClickListener);
        okButton.setOnClickListener(mOkClickListener);
        TextView messageView = (TextView) mDialog.findViewById(R.id.dialog_text);
        String message = getArguments().getString(Const.KEY.MESSAGE);
        messageView.setText(message);
    }

    public interface OnOkListener {
        void onOK();
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
            if(mListener != null){
                mListener.onOK();
            }
            dismiss();
        }
    };

}
