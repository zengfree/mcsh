package MCSH.online.nct;


import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


/**
 * basic algorithm2 ：homebcore + Node deletion
 */
public class CD {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

//    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path

    public CD(int[][] graph, int[] vertexType, int[] edgeType) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
    }

    public void query(MetaPath queryMPath, int querytype, FileWriter fw) throws IOException {
//        this.queryId = queryId;
        this.queryMPath = queryMPath;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != querytype){
//            System.out.println("queryMpath error");
            fw.write("queryMpath error\n");
        }
        else {
            //step 1: build the connected homogeneous graph
            Map<Integer, Set<Integer>> pnbMap = buildGraph();



            //step 2: compute the connected k-core
            boolean flag  = true;
            int K =1;
            while(flag){
                findKCore(pnbMap,K);
                if(pnbMap.isEmpty()){
                    flag = false;
//                    System.out.println("pnbMap is empty");
                    fw.write("pnbMap is empty\n");
                }else{
                    int size = pnbMap.keySet().size();
//                    System.out.println("kcore size:"+size);
                    fw.write("kcore size:"+size+"\n");
                    if(size<5000){
//                        System.out.println("K:"+K);
//                        System.out.println("set:"+trans(pnbMap.keySet()));
                        fw.write("K:"+K+"\n");
                        fw.write("set:"+trans(pnbMap.keySet())+"\n");
                    }
                }
                K++;
            }
        }
    }


    public Set<Integer> queryK(MetaPath queryMPath, int querytype,int queryK){
//        this.queryId = queryId;
        this.queryMPath = queryMPath;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != querytype){
            System.out.println("queryMpath error");
//            fw.write("queryMpath error\n");
            return null;
        }
        else {
            //step 1: build the connected homogeneous graph
            Map<Integer, Set<Integer>> pnbMap = buildGraph();

            //step 2: compute the connected k-core
            findKCore(pnbMap,queryK);
            return pnbMap.keySet();
        }
    }

    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: find all the vertices
        //遍历所有节点，找到与路径出发节点相同类型的节点
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
                pnbMap.put(curId, pnbSet);
            }
        }

        return pnbMap;
    }

    private void findKCore(Map<Integer, Set<Integer>> pnbMap,int queryK) {
        Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue

        //step 1: find the vertices can be deleted in the first round
        Set<Integer> deleteSet = new HashSet<Integer>();
        for(Map.Entry<Integer, Set<Integer>> entry : pnbMap.entrySet()) {
            int curId = entry.getKey();
            Set<Integer> pnbSet = entry.getValue();
            if(pnbSet.size() < queryK) {
                queue.add(curId);
                deleteSet.add(curId);
            }
        }

        //step 2: delete vertices whose degrees are less than k
        while(queue.size() > 0) {
            int curId = queue.poll();//delete curId
            Set<Integer> pnbSet = pnbMap.get(curId);//找到curID对应的邻居
            for(int pnb:pnbSet) {//update curId's pnb
                if(!deleteSet.contains(pnb)) {
                    Set<Integer> tmpSet = pnbMap.get(pnb);
                    tmpSet.remove(curId);
                    if(tmpSet.size() < queryK) {
                        queue.add(pnb);
                        deleteSet.add(pnb);
                    }
                }
            }
//            pnbMap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
            pnbMap.remove(curId);
        }


    }

    private String trans(Set<Integer> V){
//		if(V==null) return "";
        StringBuilder str5 = new StringBuilder();
        for (int i : V) {
            str5.append(i).append(", ");
        }
        return str5.toString();
    }

    public void output(Map<Integer, Set<Integer>> pnbmap) {
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
//            if (!entry.getValue().isEmpty()) {
                System.out.println("id:" + entry.getKey() + ",neibor:" + trans(entry.getValue()));
//            }
        }
        System.out.println();
    }

    public static void main(String[] args) throws IOException {
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge,null,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();


        int vertex[] = {0, 1, 0}, edge[] = {0, 0}; //MPM
        MetaPath qPath = new MetaPath(vertex, edge);

        File logfile = new File( "q_small.txt");
        FileWriter fw = new FileWriter(logfile, false);
        CD coreDec = new CD(graph,vertexType,edgeType);
        Set<Integer> clique = coreDec.queryK(qPath,qPath.vertex[0],5);
        for(int i:clique){
            fw.write(i+"\r\n");
        }
        fw.close();
    }

}
