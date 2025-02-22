package edgedb.internal.pipes.granularflow;

import edgedb.internal.protocol.Execute;
import edgedb.internal.protocol.ExecuteV2;
import edgedb.internal.protocol.Prepare;
import edgedb.internal.protocol.client.writerV2.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class GranularFlowPipeV2 implements IGranularFlowPipe{

    ProtocolWritable protocolWritable;

    @Override
    public void sendPrepareMessage(Prepare prepareMessage) {
        log.debug("Sending prepare message {}",prepareMessage);
        protocolWritable.write(new SyncBufferWritableDecorator<>(prepareMessage));
    }

    @Override
    public void sendDescribeStatementMessage() {

    }

    @Override
    public void sendExecuteMessage(Execute executeMessage) {
        log.debug("Sending execute message {}",executeMessage);
        protocolWritable.write(new SyncBufferWritableDecorator<>(executeMessage));
    }

    @Override
    public void sendExecuteMessageV2(ExecuteV2 executeMessage) {
        log.debug("Sending execute V2 message {}",executeMessage);
        protocolWritable.write(new SyncBufferWritableDecorator<>(executeMessage));
    }

    @Override
    public void sendOptimisticExecuteMessage() {
    }
}
