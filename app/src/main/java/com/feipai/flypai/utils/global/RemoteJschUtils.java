package com.feipai.flypai.utils.global;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.JsonIOException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 *
 */
public class RemoteJschUtils {

//执行远程命令时需要的jar:jsch
//	<dependency>
//        <groupId>com.jcraft</groupId>
//        <artifactId>jsch</artifactId>
//        <version>0.1.54</version>
//  </dependency>

    private Session session = null;
    private ChannelExec openChannel = null;
    BufferedReader reader = null;

    private ReentrantLock mLock = new ReentrantLock();

    private static RemoteJschUtils mInstace;

    public static RemoteJschUtils getInstance() {
        if (mInstace == null) {
            synchronized (RemoteJschUtils.class) {
                if (mInstace == null) {
                    mInstace = new RemoteJschUtils();
                }
            }
        }
        return mInstace;
    }

    public RemoteJschUtils() {
    }

    /**
     * 执行本地命令行.
     *
     * @param cmd
     * @return 之后返回的内容
     * @throws IOException
     */
    private String execLocalCmd(String cmd) throws IOException {
        String[] cmdA = {"/bin/sh", "-c", cmd};
        Process process = Runtime.getRuntime().exec(cmdA);
        LineNumberReader br = new LineNumberReader(new InputStreamReader(
                process.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    /**
     * 执行远程命令行.
     *
     * @param host IP
     * @param cmd
     * @return 执行返回的内容
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws JSchException
     */
    public static String execRemoteCmd(String host, String cmd)
            throws UnsupportedEncodingException, IOException, JSchException {
        String result = "";
        Session session = null;
        ChannelExec openChannel = null;
        BufferedReader reader = null;
        try {
//            LogUtils.d("开始Jsch服务");
            JSch jsch = new JSch();
            session = jsch.getSession("root", host, 22);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword("passwdfp");
            session.connect();

            openChannel = (ChannelExec) session.openChannel("exec");
            openChannel.setCommand(cmd);
            openChannel.connect();
            InputStream in = openChannel.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(in));
            String buf = null;
            while (reader != null && (buf = reader.readLine()) != null) {
                result += new String(buf.getBytes("gbk"), "UTF-8") + "\r\n";
//                LogUtils.d("ping值==》" + result.trim());
            }
            return result;
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        } catch (JSchException e) {
            e.printStackTrace();
        } catch (InterruptedIOException e) {
            e.printStackTrace();
        } finally {
//            closeJsch();
//            LogUtils.d("关闭Jsch服务");
            if (openChannel != null && !openChannel.isClosed()) {
                openChannel.disconnect();
            }
            if (session != null && session.isConnected()) {
                session.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public void closeJsch() {
//        LogUtils.d("关闭Jsch服务");
        if (openChannel != null && !openChannel.isClosed()) {
            openChannel.disconnect();
        }
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}