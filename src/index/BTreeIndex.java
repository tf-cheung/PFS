package index;

public class BTreeIndex {
    private static final int NODESIZE = 5;
    private static final int M = NODESIZE+1;
    public Node root;       // root node of the B-tree
    public int height;      // height of the B-tree
    public int n;           // number of key-value pairs in the B-tree

    // helper B-tree node data type
    public static final class Node
    {
        private static int nextId = 0;  // Static variable used to generate a unique ID for each node instance.

        public int m;  // Number of children for this node. This is the count of keys (or entries) that the node currently holds.

        public Entry[] children = new Entry[M];   // Array to store children entries. Each entry can be a key-value pair in leaf nodes or a key and a reference to the next child node in internal nodes.
        public int id;                          // Unique identifier for the node, assigned automatically upon node creation.


        /**
         * Constructs a node with a specified number of children.
         * This constructor initializes the node with a given number of children and assigns a unique ID to the node.
         * The unique ID is generated sequentially, ensuring that each node receives a distinct ID.
         *
         * @param k the initial number of children (or entries) the node should have. This can be less than M, allowing the node to grow.
         */
        public Node(int k)
        {
            m = k; // Set the initial number of children (or entries).
            id = nextId++; // Assign the next unique ID to this node and increment the ID counter for the next node.
        }
    }

    // internal nodes: only use key and next
    // external nodes: only use key and value
    public static class Entry
    {
        public int key;
        public int val; // offset
        public Node next; // Reference to the next node. This is used in internal nodes to point to child nodes that may contain or are related to this key.


        /**
         * Constructs an entry with a given key, value, and reference to the next node.
         * This constructor initializes an entry that can be used either in a leaf node or an internal node of the B-tree.
         * For leaf nodes, the key and value represent the actual data stored in the B-tree.
         * For internal nodes, the key is used for navigation, and the 'next' reference points to the child node that may contain further keys or values.
         *
         * @param key The key part of the entry, used for searching within the B-tree.
         * @param val The value associated with the key; relevant for leaf nodes where actual data is stored.
         * @param next In internal nodes, this points to the next child node related to this entry. For leaf nodes, this can be null.
         */
        public Entry(int key, int val, Node next)
        {
            this.key  = key;
            this.val  = val;
            this.next = next;
        }
    }

    /**
     * Initializes an B-tree.
     */
    public BTreeIndex()
    {
        root = new Node(0);
    }

    public boolean isEmpty()
    {
        return size() == 0;
    }

    public int size()
    {
        return n;
    }

    public int height()
    {
        return height;
    }

    /**
     * Retrieves the value associated with the given key from the B-tree.
     * This method starts the search from the root of the B-tree and looks for the specified key.
     * If the key is found, the associated value is returned; otherwise, -1 is returned to indicate that the key does not exist in the tree.
     *
     * @param key The key for which the associated value is to be retrieved.
     * @return The value associated with the specified key if found; -1 if the key is not found.
     */
    public int get(int key)
    {
        return search(root, key, height);
    }

    /**
     * Recursively searches for a given key in the subtree rooted at a specified node.
     * This method prints the ID of each visited node, helping in debugging and understanding the tree traversal process.
     * It differentiates between leaf nodes (external nodes) and internal nodes during the search:
     * - In leaf nodes, it directly searches for the key among the entries.
     * - In internal nodes, it navigates through the children based on the key's value.
     *
     * @param x The current node being searched.
     * @param key The key to search for.
     * @param ht The height of the current node from the leaf level (height = 0 for leaf nodes).
     * @return The value associated with the key if found; -1 if the key is not found.
     */
    private int search(Node x, int key, int ht)
    {
        Entry[] children = x.children;
        //Visiting Node ID
//        System.out.println("Visiting Node ID: " + x.id);

        // Leaf node case: perform linear search among all entries in the node.
        if (ht == 0)
        {
            for (int j = 0; j < x.m; j++)
            {
                if (eq(key, children[j].key))
                {
                    return children[j].val; // Key found, return associated value.
                }
            }
        }

        // Internal node case: navigate to the appropriate child node based on the key's value.
        else
        {
            for (int j = 0; j < x.m; j++)
            {
                // If this is the last child or the key is less than the next child's key, search in the current child's subtree.
                if (j+1 == x.m || less(key, children[j+1].key))
                {
                    return search(children[j].next, key, ht-1);
                }
            }
        }
        return -1;// Key not found in this subtree.
    }

