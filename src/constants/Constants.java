package constants;

public class Constants {
    public static final int FILE_NAME_SIZE = 20; // File name size
    public static final int FILE_INNIT_SIZE = 1024 * 1024; // Initial file size (1MB)
    public static final int HEADER_BLOCKS = 16; // Number of header blocks
    public static final int BLOCK_SIZE = 256; // Block size (256 bytes)
    public static final int HEADER_SIZE = HEADER_BLOCKS * BLOCK_SIZE; // Header size (8KB)
    public static final int BLOCKS_PER_MB = (FILE_INNIT_SIZE - HEADER_SIZE) / BLOCK_SIZE; // Number of blocks per MB
    public static final int FCB_ENTRY_SIZE = 64; // Reserved size for each FCB entry (64 bytes)

    // Metadata related constants
    public static final int METADATA_SIZE = 512; // Total metadata size
    public static final int DATABASE_NAME_SIZE = 20; // Database name size
    public static final int DATABASE_NAME_OFFSET = 0; // Database name offset
    public static final int DATABASE_SIZE_OFFSET = DATABASE_NAME_OFFSET + DATABASE_NAME_SIZE; // Total size offset
    public static final int DATABASE_SIZE_SIZE = 8; // Total size field size
    public static final int TOTAL_BLOCK_OFFSET = DATABASE_SIZE_OFFSET + DATABASE_SIZE_SIZE; // Bitmap size offset
    public static final int TOTAL_BLOCK_SIZE = 8; // Bitmap size
    public static final int TOTAL_PFS_FILES_OFFSET = TOTAL_BLOCK_OFFSET + TOTAL_BLOCK_SIZE; // Total PFS files offset
    public static final int TOTAL_PFS_FILES_SIZE = 4; // Total PFS files field size
    public static final int PFS_FILES_LIST_OFFSET = TOTAL_PFS_FILES_OFFSET + TOTAL_PFS_FILES_SIZE; // PFS files list offset
    public static final int PFS_FILES_LIST_SIZE = 256; // PFS files list size
    public static final int KEY_VALUE_ENTRIES_OFFSET = PFS_FILES_LIST_OFFSET + PFS_FILES_LIST_SIZE; // Key-value entries offset
    public static final int KEY_VALUE_ENTRIES_SIZE = 4; // Key-value entries field size
    public static final int CREATE_DATE_OFFSET = KEY_VALUE_ENTRIES_OFFSET + KEY_VALUE_ENTRIES_SIZE; // Creation date offset
    public static final int CREATE_DATE_SIZE = 8; // Creation date field size
    public static final int BLOCK_SIZE_OFFSET = CREATE_DATE_OFFSET + CREATE_DATE_SIZE; // Block size offset
    public static final int BLOCK_SIZE_METADATA_SIZE = 4; // Block size field size
    public static final int FCB_LIST_OFFSET = BLOCK_SIZE_OFFSET + BLOCK_SIZE_METADATA_SIZE; // FCB list offset
    public static final int FCB_LIST_SIZE = METADATA_SIZE - FCB_LIST_OFFSET; // FCB list size

    // Bitmap related constants
    public static final int BITMAP_OFFSET = METADATA_SIZE; // Bitmap offset
}