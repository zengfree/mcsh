package MCSH.online.basic;


import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


/**
 * build
 */
public class half_num {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;

    public half_num(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attributed , Adistance_float adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attributed;
        this.adistance = adistance;
    }


    public Map<Integer,Set<Integer>> build1(MetaPath queryMPath,int queryId){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        return buildGraph();
    }

//    public Map<Integer,Set<Integer>> build2(MetaPath queryMPath,int queryId){
//        this.queryId = queryId;
//        this.queryMPath = queryMPath;
////        this.queryK = queryK;
//
//        //step 0: check whether queryId's type matches with the meta-path
//        if(queryMPath.vertex[0] != vertexType[queryId])   return null;
//
//        return buildGraph2();
//    }

    public Map<Integer,Set<Integer>> build3(MetaPath queryMPath,int queryId){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])
            return null;

        //step 1: build the connected homogeneous graph
        return buildGraph3();
    }

    public Map<Integer,Set<Integer>> build4(MetaPath queryMPath,int queryId){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])
            return null;

        //step 1: build the connected homogeneous graph
        return buildGraph4();
    }


    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: compute the connected subgraph via batch-search with labeling (BSL)
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);
        System.out.println(keepSet.size()+":size");
        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
//        for(int curId = 0;curId < graph.length;curId ++) {
//            if(vertexType[curId] == queryMPath.vertex[0]) {
//                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
//                pnbMap.put(curId, pnbSet);
//            }
//        }
        for (int curId: keepSet) {
            Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
            pnbMap.put(curId,pnbSet);
        }

        return pnbMap;
    }


    private Map<Integer, Set<Integer>> buildGraph3() {
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);

        //step 2: build the index
        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
        Map<Integer,Set<Integer>> index = new HashMap<>();//index

        for(Map.Entry<Integer,Set<Integer>> entry:halfneibors.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            int key = entry.getKey();
            for (Integer p: pneibor){
                Set<Integer> set = index.getOrDefault(p,new HashSet<>());
                set.add(key);
                if (set.size()==1){
                    index.put(p,set);
                }
            }
        }

        //step3:build the graph
        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
        for(int id:keepSet){
            neibors.put(id,new HashSet<>());
        }

        for(Map.Entry<Integer,Set<Integer>> entry:index.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            for(Integer integer:pneibor){
                Set<Integer> pnbSet = neibors.get(integer);
                pnbSet.addAll(pneibor);
                pnbSet.remove(integer);
            }
        }

        return neibors;
    }

    private Map<Integer, Set<Integer>> buildGraph4() {
        long t1=System.nanoTime();
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);

        //step 2: build the index
        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
        Map<Integer,Set<Integer>> index = new HashMap<>();//index

        for(Map.Entry<Integer,Set<Integer>> entry:halfneibors.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            int key = entry.getKey();
            for (Integer p: pneibor){
                if(!index.containsKey(p)){
                    index.put(p,new HashSet<>());
                }
                index.get(p).add(key);
            }
        }
        long t2 = System.nanoTime();
        System.out.println((t2-t1)/1000000);

        int[] nums=new int[10000];
        for(Map.Entry<Integer,Set<Integer>> entry:index.entrySet()){
            int i=entry.getValue().size();
            nums[i]++;
        }
//        System.out.println(nums);
        for (int i = 0; i < nums.length; i++) {
            System.out.println(i+" "+nums[i]);
        }

        return new HashMap<>();
    }


    private Map<Integer,Set<Integer>> findhalf_neibors(Set<Integer> keepSet){
        Map<Integer,Set<Integer>> halfneibors = new HashMap<>();
        HalfPathSearch ms = new HalfPathSearch(graph,vertexType,edgeType, queryMPath);
        for(int keepnode:keepSet){
            Set<Integer> nb =ms.collect(keepnode);
            halfneibors.put(keepnode,nb);
        }
        return halfneibors;
    }

    public static void main(String[] args) {
        DataReader dataReader ;
        switch (args[0]){
            case "dblp":
                System.out.println("dblp");
                dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
                break;
            case "imdb":
                System.out.println("imdb");
                dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBmovieattributed);
                break;
            case "fsq":
                System.out.println("fsq");
                dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
                break;
            default:
                dataReader = new  DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        }
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        String[] Mpath = args[1].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        half_num h = new half_num(graph,vertexType,edgeType,null,null);
        MetaPath queryMPath = new MetaPath(vertex, edge);
        int queryid = Integer.parseInt(args[2]);
        System.out.println(queryMPath);
        h.build4(queryMPath,queryid);
    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
