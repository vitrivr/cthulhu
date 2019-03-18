package org.vitrivr.cthulhu.runners;

import static com.google.common.collect.Iterators.asEnumeration;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class CthulhuRunnerTest {

  @Test
  public void pickInetAddressIsSuccessful() throws UnknownHostException {
    // GIVEN - IPv4 and IPv6 addressed
    // 35f1:b02f:8843:9abb:82bf:967a:34f5:ed8b
    byte[] ipv6 = new byte[] {(byte) 0x35, (byte) 0xf1, (byte) 0xb0, (byte) 0x2f, (byte) 0x88,
        (byte) 0x43, (byte) 0x9a, (byte) 0xbb, (byte) 0x82, (byte) 0xbf, (byte) 0x96, (byte) 0x7a,
        (byte) 0x34, (byte) 0xf5, (byte) 0xed, (byte) 0x8b};
    // 192.168.0.16
    byte[] ipv4 = new byte[] {(byte) 0xC0, (byte) 0xA8, (byte) 0x00, (byte) 0x10};

    List<InetAddress> addresses = new LinkedList<>();
    addresses.add(InetAddress.getByAddress(ipv6));
    addresses.add(InetAddress.getByAddress(ipv4));
    Enumeration<InetAddress> enumeratedAddresses = asEnumeration(addresses.iterator());

    // WHEN - the function is run
    Optional<String> optionalAddress = CthulhuRunner.pickInetAddress(enumeratedAddresses);

    // THEN - The IPv4 address is returned
    assertTrue(optionalAddress.isPresent());
    assertEquals("192.168.0.16", optionalAddress.get());
  }
}
