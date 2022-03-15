package com.walmart.marketplace.dwh

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.walmart.oms.domain.error.exception.OMSBadRequestException
import com.walmart.oms.domain.error.exception.OMSThirdPartyException
import spock.lang.Specification

class ReportClientTest extends Specification {
    ReportClient reportClient;
    private static final String PROTOCOL = "sftp";

    def setup() {
        reportClient = new ReportClient()
    }

    def "DownloadReport Report with Exception"() {
        when:
        reportClient.downloadReport("url1", "path1")

        then:
        thrown OMSBadRequestException
    }

    def "Upload Report"() {
        ChannelSftp sftpChannel = Mock()
        String uploadPath = "abc"
        String localPath = "123"

        given:
        sftpChannel.put(localPath, uploadPath) >> null

        when:
        reportClient.uploadReport(sftpChannel, localPath, uploadPath)

        then:
        1 * sftpChannel.put(_, _)
    }

    def "Upload Report Exception"() {
        ChannelSftp sftpChannel = new ChannelSftp()
        String uploadPath = "abc"
        String localPath = "123"

        when:
        reportClient.uploadReport(sftpChannel, uploadPath, localPath)

        then:
        thrown OMSThirdPartyException
    }

    def "Get Channel Test"() {
        Session session = Mock()
        ChannelSftp channel = Mock()

        given:
        session.openChannel(PROTOCOL) >> channel
        channel.connect() >> null

        when:
        ChannelSftp channelSftp = reportClient.getChannel(session)

        then:
        assert channelSftp == channel
    }

    def "Get Channel Throws Exception"() {
        Session session = Mock()
        Channel channel = Mock()

        given:
        session.openChannel(PROTOCOL) >> channel
        channel.connect() >> { throw new JSchException() }

        when:
        reportClient.getChannel(session)

        then:
        thrown OMSBadRequestException
    }

    def "Close Connection"() {
        Session session = Mock()
        ChannelSftp channelSftp = Mock()

        given:
        channelSftp.exit() >> null
        session.disconnect() >> null

        when:
        reportClient.closeConnection(session, channelSftp)

        then:
        1 * session.disconnect()
        1 * channelSftp.exit()
    }

    def "Get Session throws exception"() {
        String username = "user"
        String ipAddress = "123"
        String rsaPath = "path"
        int port = 9090

        when:
        reportClient.getSession(username, ipAddress, port, rsaPath)

        then:
        thrown OMSBadRequestException
    }

}