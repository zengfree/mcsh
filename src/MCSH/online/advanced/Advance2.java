package MCSH.online.advanced;

import MCSH.util.*;

import java.util.*;

/**
 * advanced algorithm1 ：homebcore + FindCloserCommuncity
 */

public class Advance2 {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;


    public Advance2(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attributed , Adistance_float adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attributed;
        this.adistance = adistance;
    }

    public Set<Integer> query(int queryId, int queryK, MetaPath queryMPath){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        long t1=System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time:"+(t2-t1));

        //step 2: compute the connected k-core
        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }

        //step 3: delete nodes and find a closer community
        FCS_log_new fcs = new FCS_log_new();

        return fcs.findCompactC(pnbMap,cc, this.adistance,queryId,queryK);
    }

    public Set<Integer> queryM(int queryId, int queryK, MetaPath queryMPath,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        long t1 = System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time:"+(t2-t1));

        //step 2: compute the connected k-core
        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }

        //step 3: delete nodes and find a closer community
        FCS_log_new fcs = new FCS_log_new();

        return fcs.findCompactC(pnbMap,cc,queryM, this.adistance,queryId,queryK);
    }

    public Set<Integer> queryM_protect(int queryId, int queryK, MetaPath queryMPath,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;


        //step 1: build the connected homogeneous graph
        long t1=System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time:"+(t2-t1));

        //step 2: compute the connected k-core
        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }

        //step 3: delete nodes and find a closer community
        FCS_log_new fcs = new FCS_log_new();
        return fcs.findCompactC_protect(pnbMap,cc,queryM,this.adistance,queryId,queryK);
    }

    private Map<Integer, Set<Integer>> buildGraph() {

        //step 1: find all the vertices according to the attribute distance with the query q
        //遍历所有节点，找到与路径出发节点相同类型的节点
//        Set<Integer> keepSet = new HashSet<Integer>();
//        for(int curId = 0;curId < graph.length;curId ++) {
//            if(vertexType[curId] == queryMPath.vertex[0]) {
//                keepSet.add(curId);
//            }
//        }

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

        //step3:build the graph
        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
        for(int curId = 0;curId < graph.length;curId ++) {
            if(keepSet.contains(curId)) {
                neibors.put(curId,new HashSet<>());
            }
        }
//        for(int id:keepSet){
//            neibors.put(id,new HashSet<>());
//        }

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

    private Map<Integer,Set<Integer>> findhalf_neibors(Set<Integer> keepSet){
        Map<Integer,Set<Integer>> halfneibors = new HashMap<>();
        HalfPathSearch ms = new HalfPathSearch(graph,vertexType,edgeType, queryMPath);
        for(int keepnode:keepSet){
//            Set<Integer> nb =ms.collect(keepnode);
            halfneibors.put(keepnode, ms.collect(keepnode));
        }
        return halfneibors;
    }

    private Set<Integer> findKCore(Map<Integer, Set<Integer>> pnbMap) {
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
        if(pnbMap.get(queryId).size() < queryK)   return null;
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

}
