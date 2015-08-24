package jp.nvzk.iotproject.ui;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.ui.dialog.SingleFragment;
import jp.nvzk.iotproject.util.ProfileUtil;

/**
 * Created by user on 15/08/09.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText idText;
    private EditText nameText;
    private Button loginBtn;
    private Button selectBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(R.string.title_login);

    }

    @Override
    protected void onResume(){
        super.onResume();
        initView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                    finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView(){
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(mOnClickListener);
        idText = (EditText) findViewById(R.id.login_edit_text);
        nameText = (EditText) findViewById(R.id.login_name_edit_text);
        selectBtn = (Button) findViewById(R.id.login_select_btn);
        selectBtn.setOnClickListener(mOnSelectClickListener);

        if(!ProfileUtil.getUserId().isEmpty()){
            idText.setText(ProfileUtil.getUserId());
            idText.setEnabled(false);
            selectBtn.setEnabled(false);
        }
        nameText.setText(ProfileUtil.getUserName());

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 0 && resultCode == Activity.RESULT_OK){
            idText.setText(data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME));
        }
    }


    /**
     * ログインボタンクリックリスナ
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ProfileUtil.getUserId().isEmpty()) {
                if (idText.getText().toString().isEmpty() || nameText.getText().toString().isEmpty()) {
                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_empty));
                    dialog.show(getSupportFragmentManager(), "empty");
                    return;
                }
                //TODO API送信
                ProfileUtil.setUserId(idText.getText().toString());
            }
            else{
                if (nameText.getText().toString().isEmpty()) {
                    SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_empty));
                    dialog.show(getSupportFragmentManager(), "empty");
                    return;
                }
            }

            ProfileUtil.setUserName(nameText.getText().toString());

            Intent intent = new Intent(LoginActivity.this, RoomActivity.class);
            startActivity(intent);
        }
    };


    /**
     * アカウント選択ボタンリスナ
     */
    private View.OnClickListener mOnSelectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = AccountManager.get(LoginActivity.this).newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
            startActivityForResult(intent, 0);
        }
    };
}
