import os

BASE_FOLDER="D:\\INS\\251-ntfsA-py5"

if __name__ == "__main__":
    print(f"checking exist {BASE_FOLDER}")
    if not os.path.exists(BASE_FOLDER):
        print(f"folder {BASE_FOLDER} not exist. exit")
        exit(1)
    if not os.path.isdir(BASE_FOLDER):
        print(f"folder {BASE_FOLDER} not a folder. exit")
        exit(1)
    print("continue")
