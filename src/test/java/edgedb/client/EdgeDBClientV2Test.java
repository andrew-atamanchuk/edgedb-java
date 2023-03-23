package edgedb.client;


import edgedb.connection.BlockingConnection;
import edgedb.connection.IConnection;
import edgedb.connectionparams.ConnectionParams;
import edgedb.internal.protocol.CommandDataDescriptor;
import edgedb.internal.protocol.DataElement;
import edgedb.internal.protocol.DataResponse;
import edgedb.internal.protocol.constants.Cardinality;
import edgedb.internal.protocol.constants.IOFormat;
import edgedb.internal.protocol.typedescriptor.*;
import edgedb.internal.protocol.typedescriptor.decoder.TypeDecoderFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class EdgeDBClientV2Test {

    public static void main(String... args){
        EdgeDBClientV2Test edgedb_test = new EdgeDBClientV2Test();
        edgedb_test.TestParseExecuteV2();
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

        query = "INSERT Bag { name := <str>$name, volume := <int32>$volume }";

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
                CommandDataDescriptor cdd = ((ResultSetImpl) result).getCommandDataDescriptor();
                if(cdd != null) {
                    tdf.decodeDescriptors(cdd);
                }

                ArrayList<IDataContainer> result_arr = new ArrayList<>();
                for(DataResponse resp : ((ResultSetImpl) result).getDataResponses()){
                    if(resp != null && resp.getDataLength() > 0){
                        for(DataElement elem : resp.getDataElements()){
                            //log.info("DataElement: " + new String(elem.getDataElement()));
                            ByteBuffer bb = ByteBuffer.wrap(elem.getDataElement());

                            TypeDescriptor root_desc = tdf.getOutputRootTypeDescriptor();
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

    public void TestParseExecuteV2(){
        EdgeDBClientV2 clientV2 = new EdgeDBClientV2(new BlockingConnection());
        String query = "select Person {id, name, last_name, profession, birth, age, best_friend}";
        query = "select Person {name, last_name, best_friend :{name, last_name}, bags :{name, volume, @ownership, @order}}";
        query = "select Person {name, books, color, number, bags :{name, volume, @ownership}} filter .name = 'Kolia-1'";
        query = "select Person {name, values, metadata, tuple_of_arrays, nested_tuple, unnamed_tuple} filter .number = <posint64>$param1";

//        query = "update default::Person \n" +
//                "filter .name = \"Kolia-3\"\n" +
//                "set {\n" +
//                "  values := <range<std::int64>> range(2, 10, inc_lower := true, inc_upper := true)\n" +
//                "}\n";

        String query2 = "INSERT Bag { name := <str>$name, volume := <int32>$volume }";

        ConnectionParams cp = new ConnectionParams();
        cp.setPort(10705);

        try{
            IConnection connection = clientV2.getConnection(cp);
            char out_format = IOFormat.BINARY;
            char out_format2 = IOFormat.JSON_ELEMENTS;

            CommandDataDescriptor cdd = connection.sendParseV2(out_format, Cardinality.MANY, query);

            CommandDataDescriptor cdd2 = connection.sendParseV2(out_format2, Cardinality.MANY, query2);

            TypeDecoderFactoryImpl tdf = new TypeDecoderFactoryImpl();
            if(cdd == null) {
                return;
            }
            tdf.decodeDescriptors(cdd);

            TypeDecoderFactoryImpl tdf2 = new TypeDecoderFactoryImpl();
            if(cdd2 == null) {
                return;
            }
            tdf2.decodeDescriptors(cdd2);

            ObjectShapeDescriptor obj_shape_desc = (ObjectShapeDescriptor) tdf.getInputRootTypeDescriptor();
            ByteBuffer in_bb = ByteBuffer.allocate(2000);
            Map<String, Object> values = new HashMap<>();
            values.put("param1", 4L);
            IDataContainer container = fillData(obj_shape_desc, values);
            obj_shape_desc.encodeData(in_bb, container);
            in_bb.flip();

            ResultSet result = connection.sendExecuteV2(out_format, Cardinality.MANY, query, cdd.getInput_typedesc_id(), cdd.getOutput_typedesc_id(), in_bb);
            printResult(out_format, result, tdf);

            ObjectShapeDescriptor obj_shape_desc2 = (ObjectShapeDescriptor) tdf2.getInputRootTypeDescriptor();
            in_bb.clear();
            values.clear();
            values.put("name", "test_bag");
            values.put("volume", 44);
            IDataContainer container2 = fillData(obj_shape_desc2, values);
            obj_shape_desc2.encodeData(in_bb, container2);
            in_bb.flip();

            result = connection.sendExecuteV2(out_format2, Cardinality.MANY, query2, cdd2.getInput_typedesc_id(), cdd2.getOutput_typedesc_id(), in_bb);
            printResult(out_format2, result, tdf2);


            in_bb.clear();
            values.clear();
            values.put("param1", 6L);
            container = fillData(obj_shape_desc, values);
            obj_shape_desc.encodeData(in_bb, container);
            in_bb.flip();

            result = connection.sendExecuteV2(out_format, Cardinality.MANY, query, cdd.getInput_typedesc_id(), cdd.getOutput_typedesc_id(), in_bb);
            printResult(out_format, result, tdf);
            in_bb.flip();

            result = connection.sendExecuteV2(out_format, Cardinality.MANY, query, cdd.getInput_typedesc_id(), cdd.getOutput_typedesc_id(), in_bb);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printResult(char out_format, ResultSet result, TypeDecoderFactoryImpl tdf){
        if(out_format == IOFormat.JSON_ELEMENTS) {
            for(DataResponse resp : ((ResultSetImpl) result).getDataResponses()) {
                if (resp != null && resp.getDataLength() > 0) {
                    for (DataElement elem : resp.getDataElements()) {
                        log.info("DataElement: " + new String(elem.getDataElement()));
                    }
                }
            }
        }
        else{
            ArrayList<IDataContainer> result_arr = new ArrayList<>();
            for(DataResponse resp : ((ResultSetImpl) result).getDataResponses()){
                if(resp != null && resp.getDataLength() > 0){
                    for(DataElement elem : resp.getDataElements()){
                        ByteBuffer bb = ByteBuffer.wrap(elem.getDataElement());

                        TypeDescriptor root_desc = tdf.getOutputRootTypeDescriptor();
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
    }

    public IDataContainer fillData(ObjectShapeDescriptor obj_shape_desc, Map<String, Object> values){
        IDataContainer container = obj_shape_desc.createInputDataFrame();
        Iterator<IDataContainer> iter = container.getChildrenIterator();
        int child_index = 0;
        while (iter.hasNext()){
            IDataContainer child = iter.next();
            ShapeElement se = obj_shape_desc.shapeElements[child_index++];

            for(String field_name : values.keySet()) {
                if (se.getName().equalsIgnoreCase(field_name)) {
                    switch (child.getType()) {
                        case TypeDescriptor.BASE_SCALAR_DESC_TYPE:
                            child.setData(values.get(field_name));
                            break;
                        case TypeDescriptor.SCALAR_DESC_TYPE:
                            if (child.getChildrenIterator().hasNext())
                                child.getChildrenIterator().next().setData(values.get(field_name));
                            break;
                    }
                }
            }
        }

        return container;
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