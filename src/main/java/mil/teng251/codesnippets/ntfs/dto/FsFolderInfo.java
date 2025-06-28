package mil.teng251.codesnippets.ntfs.dto;

import lombok.Value;

import java.util.List;

@Value
public class FsFolderInfo {
    /**
     * Имя подпапки (путь от баковой папки до целевой папки)
     * для которой собрана информация
     */
    String subPath;

    /**
     * Переречень папок/файлов в выбранной папке
     */
    List<FsItem> items;
}
