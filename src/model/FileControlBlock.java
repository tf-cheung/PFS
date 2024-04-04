package model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class FileControlBlock {
    private String fileName;
    private int startBlock;
    private int usedBlocks;
    private int fileSize;
    private Date date;
    private long indexStartPosition;
    private long indexEndPosition;



    public FileControlBlock(String fileName, int startBlock, int usedBlocks, int fileSize, long indexStartPosition, long indexEndPosition, Date date) {
        this.fileName = fileName;
        this.startBlock = startBlock;
        this.usedBlocks = usedBlocks;
        this.fileSize = fileSize;
        this.indexStartPosition = indexStartPosition;
        this.indexEndPosition = indexEndPosition;
        this.date = date;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getStartBlock() {
        return startBlock;
    }

    public void setStartBlock(int startBlock) {
        this.startBlock = startBlock;
    }

    public int getUsedBlocks() {
        return usedBlocks;
    }

    public void setUsedBlocks(int usedBlocks) {
        this.usedBlocks = usedBlocks;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public long getIndexStartPosition() {
        return indexStartPosition;
    }

    public void setIndexStartPosition(long indexStartPosition) {
        this.indexStartPosition = indexStartPosition;
    }

    public long getIndexEndPosition() {
        return indexEndPosition;
    }

    public void setIndexEndPosition(long indexEndPosition) {
        this.indexEndPosition = indexEndPosition;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

        // Write the file name
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(fileNameBytes.length);
        dataOutputStream.write(fileNameBytes);


        // Write the start block
        dataOutputStream.writeInt(startBlock);

        dataOutputStream.writeInt(usedBlocks);

        // Write the file size
        dataOutputStream.writeInt(fileSize);

        // Write the index position
        dataOutputStream.writeLong(indexStartPosition);
        dataOutputStream.writeLong(indexEndPosition);

        // Write the date
        dataOutputStream.writeLong(date.getTime());

        // Return the byte array
        return outputStream.toByteArray();
    }

    public static FileControlBlock fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // Read the file name
        int fileNameLength = dataInputStream.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dataInputStream.read(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        // Read the start block
        int startBlock = dataInputStream.readInt();

        // Read the used blocks
        int usedBlocks = dataInputStream.readInt();

        // Read the file size
        int fileSize = dataInputStream.readInt();

        // Read the index position
        long indexStartPosition = dataInputStream.readLong();

        // Read the index position
        long indexEndPosition = dataInputStream.readLong();

        // Read the date
        long dateTimestamp = dataInputStream.readLong();
        Date date = new Date(dateTimestamp);


        // Return the FileControlBlock object
        return new FileControlBlock(fileName,startBlock,usedBlocks, fileSize, indexStartPosition,indexEndPosition, date);
    }


}
