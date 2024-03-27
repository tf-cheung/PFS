package constants;

public class Constants {
    public static final int FILE_NAME_SIZE =20; // 扩展块大小为1MB
    public static final int FILE_INNIT_SIZE = 1024 * 1024; // 扩展块大小为1MB
    public static final int HEADER_BLOCKS = 16; // 头块占用的块数
    public static final int BLOCK_SIZE = 256; // 块大小为256字节
    public static final int HEADER_SIZE = HEADER_BLOCKS * BLOCK_SIZE; // 头块大小为8KB
    public static final int BLOCKS_PER_MB = (FILE_INNIT_SIZE-HEADER_SIZE) / BLOCK_SIZE; // 每MB的块数
    public static final int FCB_ENTRY_SIZE = 64; // 预留64字节用于存储每个FCB条目

    // 元数据相关常量
    public static final int METADATA_SIZE = 512; // 元数据总大小
    public static final int DATABASE_NAME_SIZE = 20; // 数据库名称大小
    public static final int DATABASE_NAME_OFFSET = 0; // 数据库名称偏移量
    public static final int DATABASE_SIZE_OFFSET = DATABASE_NAME_OFFSET + DATABASE_NAME_SIZE; // 总大小偏移量
    public static final int DATABASE_SIZE_SIZE = 8; // 总大小字段大小
    public static final int TOTAL_PFS_FILES_OFFSET = DATABASE_SIZE_OFFSET + DATABASE_SIZE_SIZE; // 总PFS文件数偏移量
    public static final int TOTAL_PFS_FILES_SIZE = 4; // 总PFS文件数字段大小
    public static final int PFS_FILES_LIST_OFFSET = TOTAL_PFS_FILES_OFFSET + TOTAL_PFS_FILES_SIZE; // PFS文件列表偏移量
    public static final int PFS_FILES_LIST_SIZE = 256; // PFS文件列表大小
    public static final int KEY_VALUE_ENTRIES_OFFSET = PFS_FILES_LIST_OFFSET + PFS_FILES_LIST_SIZE; // 键值对数量偏移量
    public static final int KEY_VALUE_ENTRIES_SIZE = 4; // 键值对数量字段大小
    public static final int CREATE_DATE_OFFSET = KEY_VALUE_ENTRIES_OFFSET + KEY_VALUE_ENTRIES_SIZE; // 创建日期偏移量
    public static final int CREATE_DATE_SIZE = 8; // 创建日期字段大小
    public static final int BLOCK_SIZE_OFFSET = CREATE_DATE_OFFSET + CREATE_DATE_SIZE; // 块大小偏移量
    public static final int BLOCK_SIZE_METADATA_SIZE = 4; // 块大小字段大小
    public static final int FCB_LIST_OFFSET = BLOCK_SIZE_OFFSET + BLOCK_SIZE_METADATA_SIZE; // FCB列表偏移量
    public static final int FCB_LIST_SIZE = METADATA_SIZE - FCB_LIST_OFFSET; // FCB列表大小
    public static final int TOTAL_BLOCK_SIZE = 8; // 位图大小
    public static final int TOTAL_BLOCK_OFFSET = FCB_LIST_OFFSET + FCB_LIST_SIZE; // 位图大小偏移量
    // 位图相关常量

    public static final int BITMAP_OFFSET = METADATA_SIZE; // 位图偏移量



}