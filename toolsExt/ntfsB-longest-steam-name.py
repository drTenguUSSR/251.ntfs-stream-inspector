#!/usr/bin/env python3
# lookup for maximum stream name
# result on win10 v1809: streamName.len=255
import hashlib
import os

BASE_FOLDER = "D:\\INS\\251-ntfsB-py\\"


def gen_sha256(val: int) -> str:
    """Generate a hex version of SHA256(str(val)).

    I went with this to guarantee that the tests are deterministic, though
    functionally this could just be a PRNG.
    """
    m = hashlib.sha256()
    m.update(str(val).encode("utf8"))

    return m.hexdigest()


def make_file_name(ct: int) -> str:
    return "fileB-" + str(ct).zfill(4) + ".txt"


def make_stream_name(ct1: int) -> str:
    return "x" * (ct1 - 1) + "Z"


if __name__ == "__main__":
    print(f"baseFolder={BASE_FOLDER}")
    try:
        for ct in range(240, 260):
            fileName = make_file_name(ct)
            streamName = make_stream_name(ct)
            fullName = BASE_FOLDER + "\\" + fileName
            print(f"filename={fileName} streamName.len={str(len(streamName))}")
            print(f"checkName={fileName}:{streamName}")
            if os.path.exists(fullName):
                os.unlink(fullName)
                print("file {} exist. unlinked".format(fullName))

            with open(fullName, "w") as f:
                f.write("w228F")

            with open(f"{fullName}:{streamName}", "w") as f:
                f.write(f"F1.{streamName}")

            print(f"iter {ct} done")
    except OSError:
        # We expect the creation of one of the named streams to fail with
        # errno 22. This is getting translated from the win32 error
        # ERROR_FILE_SYSTEM_LIMITATION.
        print(f"error on fullName={fullName}, ct={ct}")
