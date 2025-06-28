package mil.teng251.codesnippets.ntfs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CommonHelperTest {
    @ParameterizedTest
    @CsvSource({
            "0, 0 B"
            , "100, 100 B"
            , "999, 999 B"
            , "1000, 1000 B"
            , "1023, 1023 B"
            , "1024, 1.0 KB"
            , "1025, 1.0 KB"
            , "12345, 12.1 KB"
            , "1_047_000, 1022.5 KB"
            , "1_048_576, 1.0 MB" // real 1Mb
            , "1_153_434, 1.1 MB"
            , "1_0123_456L, 9.7 MB"
            , "10_123_456_798L, 9.4 GB"
            , "17_184_063_488, 16.0 GB"
            , "211_106_232_532_992, 192.0 TB"
            , "1_500_000_000_000_000_000, 1.3 EB"
            , "1_777_777_777_777_777_777L, 1.5 EB"

    })
    void humanReadableByteCountBin(String pCount, String pResult) {
        String pCount2S = pCount.replaceAll("[_L]", "");
        long pCount2L = Long.parseLong(pCount2S);
        String res = CommonHelper.humanReadableByteCountBin(pCount2L);
        Assertions.assertEquals(pResult, res);
    }
}