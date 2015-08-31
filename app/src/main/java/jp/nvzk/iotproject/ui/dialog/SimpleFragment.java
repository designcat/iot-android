package jp.nvzk.iotproject.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.WindowManager;
import android.widget.TextView;

import jp.nvzk.iotproject.Const;
import jp.nvzk.iotproject.R;


/**
 * Created by jlm on 2014/12/11.
 * ダウンロード完了後に出てくるDialog
 */
public class SimpleFragment extends DialogFragment {

    private Dialog mDialog;


    /**
     * インスタンスの取得
     *
     * @return このクラスのインスタンス
     */
    public static SimpleFragment getInstance(String message) {
        SimpleFragment fragment = new SimpleFragment();
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
        mDialog.setContentView(R.layout.fragment_dialog_small);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
    }

    /**
     * Viewの初期化
     */
    private void initView() {
        TextView messageView = (TextView) mDialog.findViewById(R.id.dialog_text);
        String message = getArguments().getString(Const.KEY.MESSAGE);
        messageView.setText(message);
    }

}
