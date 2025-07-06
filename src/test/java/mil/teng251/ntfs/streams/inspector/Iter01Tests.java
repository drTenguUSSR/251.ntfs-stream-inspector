package mil.teng251.ntfs.streams.inspector;

import mil.teng251.ntfs.streams.inspector.dto.FsFolderContentStreams;
import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

class Iter01Tests {
    @Mock
    private CommandLine commandLine;

    @InjectMocks
    private ExecNtfsStreamsInfo processor;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (closeable != null) {
            closeable.close();
        }
    }

    @Test
    void noPathParam() {
        Mockito.when(commandLine.hasOption("svid")).thenReturn(true);
        Mockito.when(commandLine.getArgList()).thenReturn(Collections.singletonList(""));
        Assertions.assertThrows(IllegalArgumentException.class, () -> processor.makeReport(commandLine));
    }

    @Test
    void fairRun() throws IOException {
        Mockito.when(commandLine.hasOption("svid")).thenReturn(true);
        Mockito.when(commandLine.getArgList()).thenReturn(Collections.singletonList("D:\\INS\\251-subfolders-iter0"));

        List<FsFolderContentStreams> result = processor.makeReport(commandLine);
        Assertions.assertEquals(1, result.size());
        FsFolderContentStreams ents = result.get(0);
        Assertions.assertEquals("", ents.getSubPaths());
        Assertions.assertEquals(4, ents.getItems().size());
    }
}
