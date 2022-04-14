package MCSH.online.basic;


import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;

public class shujuji {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

//    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path

    public shujuji(int[][] graph, int[] vertexType, int[] edgeType) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
    }

    public void query(MetaPath queryMPath, int queryk, FileWriter fw) throws IOException {
        this.queryMPath = queryMPath;

        //step 0: check whether queryId's type matches with the meta-path
        Map<Integer, Set<Integer>> pnbMap = buildGraph();

        //step 2: compute the connected k-core
        findKCore(pnbMap,queryk);
        for (Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()) {
            int id = entry.getKey();
            Set<Integer> neibor = entry.getValue();
            for (int nei:neibor) {
                fw.write(id+" "+nei+"\n");
            }
        }

    }


    public Set<Integer> query(MetaPath queryMPath, int queryk, int queryid){
        this.queryMPath = queryMPath;

        //step 0: check whether queryId's type matches with the meta-path
        Map<Integer, Set<Integer>> pnbMap = buildGraph();

        //step 2: compute the connected k-core
        return findKCore(pnbMap,queryk,queryid);
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

    private Set<Integer> findKCore(Map<Integer, Set<Integer>> pnbMap,int queryK,int queryId) {
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

        //step 3: find the connected component containing q
        //找连通图
        if(!pnbMap.containsKey(queryId)) return null;
        if(pnbMap.get(queryId).size() < queryK)  return null;
        Set<Integer> community = new HashSet<Integer>();//vertices which have been put into queue
        Queue<Integer> ccQueue = new LinkedList<Integer>();
        ccQueue.add(queryId);
        community.add(queryId);
        while(ccQueue.size() > 0) {
            int curId = ccQueue.poll();
            for(int pnb:pnbMap.get(curId)) {//enumerate curId's neighbors
                if(!community.contains(pnb)) {
                    ccQueue.add(pnb);
                    community.add(pnb);
                }
            }
        }

        return community;
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
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge,null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge,null,Config.IMDBpersonattributed);

        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();


        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        MetaPath qPath = new MetaPath(vertex, edge);

//        File logfile = new File( "/home/hadoop/Graph.txt");
//        FileWriter fw = new FileWriter(logfile, true);
//        CoreDec2 coreDec = new CoreDec2(graph,vertexType,edgeType);
//        coreDec.query(qPath,31,fw);
//        fw.close();
        String queryfile = "/home/hadoop/q2.txt";
        BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
        String line;

        Queue<Integer> querynodes = new LinkedList<>();
        while((line = stdin.readLine()) != null){
            int queryid = Integer.parseInt(line);
            querynodes.add(queryid);
        }
        System.out.println("querynodes size:"+querynodes.size());
        stdin.close();
        for (int i = 28; i < 33; i++) {
            File logfile = new File( "/home/hadoop/shujuji/DBLP/"+i+".txt");
            FileWriter fw = new FileWriter(logfile, true);

            for (int querynode:querynodes) {
                shujuji coreDec = new shujuji(graph,vertexType,edgeType);
                Set<Integer> result = coreDec.query(qPath,i,querynode);
                System.out.println(querynode+":"+result.size());
                fw.write(querynode+":"+dataReader.trans(result)+"\n");
            }
            fw.close();
        }
        System.out.println("finish");
    }

}
