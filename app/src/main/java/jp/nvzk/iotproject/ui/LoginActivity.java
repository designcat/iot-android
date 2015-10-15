package jp.nvzk.iotproject.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

import jp.nvzk.iotproject.R;
import jp.nvzk.iotproject.model.ResponseData;
import jp.nvzk.iotproject.ui.dialog.SimpleFragment;
import jp.nvzk.iotproject.ui.dialog.SingleFragment;
import jp.nvzk.iotproject.util.JsonUtil;
import jp.nvzk.iotproject.util.ProfileUtil;
import jp.nvzk.iotproject.util.UrlUtil;

/**
 * Created by user on 15/08/09.
 */
public class LoginActivity extends AppCompatActivity {
    private EditText idText;
    private EditText nameText;
    private EditText password;
    private Button loginBtn;

    private AsyncHttpClient mClient;

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

        mClient = new AsyncHttpClient();

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
        nameText = (EditText) findViewById(R.id.login_name_edit_text);
        password = (EditText) findViewById(R.id.login_password_edit_text);
        idText = (EditText) findViewById(R.id.login_id_edit_text);

        idText.setText(ProfileUtil.getUserId());
        nameText.setText(ProfileUtil.getUserName());
        password.setText(ProfileUtil.getUserPassword());
    }


    /**
     * ログインボタンクリックリスナ
     */
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (password.getText().toString().isEmpty() || nameText.getText().toString().isEmpty() || idText.getText().toString().isEmpty()) {
                SingleFragment dialog = SingleFragment.getInstance(getString(R.string.dialog_error_empty));
                dialog.show(getSupportFragmentManager(), "empty");
                return;
            }
            signIn(idText.getText().toString(), nameText.getText().toString(), password.getText().toString());
        }
    };

    private void signIn(final String id, final String name, final String pass){
        RequestParams params = UrlUtil.getSignInParams(id, name, pass);
        mClient.post(UrlUtil.getSignInUrl(), params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                ResponseData responseData = JsonUtil.getResponse(response.toString());

                if (responseData.getMessage().equals("ok")) {
                    ProfileUtil.setUserId(id);
                    ProfileUtil.setUserName(name);
                    ProfileUtil.setUserPassword(pass);

                    Intent intent = new Intent(LoginActivity.this, GameActivity.class);
                    startActivity(intent);
                } else {
                    SimpleFragment dialog = SimpleFragment.getInstance(UrlUtil.getErrorMessage(responseData.getDetails()));
                    dialog.show(getSupportFragmentManager(), "error");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            }
        });

    }

}
