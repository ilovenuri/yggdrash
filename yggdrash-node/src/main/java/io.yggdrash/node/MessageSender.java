/*
 * Copyright 2018 Akashic Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.yggdrash.node;

import io.yggdrash.core.Block;
import io.yggdrash.core.NodeEventListener;
import io.yggdrash.core.Transaction;
import io.yggdrash.core.mapper.BlockMapper;
import io.yggdrash.core.mapper.TransactionMapper;
import io.yggdrash.core.net.NodeSyncClient;
import io.yggdrash.proto.BlockChainProto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageSender implements DisposableBean, NodeEventListener {
    private static final Logger log = LoggerFactory.getLogger(MessageSender.class);

    @Value("${grpc.port}")
    private int grpcPort;

    private NodeSyncClient nodeSyncClient;

    @PostConstruct
    public void init() {
        int port = grpcPort == 9090 ? 9091 : 9090;
        log.info("Connecting gRPC Server at [{}]", port);
        nodeSyncClient = new NodeSyncClient("localhost", port);
    }

    void ping() {
        nodeSyncClient.ping("Ping");
    }

    @Override
    @PreDestroy
    public void destroy() {
        nodeSyncClient.stop();
    }

    @Override
    public void newTransaction(Transaction tx) {
        BlockChainProto.Transaction protoTx
                = TransactionMapper.transactionToProtoTransaction(tx);
        BlockChainProto.Transaction[] txns = new BlockChainProto.Transaction[] {protoTx};
        nodeSyncClient.broadcastTransaction(txns);
    }

    @Override
    public void newBlock(Block block) {
        BlockChainProto.Block[] blocks
                = new BlockChainProto.Block[] {BlockMapper.blockToProtoBlock(block)};
        nodeSyncClient.broadcastBlock(blocks);
    }

    /**
     * Sync block list.
     *
     * @param offset the offset
     * @return the block list
     */
    @Override
    public List<Block> syncBlock(long offset) throws IOException {
        List<BlockChainProto.Block> blockList = nodeSyncClient.syncBlock(offset);
        log.debug("Synchronize block offset=" + offset);
        if (blockList == null || blockList.isEmpty()) {
            return Collections.emptyList();
        }
        log.debug("Synchronize block received=" + blockList.size());
        List<Block> syncList = new ArrayList<>(blockList.size());
        for (BlockChainProto.Block block : blockList) {
            syncList.add(BlockMapper.protoBlockToBlock(block));
        }
        return syncList;
    }

    @Override
    public List<Transaction> syncTransaction() throws IOException {
        List<BlockChainProto.Transaction> txList = nodeSyncClient.syncTransaction();
        log.debug("Synchronize transaction received=" + txList.size());
        List<Transaction> syncList = new ArrayList<>(txList.size());
        for (BlockChainProto.Transaction tx : txList) {
            syncList.add(TransactionMapper.protoTransactionToTransaction(tx));
        }
        return syncList;
    }
}
