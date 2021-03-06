package MCSH.online.exact;

import MCSH.util.Adistance_float;
import MCSH.util.BatchLinker;
import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class E2 {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;
    private int threadnum;

    public E2(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attribute , Adistance_float adistance, int threadnum) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attribute;
        this.adistance = adistance;
        this.threadnum = threadnum;
    }

    public Set<Integer> query(int queryId, int queryK, MetaPath queryMPath){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;


        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;


        //step 1: build the connected homogeneous graph
        //step 1.5: compute the connected subgraph via batch-search with labeling (BSL)
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);
        Map<Integer, Set<Integer>> pnbMap = buildGraph(keepSet);


        //step 2: compute the connected k-core
        Set<Integer> community = findKCore(pnbMap);
        if(community==null) return null;

//        System.out.println(community.size());
        Map<Integer, Map<Integer,Float>> distancemap = new HashMap<>();
        for(int i:community){
            distancemap.put(i,new HashMap<>());
        }

        //step 2: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:community){
            label.add(i);
            for (int j:community){
                if(!label.contains(j))
                {
                    float dist = adistance.cal_distance(i, j);
                    distancemap.get(i).put(j,dist);
                    distancemap.get(j).put(i,dist);
                }
            }
        }

        Map<Integer,Set<Integer>> setMap = findinfluence(pnbMap);

        //step 3: brute force to traverse all possible kcore
        ForkJoinPool pool = new ForkJoinPool(threadnum);
        ForkJoinTask<Set<Integer>> task = pool.submit(new K2(pnbMap,this.queryK, this.queryId,-1,this.adistance,distancemap,setMap));
        return task.join();
    }

    public void output(Map<Integer, Set<Integer>> pnbmap) {
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println("id:" + entry.getKey() + ",neibor:" + transset(entry.getValue()));
            }
        }
        System.out.println();
    }

    public Map<Integer,Set<Integer>> findinfluence(Map<Integer,Set<Integer>> pnbmap){
        Map<Integer,Set<Integer>> map = new HashMap<>();
        for (int key:pnbmap.keySet()) {
            map.put(key,new HashSet<>());
        }
        for(int i:pnbmap.keySet()){
            Map<Integer,Set<Integer>> map1 = copyMap(pnbmap);
            for(int j:pnbmap.get(i)){
                map1.get(j).remove(i);
            }
            map1.put(i,new HashSet<>());

            Set<Integer> set = findKCore(map1);
            if(set!=null) {
                Set<Integer> set1 = new HashSet<>(pnbmap.keySet());
                set1.removeAll(set);
                for (int j:set1){
                    map.get(j).add(i);
                }
//                map.put(i,set1);
            }else {
                for (int j:pnbmap.keySet()){
                    map.get(j).add(i);
                }
//                map.put(i,new HashSet<>(pnbmap.keySet()));
            }
        }
        return map;
    }

    private String transset(Set<Integer> set){
        StringBuffer str5 = new StringBuffer();
        for (int i : set) {
            str5.append(i + ",");
        }
        return str5.toString();
    }

    private Map<Integer, Set<Integer>> buildGraph(Set<Integer> keepSet) {

        //step 2: build the graph
        //???????????????????????????????????????????????????????????????????????????????????????P-??????????????????<v,set<>>
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
            Set<Integer> pnbSet = pnbMap.get(curId);//??????curID???????????????
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
        //????????????
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

    private Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap){
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(),newset);
        }

        return newMap;
    }

}
