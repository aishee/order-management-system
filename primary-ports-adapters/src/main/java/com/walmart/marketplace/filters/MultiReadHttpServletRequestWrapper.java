package com.walmart.marketplace.filters;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

@Slf4j
public class MultiReadHttpServletRequestWrapper extends HttpServletRequestWrapper {
  private final String body;

  public MultiReadHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
    // So that other request method behave just like before
    super(request);

    String bodyData = "";
    InputStream inputStream = request.getInputStream();
    if (inputStream != null) {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      bodyData = getBodyData(bufferedReader);
    }
    // Store request pody content in 'body' variable
    body = bodyData;
  }

  static String encode(String key, String data) {
    try {
      String encryptionMethod = "HmacSHA256";

      SecretKeySpec secretKeySpec =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), encryptionMethod);
      Mac sha256HMAC = Mac.getInstance(encryptionMethod);
      sha256HMAC.init(secretKeySpec);
      return Hex.encodeHexString(sha256HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      log.warn(
          "Not able to Encrypt to Sha 256 using Client Key and requestBody Exception is : ", e);
      return null;
    }
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body.getBytes());
    return new ServletInputStream() {
      @Override
      public boolean isFinished() {
        return false;
      }

      @Override
      public boolean isReady() {
        return false;
      }

      @Override
      public void setReadListener(ReadListener readListener) {
        throw new UnsupportedOperationException();
      }

      public int read() {
        return byteArrayInputStream.read();
      }
    };
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(this.getInputStream()));
  }

  // Use this method to read the request body N times
  public String getBody() {
    return this.body;
  }

  private String getBodyData(BufferedReader bufferedReader) {

    String bodyData = "";
    try {
      if (bufferedReader != null) {
        bodyData = bufferedReader.lines().collect(Collectors.joining());
      }
    } catch (Exception ex) {
      log.error("Error reading the request body...", ex);
    } finally {
      if (bufferedReader != null) {
        try {
          bufferedReader.close();
        } catch (IOException ex) {
          log.error("Error closing bufferedReader...");
        }
      }
    }

    return bodyData;
  }
}
