package metadata;

import constants.Constants;
import utils.Tools;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Collections;
import java.util.Date;
import java.util.Vector;

public class MetadataHandler {
    private RandomAccessFile file;

    public MetadataHandler(RandomAccessFile file) {
        this.file = file;
    }

    /**
     * Writes the initial metadata to the file.
     * @param databaseName the name of the database
     * @throws IOException if an I/O error occurs
     */
    public void writeInitialMetadata(String databaseName) throws IOException {
        writeDatabaseNameWithPadding(databaseName);
        writeInitialValues();
        initializeBitmap();
    }

    /**
     * Writes the database name with padding to the file.
     * @param string the database name
     * @throws IOException if an I/O error occurs
     */
    private void writeDatabaseNameWithPadding(String string) throws IOException {
        byte[] stringBytes = string.getBytes();
        int totalSize = Constants.DATABASE_NAME_SIZE;
        file.seek(Constants.DATABASE_NAME_OFFSET);
        file.write(stringBytes);
        if (stringBytes.length < totalSize) {
            file.write(new byte[totalSize - stringBytes.length]);
        }
    }

    /**
     * Writes the initial values to the file.
     * @throws IOException if an I/O error occurs
     */
    private void writeInitialValues() throws IOException {
        writeLongValue(Constants.DATABASE_SIZE_OFFSET, Constants.FILE_INNIT_SIZE);
        writeIntValue(Constants.TOTAL_PFS_FILES_OFFSET, 0);
        writeBytePadding(Constants.PFS_FILES_LIST_OFFSET, Constants.PFS_FILES_LIST_SIZE);
        writeIntValue(Constants.KEY_VALUE_ENTRIES_OFFSET, 0);
        writeLongValue(Constants.CREATE_DATE_OFFSET, new Date().getTime());
        writeIntValue(Constants.BLOCK_SIZE_OFFSET, Constants.BLOCK_SIZE);
        writeBytePadding(Constants.FCB_LIST_OFFSET, Constants.FCB_LIST_SIZE);
        writeIntValue(Constants.TOTAL_BLOCK_OFFSET, (Constants.FILE_INNIT_SIZE-Constants.HEADER_SIZE)/ Constants.BLOCK_SIZE);
        System.out.println("Meta initial Total block: " + (Constants.FILE_INNIT_SIZE-Constants.HEADER_SIZE)/ Constants.BLOCK_SIZE);
    }

    /**
     * Writes an integer value to the file at the specified offset.
     * @param offset the offset in the file
     * @param value the integer value to write
     * @throws IOException if an I/O error occurs
     */
    private void writeIntValue(long offset, int value) throws IOException {
        file.seek(offset);
        file.writeInt(value);
    }

    /**
     * Writes a long value to the file at the specified offset.
     * @param offset the offset in the file
     * @param value the long value to write
     * @throws IOException if an I/O error occurs
     */
    private void writeLongValue(long offset, long value) throws IOException {
        file.seek(offset);
        file.writeLong(value);
    }

    /**
     * Writes byte padding to the file at the specified offset.
     * @param offset the offset in the file
     * @param size the size of the padding
     * @throws IOException if an I/O error occurs
     */
    private void writeBytePadding(long offset, int size) throws IOException {
        file.seek(offset);
        file.write(new byte[size]);
    }


    /**
     * Updates the file size in the metadata.
     * @param newSize the new file size
     * @throws IOException if an I/O error occurs
     */
    public void updateFileSizeInMetadata(long newSize) throws IOException {
        writeLongValue(Constants.DATABASE_SIZE_OFFSET, newSize);
    }


    /**
     * Updates the bitmap in the metadata.
     * @param bitmapBytes the bitmap bytes
     * @param totalBlock the total number of blocks
     * @throws IOException if an I/O error occurs
     */
    public void updateBitmapInMetadata(byte[] bitmapBytes, int totalBlock) throws IOException {
        file.seek(Constants.TOTAL_BLOCK_OFFSET);
        file.writeInt(totalBlock);
        file.seek(Constants.BITMAP_OFFSET);
        file.write(bitmapBytes);

    }


