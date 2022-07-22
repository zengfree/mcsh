package MCSH.online.basic;


import MCSH.util.*;

import java.util.*;
import java.util.jar.JarEntry;


/**
 * basic algorithm2 ：homebcore + Node deletion
 */
public class MNindex {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;
    private int n;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;

    public MNindex(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attributed , Adistance_float adistance,int n) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attributed;
        this.adistance = adistance;
        this.n = n;
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
        System.out.println("build time:"+(t2-t1)/1e6+"ms");

        //step 2: compute the connected k-core
//        long t3 = System.nanoTime();
        Set<Integer> cc = findKCore(pnbMap);
//        long t4 = System.nanoTime();
//        System.out.println("CORE time:"+(t4-t3)/1000000);

        if(cc==null){
            System.out.println("not null");
            return null;
        }

        //step 3:
        FCS_log_new fcs =new FCS_log_new();

        return fcs.findCompactC(pnbMap,cc, this.adistance,queryId,queryK);
    }

    public Set<Integer> queryM(int queryId, int queryK, MetaPath queryMPath,int m){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        long t1=System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time:"+(t2-t1)/1e6+"ms");

        //step 2: compute the connected k-core
//        long t3 = System.nanoTime();
        Set<Integer> cc = findKCore(pnbMap);
//        long t4 = System.nanoTime();
//        System.out.println("CORE time:"+(t4-t3)/1000000);
        if(cc==null){
            System.out.println("not null");
            return null;
        }

        //step 3:
        FCS_log_new fcs = new FCS_log_new();

        return fcs.findCompactC(pnbMap,cc,m, this.adistance,queryId,queryK);
    }

    public Set<Integer> queryM_protect(int queryId, int queryK, MetaPath queryMPath,int m){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        long t1=System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time:"+(t2-t1)/1e6+"ms");

        //step 2: compute the connected k-core
//        long t3 = System.nanoTime();
        Set<Integer> cc = findKCore(pnbMap);
//        long t4 = System.nanoTime();
//        System.out.println("CORE time:"+(t4-t3)/1000000);
        if(cc==null){
            System.out.println("not null");
            return null;
        }

        //step 3:
        FCS_log_new fcs = new FCS_log_new();

        return fcs.findCompactC_protect(pnbMap,cc,m, this.adistance,queryId,queryK);
    }


    public void findKCore(Map<Integer, Set<Integer>> pnbMap,int queryK) {
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

    public Map<Integer, Set<Integer>> buildGraph1(MetaPath queryMPath) {
        //step 1: compute the connected subgraph via batch-search with labeling (BSL)
        Set<Integer> keepSet = new HashSet<>();
        for (int i = 0; i < graph.length ; i++) {
            if(vertexType[i]==queryMPath.vertex[0]){
                keepSet.add(i);
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

    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: compute the connected subgraph via batch-search with labeling (BSL)
        long t1 = System.nanoTime();
        Map<Integer,Set<Integer>> map = buildGraph1(queryMPath);
        findKCore(map,queryK);
        Set<Integer> keepSet = new HashSet<>(map.keySet());

        Map<Integer,Float> distance = new HashMap<>();
        for(int i:keepSet){
            distance.put(i,adistance.cal_distance(queryId,i));
        }

        List<Map.Entry<Integer,Float>> list = new ArrayList<>(distance.entrySet());
        list.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        keepSet = new HashSet<>();
        for (int i = 0; i < n&& i<list.size(); i++) {
            keepSet.add(list.get(i).getKey());
        }
        long t2 = System.nanoTime();
        System.out.println("cal:"+(t2-t1)/1e6+"ms");

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

    //找到包含查询点的kcore
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


}
