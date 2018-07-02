package io.yggdrash.core;

import io.yggdrash.crypto.HashUtil;
import io.yggdrash.util.SerializeUtils;
import io.yggdrash.util.TimeUtils;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;

/**
 * The type Block header.
 */
public class BlockHeader implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(BlockHeader.class);

    private final byte[] type;
    private final byte[] version;
    private final byte[] prevBlockHash;
    private final byte[] merkleRoot;
    private final long timestamp;
    private final long dataSize;
    private final byte[] signature;

    private final transient long index;

    private BlockHeader(Builder builder) {
        this.type = builder.type;
        this.version = builder.version;
        this.prevBlockHash = builder.prevBlockHash;
        this.merkleRoot = builder.merkleRoot;
        this.timestamp = builder.timestamp;
        this.dataSize = builder.dataSize;
        this.signature = builder.signature;

        this.index = builder.index;
    }

    /**
     * Gets index.
     *
     * @return the index
     */
    /*
     * Getter & Setter
     *
     * 객체를 최대한 캡슐화 하기 위해서 getter, setter 는 최소한으로 작성. 특히 setter 는 지양
     */
    public long getIndex() {
        return index;
    }

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Get block hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getBlockHash() {
        return HashUtil.sha256(SerializeUtils.serialize(this));
    }

    /**
     * Get prev block hash byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getPrevBlockHash() {
        return prevBlockHash;
    }

    /**
     * The type Builder.
     */
    public static class Builder {

        private byte[] type;
        private byte[] version;
        private byte[] prevBlockHash;
        private byte[] merkleRoot;
        private long timestamp;
        private long dataSize;
        private byte[] signature;

        private long index;

        /**
         * Instantiates a new Builder.
         */
        public Builder() {
            type = new byte[4];
            version = new byte[4];
        }

        /**
         * Prev block builder.
         *
         * @param prevBlock the prev block
         * @return the builder
         */
        public Builder prevBlock(Block prevBlock) {
            if (prevBlock == null) {
                this.index = 0;
                this.prevBlockHash = null;
            } else {
                this.index = prevBlock.nextIndex();
                this.prevBlockHash = prevBlock.getBlockByteHash();
            }
            return this;
        }

        /**
         * Block body builder.
         *
         * @param blockBody the block body
         * @return the builder
         */
        public Builder blockBody(BlockBody blockBody) throws IOException {
            this.merkleRoot = blockBody.getMerkleRoot();
            this.dataSize = blockBody.getSize();
            return this;
        }

        /**
         * Build block header.
         *
         * @return the block header
         */
        public BlockHeader build(Account from) {
            timestamp = TimeUtils.getCurrenttime();
            this.signature = from.getKey().sign(
                    HashUtil.sha256(SerializeUtils.serialize(this))).toByteArray();
            return new BlockHeader(this);
        }
    }
}
