package mil.teng251.ntfs.streams.inspector;

import com.google.common.base.Strings;
import de.vandermeer.asciitable.AT_Renderer;
import de.vandermeer.asciitable.AT_Row;
import de.vandermeer.asciitable.AsciiTable;
import de.vandermeer.asciitable.CWC_FixedWidth;
import de.vandermeer.asciithemes.u8.U8_Grids;
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import mil.teng251.ntfs.streams.inspector.dto.FsFolderInfo;
import mil.teng251.ntfs.streams.inspector.dto.FsItemStream;
import org.apache.commons.cli.CommandLine;
import org.apache.tika.Tika;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * for IDE run:
 * VM options:
 * -Djava.io.tmpdir=tmpFolder -Dlog4j2.configurationFile=config/log4j2.xml
 * run argument:
 * run --args="-snippetName=n-streams -path=D:\INS\251-ntfs-multi"
 * =========================================================================
 * practical limit for streamName=255 chars
 * <p>
 * https://learn.microsoft.com/en-us/windows/win32/fileio/maximum-file-path-limitation?tabs=registry
 * The Windows API has many functions that also have Unicode versions to
 * permit an extended-length path for a maximum total
 * path length of 32,767 characters
 * To specify an extended-length path, use the "\\?\" prefix. For example, "\\?\D:\very long path".
 * <p>
 * https://colatkinson.site/windows/ntfs/2019/05/14/alternate-data-stream-size/
 * $ATTRIBUTE_LIST is capped at 256kb, which is exhausted more quickly due to the longer stream names.
 * (sum for all steam name length)
 * <p>
 * find stream 2 - https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-findfirststreamw
 * ==========================================================================
 * add binary stream to existing file via powershell
 * $data = [System.IO.File]::ReadAllBytes('D:\ins\251-ntfs-multi\simple-bin.7z')
 * $data | Set-Content -Encoding Byte -Stream mm1 .\file-with-bin.txt
 */
@Slf4j
public class ExecNtfsStreamsInfo implements SnipExec {
    @Override
    public void execute(CommandLine commandLine) throws IOException {
        log.error("execute-beg");
        boolean hasDVID = commandLine.hasOption(App.OPT_SKIP_VALIDATE_INTERNET_DOWNLOAD);
        log.debug("disable-validate-internet-download={}", hasDVID);

        String cmdPath = commandLine.getArgList().get(0);

        if (Strings.isNullOrEmpty(cmdPath)) {
            throw new IllegalArgumentException("param path is null or empty!");
        }

        cmdPath = CommonHelper.dropPathSeparator(cmdPath);
        log.debug("cmdPath={} disableValidateInternetDownload={}", cmdPath, hasDVID);
        FileItemProcessor proc = new FileItemProcessor();

        List<FsFolderInfo> fileList = proc.loadFilesInfo(cmdPath);
        log.debug("fileList({})=[", fileList.size());
        for (FsFolderInfo item : fileList) {
            log.debug("- file: {}", item);
        }
        log.debug("]");

        List<FsItemStream> streamList = proc.loadStreams(cmdPath, fileList);
        log.debug("streamList({})=[", fileList.size());
        for (FsItemStream item : streamList) {
            log.debug("- stream: {}", item);
        }
        log.debug("]");

        log.error("execute-end");
    }

    //@Override
    public void execute2(CommandLine commandLine) throws IOException {
        String cmdPath = commandLine.getOptionValue("CMD_PATH");
        if (Strings.isNullOrEmpty(cmdPath)) {
            throw new IllegalArgumentException("param '-path' is null or empty!");
        }
        cmdPath = CommonHelper.dropPathSeparator(cmdPath);
        boolean cmdValidateInternet = commandLine.hasOption("VALIDATE_INTERNET_DOWNLOAD");
        int cmdAdsLimit = Integer.parseInt(commandLine.getOptionValue("LOAD_ADS_LIMIT", "0"));
        log.debug("cmdPath={} validateInternet={} cmdAdsLimit={}", cmdPath, cmdValidateInternet, cmdAdsLimit);

        FileItemProcessorPre proc = new FileItemProcessorPre();
        List<NtfsStreamInfo> streamsList = proc.fileItemProcessor(cmdPath);

        StringBuilder resultReport = new StringBuilder();
        resultReport.append(String.format("%nresult info for path=%s size=%d:", cmdPath, streamsList.size()));

        // https://github.com/vdmeer/asciitable?tab=readme-ov-file#gradle-grails
        // https://www.javatips.net/api/asciitable-master/src/test/java/de/vandermeer/asciitable/examples/AT_03_AlignmentOptions.java
        AsciiTable table = new AsciiTable();
        CWC_FixedWidth cwcFixed = new CWC_FixedWidth();
        cwcFixed.add(30).add(30).add(30).add(15);
        AT_Renderer tableRender = AT_Renderer.create().setCWC(cwcFixed);
        table.setRenderer(tableRender);
        table.getContext().setGrid(U8_Grids.borderStrongDoubleLight());

        AT_Row row;

        table.addRule();
        row = table.addRow("folder", "file", "stream", "length");
        row.getCells().get(0).getContext().setTextAlignment(TextAlignment.CENTER);
        row.getCells().get(1).getContext().setTextAlignment(TextAlignment.CENTER);
        row.getCells().get(2).getContext().setTextAlignment(TextAlignment.CENTER);
        row.getCells().get(3).getContext().setTextAlignment(TextAlignment.CENTER);
        table.addStrongRule();

        for (NtfsStreamInfo dat : streamsList) {
            row = table.addRow(
                    dat.getFolderName() == null ? "" : dat.getFolderName(),
                    dat.getFileName(),
                    dat.getStreamName() == null ? "" : dat.getStreamName(),
                    CommonHelper.humanReadableByteCountBin(dat.getStreamLength()));
            row.getCells().get(0).getContext().setTextAlignment(TextAlignment.RIGHT);
            row.getCells().get(1).getContext().setTextAlignment(TextAlignment.RIGHT);
            row.getCells().get(2).getContext().setTextAlignment(TextAlignment.RIGHT);
            row.getCells().get(3).getContext().setTextAlignment(TextAlignment.RIGHT);
            table.addRule();
            table.addRow(null, null, null, dat.getReport() == null ? "" : dat.getReport());
            table.addStrongRule();
        }

        resultReport.append("\n").append(table.render());

        log.debug("info:{}", resultReport.toString());

        //use case:
        // вывод данных об одном файле
        //-path='D:\INS\\demo-ntfs-file-streams\bravo.txt'
        //fileStreamNTFS.getStreams(null,null);
        // вывод данных о папке (и ADS в папке) + о всех вложенных файлах-папках
        //-path='D:\INS\demo-ntfs-file-streams\sub1
    }

    private void processTestFixWork(String cmdPath) {
        log.warn("processTestFixWork beg. cmdPath={}", cmdPath);
        Path datPath;
        datPath = Paths.get(cmdPath, "downloaded-from-internet.pdf"); //resp=application/pdf
        //datPath=Paths.get(cmdPath,"Zone-Identifier-one.bin"); //resp=text/plain
        if (!Files.exists(datPath)) {
            String msg = "file not exist [" + datPath.toAbsolutePath().toString() + "]";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        Tika tika = new Tika();
        try {
            String resp = tika.detect(datPath);
            log.debug("resp={}", resp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        log.warn("processTestFixWork end");
    }

}
