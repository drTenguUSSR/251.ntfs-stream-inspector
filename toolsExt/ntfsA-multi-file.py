#!/usr/bin/env python3
#autor: https://colatkinson.site/windows/ntfs/2019/05/14/alternate-data-stream-size/
# create multy files with streama in current directory
#$ python named_streams.py
#author     : a.txt 1637 b.txt 2729 c.txt 4094 d.txt 5458 e.txt 6550
#win10 v1809: a.txt 1637 b.txt 2729 c.txt 4094 d.txt 5459 e.txt 6551
import hashlib
import os
from typing import Callable

BASE_FOLDER = "D:\\INS\\251-ntfsA-py\\"

def gen_sha256(val: int) -> str:
    """Generate a hex version of SHA256(str(val)).

    I went with this to guarantee that the tests are deterministic, though
    functionally this could just be a PRNG.
    """
    m = hashlib.sha256()
    m.update(str(val).encode("utf8"))

    return m.hexdigest()


# Some truncated versions of the above


def gen_sha128(val: int) -> str:
    orig = gen_sha256(val)

    return orig[:32]


def gen_sha64(val: int) -> str:
    orig = gen_sha256(val)

    return orig[:16]


def gen_sha32(val: int) -> str:
    orig = gen_sha256(val)

    return orig[:8]


def gen_sha24(val: int) -> str:
    orig = gen_sha256(val)

    return orig[:6]


def run_test(path: str, gen_fn: Callable[[int], str]) -> None:
    path2=f"{BASE_FOLDER}{path}"
    # Remove anything leftover from a previous run
    if os.path.exists(path2):
        os.unlink(path2)

    with open(path2, "w") as f:
        f.write("F")

    ct = 0
    try:
        # Try to create up to 50000 named streams
        for ct in range(50000):
            # The form ends up being [path]:[some hex string]
            with open(f"{BASE_FOLDER}{path}:{gen_fn(ct)}", "w") as f:
                f.write("F")
    except OSError:
        # We expect the creation of one of the named streams to fail with
        # errno 22. This is getting translated from the win32 error
        # ERROR_FILE_SYSTEM_LIMITATION.
        print(path, ct)


if __name__ == "__main__":
    print(f"checking exist {BASE_FOLDER}")
    if not os.path.exists(BASE_FOLDER):
        print(f"folder {BASE_FOLDER} not exist. exit")
        exit(1)
    if not os.path.isdir(BASE_FOLDER):
        print(f"folder {BASE_FOLDER} not a folder. exit")
        exit(1)
    print("continue")

#    run_test("a.txt", gen_sha256)  # 64 characters
#    run_test("b.txt", gen_sha128)  # 32 characters
#    run_test("c.txt", gen_sha64)   # 16 characters
    run_test("d.txt", gen_sha32)   #  8 characters
#    run_test("e.txt", gen_sha24)   #  6 characters
