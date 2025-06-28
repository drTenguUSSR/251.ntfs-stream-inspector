package mil.teng251.codesnippets.ntfs.future4;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.W32APIOptions;

import java.util.ArrayList;
import java.util.List;

public class Files223 {

    public static void main(String[] args) {
        String directoryPath = "D:\\INS\\251-some-links";
        //String directoryPath = "D:\\MyDocsTeng-big\\distr-windows10-2024";
        listFiles(directoryPath);
    }

    public static void listFiles(String directoryPath) {
        List<FileInfo> fileList = new ArrayList<>();

        WinBase.WIN32_FIND_DATA findData = new WinBase.WIN32_FIND_DATA();
        WinNT.HANDLE hFind = Kernel32.INSTANCE.FindFirstFileW(directoryPath + "\\*", findData);

        if (hFind == null || Pointer.nativeValue(hFind.getPointer()) == Pointer.nativeValue(Pointer.NULL)) {
            throw new IllegalStateException("Failed to get handle. Error: " + Native.getLastError());
        }

        try {
            do {
                String fileName = Native.toString(findData.cFileName);
                if (fileName.equals(".") || fileName.equals("..")) {
                    continue;
                }
                System.out.println(fileName);

                String fullPath = directoryPath + "\\" + fileName;
                FileInfo fileInfo = new FileInfo();
                fileInfo.name = fullPath;

                // Check if it's a file or directory
                fileInfo.isFile = (findData.dwFileAttributes & WinNT.FILE_ATTRIBUTE_DIRECTORY) == 0;

                // Check for hard link, junction point, and symlink
                //https://learn.microsoft.com/en-us/windows/win32/fileio/file-attribute-constants
                //https://learn.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-findfirstfilew
                //https://learn.microsoft.com/en-us/windows/win32/api/minwinbase/ns-minwinbase-win32_find_dataa
                //https://learn.microsoft.com/en-us/windows/win32/fileio/file-attribute-constants

                //A file or directory that has an associated reparse point, or a file that is a symbolic link.
                fileInfo.isHardLink = (findData.dwFileAttributes & WinNT.FILE_ATTRIBUTE_REPARSE_POINT) != 0;


                fileInfo.isJunctionPoint = (findData.dwFileAttributes & WinNT.FILE_ATTRIBUTE_DIRECTORY) != 0 &&
                        (findData.dwReserved0 == WinNT.IO_REPARSE_TAG_MOUNT_POINT);


                fileInfo.isSymlink = (findData.dwFileAttributes & WinNT.FILE_ATTRIBUTE_REPARSE_POINT) != 0 &&
                        (findData.dwReserved0 == WinNT.IO_REPARSE_TAG_SYMLINK);

                // Get the file ID for hard links
                if (fileInfo.isFile) {
                    WinNT.HANDLE hFile = Kernel32.INSTANCE.CreateFile(fullPath, WinNT.GENERIC_READ,
                            WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE, null,
                            WinNT.OPEN_EXISTING, WinNT.FILE_ATTRIBUTE_NORMAL, null);
                    if (hFile == WinBase.INVALID_HANDLE_VALUE) {
                        System.out.println("error CreateFile(" + fullPath + "): error 0x"
                                + Integer.toHexString(Kernel32.INSTANCE.GetLastError())
                                +" skip"
                        );
                    }
                    if (!WinBase.INVALID_HANDLE_VALUE.equals(hFile)) {
                        BY_HANDLE_FILE_INFORMATION fileInfoStruct = new BY_HANDLE_FILE_INFORMATION();
                        try {
                            if (Kernel32.INSTANCE.GetFileInformationByHandle(hFile, fileInfoStruct)) {
                                fileInfo.fileId = ((long) fileInfoStruct.dwFileIndexHigh << 32) | (fileInfoStruct.dwFileIndexLow & 0xFFFFFFFFL);
                                fileInfo.fileSize = ((long) fileInfoStruct.nFileSizeHigh << 32) | (fileInfoStruct.nFileSizeLow & 0xFFFFFFFFL);
                            }
                        } finally {
                            Kernel32.INSTANCE.CloseHandle(hFile);
                        }
                    }
                }

                fileList.add(fileInfo);

                // Recursively list subdirectories
                //if (!fileInfo.isFile) {
                //    listFilesAndDirectories(fullPath, fileList);
                //}

            } while (Kernel32.INSTANCE.FindNextFileW(hFind, findData));
        } finally {
            Kernel32.INSTANCE.FindClose(hFind);
        }

        System.out.println("report=[");
        for (FileInfo fileInfo : fileList) {
            System.out.println("- " + fileInfo.toString());
        }
        System.out.println("]");
    }

    public interface Kernel32 extends Library {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class, W32APIOptions.UNICODE_OPTIONS);

        int GetLastError();

        WinNT.HANDLE CreateFile(String lpFileName, int dwDesiredAccess, int dwShareMode,
                                WinBase.SECURITY_ATTRIBUTES lpSecurityAttributes,
                                int dwCreationDisposition, int dwFlagsAndAttributes,
                                WinNT.HANDLE hTemplateFile);

        boolean CloseHandle(WinNT.HANDLE hObject);

        boolean GetFileInformationByHandle(WinNT.HANDLE hFile, BY_HANDLE_FILE_INFORMATION lpFileInformation);

        WinNT.HANDLE FindFirstFileW(String lpFileName, WinBase.WIN32_FIND_DATA lpFindFileData);

        boolean FindNextFileW(WinNT.HANDLE hFindFile, WinBase.WIN32_FIND_DATA lpFindFileData);

        boolean FindClose(WinNT.HANDLE hFindFile);

    }

    public static class FileInfo {
        String name;
        boolean isFile;
        boolean isHardLink;
        boolean isJunctionPoint;
        boolean isSymlink;
        long fileId; // Unique identifier for hard links
        long fileSize;

//        @Override
//        public String toString() {
//            return String.format("| %-40s | %-6s | %-10s | %-15s | %-8s | %-15d |",
//                    name, isFile, isHardLink, isJunctionPoint, isSymlink, fileId);
//        }

        @Override
        public String toString() {
            return "FileInfo{" +
                    "name='" + name + '\'' +
                    ", isFile=" + isFile +
                    ", isHardLink=" + isHardLink +
                    ", isJunctionPoint=" + isJunctionPoint +
                    ", isSymlink=" + isSymlink +
                    ", fileId=" + fileId +
                    ", fileSize=" + fileSize +
                    '}';
        }
    }

    @Structure.FieldOrder({"dwFileAttributes", "ftCreationTime", "ftLastAccessTime", "ftLastWriteTime",
            "dwVolumeSerialNumber", "nFileSizeHigh", "nFileSizeLow", "nNumberOfLinks",
            "dwFileIndexHigh", "dwFileIndexLow"})
    public static class BY_HANDLE_FILE_INFORMATION extends Structure {
        public int dwFileAttributes;
        public WinNT.FILETIME ftCreationTime;
        public WinNT.FILETIME ftLastAccessTime;
        public WinNT.FILETIME ftLastWriteTime;
        public int dwVolumeSerialNumber;
        public int nFileSizeHigh;
        public int nFileSizeLow;
        public int nNumberOfLinks;
        public int dwFileIndexHigh;
        public int dwFileIndexLow;
    }
}
