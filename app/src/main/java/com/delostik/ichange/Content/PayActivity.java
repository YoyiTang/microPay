package com.delostik.ichange.Content;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.delostik.ichange.HttpUtils;
import com.delostik.ichange.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class PayActivity extends Activity {

    double pay = 0;
    private  AlertDialog longinDialog;
    private View longinDialogView;
    JSONObject payRes = new JSONObject();
    JSONObject checkRes = new JSONObject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay);
        Button confirm = (Button)findViewById(R.id.btn_confim_pay2);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText edit = (EditText) findViewById(R.id.edit_pay_num2);
                try {
                    pay = Double.parseDouble(edit.getText().toString());
                } catch (Exception e) {
                    pay = 0;
                }
                if (pay <= 0) {
                    Toast.makeText(PayActivity.this, "请输入大于0的金额", Toast.LENGTH_SHORT);
                } else {
                    pswDialog();
                }
            }
        });
    }

    public void pswDialog () {
        Context mContext = PayActivity.this;

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        longinDialogView = layoutInflater.inflate(R.layout.psw, null);

        longinDialog = new AlertDialog.Builder(mContext).setTitle("确认支付").setView(longinDialogView).create();
        longinDialog.show();

        Button confirm = (Button)longinDialogView.findViewById(R.id.btn_dia_confirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(checkPayPassword).start();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("checkRes");

            try {
                checkRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
                Log.i("checkerr: ", e.getMessage().toString());
            }
            returnCheckRes();
        }
    };

    Runnable checkPayPassword = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("id", getIntent().getStringExtra("id"));
            EditText pwd = (EditText)longinDialogView.findViewById(R.id.edit_dia_password);
            params.put("pwd", pwd.getText().toString());
            params.put("cookie", getIntent().getStringExtra("cookie"));
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/User/verifyPayPwd.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("checkRes", strResult);
            msg.setData(data);
            handler.sendMessage(msg);
        }
    };

    private void returnCheckRes() {
        String cookie = new String();
        String id = new String();
        if (checkRes == JSONObject.NULL) {
            Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
        } else {
            int code = 0;
            try {
                code = checkRes.getInt("code");
            } catch (JSONException e) {
                Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (code) {
                case 0:
                    Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case 100:
                    try {
                        String msg = checkRes.getString("info");
                        Toast.makeText(PayActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 200:
                    new Thread(dopay).start();
                    break;
            }
        }
    }


    private Handler payHandler  = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle data = msg.getData();
            String json = data.getString("payRes");

            try {
                payRes = new JSONObject(json);
            } catch (JSONException e) {
                Log.i("JSONException: ", e.getMessage().toString());
            }
            returnPayRes();
        }
    };

    Runnable dopay = new Runnable() {
        @Override
        public void run() {
            Map<String,String> params = new HashMap<String, String>();
            params.put("userId", getIntent().getStringExtra("id"));
            EditText user = (EditText)findViewById(R.id.edit_pay_account);
            String username = user.getText().toString();

            params.put("otherUser", username);
            params.put("cookie", getIntent().getStringExtra("cookie"));
            params.put("money", String.valueOf(pay));
            String strUrlPath = "http://121.40.194.163/microPay/index.php?s=/Home/user/exchangeToOther.html";
            String strResult= HttpUtils.submitPostData(strUrlPath, params, "utf-8");
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putString("payRes", strResult);
            msg.setData(data);
            payHandler.sendMessage(msg);
        }
    };

    private void returnPayRes() {
        String cookie = new String();
        String id = new String();
        if (payRes == JSONObject.NULL) {
            Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
        } else {
            int code = 0;
            try {
                code = payRes.getInt("code");
            } catch (JSONException e) {
                Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                return;
            }
            switch (code) {
                case 0:
                    Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    break;
                case 100:
                    try {
                        String msg = payRes.getString("info");
                        Toast.makeText(PayActivity.this, msg, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(PayActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 200:
                    Intent intent = new Intent(PayActivity.this, IndexActivity.class);
                    startActivity(intent);
            }
        }
    }
}
