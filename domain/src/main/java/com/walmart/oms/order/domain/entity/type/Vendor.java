package com.walmart.oms.order.domain.entity.type;

import com.walmart.fms.order.domain.entity.type.Vendor.VendorType;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Enumeration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum Vendor {
  UBEREATS("UBEREATS", VendorType.UBEREATS),
  JUSTEAT("JUSTEAT", VendorType.JUSTEAT);

  private final String vendorId;
  private final VendorType vendorType;

  Vendor(String vendorId, VendorType vendorType) {
    this.vendorId = vendorId;
    this.vendorType = vendorType;
  }

  /**
   * UniqueId generator for marketPlace order
   *
   * <p>Distributed 64-bit unique ID generator inspired by Twitter Snowflake *
   */
  public enum SequenceGenerator {
    INSTANCE;
    private static final long CUSTOM_EPOCH = 1420070400000L;
    private final int totalBits = 63;
    private final int epochBits = 42;
    private final int nodeIdBits = 10;
    private final int sequenceBits = 11;
    private final int maxNodeId = (int) (Math.pow(2, nodeIdBits) - 1);
    private final int maxSequence = (int) (Math.pow(2, sequenceBits) - 1);
    private final int nodeId;
    private volatile long lastTimestamp = -1L;
    private volatile long sequence = 0L;

    // Let SequenceGenerator generate a nodeId
    SequenceGenerator() {
      this.nodeId = createNodeId();
    }

    // Get current timestamp in milliseconds, adjust for the custom epoch.
    private static long timestamp() {
      return Instant.now().toEpochMilli() - CUSTOM_EPOCH;
    }

    public synchronized long nextId() {
      long currentTimestamp = timestamp();

      if (currentTimestamp < lastTimestamp) {
        throw new IllegalStateException("Invalid System Clock!");
      }

      if (currentTimestamp == lastTimestamp) {
        sequence = (sequence + 1) & maxSequence;
        if (sequence == 0) {
          // Sequence Exhausted, wait till next millisecond.
          currentTimestamp = waitNextMillis(currentTimestamp);
        }
      } else {
        // reset sequence to start with zero for the next millisecond
        sequence = 0;
      }

      lastTimestamp = currentTimestamp;

      long id = currentTimestamp << (totalBits - epochBits);
      id |= (nodeId << (totalBits - epochBits - nodeIdBits));
      id |= sequence;
      return id;
    }

    // Block and wait till next millisecond
    private long waitNextMillis(long currentTimestamp) {
      while (currentTimestamp == lastTimestamp) {
        currentTimestamp = timestamp();
      }
      return currentTimestamp;
    }

    private int createNodeId() {
      int nodeID;
      try {
        StringBuilder sb = new StringBuilder();
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
          NetworkInterface networkInterface = networkInterfaces.nextElement();
          byte[] mac = networkInterface.getHardwareAddress();
          if (mac != null) {
            for (int i = 0; i < mac.length; i++) {
              sb.append(String.format("%02X", mac[i]));
            }
          }
        }
        nodeID = sb.toString().hashCode();
      } catch (Exception ex) {
        nodeID = (new SecureRandom().nextInt());
      }
      nodeID = nodeID & maxNodeId;
      return nodeID;
    }
  }
}
