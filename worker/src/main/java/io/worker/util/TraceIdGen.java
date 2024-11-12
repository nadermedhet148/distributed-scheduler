package io.worker.util;

public final class TraceIdGen {

  public static String hexId() {
    long randomLong = Common.RNG.nextLong();
    // get the lower 14 bits of System.nanoTime()
    long timeBits = System.nanoTime() & 0x3FFF;
    // combine the two values
    long combined = randomLong ^ timeBits;
    return HexConverter.toHexWithPadding(combined);
  }
}
