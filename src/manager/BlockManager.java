package manager;
import constants.Constants;
import metadata.MetadataHandler;
import utils.ApplicationContext;
import utils.Tools;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Vector;

public class BlockManager {
    private Vector<Boolean> bitmap; // 用于跟踪块的分配情况
    private int totalBlocks; // 当前文件的总块数
    private RandomAccessFile file;



    // 构造方法
    public BlockManager() {
        this.totalBlocks = (Constants.FILE_INNIT_SIZE - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE;
        this.bitmap = new Vector<>(totalBlocks);
        initializeBitmap();
    }

    public BlockManager(RandomAccessFile file) throws IOException {
        this.file = file;
        MetadataHandler metadataHandler = new MetadataHandler(file);
        long fileSize = metadataHandler.readDatabaseSize();
        this.totalBlocks = (int) ((fileSize - Constants.HEADER_SIZE) / Constants.BLOCK_SIZE);

        Vector<Boolean> bitmapFromMetadata = metadataHandler.readBitmapFromMetadata();
        if (bitmapFromMetadata != null && bitmapFromMetadata.size() == totalBlocks) {
            // 如果元数据中存在有效的bitmap,则使用它
            this.bitmap = bitmapFromMetadata;
        } else {
            // 如果元数据中不存在有效的bitmap,则创建一个新的bitmap
            this.bitmap = new Vector<>(totalBlocks);
            initializeBitmap();
        }

    }

    private void initializeBitmap() {
        for (int i = 0; i < totalBlocks; i++) {
            bitmap.add(false);
        }
    }

    /**
     * Allocates a free block starting from the specified index and returns the block index.
     * @param startIndex the index to start searching from
     * @return the allocated block index, or -1 if no free block is found
     * @throws IOException if an I/O error occurs
     */
    public synchronized int allocateBlock(int startIndex) throws IOException {
        for (int i = startIndex; i < totalBlocks; i++) {
            if (!bitmap.get(i)) {
                bitmap.set(i, true);
                return i;
            }
        }
        return -1;
    }

    /**
     * Allocates a contiguous set of free blocks and returns an array of block indices.
     * @param numBlocks the number of blocks to allocate
     * @return an array of allocated block indices, or null if not enough contiguous blocks are found
     * @throws IOException if an I/O error occurs
     */
    public synchronized int[] allocateContiguousBlocks(int numBlocks) throws IOException {
        int startIndex = 0;
        int contiguousBlocks = 0;
        int[] allocatedBlocks = new int[numBlocks];



        for (int i = 0; i < totalBlocks; i++) {
            if (!bitmap.get(i)) {
                if (contiguousBlocks == 0) {
                    startIndex = i;
                }
                contiguousBlocks++;
                if (contiguousBlocks == numBlocks) {
                    // 找到了足够的连续块
                    for (int j = 0; j < numBlocks; j++) {
                        bitmap.set(startIndex + j, true);
                        allocatedBlocks[j] = startIndex + j;
                    }
                    return allocatedBlocks;
                }
            } else {
                contiguousBlocks = 0;
            }
        }

        // 没有找到足够的连续块
        return null;
    }


    /**
     * Expands the bitmap to accommodate the new file size.
     * @param file the RandomAccessFile representing the file
     * @param newFileSize the new file size
     * @throws IOException if an I/O error occurs
     */
    public synchronized void expand(RandomAccessFile file, long newFileSize) throws IOException {

        long newTotalBlocks = (newFileSize-Constants.HEADER_SIZE) / Constants.BLOCK_SIZE;
        int oldBitmapSize = bitmap.size();

        // 创建一个新的位图向量
        Vector<Boolean> newBitmap = new Vector<>((int) newTotalBlocks);

        // 将原有位图中的块状态复制到新位图中
        for (int i = 0; i < oldBitmapSize; i++) {
            newBitmap.add(bitmap.get(i));
        }

        // 将新增的块标记为未使用状态
        for (int i = oldBitmapSize; i < newTotalBlocks; i++) {
            newBitmap.add(false);
        }

        // 用新位图替换原有位图
        this.bitmap = newBitmap;
        this.totalBlocks = (int) newTotalBlocks;

        MetadataHandler metadataHandler = new MetadataHandler(file);
        byte[] updatedBitmapBytes = getBitmapAsBytes();
        metadataHandler.updateBitmapInMetadata(updatedBitmapBytes,totalBlocks);
//        Tools.printBitmap(getBitmap(file));

    }

    public synchronized void freeBlock(int blockIndex) {
        if (blockIndex >= 0 && blockIndex < totalBlocks) {
            bitmap.set(blockIndex, false);
        }
    }


    //getter
    public int getTotalBlocks() {
        return totalBlocks;
    }
    public byte[] getBitmapAsBytes() {
        int byteCount = (int) Math.ceil(totalBlocks / 8.0);
        byte[] bytes = new byte[byteCount];

        for (int i = 0; i < totalBlocks; i++) {
            if (bitmap.get(i)) {
                bytes[i / 8] |= (1 << (i % 8));
            }
        }

        return bytes;
    }

    public int getUsedBlocks() {
        int usedBlocks = 0;
        for (boolean isUsed : bitmap) {
            if (isUsed) {
                usedBlocks++;
            }
        }
        return usedBlocks;
    }

    public long getRemainingSpace(RandomAccessFile file) throws IOException {
        int usedBlocks = 0;
        for (boolean isUsed : bitmap) {
            if (isUsed) {
                usedBlocks++;
            }
        }
        long usedSpace = (long) usedBlocks * Constants.BLOCK_SIZE;
        return file.length() - usedSpace- Constants.HEADER_SIZE;
    }
    //setter
    public void setBlockUsed(int blockIndex, boolean used) {
        bitmap.set(blockIndex, used);
    }

    //tools
    public void printBlockUsage() throws IOException {
        System.out.println("Block usage:");
        for (int i = 0; i < totalBlocks; i++) {
            System.out.println("Block " + i + ": " + (isBlockUsed(i) ? "Used" : "Free"));
        }
    }

    /**
     * Finds and returns the index of the first free block.
     * @return the index of the first free block, or -1 if no free block is found
     * @throws IOException if an I/O error occurs
     */
    public int findFirstFreeBlock() throws IOException {
        for (int i = 0; i < totalBlocks; i++) {
            if (!isBlockUsed(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the specified block is used.
     * @param blockIndex the index of the block to check
     * @return true if the block is used, false otherwise
     * @throws IOException if an I/O error occurs
     */
    public boolean isBlockUsed(int blockIndex) throws IOException {
        return bitmap.get(blockIndex);
    }

    /**
     * Releases a contiguous set of blocks starting from the start block index.
     * @param startBlock the index of the start block
     * @param numBlocks the number of blocks to release
     * @throws IOException if an I/O error occurs
     */
    public synchronized void releaseContiguousBlocks(int startBlock, int numBlocks) throws IOException {
        for (int i = startBlock; i < startBlock + numBlocks; i++) {
            if (i >= 0 && i < totalBlocks) {
                bitmap.set(i, false);
            }
        }
        RandomAccessFile file = new RandomAccessFile(ApplicationContext.getDbFileName(), "rw");
        MetadataHandler metadataHandler = new MetadataHandler(file);
        byte[] updatedBitmapBytes = getBitmapAsBytes();
        metadataHandler.updateBitmapInMetadata(updatedBitmapBytes, totalBlocks);
    }

    /**
     * Returns the number of available (free) blocks.
     * @return the number of available blocks
     */
    public int getAvailableBlocks() {
        int availableBlocks = 0;
        for (boolean isUsed : bitmap) {
            if (!isUsed) {
                availableBlocks++;
            }
        }
        return availableBlocks;
    }
}
