package io.yggdrash.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.yggdrash.crypto.ECKey;
import io.yggdrash.crypto.HashUtil;
import io.yggdrash.util.ByteUtil;
import io.yggdrash.util.TimeUtils;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.security.SignatureException;

public class TransactionHeader implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(TransactionHeader.class);
    //todo: check Logger type for serializing(transit) or performance

    private byte[] type;
    private byte[] version;
    private byte[] dataHash;
    private long timestamp;
    private long dataSize;
    private byte[] signature;

    public TransactionHeader() {
    }

    public TransactionHeader(byte[] type,
                             byte[] version,
                             byte[] dataHash,
                             long timestamp,
                             long dataSize,
                             byte[] signature) {
        this.type = type;
        this.version = version;
        this.dataHash = dataHash;
        this.timestamp = timestamp;
        this.dataSize = dataSize;
        this.signature = signature;
    }

    /**
     * TransactionHeader Constructor.
     *
     * @param dataHash data hash
     * @param dataSize data size
     * @throws IOException IOException
     */
    public TransactionHeader(byte[] dataHash, long dataSize) throws IOException {
        if (dataHash == null) {
            throw new IOException("dataHash is not valid");
        }

        if (dataSize <= 0) {
            throw new IOException("dataSize is not valid");
        }

        this.type = new byte[4];
        this.version = new byte[4];
        this.dataHash = dataHash;
        this.dataSize = dataSize;
    }

    /**
     * @deprecated
     * TransactionHeader Constructor.
     *  - do not use Account parameter for generating TransactionHeader.
     * @param from     account for creating tx
     * @param dataHash data hash
     * @param dataSize data size
     * @throws IOException IOException
     */
    @Deprecated
    public TransactionHeader(Account from, byte[] dataHash, long dataSize) throws IOException {
        this(dataHash, dataSize);

        if (from == null || from.getKey().getPrivKeyBytes() == null) {
            throw new IOException("Account from is not valid");
        }

        this.timestamp = TimeUtils.time();
        this.signature = from.getKey().sign(getDataHashForSigning()).toBinary();
    }

    /**
     * TransactionHeader Constructor.
     *
     * @param wallet   node wallet class
     * @param dataHash data hash
     * @param dataSize data size
     * @throws IOException IOException
     */
    public TransactionHeader(Wallet wallet, byte[] dataHash, long dataSize) throws IOException {
        this(dataHash, dataSize);

        if (wallet == null || wallet.getAddress() == null) {
            throw new IOException("Wallet is not valid");
        }

        this.timestamp = TimeUtils.time();
        this.signature = wallet.signHashedData(getDataHashForSigning());
    }


    public byte[] getType() {
        return type;
    }

    public byte[] getVersion() {
        return version;
    }

    public byte[] getDataHash() {
        return dataHash;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public long getDataSize() {
        return dataSize;
    }

    /**
     * Get the transaction hash.
     *
     * @return transaction hash
     * @throws IOException IOException
     */
    public byte[] getHash() throws IOException {
        ByteArrayOutputStream transaction = new ByteArrayOutputStream();

        transaction.write(type);
        transaction.write(version);
        transaction.write(this.dataHash);
        transaction.write(ByteUtil.longToBytes(timestamp));
        transaction.write(ByteUtil.longToBytes(dataSize));
        transaction.write(this.signature);

        return HashUtil.sha3(transaction.toByteArray());
    }

    /**
     * Get the transaction hash as hex string.
     *
     * @return transaction hash as hex string
     */
    public String getHashString() throws IOException {
        return Hex.encodeHexString(this.getHash());
    }

    public void setSignature(byte[] signature) {
        this.signature = signature;
    }

    /**
     * Get transaction signature.
     *
     * @return transaction signature
     */
    public byte[] getSignature() {
        return signature;
    }

    /**
     * Get the data hash for signing.
     *
     * @return hash of sign data
     * @throws IOException IOException
     */
    public byte[] getDataHashForSigning() throws IOException {

        if (type == null) {
            throw new IOException("getDataHashForSigning(): type is null");
        }

        if (version == null) {
            throw new IOException("getDataHashForSigning(): version is null");
        }

        if (dataHash == null) {
            throw new IOException("getDataHashForSigning(): dataHash is null");
        }

        ByteArrayOutputStream transaction = new ByteArrayOutputStream();

        transaction.write(type);
        transaction.write(version);
        transaction.write(dataHash);
        transaction.write(ByteUtil.longToBytes(timestamp));
        transaction.write(ByteUtil.longToBytes(dataSize));

        return HashUtil.sha3(transaction.toByteArray());
    }

    /**
     * Get the address.
     *
     * @return address
     */
    public byte[] getAddress() throws IOException, SignatureException {
        ECKey keyFromSig = ECKey.signatureToKey(getDataHashForSigning(), signature);

        return keyFromSig.getAddress();
    }

    /**
     * Get the address.
     *
     * @return address
     */
    public String getAddressToString() throws IOException, SignatureException {
        return Hex.encodeHexString(getAddress());
    }

    /**
     * Get the public key.
     *
     * @return public key
     */
    public byte[] getPubKey() throws IOException, SignatureException {
        ECKey keyFromSig = ECKey.signatureToKey(getDataHashForSigning(), signature);

        return keyFromSig.getPubKey();
    }

    /**
     * Get ECKey(include pubKey) using sig & signData.
     *
     * @return ECKey(include pubKey)
     */
    @JsonIgnore
    public ECKey getEcKey() throws IOException, SignatureException {

        return ECKey.signatureToKey(getDataHashForSigning(), signature);
    }

    @Override
    public String toString() {
        return "TransactionHeader{"
                + "type=" + Hex.encodeHexString(type)
                + ", version=" + Hex.encodeHexString(version)
                + ", dataHash=" + Hex.encodeHexString(dataHash)
                + ", timestamp=" + timestamp
                + ", dataSize=" + dataSize
                + ", signature=" + Hex.encodeHexString(signature)
                + '}';
    }


}
