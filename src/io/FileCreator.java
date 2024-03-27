package io;

import constants.Constants;
import manager.BlockManager;
import metadata.MetadataHandler;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileCreator {

    public FileCreator()  {
    }
    /**
     * 打开文件，如果文件不存在则创建文件
     * Open the file, if the file does not exist, create the file
     * @param databaseName 数据库名/database name
     * @return RandomAccessFile 对象/RandomAccessFile object
     * @throws IOException 打开文件时可能抛出的异常/Exceptions that may be thrown when opening a file
     */
    public RandomAccessFile openFile(String databaseName) throws IOException {
        File file = new File(databaseName);
        if (file.exists()) {
            System.out.println("Database opened.");
            return new RandomAccessFile(file, "rw");
        } else {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(databaseName, "rw");
                randomAccessFile.setLength(Constants.FILE_INNIT_SIZE);
                writeInitialMetadata(databaseName, randomAccessFile);
                randomAccessFile.seek(Constants.DATABASE_NAME_OFFSET);
                System.out.println("Database created: " + databaseName);
                return randomAccessFile;
            } catch (IOException e) {
                System.err.println("An exception occurred while creating or writing to the file: " + e.getMessage());
                throw e;
            }
        }
    }

    /**
     * Initializes and writes basic metadata to a database file. This includes the database name, initial sizes,
     * date, and placeholders for data structures. Each metadata element is placed at a specific offset in the file.
     *
     * @param databaseName The name of the database to write.
     * @param file The file to write metadata to.
     * @throws IOException If writing to the file fails.
     */
    private void writeInitialMetadata(String databaseName, RandomAccessFile file) throws IOException {
        MetadataHandler metadataHandler = new MetadataHandler(file);
        metadataHandler.writeInitialMetadata( databaseName);
    }

    public void extendFile(RandomAccessFile file, long extendSize) throws IOException {
        long currentSize = file.length();
        file.setLength(currentSize + extendSize);
        MetadataHandler metadataHandler = new MetadataHandler(file);
        metadataHandler.updateFileSizeInMetadata(currentSize + extendSize);
        System.out.println("File extended to " + (currentSize + extendSize) + " bytes.");
        BlockManager blockManager = new BlockManager(file);
        blockManager.expand(file, currentSize + extendSize);

    }
}
