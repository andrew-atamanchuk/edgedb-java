package edgedb.client;

import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.DataResponse;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

public class ResultSetImpl implements ResultSet{
    CommandDataDescriptor data_descriptor = null;
    List<DataResponse> dataResponses = new ArrayList<>();
    @Override
    public void addResultData(DataResponse dataResponse) {
        dataResponses.add(dataResponse);
    }

    @Override
    public void setCommandDataDescriptor(CommandDataDescriptor descriptor) {
        this.data_descriptor = descriptor;
    }

    public CommandDataDescriptor getCommandDataDescriptor() {
        return data_descriptor;
    }

    public List<DataResponse> getDataResponses(){
        return dataResponses;
    }
}
