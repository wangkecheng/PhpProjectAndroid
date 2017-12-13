package warron.phpprojectandroid.LoginMudule;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Map;

import warron.phpprojectandroid.Base.BaseActivity;
import warron.phpprojectandroid.R;
import warron.phpprojectandroid.Tools.NetRequestMudule.AsyncHttpNet;
import warron.phpprojectandroid.Tools.NetRequestMudule.DataModel;


public class ForgetPassAC extends BaseActivity {
    private EditText phoneTextView;
    private EditText passTextView;
    private EditText verPassTextView;
    private EditText verCodeTextView;
    @Override
    public int getContentView() {
        return R.layout.forget_pass_layout;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        phoneTextView = (EditText) findViewById(R.id.fogetPassPassTextView);
        verCodeTextView = (EditText) findViewById(R.id.fogetPassVertifyTextView);
        passTextView = (EditText) findViewById(R.id.fogetPassPassTextView);
        verPassTextView = (EditText) findViewById(R.id.fogetPassVerPassTextView);
    }
    @Override
    protected void initToolBar() {
        super.initToolBar();
        toolbar.setTitle("找回密码");
    }

    public void forgetSendVerCodeBtnClick(View view){

    }
    public void forgetFindBtnClick(View view){
        AsyncHttpNet net = new AsyncHttpNet(this){

            @Override
            public void onSuccess(Map resultMap) {
                Toast.makeText(ForgetPassAC.this, resultMap.get("msg").toString(), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void fail(int statusCode, Map resultMap) {

            }
        };
        DataModel model = new DataModel();//这个类是请求数据模型，请求参数可以写到这个类中，AsyncHttpNet中会将模型转为字典
        model.userName = phoneTextView.getText().toString();
        model.vertifyCode = verCodeTextView.getText().toString();
        model.password = passTextView.getText().toString();
        model.vertifyPass = verPassTextView.getText().toString();
        net.post(model,"resetpass.php");
    }
};

