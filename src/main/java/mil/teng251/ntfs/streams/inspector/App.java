package mil.teng251.ntfs.streams.inspector;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.List;

/**
 * run via ide
 *
 * run configuration/gradle run/VM options:
 *      -Djava.io.tmpdir=tmpFolder
 *      -Dlog4j2.configurationFile=config/log4j2.xml
 * run configuration/gradle run:
 *      run --args="-dvid D:\INS\251-subfolders-iter"
 * =============================================
 * NtfsStreamsInfo: компилировать под JDK 1.8-x64 для запуска на win-x64
 */
@Slf4j
public class App {
    //sdds - (for file)   skip default data stream
    //sef  - (for folder) skip empty folders
    private static String HELP_FOOTER_USAGE = ""
            + "\nпримеры вызова:"
            + "\n    java -jar client.jar D:\\INS\\251-ntfs-multi"
            + "\n    java -jar client.jar -svid D:\\INS\\251-ntfs-multi"
            + "";

    public static Option OPT_SKIP_VALIDATE_INTERNET_DOWNLOAD = Option.builder("svid")
            .longOpt("skip-validate-internet-download")
            .numberOfArgs(0)
            .required(false)
            .desc("if present - skip validate 'Zone.Identifier' stream")
            .build();

    public static void main(String[] args) throws IOException {
        log.debug("app-beg");
        if (args == null) {
            log.warn("call for help:\n\n\tjava -jar Client.jar ");
            return;
        }
        log.debug("app arg({})=[", args.length);
        for (int i1 = 0; i1 < args.length; i1++) {
            log.debug(" - !{}!", args[i1]);
        }
        log.debug("]");

        Options options = new Options();
        options.addOption(OPT_SKIP_VALIDATE_INTERNET_DOWNLOAD);

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);

            List<String> restParam = commandLine.getArgList();
            if (restParam.size() != 1) {
                String msg="must be only one path in arguments";
                log.error(msg);
                printHelp(options);
                System.err.println(msg);
            }
            ExecNtfsStreamsInfo exec = new ExecNtfsStreamsInfo();
            exec.execute(commandLine);
        } catch (ParseException ex) {
            printHelp(options);
            log.debug("ParseException", ex);
            return;
        }
        log.debug("app-end");
    }

    public static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar Client.jar <option> <arguments>",
                "--- ---",
                options,
                "--- ---"+HELP_FOOTER_USAGE);
    }
}
