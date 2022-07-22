package MCSH.online.advanced;

import MCSH.util.Adistance_float;
import MCSH.util.FindCloseCommunity;
import MCSH.util.HalfPathSearch;
import MCSH.util.MetaPath;

import java.util.*;

/**
 * advanced algorithm1 ：homebcore + FindCloseCommuncity
 */

public class advanced1 {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;


    public advanced1(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attributed , Adistance_float adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attributed;
        this.adistance = adistance;
    }

    //delete 1 node
    public Set<Integer> query(int queryId, int queryK, MetaPath queryMPath){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;


        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

//        System.out.println("build graph");
        //step 1: build the connected homogeneous graph
        long t1 = System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time1:"+ (t2-t1));

        //step 2: compute the connected k-core
//        Set<Integer> cc = findKCore(pnbMap);
//        if(cc==null){
//            return null;
//        }
//        for(Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()){
//            int key = entry.getKey();
//            if(!cc.contains(key)){
//                pnbMap.put(key,new HashSet<>());
//            }
//        }
//
//        System.out.println("find a cc and try to delete node");
//        //step 3: delete nodes and find a closer community
//        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
//        Set<Integer> community = findCloseCommunity.findCompactC(pnbMap,cc,this.adistance,queryId,queryK);
//
//        return community;
        return null;
    }

    public Set<Integer> query2(int queryId, int queryK, MetaPath queryMPath){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;


        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

//        System.out.println("build graph");
        //step 1: build the connected homogeneous graph
        long t1 = System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph2();
        long t2 = System.nanoTime();
        System.out.println("build time2:"+ (t2-t1));

        //step 2: compute the connected k-core
//        Set<Integer> cc = findKCore(pnbMap);
//        if(cc==null){
//            return null;
//        }
//        for(Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()){
//            int key = entry.getKey();
//            if(!cc.contains(key)){
//                pnbMap.put(key,new HashSet<>());
//            }
//        }
//
//        System.out.println("find a cc and try to delete node");
//        //step 3: delete nodes and find a closer community
//        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
//        Set<Integer> community = findCloseCommunity.findCompactC(pnbMap,cc,this.adistance,queryId,queryK);

//        return community;
        return null;
    }

    //delete m nodes
    public Set<Integer> queryM(int queryId, int queryK, MetaPath queryMPath,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        System.out.println("build graph");
        //step 1: build the connected homogeneous graph
        Map<Integer, Set<Integer>> pnbMap = buildGraph();

        //step 2: compute the connected k-core
        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }
        for(Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()){
            int key = entry.getKey();
            if(!cc.contains(key)){
                pnbMap.put(key,new HashSet<>());
            }
        }

        //step 3: delete nodes and find a closer community
        System.out.println("find a cc and try to delete node");
        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
        return findCloseCommunity.findCompactC(pnbMap,cc,queryM,this.adistance,queryId,queryK);
    }

    //delete m nodes with protect
    public Set<Integer> queryM_protect(int queryId, int queryK, MetaPath queryMPath,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        System.out.println("build graph");
        //step 1: build the connected homogeneous graph
        Map<Integer, Set<Integer>> pnbMap = buildGraph();

        //step 2: compute the connected k-core
        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }
//        for(Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()){
//            int key = entry.getKey();
//            if(!cc.contains(key)){
//                pnbMap.put(key,new HashSet<>());
//            }
//        }

        //step 3: delete nodes and find a closer community
        System.out.println("find a cc and try to delete node");
        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
        return findCloseCommunity.findCompactC_protect(pnbMap,cc,queryM,this.adistance,queryId,queryK);
    }

    private Map<Integer, Set<Integer>> buildGraph() {

        //step 1: find all the vertices according to the attribute distance with the query q
        //遍历所有节点，找到与路径出发节点相同类型的节点
        Set<Integer> keepSet = new HashSet<Integer>();
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                keepSet.add(curId);
            }
        }

        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
        for(Integer id:keepSet){
            Set<Integer> neibor = new HashSet<>();
            neibors.put(id,neibor);
        }

        Queue<Integer> queue = new LinkedList<>();
        Set<Integer> connected = new HashSet<>();
        queue.add(queryId);
        while (queue.size()>0){
            int start = queue.poll();
            connected.add(start);
            Set<Integer> neibor = neibors.get(start);
            for(Integer keepnode:keepSet){
                if(!connected.contains(keepnode)){
                    Set<Integer> set1 = halfneibors.get(start);Set<Integer> set2 = halfneibors.get(keepnode);
                    if(set1==null||set2==null){
                        continue;
                    }
                    int flag = 1;
                    if(set1.size()<=set2.size()){
                        for(Integer n:set1){
                            if(set2.contains(n)){
                                flag = 0;
                                break;
                            }
                        }
                    }
                    else {
                        for(Integer n:set2){
                            if(set1.contains(n)){
                                flag = 0;
                                break;
                            }
                        }
                    }
                    if(flag==0){
                        neibor.add(keepnode);
                        if(!queue.contains(keepnode)){
                            queue.add(keepnode);
                        }
                        neibors.get(keepnode).add(start);
                    }
                }
            }
        }

        return neibors;
    }

    private Map<Integer, Set<Integer>> buildGraph2() {

        //step 1: find all the vertices according to the attribute distance with the query q
        //遍历所有节点，找到与路径出发节点相同类型的节点
        Set<Integer> keepSet = new HashSet<Integer>();
        for(int curId = 0;curId < graph.length;curId ++) {
            if(vertexType[curId] == queryMPath.vertex[0]) {
                keepSet.add(curId);
            }
        }

        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
        Map<Integer,Boolean> visited = new HashMap<>();//判断节点是否被访问过
        for(int node:keepSet){
            visited.put(node,false);
        }

        Queue<Integer> queue = new LinkedList<>();;
        queue.add(queryId);
        while (queue.size()>0){
            int start = queue.poll();
            visited.put(start,true);
            Set<Integer> nei;
            if(neibors.containsKey(start)){
                nei = neibors.get(start);
            }else {
                nei = new HashSet<>();
                neibors.put(start,nei);
            }
            for(Integer keepnode:keepSet){
                if(!visited.get(keepnode)){
                    Set<Integer> set1 = halfneibors.get(start);Set<Integer> set2 = halfneibors.get(keepnode);
                    if(set1==null||set2==null){
                        continue;
                    }
                    int flag = 1;
                    if(set1.size()<=set2.size()){
                        for(Integer n:set1){
                            if(set2.contains(n)){
                                flag = 0;
                                break;
                            }
                        }
                    }
                    else {
                        for(Integer n:set2){
                            if(set1.contains(n)){
                                flag = 0;
                                break;
                            }
                        }
                    }
//                    retainallnum++;
                    if(flag==0){
                        nei.add(keepnode);
                        if(!visited.get(keepnode)){
                            queue.add(keepnode);
                        }
                        if(neibors.containsKey(keepnode)){
                            neibors.get(keepnode).add(start);
                        }else {
                            Set<Integer> set = new HashSet<>();
                            set.add(start);
                            neibors.put(keepnode,set);
                        }

//                        System.out.println("node1:"+start+",node2"+keepnode);
                    }
                }
            }
        }

        return neibors;
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
