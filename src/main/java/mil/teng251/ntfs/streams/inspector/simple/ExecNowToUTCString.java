package mil.teng251.ntfs.streams.inspector.simple;

import lombok.extern.slf4j.Slf4j;
import mil.teng251.ntfs.streams.inspector.SnipExec;
import org.apache.commons.cli.CommandLine;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html
 * The clock implementation provided here is based on System.currentTimeMillis().
 * for IDE run:
 * VM options:
 *  -Djava.io.tmpdir=tmpFolder -Dlog4j2.configurationFile=config/log4j2.xml
 * run argument:
 *  run --args="-snippetName=utc"
 */

@Slf4j
public class ExecNowToUTCString implements SnipExec {
    private static String nowToUTCString() {
        OffsetDateTime dtm = OffsetDateTime.now(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dtm);
    }

    public static void execWithNano() {
        OffsetDateTime dtm = OffsetDateTime.of(2017, 4, 11, 23, 55,
                10, 123456789, ZoneOffset.UTC);
        String output = DateTimeFormatter.ofPattern("YYYY-MM-dd'T'HH:mm:ss.SSS").format(dtm);
        log.debug("execWithNano={}", output);//2017-04-11T23:55:10.123456789
    }

    @Override
    public void execute(CommandLine commandLine) {
        log.warn("TempNowToUTCStringExample - beg/2");
        String res = nowToUTCString();
        log.warn("expected=!2025-02-14T06:30:09.196!");
        //result:         res=!2025-02-14T07:36:06.120!
        log.warn("     res=!{}!", res);
        log.warn("TempNowToUTCStringExample - end");
    }
}
