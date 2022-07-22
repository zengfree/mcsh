package MCSH;

import MCSH.util.BatchLinker;
import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class transatt {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

        Map<Integer,Integer> he2ho = new HashMap<>();
        Map<Integer,Integer> ho2he = new HashMap<>();

//        int[] vertex = {1,0,1},edge={21,9};
        int[] vertex = {0,3,0},edge={0,6};

        MetaPath queryMPath = new MetaPath(vertex, edge);

        Set<Integer> keepSet = new HashSet<Integer>();
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                keepSet.add(curId);
            }
        }

        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
                if(pnbSet.size()>0){
                    pnbMap.put(curId, pnbSet);
                }
            }
        }

        try {
            FileWriter fileWriter = new FileWriter("corr_fsq.txt",false);
            int index=1;
            for (int curId:pnbMap.keySet()             ) {
                he2ho.put(index,curId);
                ho2he.put(curId,index);
                fileWriter.write(index+" "+curId+"\r\n");
                index++;
            }
            fileWriter.close();

            fileWriter = new FileWriter("graph_fsq.txt",false);
            for (int key:pnbMap.keySet()) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(ho2he.get(key));
                for(int nei:pnbMap.get(key)){
                    stringBuffer.append(" "+ho2he.get(nei));
                }
                stringBuffer.append("\r\n");
                fileWriter.write(stringBuffer.toString());
            }
            fileWriter.close();

            fileWriter = new FileWriter("node_fsq.txt",false);
            for (int key:pnbMap.keySet()) {
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append(ho2he.get(key));
                stringBuffer.append("\t").append(key).append("\t");
                float[] arr = attribute.get(key);
                for (int i = 0; i < 12; i++) {
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
