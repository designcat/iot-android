package jp.nvzk.iotprojectandroid.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import jp.nvzk.iotprojectandroid.R;
import jp.nvzk.iotprojectandroid.util.ProfileUtil;

/**
 * Created by user on 15/08/09.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText editText;
    private EditText nameText;
    private Button loginBtn;

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

    private void initView(){
        loginBtn = (Button) findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(mOnClickListener);
        editText = (EditText) findViewById(R.id.login_edit_text);
        nameText = (EditText) findViewById(R.id.login_name_edit_text);

        if(!ProfileUtil.getUserId().isEmpty()){
            editText.setVisibility(View.GONE);
        }
        nameText.setText(ProfileUtil.getUserName());

    }


    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if(ProfileUtil.getUserId().isEmpty()) {
                if (editText.getText().toString().isEmpty()) {
                    //TODO ユーザIDを入力してください
                    return;
                }
                if (nameText.getText().toString().isEmpty()) {
                    //TODO ユーザ名を入力してください
                    return;
                }
                //TODO API送信
                ProfileUtil.setUserId(editText.getText().toString());
            }
            else{
                if (nameText.getText().toString().isEmpty()) {
                    //TODO ユーザ名を入力してください
                    return;
                }
                ProfileUtil.setUserName(nameText.getText().toString());
            }

            Intent intent = new Intent(LoginActivity.this, RoomActivity.class);
            startActivity(intent);
        }
    };
}
