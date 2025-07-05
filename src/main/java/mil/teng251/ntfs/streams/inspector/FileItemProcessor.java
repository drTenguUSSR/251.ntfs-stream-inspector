package mil.teng251.ntfs.streams.inspector;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import mil.teng251.ntfs.streams.inspector.dto.FsFolderContentItems;
import mil.teng251.ntfs.streams.inspector.dto.FsFolderContentStreams;
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

    /**
     * Подготовка отчетных данных для всех папок/файлов в указанной в папке
     *
     * @param cmdPath путь к базовой папке
     * @return список потоков и отчета по ним.
     * @throws IOException
     */
    public List<FsFolderContentStreams> createFullReportForSubfolders(String cmdPath) throws IOException {
        log.debug("loadFilesInfo: cmdPath={}", cmdPath);

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
        List<FsFolderContentStreams> res = new ArrayList<>();
        while (!unprocessedSubFolders.isEmpty()) {
            String currProcPath = unprocessedSubFolders.poll();
            log.debug("loadFilesInfo: loop-files-res: cur='{}' rest-folder='{}'", currProcPath, unprocessedSubFolders);
            List<FsItem> folderContent = processOneFolder(cmdPath, currProcPath, unprocessedSubFolders);
            List<FsItemStream> resSub = new ArrayList<>();
            for (FsItem xfile : folderContent) {
                List<FsItemStream> streamsOneFile = createReportForOneFile(cmdPath, currProcPath, xfile);
                resSub.addAll(streamsOneFile);
            }
            FsFolderContentStreams fcs = new FsFolderContentStreams(currProcPath, resSub);
            res.add(fcs);
        }
        return res;
    }

    /**
     * Загрузка файловых данных на уровне одной папки
     *
     * @param cmdPath
     * @param currProcPath
     * @param unprocessedSubFolders
     * @return
     */
    public List<FsItem> processOneFolder(String cmdPath, String currProcPath, Deque<String> unprocessedSubFolders) {
        String currPath = cmdPath + (Strings.isNullOrEmpty(currProcPath) ? "" : File.separator + currProcPath);
        log.debug("processOneFolder: {}", currPath);
        File directory = new File(currPath);
        File[] files = directory.listFiles();
        List<FsItem> folderContent = new ArrayList<>();
        if (files == null) {
            return folderContent;
        }

        for (File fdat : files) {
            FsItem subA;
            if (fdat.isFile()) {
                log.debug("iter-work: file '{}'", fdat.getName());
                subA = new FsItem(fdat.getName(), false);
                //totalFiles++;
            } else {
                String subPath = Strings.isNullOrEmpty(currProcPath)
                        ? fdat.getName()
                        : (currProcPath + File.separator + fdat.getName());
                log.debug("iter-work: folder '{}' as '{}'", fdat.getName(), subPath);
                subA = new FsItem(fdat.getName(), true);
                unprocessedSubFolders.add(subPath);
                //totalFolders++;
            }
            folderContent.add(subA);
        }
        return folderContent;
    }

    /**
     * результат: List<FsFolderContentStreams>
     * FsFolderContentStreams:=subPath:string, items:List<FsItemStream>
     * subPath - имя подпапок
     * items - список потоков для содержимого папки
     *
     * @param cmdPath
     * @param fileList
     * @return
     * @throws IOException
     */
    public List<FsFolderContentStreams> loadStreams(String cmdPath, List<FsFolderContentItems> fileList) throws IOException {
        log.debug("loadStreams BEG");
        List<FsFolderContentStreams> res = new ArrayList<>();
        for (FsFolderContentItems folderItem : fileList) {
            List<FsItem> allFiles = folderItem.getItems();
            String subPaths = folderItem.getSubPaths();
            List<FsItemStream> resSub = new ArrayList<>();
            for (FsItem xfile : allFiles) {
                List<FsItemStream> subs = createReportForOneFile(cmdPath, subPaths, xfile);
                resSub.addAll(subs);
            }
            log.debug("resSub.size={}", resSub.size());
            FsFolderContentStreams dataSub = new FsFolderContentStreams(folderItem.getSubPaths(), resSub);
            res.add(dataSub);
        }
        log.debug("loadStreams END");
        return res;
    }

    /**
     * Возвращает список потоков (с отчетами) по указанному одному файлу
     *
     * @param cmdPath  базовая папка
     * @param subPaths перечень подпапок
     * @param xfile    ссылка на конкретный файл
     * @return список NTFS-потоков с отчетами для каждого
     * @throws IOException
     */
    public List<FsItemStream> createReportForOneFile(String cmdPath, String subPaths, FsItem xfile) throws IOException {
        log.debug("load stream info from: base='{}' subPaths='{}' xfile='{}'", cmdPath, subPaths, xfile.getName());
        List<FsItemStream> resSub = new ArrayList<>();
        List<NtfsStreamInfo> allStreams = ntfsWrapper.getStreams(cmdPath, subPaths, xfile.getName());
        boolean allDetail = true;
        if (allDetail && allStreams.isEmpty()) {
            FsItemStream item = new FsItemStream(xfile, null
                    , 0
                    , "directory w/o stream"
            );
            resSub.add(item);
        }
        for (NtfsStreamInfo fileStream : allStreams) {
            if (fileStream.getStreamName() == null) {
                //continue; //skip main data
            }
            if (xfile.isFolder()) {
                FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                        , fileStream.getStreamLength()
                        , "directory has a stream. name=[" + fileStream.getStreamName() + "]"
                );
                resSub.add(item);
                continue;
            }
            String ref2Stream = CommonHelper.makeFullPath(cmdPath, subPaths, xfile.getName())
                    + (fileStream.getStreamName() == null ? "" : ":" + fileStream.getStreamName());
            log.debug("ref2Stream={}", ref2Stream);
            NtfsWrapper.ReadStreamLimitedResult readResult = NtfsWrapper.readStreamLimited(ref2Stream, 500);
            if (readResult.isOverflow()) {
                FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                        , fileStream.getStreamLength()
                        , "stream too long"
                );
                resSub.add(item);
                continue;
            }
            if (!CommonHelper.isValidUtf8(readResult.getData())) {
                FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                        , fileStream.getStreamLength()
                        , "stream is not text. utf-8");
                resSub.add(item);
                continue;
            }
            if (!CommonHelper.isTextData(readResult.getData())) {
                FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                        , fileStream.getStreamLength(), "stream is not text. 866/1251/utf-8");
                resSub.add(item);
                continue;
            }
            if (allDetail) {
                FsItemStream item = new FsItemStream(xfile, fileStream.getStreamName()
                        , fileStream.getStreamLength(), "not-suspect");
                resSub.add(item);
            }
        }
        return resSub;
    }
}
