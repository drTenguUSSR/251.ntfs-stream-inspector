package mil.teng251.ntfs.streams.inspector.dto;

import lombok.Value;

import java.util.List;

@Value
public class FsFolderContentStreams {
    /**
     * Имя подпапки (путь от базовой папки до целевой папки)
     * для которой собрана информация
     */
    String subPaths;

    /**
     * Переречень ntfs-потоков в выбранной папке
     */
    List<FsItemStream> items;
}
