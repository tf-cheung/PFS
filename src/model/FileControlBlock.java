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

        // 写入文件名
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        dataOutputStream.writeInt(fileNameBytes.length);
        dataOutputStream.write(fileNameBytes);


        // 写入起始块号
        dataOutputStream.writeInt(startBlock);

        dataOutputStream.writeInt(usedBlocks);

        // 写入文件大小
        dataOutputStream.writeInt(fileSize);

        // 写入索引地址
        dataOutputStream.writeLong(indexStartPosition);
        dataOutputStream.writeLong(indexEndPosition);

        dataOutputStream.writeLong(date.getTime());


        // 返回字节数组
        return outputStream.toByteArray();
    }

    public static FileControlBlock fromBytes(byte[] bytes) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
        DataInputStream dataInputStream = new DataInputStream(inputStream);

        // 读取文件名
        int fileNameLength = dataInputStream.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        dataInputStream.read(fileNameBytes);
        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

        // 读取起始块号
        int startBlock = dataInputStream.readInt();

        int usedBlocks = dataInputStream.readInt();

        // 读取文件大小
        int fileSize = dataInputStream.readInt();

        // 读取索引地址
        long indexStartPosition = dataInputStream.readLong();

        long indexEndPosition = dataInputStream.readLong();

        long dateTimestamp = dataInputStream.readLong();
        Date date = new Date(dateTimestamp);


        // 创建FileControlBlock对象并返回
        return new FileControlBlock(fileName,startBlock,usedBlocks, fileSize, indexStartPosition,indexEndPosition, date);
    }


}
