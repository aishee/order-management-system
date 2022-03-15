package com.walmart.marketplace.dwh;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import io.strati.libs.forklift.org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** This class is used to create connection to ssh location and transfer file. */
@Component
@Slf4j
public class ReportClient {

  private static final String PROTOCOL = "sftp";

  /**
   * Connecting to remote VM using private key and username.
   *
   * @param userName Username for remote VM.
   * @param ipAddress IP address of remote VM.
   * @param port Port required for SFTP.
   * @param rsaPath Private key file path.
   * @return
   */
  public Session getSession(String userName, String ipAddress, int port, String rsaPath) {
    try {
      Session session;
      JSch jsch = new JSch();
      jsch.addIdentity(rsaPath);
      session = jsch.getSession(userName, ipAddress, port);
      session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
      session.setConfig("StrictHostKeyChecking", "no");
      session.connect();
      log.info("Connection successful");
      return session;
    } catch (JSchException e) {
      String message =
          String.format("Error while initializing sftp session, rsa file path %s", rsaPath);
      log.error(message, e);
      throw new OMSBadRequestException(message);
    }
  }

  /**
   * SFTP protocol for creation of channel.
   *
   * @param session SFTP protocol for the existing session.
   * @return
   */
  public ChannelSftp getChannel(Session session) {
    try {
      ChannelSftp sftpChannel;
      Channel channel = session.openChannel(PROTOCOL);
      channel.connect();
      sftpChannel = (ChannelSftp) channel;
      return sftpChannel;
    } catch (JSchException e) {
      String message = "Error while opening the channel";
      log.error(message, e);
      throw new OMSBadRequestException(message);
    }
  }

  /**
   * Closing the connection.
   *
   * @param session session that needs to be closed.
   * @param channelSftp SFTP channel to be disconnect.
   */
  public void closeConnection(Session session, ChannelSftp channelSftp) {

    if (null != channelSftp) {
      channelSftp.exit();
      log.info("Successfully closed Jsch channel");
    }

    if (null != session) {
      session.disconnect();
      log.info("Successfully closed Jsch session");
    }
  }

  /**
   * Uploading report on remote vm using SFTP channel.
   *
   * @param sftpChannel Channel for SFTP protocol
   * @param localFilePath File path on local machine.
   * @param uploadPath Upload path on remote location.
   */
  public void uploadReport(ChannelSftp sftpChannel, String localFilePath, String uploadPath) {
    try {
      log.info("Upload started for local file path {} ", localFilePath);
      sftpChannel.put(localFilePath, uploadPath);
      log.info(
          "Successfully uploaded report, local file path : {}, uploadPath {} ",
          localFilePath,
          uploadPath);
    } catch (SftpException e) {
      String message =
          String.format(
              "Exception while uploading report file from local path %s, upload path %s",
              localFilePath, uploadPath);
      throw new OMSThirdPartyException(message);
    }
  }

  /**
   * Downloading report from Http url and placing on local file path.
   *
   * @param downloadUrl Download http url.
   * @param localFilePath Local file path where downloaded file will be stored.
   */
  public void downloadReport(String downloadUrl, String localFilePath) {
    log.info("Download started from Url : {} ", downloadUrl);
    downloadFile(downloadUrl, localFilePath);
    log.info("Successfully downloaded report, local file path : {} ", localFilePath);
  }

  private void downloadFile(String downloadUrl, String file) {
    try {
      FileUtils.copyURLToFile(new URL(downloadUrl), new File(file));
    } catch (IOException e) {
      String message =
          String.format("Error while downloading file name from http url : %s ", downloadUrl);
      log.error(message, e);
      throw new OMSBadRequestException(message);
    }
  }
}