    /**
     * Inserts a key-value pair into the B-tree. If the key already exists, this method will overwrite its existing value.
     * This method handles the insertion process starting from the root. If necessary, it splits the root node, thereby increasing the tree's height.
     *
     * @param key The key to insert into the B-tree.
     * @param val The value associated with the key.
     */
    public void put(int key, int val)
    {
        Node u = insert(root, key, val, height); // Attempt to insert the key-value pair, which might result in a split at the root.
        n++; // Increment the number of key-value pairs in the B-tree.

        // If no split occurred at the root, insertion is complete.
        if (u == null)
        {
            return;
        }

        // Split the root: Create a new root node with two children.
        Node t = new Node(2);
        t.children[0] = new Entry(root.children[0].key, -1, root); // First child of the new root is the old root.
        t.children[1] = new Entry(u.children[0].key, -1, u); // Second child is the node returned from the split.
        root = t; // Update the root of the B-tree
        height++; // Increase the height of the B-tree as a result of splitting the root.
    }

    /**
     * Attempts to insert a key-value pair into a subtree of the B-tree. It might trigger a split of the node if it's full.
     *
     * @param h The root of the current subtree.
     * @param key The key to insert.
     * @param val The value associated with the key.
     * @param ht The height of the current node from the bottom.
     * @return A new node if the current node was split, null otherwise.
     */
    private Node insert(Node h, int key, int val, int ht)
    {
        int j;
        Entry t = new Entry(key, val, null); // Create a new entry for the key-value pair.

        // Handle insertion in a leaf node.
        if (ht == 0)
        {
            for (j = 0; j < h.m; j++)
            {
                if (less(key, h.children[j].key))// Find the correct position for the new key.
                {
                    break;
                }
            }
        }

        // Handle insertion in an internal node.
        else
        {
            for (j = 0; j < h.m; j++)
            {
                // Move to the next child if necessary, and insert recursively.
                if ((j+1 == h.m) || less(key, h.children[j+1].key))
                {
                    Node u = insert(h.children[j++].next, key, val, ht-1);
                    if (u == null)
                    {
                        return null; // No split occurred.
                    }
                    t.key = u.children[0].key; // Update the key to be the smallest key in the new node.
                    t.val = -1; // For internal nodes, val is not used.
                    t.next = u; // Link to the newly created node as a result of the split.
                    break;
                }
            }
        }

        // Shift entries to make room for the new entry.
        for (int i = h.m; i > j; i--)
        {
            h.children[i] = h.children[i-1];
        }
        h.children[j] = t; // Insert the new entry at the found position.
        h.m++;  // Increment the number of entries in the node.
        if (h.m < M)
        {
            // If the node is not full, return null.
            return null;
        }
        else
        {
            // If the node is full, split it and return the new node.
            return split(h);
        }
    }

    /**
     * Splits a node in half when it becomes too full after an insertion.
     *
     * @param h The node to split.
     * @return The new node created as a result of the split.
     */    private Node split(Node h) {
        int splitPoint = M / 2; // Determine the split point. For odd M, adjust to ensure more keys on the left.
        if (M % 2 != 0) {
            // Adjust split point for odd M to balance the split.
            splitPoint++;
        }
        Node t = new Node(splitPoint);  // Create a new node to hold the entries from the split.
        h.m = M - splitPoint; // Update the number of entries in the original node.

        // Move entries to the new node.
        for (int j = 0; j < splitPoint; j++) {
            t.children[j] = h.children[M - splitPoint + j];
        }
        return t;  // Return the new node resulting from the split.
    }


    public String toString()
    {
        return toString(root, height, "") + "\n";
    }

