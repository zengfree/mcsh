package MCSH;

import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;

public class transfsq {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
//        int[][] graph = dataReader.readGraph();
//        int[] vertexType = dataReader.readVertexType();
//        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

        Map<Integer,Integer> he2ho = new HashMap<>();
        Map<Integer,Integer> ho2he = new HashMap<>();
        try {
            File file = new File("C:\\Users\\DELL\\Desktop\\结果\\map_fsq0-0-3-6-0.txt");
            BufferedReader stdin = new BufferedReader(new FileReader(file));
            String line;
            int ix =1;
            FileWriter fileWriter = new FileWriter("corr_fsq.txt",false);
            while((line = stdin.readLine()) != null){
                String[] s = line.split(" ");
                int key = Integer.parseInt(s[0]);
                he2ho.put(ix,key);
                ho2he.put(key,ix);
//                fileWriter.write(ix+" "+key+"\r\n");
                ix++;
            }
            fileWriter.close();
            stdin.close();
//            FileWriter fileWriter = new FileWriter("corr_fsq.txt",false);
//            int index=1;
//            for (int curId:pnbMap.keySet()             ) {
//                he2ho.put(index,curId);
//                ho2he.put(curId,index);
//                fileWriter.write(index+" "+curId+"\r\n");
//                index++;
//            }
//            fileWriter.close();

//            fileWriter = new FileWriter("graph_fsq.txt",false);
//            stdin = new BufferedReader(new FileReader(file));
//            while((line = stdin.readLine()) != null){
//                String[] s = line.split(" ");
//                int key = Integer.parseInt(s[0]);
//                StringBuffer stringBuffer = new StringBuffer();
//                stringBuffer.append(ho2he.get(key));
//                for (int i = 1; i < s.length; i++) {
//                    stringBuffer.append(" ").append(ho2he.get(Integer.parseInt(s[i])));
//                }
//                stringBuffer.append("\r\n");
//                fileWriter.write(stringBuffer.toString());
//            }
//            fileWriter.close();
//            stdin.close();


            fileWriter = new FileWriter("att_fsq.txt",false);
            for (int key: he2ho.values()) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(key);
//                stringBuffer.append(" ");
                float[] arr = attribute.get(key);
                for (int i = 0; i < 11; i++) {
                    if(arr[i]==1){
                        stringBuffer.append(" ").append(i);
                    }
                }
                stringBuffer.append(" 133");
                stringBuffer.append("\r\n");
                fileWriter.write(stringBuffer.toString());
            }
            fileWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
