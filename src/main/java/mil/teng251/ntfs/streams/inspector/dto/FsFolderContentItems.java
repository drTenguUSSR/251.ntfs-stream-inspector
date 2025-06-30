package mil.teng251.ntfs.streams.inspector.dto;

import lombok.Value;

import java.util.List;

@Value
public class FsFolderContentItems {
    /**
     * Имя подпапки (путь от баковой папки до целевой папки)
     * для которой собрана информация
     */
    String subPaths;

    /**
     * Переречень папок/файлов в выбранной папке
     */
    List<FsItem> items;
}
