package edgedb.internal.pipes.granularflow;

import edgedb.internal.protocol.Execute;
import edgedb.internal.protocol.ExecuteV2;
import edgedb.internal.protocol.Prepare;


public interface IGranularFlowPipe {
    public void sendPrepareMessage(Prepare prepareMessage);
    public void sendDescribeStatementMessage();
    public void sendExecuteMessage(Execute executeMessage);
    public void sendOptimisticExecuteMessage();
    public void sendExecuteMessageV2(ExecuteV2 executeMessage);
}
