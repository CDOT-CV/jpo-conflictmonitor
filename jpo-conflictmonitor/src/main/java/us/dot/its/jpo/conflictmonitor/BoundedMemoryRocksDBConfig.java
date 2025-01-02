package us.dot.its.jpo.conflictmonitor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.state.RocksDBConfigSetter;
import org.rocksdb.BlockBasedTableConfig;
import org.rocksdb.CompressionType;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

import java.util.Map;

// Refs:
// https://docs.confluent.io/platform/current/streams/developer-guide/memory-mgmt.html#rocksdb
// https://docs.confluent.io/platform/current/streams/developer-guide/config-streams.html#rocksdb-config-setter
@Slf4j
public class BoundedMemoryRocksDBConfig implements RocksDBConfigSetter {

    public static final long TOTAL_OFF_HEAP_MEMORY = 128 * 1024L;
    public static final double INDEX_FILTER_BLOCK_RATIO = 0.1;
    public static final long TOTAL_MEMTABLE_MEMORY = 64 * 1024L;
    public static final long BLOCK_SIZE = 16 * 1024L; // Default 4K
    public static final int N_MEMTABLES = 4;

    // Default 64MB
    public static final long MEMTABLE_SIZE = BLOCK_SIZE * N_MEMTABLES;

    static {
        RocksDB.loadLibrary();
    }

    // See #1 below
    private static final org.rocksdb.Cache cache
            = new org.rocksdb.LRUCache(TOTAL_OFF_HEAP_MEMORY, -1, false, INDEX_FILTER_BLOCK_RATIO);
    private static final org.rocksdb.WriteBufferManager writeBufferManager
            = new org.rocksdb.WriteBufferManager(TOTAL_MEMTABLE_MEMORY, cache);

    @Override
    public void setConfig(final String storeName, final Options options, final Map<String, Object> configs) {

        log.info("Setting RocksDB config for store {}", storeName);

        BlockBasedTableConfig tableConfig = (BlockBasedTableConfig) options.tableFormatConfig();

        // These three options in combination will limit the memory used by RocksDB to the size passed to the block
        // cache (TOTAL_OFF_HEAP_MEMORY)
        tableConfig.setBlockCache(cache);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        options.setWriteBufferManager(writeBufferManager);

        // These options are recommended to be set when bounding the total memory
        // See #2 below
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setPinTopLevelIndexAndFilter(true);
        // See #3 below
        tableConfig.setBlockSize(BLOCK_SIZE);
        options.setMaxWriteBufferNumber(N_MEMTABLES);
        options.setWriteBufferSize(MEMTABLE_SIZE);
        // Enable compression (optional). Compression can decrease the required storage
        // and increase the CPU usage of the machine. For CompressionType values, see
        // https://javadoc.io/static/org.rocksdb/rocksdbjni/6.4.6/org/rocksdb/CompressionType.html.
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);

        options.setTableFormatConfig(tableConfig);

        log.info("Rocksdb set table config: {}, options: {}", tableConfig, options);
    }

    @Override
    public void close(final String storeName, final Options options) {
        // Cache and WriteBufferManager should not be closed here, as the same objects are shared by every store instance.
    }
}
