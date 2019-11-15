package com.project.search.common.utils;

import com.jcraft.jsch.*;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

@Component
public class SftpUtil implements AutoCloseable {

    private Session session = null;
    private ChannelSftp channel = null;


    /**
     * 连接sftp服务器
     *
     * @param serverIP 服务IP
     * @param port     端口
     * @param userName 用户名
     * @param password 密码
     * @throws SocketException SocketException
     * @throws IOException     IOException
     * @throws JSchException   JSchException
     */
    public void connectServer(String serverIP, int port, String userName, String password) throws SocketException, IOException, JSchException {
        JSch jsch = new JSch();
        // 根据用户名，主机ip，端口获取一个Session对象
        session = jsch.getSession(userName, serverIP, port);
        // 设置密码
        session.setPassword(password);
        // 为Session对象设置properties
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        // 通过Session建立链接
        session.connect();
        // 打开SFTP通道
        channel = (ChannelSftp) session.openChannel("sftp");
        // 建立SFTP通道的连接
        channel.connect();

    }

    /**
     * 自动关闭资源
     */
    public void close() {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }

    public List<ChannelSftp.LsEntry> getDirList(String path) throws SftpException {
        List<ChannelSftp.LsEntry> list = new ArrayList<>();
        if (channel != null) {
            Vector vv = channel.ls(path);
            if (vv == null && vv.size() == 0) {
                return list;
            } else {
                Object[] aa = vv.toArray();
                for (int i = 0; i < aa.length; i++) {
                    ChannelSftp.LsEntry temp = (ChannelSftp.LsEntry) aa[i];
                    list.add(temp);

                }
            }
        }
        return list;
    }

    /**
     * 下载文件
     *
     * @param remotePathFile 远程文件
     * @param localPathFile  本地文件[绝对路径]
     * @throws SftpException SftpException
     * @throws IOException   IOException
     */
    public void downloadFile(String remotePathFile, String localPathFile) throws SftpException, IOException {
        try (FileOutputStream os = new FileOutputStream(new File(localPathFile))) {
            if (channel == null)
                throw new IOException("sftp server not login");
            channel.get(remotePathFile, os);
        }
    }

    /**
     * 上传文件
     *
     * @param remoteFile 远程文件
     * @param localFile
     * @throws SftpException
     * @throws IOException
     */
    public void uploadFile(String remoteFile, String localFile) throws SftpException, IOException {
        try (FileInputStream in = new FileInputStream(new File(localFile))) {
            if (channel == null)
                throw new IOException("sftp server not login");
            channel.put(in, remoteFile);
        }
    }

    /**
     * 上传文件
     *
     * @param remoteFile 远程文件
     * @param inputStream
     * @throws SftpException
     * @throws IOException
     */
    public void uploadFile(String remoteFile, InputStream inputStream) throws SftpException, IOException {
        if (channel == null)
            throw new IOException("sftp server not login");
        channel.put(inputStream, remoteFile);
    }
}
