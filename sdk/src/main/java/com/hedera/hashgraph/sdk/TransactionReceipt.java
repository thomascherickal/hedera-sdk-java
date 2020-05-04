package com.hedera.hashgraph.sdk;

import com.google.common.base.MoreObjects;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.hedera.hashgraph.sdk.proto.ExchangeRateSet;
import com.hedera.hashgraph.sdk.proto.TimestampSeconds;

import javax.annotation.Nullable;

/**
 * The consensus result for a transaction, which might not be currently
 * known, or may succeed or fail.
 */
public final class TransactionReceipt {
    /**
     * Whether the transaction succeeded or failed (or is unknown).
     */
    public final Status status;

    /**
     * The exchange rate of Hbars to cents (USD).
     */
    public final ExchangeRate exchangeRate;

    /**
     * The account ID, if a new account was created.
     */
    @Nullable
    public final AccountId accountId;

    /**
     * The file ID, if a new file was created.
     */
    @Nullable
    public final FileId fileId;

    /**
     * The contract ID, if a new contract was created.
     */
    @Nullable
    public final ContractId contractId;

    /**
     * The topic ID, if a new topic was created.
     */
    @Nullable
    public final TopicId topicId;

    /**
     * Updated sequence number for a consensus service topic.
     * Set for {@link MessageSubmitTransaction}.
     */
    @Nullable
    public final Long topicSequenceNumber;

    /**
     * Updated running hash for a consensus service topic.
     * Set for {@link MessageSubmitTransaction}.
     */
    @Nullable
    public final ByteString topicRunningHash;

    private TransactionReceipt(
        Status status,
        ExchangeRate exchangeRate,
        @Nullable AccountId accountId,
        @Nullable FileId fileId,
        @Nullable ContractId contractId,
        @Nullable TopicId topicId,
        @Nullable Long topicSequenceNumber,
        @Nullable ByteString topicRunningHash
    ) {
        this.status = status;
        this.exchangeRate = exchangeRate;
        this.accountId = accountId;
        this.fileId = fileId;
        this.contractId = contractId;
        this.topicId = topicId;
        this.topicSequenceNumber = topicSequenceNumber;
        this.topicRunningHash = topicRunningHash;
    }

    static TransactionReceipt fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionReceipt transactionReceipt) {
        var status = Status.valueOf(transactionReceipt.getStatus());

        var rate = transactionReceipt.getExchangeRate();
        var exchangeRate = ExchangeRate.fromProtobuf(rate.getCurrentRate());

        var accountId =
            transactionReceipt.hasAccountID()
                ? AccountId.fromProtobuf(transactionReceipt.getAccountID())
                : null;

        var fileId =
            transactionReceipt.hasFileID()
                ? FileId.fromProtobuf(transactionReceipt.getFileID())
                : null;

        var contractId =
            transactionReceipt.hasContractID()
                ? ContractId.fromProtobuf(transactionReceipt.getContractID())
                : null;

        var topicId =
            transactionReceipt.hasTopicID()
                ? TopicId.fromProtobuf(transactionReceipt.getTopicID())
                : null;

        var topicSequenceNumber =
            transactionReceipt.getTopicSequenceNumber() == 0
                ? null
                : transactionReceipt.getTopicSequenceNumber();

        var topicRunningHash =
            transactionReceipt.getTopicRunningHash().isEmpty()
                ? null
                : transactionReceipt.getTopicRunningHash();

        return new TransactionReceipt(
            status,
            exchangeRate,
            accountId,
            fileId,
            contractId,
            topicId,
            topicSequenceNumber,
            topicRunningHash
        );
    }

    public static TransactionReceipt fromBytes(byte[] bytes) throws InvalidProtocolBufferException {
        return fromProtobuf(com.hedera.hashgraph.sdk.proto.TransactionReceipt.parseFrom(bytes).toBuilder().build());
    }

    com.hedera.hashgraph.sdk.proto.TransactionReceipt toProtobuf() {
        var transactionReceiptBuilder = com.hedera.hashgraph.sdk.proto.TransactionReceipt.newBuilder()
            .setStatus(status.code)
            .setExchangeRate(ExchangeRateSet.newBuilder()
                .setCurrentRate(com.hedera.hashgraph.sdk.proto.ExchangeRate.newBuilder()
                    .setHbarEquiv(exchangeRate.hbars)
                    .setCentEquiv(exchangeRate.cents)
                    .setExpirationTime(TimestampSeconds.newBuilder()
                        .setSeconds(exchangeRate.expirationTime.getEpochSecond())
                    )
                )
            );

        if (accountId != null) {
            transactionReceiptBuilder.setAccountID(accountId.toProtobuf());
        }

        if (fileId != null) {
            transactionReceiptBuilder.setFileID(fileId.toProtobuf());
        }

        if (contractId != null) {
            transactionReceiptBuilder.setContractID(contractId.toProtobuf());
        }

        if (topicId != null) {
            transactionReceiptBuilder.setTopicID(topicId.toProtobuf());
        }

        if (topicSequenceNumber != null) {
            transactionReceiptBuilder.setTopicSequenceNumber(topicSequenceNumber);
        }

        if (topicRunningHash != null) {
            transactionReceiptBuilder.setTopicRunningHash(topicRunningHash);
        }

        return transactionReceiptBuilder.build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
            .add("status", status)
            .add("exchangeRate", exchangeRate)
            .add("accountId", accountId)
            .add("fileId", fileId)
            .add("contractId", contractId)
            .add("topicId", topicId)
            .add("topicSequenceNumber", topicSequenceNumber)
            .toString();
    }

    public byte[] toBytes() {
        return toProtobuf().toByteArray();
    }
}
