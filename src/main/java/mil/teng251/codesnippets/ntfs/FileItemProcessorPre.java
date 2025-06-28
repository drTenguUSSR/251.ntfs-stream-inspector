package mil.teng251.codesnippets.ntfs;

import lombok.extern.slf4j.Slf4j;
import mil.teng251.codesnippets.ntfs.wrapper.NtfsWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileItemProcessorPre {
    private NtfsWrapper ntfsWrapper = new NtfsWrapper();

    public List<NtfsStreamInfo> fileItemProcessor(String cmdPath) throws IOException {
        Path paramPath = Paths.get(cmdPath);
        if (!Files.exists(paramPath)) {
            throw new IllegalArgumentException("path [" + cmdPath + "] not exist");
        }
        boolean isDirectory = Files.isDirectory(paramPath);
        log.debug("isDirectory={}", isDirectory);

        List<NtfsStreamInfo> streamsList = new ArrayList<>();
        if (isDirectory) {
            Set<String> allFiles;
            try (Stream<Path> streamItem = Files.list(paramPath)) {
                allFiles = streamItem
                        .filter(Files::isRegularFile)
                        .map(Path::getFileName)
                        .map(Path::toString)
                        .collect(Collectors.toSet());
            }
            allFiles.add(null);
            for (String xfile : allFiles) {
                processFile(cmdPath, xfile, streamsList);
            }
        } else {
            processFile(cmdPath, null, streamsList);
        }
        return streamsList;
    }

    /**
     * @param basePath    базовый путь
     * @param xfile       имя файла, внутри базового пути
     * @param streamsList out, полный найденный список потоков
     * @throws IOException
     */

    private void processFile(String basePath, String xfile
            , List<NtfsStreamInfo> streamsList) throws IOException {
        List<NtfsStreamInfo> stm = ntfsWrapper.getStreams(basePath, null, xfile);
        List<NtfsStreamInfo> outList = new ArrayList<>();
        log.debug("processFile: beg base={}. file={}", basePath, xfile);
        for (NtfsStreamInfo item : stm) {
            if (item.getStreamName() == null) {
                log.debug("skip main data");
                continue;
            }
            String fullWork = CommonHelper.makeFullPath(basePath, null, xfile);
            Path fullWorkPath = Paths.get(fullWork);
            if (Files.isDirectory(fullWorkPath)) {
                item.setReport("directory has a stream. name=[" + item.getStreamName() + "]");
                outList.add(item);
                continue;
            }

            fullWork += ":" + item.getStreamName();
            log.warn("fullWork={}", fullWork);
            NtfsWrapper.ReadStreamLimitedResult readResult = NtfsWrapper.readStreamLimited(fullWork, 500);
            if (readResult.isOverflow()) {
                item.setReport("stream too long");
                outList.add(item);
                continue;
            }

            if (!CommonHelper.isValidUtf8(readResult.getData())) {
                item.setReport("stream is not text. utf-8");
                outList.add(item);
                continue;
            }

            if (!CommonHelper.isTextData(readResult.getData())) {
                item.setReport("stream is not text. 866/1251/utf-8");
                outList.add(item);
            }

        }
        log.debug("processFile: end");
        streamsList.addAll(outList);
    }

}
