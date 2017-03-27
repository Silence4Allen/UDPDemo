package com.allen.demo.udp;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.content.ContentValues.TAG;
import static java.lang.System.in;

/**
 * Created by Allen on 2017/2/28.
 */

public class Client implements Runnable {
    private int SERVER_PORT = 6666;
    private DatagramPacket packetRec, packetSend;
    private DatagramSocket socket;
    private boolean isLife = true;
    private boolean isLifeOver = false;
    private ExecutorService singleExecutor;

    public Client() {
        singleExecutor = Executors.newSingleThreadExecutor();
    }

    private void setSoTime(int ms) {
        try {
            socket.setSoTimeout(ms);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public boolean isLife() {
        return isLife;
    }

    public void setLife(boolean b) {
        this.isLife = b;
    }

    public boolean getLifeOver() {
        return isLifeOver;
    }

    public void sendMsg(final String content) {
        singleExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (content.length() != 0) {
                    InetAddress SERVER_IP = null;
                    try {
                        SERVER_IP = InetAddress.getByName("localhost");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    packetSend = new DatagramPacket(content.getBytes(), content.getBytes().length,
                            SERVER_IP, SERVER_PORT);
                    try {
                        socket.send(packetSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void initSocket() {
        if (socket == null) {
            try {
                socket = new DatagramSocket(7777);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        initSocket();
        while (isLife) {
            try {
                byte[] msg = new byte[1024];
                packetRec = new DatagramPacket(msg, msg.length);
                socket.receive(packetRec);
                listener.onMsgReceived(this, packetRec.getData());
            } catch (IOException e) {
                e.printStackTrace();
                isLife = false;
            }
        }
        socket.close();
        isLifeOver = true;
    }

    private OnMsgReceivedListener listener;

    public void setOnMsgReceivedListener(OnMsgReceivedListener listener) {
        this.listener = listener;
    }

    public interface OnMsgReceivedListener {
        void onMsgReceived(Runnable runnable, byte[] data);
    }
}
