package mil.teng251.ntfs.streams.inspector;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

class CmdLineHelper {
    public static final Option OPT_SKIP_VALIDATE_INTERNET_DOWNLOAD = Option.builder("svid")
            .longOpt("skip-validate-internet-download")
            .numberOfArgs(0)
            .required(false)
            .desc("if present - skip validate 'Zone.Identifier' stream")
            .build();

    private CmdLineHelper() {
    }

    public static Options makeOptions() {
        Options options = new Options();
        options.addOption(OPT_SKIP_VALIDATE_INTERNET_DOWNLOAD);

        return options;
    }
}
