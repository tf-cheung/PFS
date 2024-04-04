# PFS (Portable File System)

This project implements a Portable File System (PFS) that allows efficient storage and retrieval of data using a block-based architecture. The PFS supports various operations such as creating files, writing data to files, reading data from files, deleting files, and managing metadata.

## Features

- Block-based storage: The PFS divides the storage into fixed-size blocks, allowing efficient allocation and management of data.
- Dynamic file expansion: The PFS supports dynamic expansion of the database file size. When importing data that exceeds the current database file size, the PFS automatically expands the file size by 1 MB.
- Metadata management: The PFS maintains metadata information such as file control blocks (FCBs), bitmap, and other file-related details.
- File operations: The PFS supports creating files, writing data to files, reading data from files, and deleting files.
- Indexing: The PFS utilizes a B-Tree index to enable fast searching and retrieval of data based on keys.
- Serialization and deserialization: The PFS includes utility classes for serializing and deserializing data and index structures.
- Command-line interface: The PFS provides a command-line interface for interacting with the file system and performing various operations.


## Database Structure Overview

The database adopts a block storage model, with the initial 16 blocks (each 256 bytes) designated as header blocks. These header blocks store metadata, File Control Blocks (FCBs), and a bitmap, essential for managing the file system's metadata. This setup includes information about file locations, sizes, permissions, and the overall utilization of space.

### Header Blocks Composition

- **METADATA**: Holds key information about the file system, such as version, creation date, and additional relevant data. This is vital for defining the file system's configuration and operational guidelines.
- **FCB (File Control Block)**: Associates with each file to detail its name, starting block, the total number of blocks used, file size, and index positions (start and end), including the file's creation date.
- **Bitmap**: Employs a binary indicator for each block to denote its status (0 for free, 1 for occupied), optimizing space management by identifying free blocks for new file allocations or expansions.

### File Storage and Indexing Mechanism

Files are stored in blocks subsequent to the header blocks, with each file followed by an index detailing the file's structure and content for efficient access and retrieval. This index location is recorded in the file's FCB.

### Architectural Highlights

- **File Import and Management**: Identifies contiguous free blocks to store file data based on its size. After data entry, an index is appended, and the FCB is updated with this index position.
- **Space Management**: The bitmap is key to swiftly pinpointing free blocks for new or expanding files.
- **Rapid Data Retrieval**: FCBs allow for quick access to both file data and indexes, easing the process of reading and content retrieval.
- **Scalability and Maintainability**: The system is designed to add new files without affecting the existing setup, aided by centralized management of metadata and bitmaps for enhanced system upkeep.

This configuration offers an efficient and adaptable approach to file management and swift data access, integrating metadata, indexes, and data for optimal file storage.


## Architecture

The PFS project is organized into several packages and classes:

- `constants`: Contains the `Constants` class that defines various constants used throughout the program.
- `index`: Contains the `BTreeIndex` class that implements the B-Tree index structure for efficient data retrieval.
- `io`: Contains classes for file I/O operations, such as `BlockWriter`, `CSVReader`, and `FileCreator`.
- `manager`: Contains classes for managing different aspects of the PFS, including `BlockManager`, `FCBManager`, and `IndexManager`.
- `metadata`: Contains the `MetadataHandler` class for managing metadata information.
- `model`: Contains the `FileControlBlock` class that represents the file control block (FCB) structure.
- `utils`: Contains utility classes such as `ApplicationContext`, `SerializationUtils`, and `Tools`.
- `Main`: The entry point of the program that handles user commands and interacts with the PFS.

## Usage

To use the PFS program, follow these steps:

1. Compile the source code using a Java compiler.
2. Run the compiled program using the command-line interface.
3. Use the available commands to interact with the PFS:
    - `open <file_name>`: Opens a PFS file.
    - `put <csv_file>`: Imports data from a CSV file into the PFS.
    - `find <file_name.id>`: Finds a specific data entry in a file based on the ID.
    - `get <file_name>`: Retrieves data from a file and saves it as a CSV file.
    - `rm <file_name>`: Deletes a file from the PFS.
    - `dir`: Lists all the files in the PFS along with their details.
    - `kill <file_name>`: Deletes a PFS file.
    - `quit`: Exits the program.

## Known Limitations

- The performance of the PFS may degrade when dealing with a large number of files or extremely large file sizes.
- The PFS does not support concurrent access or file locking mechanisms.
- The error handling and exception management can be improved to provide more informative messages to the user.

## Assumptions

- The PFS assumes that the input CSV files follow a specific format, with the first line being the header and subsequent lines representing data entries.
- The PFS assumes that the file names and data entries do not exceed the maximum sizes defined in the constants.
- The PFS assumes that the user provides valid and well-formatted commands through the command-line interface.

## Future Enhancements

- Optimize the performance of the PFS for large-scale data storage and retrieval.
- Add support for concurrent access and file locking mechanisms.
- Enhance the error handling and exception management to provide more robust and user-friendly error messages.
- Implement additional features such as file permissions, user authentication, and data compression.

## Contributors

#### Group 9
- Ting Fung Cheung (NEUID: 002796566)
- Muzi Li (NEUID: 002656464)

Feel free to contribute to this project by submitting pull requests or reporting issues on the project's GitHub repository.
