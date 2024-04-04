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
     * Open the file, if the file does not exist, create the file
     * @param databaseName database name
     * @return RandomAccessFile object
     * @throws IOException Exceptions that may be thrown when opening a file
     */
    public RandomAccessFile openFile(String databaseName) throws IOException {
        File file = new File(databaseName);

        // If the file exists, open the file
        if (file.exists()) {
            System.out.println("Database opened.");
            return new RandomAccessFile(file, "rw");
        } else {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(databaseName, "rw"); // Open the file in read-write mode
                randomAccessFile.setLength(Constants.FILE_INNIT_SIZE);// Set the initial size of the file
                writeInitialMetadata(databaseName, randomAccessFile);// Write the initial metadata to the file
                randomAccessFile.seek(Constants.DATABASE_NAME_OFFSET);// Move the file pointer to the beginning of the file
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
        metadataHandler.writeInitialMetadata( databaseName); // Write the initial metadata to the file
    }

    public void extendFile(RandomAccessFile file,BlockManager blockManager,  long extendSize) throws IOException {
        long currentSize = file.length(); // Get the current size of the file
        file.setLength(currentSize + extendSize); // Extend the file to the new size
        MetadataHandler metadataHandler = new MetadataHandler(file); // Get the metadata handler
        metadataHandler.updateFileSizeInMetadata(currentSize + extendSize); // Update the file size in the metadata
        System.out.println("File extended to " + (currentSize + extendSize) + " bytes."); // Print the new file size
        blockManager.expand(file, currentSize + extendSize); // Expand the block manager to accommodate the new size

    }
}
