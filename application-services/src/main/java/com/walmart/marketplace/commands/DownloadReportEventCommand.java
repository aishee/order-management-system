package com.walmart.marketplace.commands;

import com.walmart.marketplace.order.domain.uber.ReportType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DownloadReportEventCommand {

  private List<String> reportUrls;

  private ReportType reportType;
}
