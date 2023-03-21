package edgedb.client;


import edgedb.connection.BlockingConnection;
import edgedb.connection.IConnection;
import edgedb.connectionparams.ConnectionParams;
import edgedb.exceptions.*;
import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.DataElement;
import edgedb.internal.protocol.DataResponse;
import edgedb.internal.protocol.typedescriptor.IDataContainer;
import edgedb.internal.protocol.typedescriptor.ObjectShapeDescriptor;
import edgedb.internal.protocol.typedescriptor.ShapeElement;
import edgedb.internal.protocol.typedescriptor.TypeDescriptor;
import edgedb.internal.protocol.typedescriptor.decoder.TypeDecoderFactoryImpl;
import edgedb.internal.protocol.utility.UUIDUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;

@Slf4j
public class EdgeDBClientV2Test {

    public static void main(String... args){
        EdgeDBClientV2Test edgedb_test = new EdgeDBClientV2Test();
        edgedb_test.TestGranularFlow();
    }

    @Test
    public void TestGetConnection() {
        EdgeDBClientV2 clientV2 = new EdgeDBClientV2(new BlockingConnection());
        try {
            clientV2.getConnection(new ConnectionParams());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestTerminateConnection() {
        EdgeDBClientV2 clientV2 = new EdgeDBClientV2(new BlockingConnection());
        try {
            clientV2.getConnection(new ConnectionParams());
            clientV2.terminateConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void TestGranularFlow(){
        EdgeDBClientV2 clientV2 = new EdgeDBClientV2(new BlockingConnection());
        String query = "select Person {id, name, last_name, profession, birth, age, best_friend}";
        query = "select Person {name, last_name, best_friend :{name, last_name}, bags :{name, volume, @ownership, @order}}";
        query = "select Person {name, books, color, number, bags :{name, volume, @ownership}} filter .name = 'Kolia-1'";
        query = "select Person {name, values, metadata, tuple_of_arrays, nested_tuple, unnamed_tuple} filter .name = 'Kolia-3'";

//        query = "update default::Person \n" +
//                "filter .name = \"Kolia-3\"\n" +
//                "set {\n" +
//                "  values := <range<std::int64>> range(2, 10, inc_lower := true, inc_upper := true)\n" +
//                "}\n";

        ConnectionParams cp = new ConnectionParams();
        cp.setPort(10705);

        try{
            IConnection connection = clientV2.getConnection(cp);
            boolean is_json = false;

            ResultSet result = null;
            if(is_json) {
                result = connection.queryJSON(query);
                for(DataResponse resp : ((ResultSetImpl) result).getDataResponses()) {
                    if (resp != null && resp.getDataLength() > 0) {
                        for (DataElement elem : resp.getDataElements()) {
                            log.info("DataElement: " + new String(elem.getDataElement()));
                        }
                    }
                }
                return;
            }
            else{
                result = connection.query(query);
            }

            if(result instanceof ResultSetImpl){
                TypeDecoderFactoryImpl tdf = new TypeDecoderFactoryImpl();
                CommandDataDescriptor cdd = ((ResultSetImpl) result).getDataDescriptor();
                if(cdd != null) {
                    ByteBuffer bb = ByteBuffer.wrap(cdd.getOutput_typedesc());
                    tdf.decodeDescriptors(bb);
                }

                ArrayList<IDataContainer> result_arr = new ArrayList<>();
                for(DataResponse resp : ((ResultSetImpl) result).getDataResponses()){
                    if(resp != null && resp.getDataLength() > 0){
                        for(DataElement elem : resp.getDataElements()){
                            //log.info("DataElement: " + new String(elem.getDataElement()));
                            ByteBuffer bb = ByteBuffer.wrap(elem.getDataElement());

                            TypeDescriptor root_desc = tdf.getRootTypeDescriptor();
                            result_arr.add(root_desc.decodeData(bb, bb.remaining()));
                        }
                    }
                }

                System.out.println("Decoded results:");
                for (IDataContainer row : result_arr){
                    if(row.getCountChildren() > 0)
                        printData(row.getChildrenIterator());
                    System.out.println();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printData(Iterator<IDataContainer> iterator){
        System.out.print("[");
        while (iterator.hasNext()){
            IDataContainer cont = iterator.next();
            printContainer(cont);
        }
        System.out.print("]");
    }

    public void printContainer(IDataContainer cont){
        if(cont == null)
            return;

        if(cont.getCountChildren() > 0){
            System.out.print("<" + cont.getType()+">");
            printData(cont.getChildrenIterator());
        }
        else{
            if(cont.getData() instanceof Object[]){
                printArray((Object[])cont.getData());
            }
            else {
                System.out.print(cont.getData() + "; ");
            }
        }
    }

    public void printArray(Object[] arr){
        System.out.print("[");
        for(int i = 0; i < arr.length; i++){
            if(arr[i] instanceof IDataContainer){
                printContainer((IDataContainer)arr[i]);
            }
            else {
                System.out.print(arr[i] + "; ");
            }
        }
        System.out.print("]");
    }
}