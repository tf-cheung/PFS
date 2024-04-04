package manager;

import constants.Constants;
import index.BTreeIndex;
import io.FileCreator;
import metadata.MetadataHandler;
import model.FileControlBlock;
import utils.ApplicationContext;
import utils.SerializationUtils;

import java.io.IOException;
import java.io.RandomAccessFile;

public class IndexManager {

    /**
     * Reads the index data from the file based on the FCB information and returns the deserialized BTreeIndex.
     * @param database the RandomAccessFile representing the database
     * @param fileName the name of the file to read the index from
     * @return the deserialized BTreeIndex
     * @throws IOException if an I/O error occurs
     */
    public BTreeIndex readIndexFromFile(RandomAccessFile database, String fileName) throws IOException {
        FCBManager fcbManager = new FCBManager();
        FileControlBlock fcb = fcbManager.findFCBByFileName(database,fileName);


        int startPosition = (int) fcb.getIndexStartPosition();
        int endPosition = (int) fcb.getIndexEndPosition();

        // Calculate the length of the index data
        int indexDataLength = (int) (endPosition -startPosition);
        // Read the index data from the file
        byte[] indexData = new byte[indexDataLength];
        database.seek(startPosition);
        database.read(indexData);

        // Deserialize the index data
        SerializationUtils serializationUtils = new SerializationUtils();
        BTreeIndex indexTree = serializationUtils.deserializeIndexTree(indexData);
        return indexTree;
    }

    /**
     * Writes the serialized BTreeIndex to the file. It allocates contiguous blocks for the index data
     * and updates the FCB and metadata.
     * @param database the RandomAccessFile representing the database
     * @param blockManager the BlockManager instance
     * @param indexTree the BTreeIndex to write
     * @throws IOException if an I/O error occurs
     */
    public void writeIndexToFile(RandomAccessFile database, BlockManager blockManager, BTreeIndex indexTree) throws IOException {
        FCBManager fcbManager = new FCBManager();
        SerializationUtils serializationUtils = new SerializationUtils();

        // Serialize the index tree
        byte[] indexData = serializationUtils.serializeIndexTree(indexTree);
        int indexDataLength = indexData.length;
        int requiredBlocks = (int) Math.ceil((double) indexDataLength / Constants.BLOCK_SIZE);
        int[] allocatedBlocks = blockManager.allocateContiguousBlocks(requiredBlocks);
        int currentBlockIndex = blockManager.allocateBlock(0);

        if (allocatedBlocks == null) {
            // Expand the file size to accommodate the index data
            int expandUnits = (int) Math.ceil((double) indexDataLength / Constants.FILE_INNIT_SIZE);
            // Extend the file size
            FileCreator fileCreator = new FileCreator();
            fileCreator.extendFile(database,blockManager, expandUnits * Constants.FILE_INNIT_SIZE);
            // Allocate contiguous blocks for the index data
            allocatedBlocks = blockManager.allocateContiguousBlocks(requiredBlocks);

        }

        // Write the index data to the allocated blocks
        int startBlockIndex = allocatedBlocks[0];
        long indexStartPosition = (long) (startBlockIndex + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE;
        long indexEndPosition = indexStartPosition + indexDataLength;
        database.seek(indexStartPosition);
        database.write(indexData);

        // Update the FCB with the index position information
        String fileName = ApplicationContext.getCsvFileName();
        FileControlBlock fcb = fcbManager.findFCBByFileName(database,fileName);
        fcb.setIndexStartPosition(indexStartPosition);
        fcb.setIndexEndPosition(indexEndPosition);

        // Update the FCB in the metadata
        fcbManager.updateOrAddFCBInMetadata(database,fcb);
        MetadataHandler metadataHandler = new MetadataHandler(database);
        metadataHandler.updateBitmapInMetadata(blockManager.getBitmapAsBytes(),blockManager.getTotalBlocks());

    }


    /**
     * Removes the index data for the specified file. It releases the allocated blocks, clears the index data,
     * updates the FCB, and updates the bitmap in the metadata.
     * @param database the RandomAccessFile representing the database
     * @param blockManager the BlockManager instance
     * @param fileName the name of the file to remove the index for
     * @throws IOException if an I/O error occurs
     */
    public void removeIndexForFile(RandomAccessFile database, BlockManager blockManager, String fileName) throws IOException {
        FCBManager fcbManager = new FCBManager();
        FileControlBlock fcb = fcbManager.findFCBByFileName(database, fileName);

        // If the FCB has index position information
        if (fcb != null) {
            int startBlockIndex = (int) ((fcb.getIndexStartPosition() - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE);
            int endBlockIndex = (int) ((fcb.getIndexEndPosition() - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE);

            // Release the contiguous blocks
            blockManager.releaseContiguousBlocks(startBlockIndex, endBlockIndex - startBlockIndex + 1);

            // Clear the index data in the blocks
            byte[] emptyData = new byte[Constants.BLOCK_SIZE];
            for (int i = startBlockIndex; i <= endBlockIndex; i++) {
                long position = (long) (i + Constants.HEADER_BLOCKS) * Constants.BLOCK_SIZE;
                database.seek(position);
                database.write(emptyData);
            }

            // Update the FCB with the index position information
            fcb.setIndexStartPosition(0);
            fcb.setIndexEndPosition(0);
            fcbManager.updateOrAddFCBInMetadata(database, fcb);

            // Update the bitmap in the metadata
            MetadataHandler metadataHandler = new MetadataHandler(database);
            metadataHandler.updateBitmapInMetadata(blockManager.getBitmapAsBytes(), blockManager.getTotalBlocks());
        }
    }
}
