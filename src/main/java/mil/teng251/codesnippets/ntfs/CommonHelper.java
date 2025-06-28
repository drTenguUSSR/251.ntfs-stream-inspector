package mil.teng251.codesnippets.ntfs;

import com.google.common.base.Strings;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

public class CommonHelper {
    /**
     * https://stackoverflow.com/questions/3758606/how-can-i-convert-byte-size-into-a-human-readable-format-in-java
     *
     * @param bytes
     * @return size in kilobytes, megabytes, etc
     */
    public static String humanReadableByteCountBin(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format("%.1f %cB", value / 1024.0, ci.current());
    }

    public static String dropPathSeparator(String basePath) {
        if (basePath.endsWith("\\") || basePath.endsWith("/")) {
            return basePath.substring(0, basePath.length() - 1);
        }
        return basePath;
    }

    public static String makeFullPath(String basePath, String subPath, String fileName) {
        return basePath
                + (Strings.isNullOrEmpty(subPath) ? "" : "\\" + subPath)
                + (Strings.isNullOrEmpty(fileName) ? "" : "\\" + fileName);
    }

    public static boolean isValidUtf8(byte[] data) {
        try {
            String test = new String(data, StandardCharsets.UTF_8);
            return test.equals(new String(test.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isTextData(byte[] byteArray) {
        if (isTextInEncoding(byteArray, Charset.forName("CP866"))) {
            return true;
        }
        if (isTextInEncoding(byteArray, Charset.forName("windows-1251"))) {
            return true;
        }
        if (isTextInEncoding(byteArray, Charset.forName("utf-8"))) {
            return true;
        }
        return false;
    }

    private static boolean isTextInEncoding(byte[] byteArray, Charset charset) {
        String decodedString = new String(byteArray, charset);
        return decodedString.chars().allMatch(c -> c >= 32 || c == 9 || c == 10 || c == 13);
    }
}
