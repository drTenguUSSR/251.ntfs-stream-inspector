package mil.teng251.ntfs.streams.inspector;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import mil.teng251.ntfs.streams.inspector.dto.FsFolderInfo;
import mil.teng251.ntfs.streams.inspector.dto.FsItem;
import mil.teng251.ntfs.streams.inspector.dto.FsItemStream;
import mil.teng251.ntfs.streams.inspector.wrapper.NtfsWrapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Slf4j
public class FileItemProcessor {
    private NtfsWrapper ntfsWrapper = new NtfsWrapper();

    public List<FsFolderInfo> loadFilesInfo(String cmdPath) throws IOException {
        log.warn("fileItemProcessor: cmdPath={}", cmdPath);

        Path paramPath = Paths.get(cmdPath);
        if (!Files.exists(paramPath)) {
            throw new IllegalArgumentException("path [" + cmdPath + "] not exist");
        }

        if (!Files.isDirectory(paramPath)) {
            throw new IllegalArgumentException("not directory [" + cmdPath + "]");
        }
        Deque<String> unprocessedSubFolders = new ArrayDeque<>();
        unprocessedSubFolders.add("");

        //TODO: 1. try (Stream<Path> streamItem = Files.list(paramPath))
        //TODO: 2. use native win32 call
        List<FsFolderInfo> res = new ArrayList<>();
        long totalFolders = 0;
        long totalFiles = 0;
        while (!unprocessedSubFolders.isEmpty()) {
            String currProcPath = unprocessedSubFolders.poll();

            String currPath = cmdPath + (Strings.isNullOrEmpty(currProcPath) ? "" : File.separator + currProcPath);
            log.debug("processSub: {}", currPath);
            File directory = new File(currPath);
            File[] files = directory.listFiles();
            if (files == null) {
                continue;
            }

            List<FsItem> folderContent = new ArrayList<>();
            for (File fdat : files) {
                FsItem subA;

                if (fdat.isFile()) {
                    log.debug("iter-work: file '{}'", fdat.getName());
                    subA = new FsItem(fdat.getName(), false);
                    totalFiles++;
                } else {
                    String subPath = Strings.isNullOrEmpty(currProcPath)
                            ? fdat.getName()
                            : (currProcPath + File.separator + fdat.getName());
                    log.debug("iter-work: folder '{}' as '{}'", fdat.getName(), subPath);
                    subA = new FsItem(fdat.getName(), true);
                    unprocessedSubFolders.add(subPath);
                    totalFolders++;
                }
                folderContent.add(subA);
            }
            res.add(new FsFolderInfo(currProcPath, folderContent));
        }
        log.debug("summarize: files={} folders={}", totalFiles, totalFolders);
        return res;
    }

    public List<FsItemStream> loadStreams(String cmdPath, List<FsFolderInfo> fileList) throws IOException {
        log.debug("loadStreams BEG");
        List<FsItemStream> res = new ArrayList<>();
        for (FsFolderInfo folderItem : fileList) {
            List<FsItem> allFiles = folderItem.getItems();
            for (FsItem xfile : allFiles) {
                log.debug("load stream info from: base='{}' subPath='{}' xfile='{}'", cmdPath, folderItem.getSubPath(), xfile.getName());
                List<NtfsStreamInfo> allStreams = ntfsWrapper.getStreams(cmdPath, folderItem.getSubPath(), xfile.getName());
                for (NtfsStreamInfo fileStream : allStreams) {
                    if (fileStream.getStreamName() == null) {
                        //continue; //skip main data
                    }
                    if (xfile.isFolder()) {
                        FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                                , fileStream.getStreamLength()
                                , "directory has a stream. name=[" + fileStream.getStreamName() + "]"
                        );
                        res.add(item);
                        continue;
                    }
                    String ref2Stream = CommonHelper.makeFullPath(cmdPath, folderItem.getSubPath(), xfile.getName())
                            + (fileStream.getStreamName() == null ? "" : ":" + fileStream.getStreamName());
                    log.debug("ref2Stream={}", ref2Stream);
                    NtfsWrapper.ReadStreamLimitedResult readResult = NtfsWrapper.readStreamLimited(ref2Stream, 500);
                    if (readResult.isOverflow()) {
                        FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                                , fileStream.getStreamLength()
                                , "stream too long"
                        );
                        res.add(item);
                        continue;
                    }
                    if (!CommonHelper.isValidUtf8(readResult.getData())) {
                        FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                                , fileStream.getStreamLength()
                                , "stream is not text. utf-8");
                        res.add(item);
                        continue;
                    }
                    if (!CommonHelper.isTextData(readResult.getData())) {
                        FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                                , fileStream.getStreamLength(), "stream is not text. 866/1251/utf-8");
                        res.add(item);
                    }
                    FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                            , fileStream.getStreamLength(), "not-suspect");
                    res.add(item);
                }
            }
        }
        log.debug("loadStreams END");
        return res;
    }
}
