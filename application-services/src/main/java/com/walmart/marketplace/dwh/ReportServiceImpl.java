package com.walmart.marketplace.dwh;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.walmart.common.metrics.MetricConstants;
import com.walmart.common.metrics.MetricService;
import com.walmart.marketplace.commands.DownloadReportEventCommand;
import com.walmart.marketplace.dwh.config.DWHConfig;
import com.walmart.marketplace.order.domain.uber.ReportType;
import com.walmart.oms.domain.error.exception.OMSBadRequestException;
import com.walmart.oms.domain.error.exception.OMSThirdPartyException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.retry.RetryRegistry;
import io.strati.configuration.annotation.ManagedConfiguration;
import io.strati.libs.forklift.org.apache.commons.io.FilenameUtils;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReportServiceImpl implements ReportService {

  @Autowired private ReportClient reportClient;

  @Autowired private RetryRegistry retryRegistry;

  @ManagedConfiguration private DWHConfig dwhConfig;

  @Autowired private MetricService metricService;

  @Override
  public void uploadReport(DownloadReportEventCommand downloadReportEventCommand) {

    downloadReportEventCommand
        .getReportUrls()
        .forEach(
            url ->
                executeApiWithRetries(
                    fetchAndUploadReport(url, downloadReportEventCommand.getReportType())));
  }

  private Supplier<Boolean> fetchAndUploadReport(String url, ReportType reportType) {
    return () -> {
      try {
        String localFilePath = getLocalFilePath(url);
        reportClient.downloadReport(url, localFilePath);
        return uploadReportToDWH(localFilePath, reportType);
      } finally {
        deleteLocalFile(getLocalFilePath(url));
        metricService.incrementCounterByType(
            MetricConstants.MetricCounters.BUSINESS_REPORT_INVOCATION.getCounter(),
            MetricConstants.MetricTypes.BUSINESS_REPORT.getType());
      }
    };
  }

  private String getLocalFilePath(String url) {
    String localPath = dwhConfig.getLocalDownloadDirectory() + getFileName(url);
    log.info("Download file local path : {}", localPath);
    return localPath;
  }

  private void deleteLocalFile(String localFilePath) {
    try {
      log.info("Started deleting file, local file location : {}", localFilePath);
      Files.deleteIfExists(Paths.get(localFilePath));
      log.info("Successfully deleted file, local file location : {}", localFilePath);
    } catch (IOException e) {
      String message =
          String.format("Exception while deleting file, local file location : %s", localFilePath);
      log.error(message, e);
    }
  }

  private boolean uploadReportToDWH(String localFilePath, ReportType reportType) {
    ChannelSftp sftpChannel = null;
    Session session = null;
    try {
      session =
          reportClient.getSession(
              dwhConfig.getUserName(),
              dwhConfig.getIpAddress(),
              dwhConfig.getPort(),
              dwhConfig.getRsaPath());
      sftpChannel = reportClient.getChannel(session);
      reportClient.uploadReport(sftpChannel, localFilePath, getUploadPath(reportType));
    } finally {
      reportClient.closeConnection(session, sftpChannel);
    }
    return true;
  }

  private String getUploadPath(ReportType reportType) {
    log.info("Report Type {}", reportType);
    String uploadPath = dwhConfig.getOrderHistoryUploadPath();

    if (reportType.isDownTimeReport()) {
      uploadPath = dwhConfig.getDownTimeUploadPath();
    }
    return uploadPath;
  }

  private <T> void executeApiWithRetries(Supplier<T> reportDownloadCallable) {
    try {
      Decorators.ofSupplier(reportDownloadCallable)
          .withRetry(retryRegistry.retry("UBER"))
          .decorate()
          .get();

    } catch (OMSThirdPartyException exception) {
      String exceptionType = exception.getClass().getSimpleName();
      metricService.incrementCounterByType(
          MetricConstants.MetricCounters.BUSINESS_REPORT_EXCEPTION.getCounter(), exceptionType);
      log.error("Error after all retries completed", exception);
    } catch (Exception exception) {
      String exceptionType = exception.getClass().getSimpleName();
      metricService.incrementCounterByType(
          MetricConstants.MetricCounters.BUSINESS_REPORT_EXCEPTION.getCounter(), exceptionType);
      log.error("Exception while invoking download report api retry ", exception);
    }
  }

  private String getFileName(String downloadUrl) {
    try {
      URL url = new URL(downloadUrl);
      return FilenameUtils.getName(url.getPath());
    } catch (IOException e) {
      String message =
          String.format("Error while extracting file name from url : %s ", downloadUrl);
      log.error(message, e);
      throw new OMSBadRequestException(message);
    }
  }
}
