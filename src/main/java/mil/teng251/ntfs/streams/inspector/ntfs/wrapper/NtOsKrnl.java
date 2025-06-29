package mil.teng251.ntfs.streams.inspector.ntfs.wrapper;


import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

@SuppressWarnings("SpellCheckingInspection")
interface NtOsKrnl extends StdCallLibrary, WinNT {
    NtOsKrnl INSTANCE = Native.load("NtDll", NtOsKrnl.class, W32APIOptions.UNICODE_OPTIONS);

    int FILE_SHARE_ALL = FILE_SHARE_READ | FILE_SHARE_WRITE | FILE_SHARE_DELETE;
    int MAX_PATH = 1024;

    /**
     * return from NtQueryInformationFile
     * https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-erref/596a1078-e883-4972-9bbc-49e60bebca55
     * https://joyasystems.com/list-of-ntstatus-codes
     */
    public static final int STATUS_INFO_LENGTH_MISMATCH = 0xC0000004;
    public static final int STATUS_BUFFER_OVERFLOW = 0x80000005;
    int NTQUERYINFORMATIONFILE_FILESTREAMINFORMATION = 22;

    /**
     * <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/ntifs/nf-ntifs-ntqueryinformationfile">
     * ms docs for NtQueryInformationFile
     * </a>
     *
     * @param FileHandle
     * @param ioStatusBlock        - {@link IoStatusBlock}
     * @param fileInformation      - {@link FileStreamFullInfo}
     * @param length
     * @param fileInformationClass
     * @return
     */
    int NtQueryInformationFile(
            HANDLE FileHandle,
            IoStatusBlock ioStatusBlock,
            Pointer fileInformation,
            long length,
            int fileInformationClass);

    /**
     * <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/wdm/ns-wdm-_io_status_block">
     * MS doc for IO_STATUS_BLOCK
     * </a>
     * <pre>
     * typedef struct _IO_STATUS_BLOCK {
     *  union {
     *          NTSTATUS Status; // https://learn.microsoft.com/en-us/windows-hardware/drivers/kernel/using-ntstatus-values
     *                              0x40000000 − 0x7FFFFFFF
     *          PVOID    Pointer; // Reserved. For internal use only.
     *  };
     *  ULONG_PTR Information;
     * } IO_STATUS_BLOCK, *PIO_STATUS_BLOCK;
     * </pre>
     */
    @Structure.FieldOrder({"Status", "Information"})
    final class IoStatusBlock extends Structure implements Structure.ByReference {
        public WinDef.LONG Status;
        public BaseTSD.ULONG_PTR Information;
    }

    /**
     * <a href="https://learn.microsoft.com/en-us/windows-hardware/drivers/ddi/ntifs/ns-ntifs-_file_stream_information">
     * MS docs for file_stream_information
     * </a>
     * <pre>
     * typedef struct _FILE_STREAM_INFORMATION {
     *  ULONG         NextEntryOffset;
     *  ULONG         StreamNameLength;
     *  LARGE_INTEGER StreamSize;
     *  LARGE_INTEGER StreamAllocationSize;
     *  WCHAR         StreamName[1];
     * } FILE_STREAM_INFORMATION, *PFILE_STREAM_INFORMATION;
     * </pre>
     */
    @Structure.FieldOrder({"NextEntryOffset", "StreamNameLength", "StreamSize", "StreamAllocationSize", "StreamName"})
    final class FileStreamFullInfo extends Structure implements Structure.ByReference {
        public int NextEntryOffset; // 4 byte
        public int StreamNameLength; // 4 byte
        public WinNT.LARGE_INTEGER StreamSize; // 8 byte
        public WinNT.LARGE_INTEGER StreamAllocationSize; // 8 byte
        public char[] StreamName = new char[MAX_PATH];

        public void load(Pointer p) {
            useMemory(p);
            read();
        }
    }
}