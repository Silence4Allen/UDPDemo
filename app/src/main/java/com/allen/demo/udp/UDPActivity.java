package com.allen.demo.udp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Allen on 2016/12/29.
 */

public class UDPActivity extends AppCompatActivity implements View.OnClickListener, Client
        .OnMsgReceivedListener, Server.OnMsgReceivedListener {
    private final static String TAG = "UDPActivity";
    private Client client;
    private Server server;
    private Thread clientThread, serverThread;
    private StringBuilder clientContent, serverContent;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            String content = msg.getData().getString("content");
            switch (msg.what) {
                case 1:
                    cShowContent.setText(clientContent.append("\r\n" + content));
                    break;
                case 2:
                    sShowContent.setText(serverContent.append("\r\n" + content));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp);
        initView();
        client = new Client();
        clientThread = new Thread(client);
        clientThread.start();
        client.setOnMsgReceivedListener(this);

        server = new Server();
        serverThread = new Thread(server);
        serverThread.start();
        server.setOnMsgReceivedListener(this);
        clientContent = new StringBuilder(cEdt.getText().toString().trim());
        serverContent = new StringBuilder(sEdt.getText().toString().trim());
    }

    private EditText cEdt, sEdt;
    private Button cSendBtn, sSendBtn, cClearBtn, sClearBtn;
    private TextView cShowContent, sShowContent;

    private void initView() {
        cEdt = (EditText) findViewById(R.id.client_editText);
        sEdt = (EditText) findViewById(R.id.server_editText);
        cSendBtn = (Button) findViewById(R.id.client_send);
        sSendBtn = (Button) findViewById(R.id.server_send);
        cClearBtn = (Button) findViewById(R.id.client_clear);
        sClearBtn = (Button) findViewById(R.id.server_clear);
        cShowContent = (TextView) findViewById(R.id.client_received_content);
        sShowContent = (TextView) findViewById(R.id.server_received_content);
        cSendBtn.setOnClickListener(this);
        sSendBtn.setOnClickListener(this);
        cClearBtn.setOnClickListener(this);
        sClearBtn.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.client_send:
                client.sendMsg(cEdt.getText().toString().trim());
                cEdt.setText("");
                cEdt.clearFocus();
                break;
            case R.id.server_send:
                server.sendMsg(sEdt.getText().toString().trim());
                sEdt.setText("");
                sEdt.clearFocus();
                break;
            case R.id.client_clear:
                cShowContent.setText("");
                clientContent.delete(0, clientContent.length());
                break;
            case R.id.server_clear:
                sShowContent.setText("");
                serverContent.delete(0, serverContent.length());
            default:
                break;
        }
    }

    @Override
    public void onMsgReceived(Runnable runnable, byte[] data) {
        Message msg = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("content", getStringDate() + "\r\n" + new String(data));
        Log.e(TAG, "onMsgReceived: " + new String(data));
        msg.setData(bundle);
        if (runnable instanceof Client) {
            msg.what = 1;
            handler.sendMessage(msg);
        } else if (runnable instanceof Server) {
            msg.what = 2;
            handler.sendMessage(msg);
        }
    }

    /**
     * 获取现在时间
     *
     * @return返回字符串格式 yyyy-MM-dd HH:mm:ss
     */
    public static String getStringDate() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy: ");
        client.setLife(false);
        server.setLife(false);
        super.onDestroy();
    }
}
