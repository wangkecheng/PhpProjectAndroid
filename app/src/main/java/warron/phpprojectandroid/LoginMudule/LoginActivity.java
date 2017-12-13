package warron.phpprojectandroid.LoginMudule;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import warron.phpprojectandroid.Base.BaseActivity;
import warron.phpprojectandroid.MainActivity;
import warron.phpprojectandroid.R;
import warron.phpprojectandroid.Tools.NetRequestMudule.AsyncHttpNet;
import warron.phpprojectandroid.Tools.NetRequestMudule.DataModel;


public class LoginActivity extends BaseActivity {
    private EditText phoneTextView;
    private EditText passTextView;

    @Override
    public int getContentView() {
            return R.layout.login_layout;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            phoneTextView = (EditText) findViewById(R.id.phoneTexView);
            passTextView = (EditText) findViewById(R.id.passTexView);
        }
        @Override
        protected void initToolBar() {}

    public void loginBtnClick(View view){
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        AsyncHttpNet net = new AsyncHttpNet(this){
            @Override
            public void onSuccess(Map resultMap) {
                Toast.makeText(LoginActivity.this, resultMap.get("msg").toString(), Toast.LENGTH_SHORT).show();
//                startActivity(new Intent(LoginActivity.this, MainActivity.class));
            }

            @Override
            public void fail(int statusCode, Map resultMap) {

            }
        };
        DataModel model = new DataModel();//这个类是请求数据模型，请求参数可以写到这个类中，AsyncHttpNet中会将模型转为字典
        model.userName = phoneTextView.getText().toString();
        model.password = passTextView.getText().toString();
        net.post(model,"login.php");
    }
    public void forgetBtnClick(View view){
        startActivity(new Intent(LoginActivity.this, ForgetPassAC.class));
    }
    public void registerBtnClick(View view){
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }

    public void isShowPassBtnClick(View view){
        Button btn = (Button) view;
        if (passTextView.getInputType() ==  InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){
            btn.setBackgroundResource(R.mipmap.b_ic_yanjingb);
            passTextView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        }else{
            btn.setBackgroundResource(R.mipmap.b_ic_yanjing);
            passTextView.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }

    public void qqBtnClick(View view){

    }
    public void weChatBtnClick(View view){

    }
    public void protocolBtnClick(View view){

    }
}
