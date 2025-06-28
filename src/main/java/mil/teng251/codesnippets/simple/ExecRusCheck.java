package mil.teng251.codesnippets.simple;

import lombok.extern.slf4j.Slf4j;
import mil.teng251.codesnippets.SnipExec;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * Проверка вывода русского текста:
 * = запуск из IDE:
 * - system.out в консоли (UTF-8) - корректно
 * - log.debug в консоли - корректно
 * - log.debug в файле - корректно
 * --------------- настройка Intellij Idea консоли: Default Encoding = UTF-8
 * By default, IntelliJ IDEA uses the system encoding to view console output.
 * <p>
 * - In the Settings dialog (Ctrl + Alt + S), select Editor | General | Console.
 * - Select the default encoding from the Default Encoding list.
 * - Click OK to apply the changes.
 * - Reopen your console.
 * ---------------
 * 
 * = запуск из cmd (866):
 * - system.out в консоли - корректно
 * - log.debug в консоли - НЕкорректно
 * - log.debug в файле - корректно
 *
 * = запуск из cmd (chcp 1251):
 * - system.out в консоли - корректно
 * - log.debug в консоли - корректно
 * - log.debug в файле - корректно
 */
@Slf4j
public class ExecRusCheck implements SnipExec {
    private static String nowToUTCString() {
        OffsetDateTime dtm = OffsetDateTime.now(ZoneOffset.UTC);
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dtm);
    }

    @Override
    public void execute(CommandLine commandLine) throws IOException {
        log.warn("RusCheck-beg");
        String msg = "BEG: " + nowToUTCString() + ". Проверка русского текста.ЯяЪ";
        log.warn("check:{}", msg);
        System.out.println(msg);
        log.warn("RusCheck-end");
    }
}
