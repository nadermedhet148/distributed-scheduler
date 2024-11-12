package io.worker.util;

public class HexConverter {

  public static String toHexWithPadding(long value) {
    char[] hexChars = new char[16];
    for (int i = 15; i >= 0; i--) {
      int v = (int) (value & 0x0F);
      hexChars[i] = HEX_ARRAY[v];
      value >>= 4;
    }
    return new String(hexChars);
  }

  private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
}