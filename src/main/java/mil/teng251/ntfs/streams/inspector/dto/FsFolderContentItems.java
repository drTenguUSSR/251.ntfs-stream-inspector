package mil.teng251.ntfs.streams.inspector.dto;

import lombok.Value;

import java.util.List;

@Value
public class FsFolderContentItems {
    /**
     * Имя подпапапок (путь от базовой папки до целевой папки)
     * для которой собрана информация
     */
    String subPaths;

    /**
     * Перечень папок/файлов в выбранной папке
     */
    List<FsItem> items;
}
