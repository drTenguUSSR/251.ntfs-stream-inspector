package mil.teng251.ntfs.streams.inspector.ntfs.dto;

import lombok.Value;

/**
 * Данные об элементе в файловой системе
 */
@Value
public class FsItem {
    /**
     * Имя файла/папки
     */
    String name;
    /**
     * true - элемент является папкой
     */
    boolean folder;
}
