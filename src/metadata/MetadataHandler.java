package metadata;

import constants.Constants;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.Vector;

public class MetadataHandler {
    private RandomAccessFile file;

    public MetadataHandler(RandomAccessFile file) {
        this.file = file;
    }

    // Writer methods
    public void writeInitialMetadata(String databaseName) throws IOException {
        writeDatabaseNameWithPadding(databaseName);
        writeInitialValues();
    }


    private void writeDatabaseNameWithPadding(String string) throws IOException {
        byte[] stringBytes = string.getBytes();
        int totalSize = Constants.DATABASE_NAME_SIZE;
        file.seek(Constants.DATABASE_NAME_OFFSET);
        file.write(stringBytes);
        if (stringBytes.length < totalSize) {
            file.write(new byte[totalSize - stringBytes.length]);
        }
    }

    private void writeInitialValues() throws IOException {
        writeLongValue(Constants.DATABASE_SIZE_OFFSET, Constants.FILE_INNIT_SIZE);
        writeIntValue(Constants.TOTAL_PFS_FILES_OFFSET, 0);
        writeBytePadding(Constants.PFS_FILES_LIST_OFFSET, Constants.PFS_FILES_LIST_SIZE);
        writeIntValue(Constants.KEY_VALUE_ENTRIES_OFFSET, 0);
        writeLongValue(Constants.CREATE_DATE_OFFSET, new Date().getTime());
        writeIntValue(Constants.BLOCK_SIZE_OFFSET, Constants.BLOCK_SIZE);
        writeBytePadding(Constants.FCB_LIST_OFFSET, Constants.FCB_LIST_SIZE);
    }

    private void writeIntValue(long offset, int value) throws IOException {
        file.seek(offset);
        file.writeInt(value);
    }

    private void writeLongValue(long offset, long value) throws IOException {
        file.seek(offset);
        file.writeLong(value);
    }

    private void writeBytePadding(long offset, int size) throws IOException {
        file.seek(offset);
        file.write(new byte[size]);
    }

    public void updateFileSizeInMetadata(long newSize) throws IOException {
        writeLongValue(Constants.DATABASE_SIZE_OFFSET, newSize);
    }

    public void updateBitmapInMetadata(byte[] bitmapBytes, int totalBlock) throws IOException {
        file.seek(Constants.BITMAP_OFFSET);
        file.writeInt(totalBlock);
        file.seek(Constants.BITMAP_OFFSET);
        file.write(bitmapBytes);
    }

    // Reader methods
    public String readDatabaseName() throws IOException {
        file.seek(Constants.DATABASE_NAME_OFFSET);
        byte[] buffer = new byte[Constants.DATABASE_NAME_SIZE];
        file.read(buffer);
        return new String(buffer).trim();
    }

    public long readDatabaseSize() throws IOException {
        file.seek(Constants.DATABASE_SIZE_OFFSET);
        return file.readLong();
    }

    public int readTotalPFSFiles() throws IOException {
        file.seek(Constants.TOTAL_PFS_FILES_OFFSET);
        return file.readInt();
    }

    public String readPFSFilesList() throws IOException {
        file.seek(Constants.PFS_FILES_LIST_OFFSET);
        byte[] buffer = new byte[Constants.PFS_FILES_LIST_SIZE];
        file.read(buffer);
        return new String(buffer).trim();
    }

    public int readKeyValueEntries() throws IOException {
        file.seek(Constants.KEY_VALUE_ENTRIES_OFFSET);
        return file.readInt();
    }

    public Date readCreateDate() throws IOException {
        file.seek(Constants.CREATE_DATE_OFFSET);
        return new Date(file.readLong());
    }

    public int readBlockSize() throws IOException {
        file.seek(Constants.BLOCK_SIZE_OFFSET);
        return file.readInt();
    }

    public byte[] readFCBList() throws IOException {
        file.seek(Constants.FCB_LIST_OFFSET);
        byte[] buffer = new byte[Constants.FCB_LIST_SIZE];
        file.read(buffer);
        return buffer;
    }

//    public byte[] readBitmapFromMetadata(int bitmapSize) throws IOException {
//        file.seek(Constants.BITMAP_OFFSET);
//        byte[] bitmapBytes = new byte[bitmapSize];
//        file.read(bitmapBytes);
//        return bitmapBytes;
//    }

    /**
     * Read bitmap from metadata
     * @return Vector of boolean
     */
    public Vector<Boolean> readBitmapFromMetadata () {
        Vector<Boolean> bitmap = new Vector<>();
        try {
            file.seek(Constants.TOTAL_BLOCK_OFFSET);
            int totalBlock = file.readInt();
            file.seek(Constants.BITMAP_OFFSET);
            byte[] bitmapBytes = new byte[totalBlock];
            file.read(bitmapBytes);
            for (byte b : bitmapBytes) {
                for (int i = 0; i < 8; i++) {
                    bitmap.add((b & (1 << i)) != 0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

}
