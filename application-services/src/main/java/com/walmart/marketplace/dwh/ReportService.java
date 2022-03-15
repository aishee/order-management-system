package com.walmart.marketplace.dwh;

import com.walmart.marketplace.commands.DownloadReportEventCommand;

public interface ReportService {
  void uploadReport(DownloadReportEventCommand downloadReportEventCommand);
}