    /**
     * Recursively generates a string representation of a subtree.
     * This method formats each node's information, including its ID, keys, and recursively includes its children.
     *
     * @param h The current node being processed.
     * @param ht The height of the current node, with 0 indicating a leaf node.
     * @param indent A string used for formatting the output to reflect the tree's hierarchical structure.
     * @return A string representation of the subtree rooted at node h.
     */
    private String toString(Node h, int ht, String indent) {
        StringBuilder s = new StringBuilder();
        Entry[] children = h.children;

        if (ht == 0) {
            // Formatting for leaf nodes
            s.append(indent).append("Node ID: ").append(h.id).append(" Leaf child keys: [");
            for (int j = 0; j < h.m; j++) {
                s.append(children[j].key);
                if (j < h.m - 1) s.append(", ");
            }

            // Pad the representation for nodes with fewer than NODESIZE keys
            for (int j = h.m; j < NODESIZE; j++) {
                s.append(", "); // Add commas for missing children for consistent formatting
            }
            s.append("]");
            s.append("\n");
        } else {
            // Formatting for internal nodes
            s.append(indent).append("Node ID: ").append(h.id).append(" Index: [");
            for (int j = 0; j < h.m; j++) {
                s.append(children[j].key);
                if (j < h.m - 1) s.append(", ");
            }
            // Pad the representation for nodes with fewer than NODESIZE keys
            for (int j = h.m; j < NODESIZE; j++) {
                s.append(", "); // Consistent formatting for missing children
            }
            s.append("]");
            s.append("\n");

            // Recursively include string representations of child nodes
            for (int j = 0; j < h.m; j++) {
                if (children[j] != null && children[j].next != null) {
                    s.append(toString(children[j].next, ht - 1, indent + "  "));
                } else {
                    // Mark non-existent children in the representation
                    s.append(indent + "  ").append("Node ID: N/A, [N/A]\n");
                }
            }
        }
        return s.toString();
    }

    /**
     * Searches for a node by its unique ID throughout the B-tree.
     * This method traverses the tree starting from the root and checks each node's ID against the target ID.
     *
     * @param id The unique ID of the node to find.
     * @return A string with the node's details if found; "Node not found" otherwise.
     */
    public String findNodeById(int id) {
        return findNodeById(root, id, 0); // Start the search from the root node at level 0
    }

    /**
     * Recursively searches for a node by ID in a subtree.
     * If the node with the specified ID is found, this method formats and returns its details.
     *
     * @param node The current node being checked.
     * @param id The ID of the node to find.
     * @param level The current level in the tree, used for formatting the output.
     * @return A string with the node's details if found; "Node not found" otherwise.
     */
    private String findNodeById(Node node, int id, int level) {
        if (node == null) return "Node not found";

        if (node.id == id) {
            // Node found, format and return its details
            StringBuilder sb = new StringBuilder();
            sb.append("Node ID: ").append(id).append(", Level: ").append(level).append(", Keys: ");
            for (int i = 0; i < node.m; i++) {
                sb.append(node.children[i].key);
                if (i < node.m - 1) sb.append(", ");
            }

            sb.append(", Value: ");
            for (int i = 0; i < node.m; i++) {
                sb.append(node.children[i].val);
                //sb.append(node.children[i].next.children[0].val);
                if (i < node.m - 1) sb.append(", ");
            }

            return sb.toString();
        }

        // Node not found at the current level, search in child nodes
        for (int i = 0; i < node.m; i++) {
            if (node.children[i].next != null) {
                String result = findNodeById(node.children[i].next, id, level + 1);
                if (!result.equals("Node not found")) {
                    // Node found in a subtree, return its details
                    return result;
                }
            }
        }

        return "Node not found";
    }

    public static void resetNextId() {
        Node.nextId = 0;
    }
    /**
     * Determines whether the first key is less than the second key.
     * This method is used throughout the B-tree implementation to maintain order among keys,
     * ensuring that the tree's properties are preserved during insertions, deletions, and searches.
     *
     * @param k1 The first key to compare.
     * @param k2 The second key to compare.
     * @return true if the first key is less than the second key; false otherwise.
     */
    private boolean less(int k1, int k2) {
        return k1 < k2;
    }

    /**
     * Determines whether two keys are equal.
     * This method supports the B-tree's search functionality, allowing it to correctly identify when a key
     * has been found during traversal. It's also utilized in ensuring that duplicate keys are not inserted
     * into the tree, preserving the integrity of the stored data.
     *
     * @param k1 The first key to compare.
     * @param k2 The second key to compare.
     * @return true if the keys are equal; false otherwise.
     */
    private boolean eq(int k1, int k2) {
        return k1 == k2;
    }

}