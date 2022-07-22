package MCSH;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class randomattribute {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

        try {
            FileWriter fileWriter = new FileWriter("attribute.txt",false);
            fileWriter.write("ResearchInterests text 10 positionaltitles continuousvalue 1 papernum continuousvalue 1");
            for (int id:attribute.keySet()){
                if(id!=-1){
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(id);
                    float[] arr = attribute.get(id);
                    for (int i = 67; i < 79; i++) {
                        stringBuffer.append(" ").append(arr[i]);
                    }

                    stringBuffer.append("\r\n");
                    fileWriter.write(stringBuffer.toString());
                }

            }
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
