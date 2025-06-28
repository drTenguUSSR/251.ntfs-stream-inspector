package mil.teng251.codesnippets;

import org.apache.commons.cli.CommandLine;

import java.io.IOException;

public interface SnipExec {
    void execute(CommandLine commandLine) throws IOException;
}
