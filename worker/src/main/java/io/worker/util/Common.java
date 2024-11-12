package io.worker.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.random.RandomGenerator;

public final class Common {

  public static final String TRACE_ID = "traceId";

  public static boolean isBlank(String str) {
    return !isNotBlank(str);
  }

  public static boolean isNotBlank(String str) {
    return str != null && !str.isBlank();
  }


  public static String generateRand() {
    return generateRand(generateRandBytes());
  }
  public static byte[] generateRandBytes() {
    byte[] randBytes = new byte[16];
    RNG.nextBytes(randBytes);
    randBytes[6] &= 0x0f;
    randBytes[6] |= 0x40;
    randBytes[8] &= 0x3f;
    randBytes[8] |= (byte) 0x80;
    return randBytes;
  }
  public static String generateRand(byte[] bytes) {
    long msb = 0;
    long lsb = 0;
    for (int i = 0; i < 8; i++) {
      msb = (msb << 8) | (bytes[i] & 0xff);
    }
    for (int i = 8; i < 16; i++) {
      lsb = (lsb << 8) | (bytes[i] & 0xff);
    }
    return new UUID(msb, lsb).toString();
  }

  public static LocalDateTime getTimeWithZone(String offset) {
    return LocalDateTime.ofInstant(Instant.now(), ZoneId.of(offset));
  }

  public static LocalDate utcDate(String offset) {
    return LocalDate.ofInstant(Instant.now(), ZoneId.of(offset));
  }


  public static LocalDateTime utcTime() {
    return LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC);
  }
  public static LocalDate utcDate() {
    return Instant.now().atOffset(ZoneOffset.UTC).toLocalDate();
  }
  public static Instant utcInstant() {
    return Instant.now();
  }
  static final RandomGenerator RNG = RandomGenerator.of("Xoroshiro128PlusPlus");
}