    /**
     * Reads the database name from the file.
     * @return the database name
     * @throws IOException if an I/O error occurs
     */
    public String readDatabaseName() throws IOException {
        file.seek(Constants.DATABASE_NAME_OFFSET);
        byte[] buffer = new byte[Constants.DATABASE_NAME_SIZE];
        file.read(buffer);
        return new String(buffer).trim();
    }

    /**
     * Reads the database size from the file.
     * @return the database size
     * @throws IOException if an I/O error occurs
     */
    public long readDatabaseSize() throws IOException {
        file.seek(Constants.DATABASE_SIZE_OFFSET);
        return file.readLong();
    }


    /**
     * Reads the total number of PFS files from the file.
     * @return the total number of PFS files
     * @throws IOException if an I/O error occurs
     */
    public int readTotalPFSFiles() throws IOException {
        file.seek(Constants.TOTAL_PFS_FILES_OFFSET);
        return file.readInt();
    }

    /**
     * Reads the PFS files list from the file.
     * @return the PFS files list
     * @throws IOException if an I/O error occurs
     */
    public String readPFSFilesList() throws IOException {
        file.seek(Constants.PFS_FILES_LIST_OFFSET);
        byte[] buffer = new byte[Constants.PFS_FILES_LIST_SIZE];
        file.read(buffer);
        return new String(buffer).trim();
    }


    /**
     * Reads the key-value entries from the file.
     * @return the key-value entries
     * @throws IOException if an I/O error occurs
     */
    public int readKeyValueEntries() throws IOException {
        file.seek(Constants.KEY_VALUE_ENTRIES_OFFSET);
        return file.readInt();
    }


    /**
     * Reads the creation date from the file.
     * @return the creation date
     * @throws IOException if an I/O error occurs
     */
    public Date readCreateDate() throws IOException {
        file.seek(Constants.CREATE_DATE_OFFSET);
        return new Date(file.readLong());
    }
    /**
     * Reads the block size from the file.
     * @return the block size
     * @throws IOException if an I/O error occurs
     */
    public int readBlockSize() throws IOException {
        file.seek(Constants.BLOCK_SIZE_OFFSET);
        return file.readInt();
    }

    /**
     * Reads the FCB list from the file.
     * @return the FCB list bytes
     * @throws IOException if an I/O error occurs
     */
    public byte[] readFCBList() throws IOException {
        file.seek(Constants.FCB_LIST_OFFSET);
        byte[] buffer = new byte[Constants.FCB_LIST_SIZE];
        file.read(buffer);
        return buffer;
    }

    /**
     * Reads the total number of blocks from the file.
     * @return the total number of blocks
     * @throws IOException if an I/O error occurs
     */
    public int readTotalBlock() throws IOException {
        file.seek(Constants.TOTAL_BLOCK_OFFSET);
        return file.readInt();
    }
    /**
     * Reads the bitmap from the metadata.
     * @return the bitmap as a vector of booleans
     */
    public Vector<Boolean> readBitmapFromMetadata() {
        Vector<Boolean> bitmap = new Vector<>();
        try {
            file.seek(Constants.TOTAL_BLOCK_OFFSET);
            int totalBlock = file.readInt();
            int byteCount = (int) Math.ceil(totalBlock / 8.0);
            file.seek(Constants.BITMAP_OFFSET);
            byte[] bitmapBytes = new byte[byteCount];
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

    /**
     * Initializes the bitmap in the metadata.
     * @throws IOException if an I/O error occurs
     */
    private void initializeBitmap() throws IOException {
        int totalBlocks = readTotalBlock();
        Vector<Boolean> bitmap = new Vector<>(Collections.nCopies(totalBlocks, false));
        byte[] bitmapBytes = Tools.getBitmapAsBytes(bitmap);
        updateBitmapInMetadata(bitmapBytes, totalBlocks);
    }
}
