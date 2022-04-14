package MCSH.online.exact;

import MCSH.util.Adistance;
import MCSH.util.BatchLinker;
import MCSH.util.BatchSearch;
import MCSH.util.MetaPath;

import java.util.*;

public class ExactAlgorithm_advance{
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,Integer> serial_number_correspondence = null;
    private Map<Integer,double[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance adistance = null;
//    private static ExecutorService es;
//    private String pnbMapPath = "pnbmap.txt";

    public ExactAlgorithm_advance(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, Integer> serial_number_correspondence, Map<Integer, double[]> attribute , Adistance adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.serial_number_correspondence = serial_number_correspondence;
        this.attribute = attribute;
        this.adistance = adistance;
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

        System.out.println("community:"+community.size());
        Map<Integer, double[]> distancemap = new HashMap<>();
        for(int i:community){
            distancemap.put(i,new double[this.graph.length]);
        }

        //step 2: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:community){
            label.add(i);
            for (int j:community){
                if(!label.contains(j))
                {
                    double dist = adistance.cal_distance(i, j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
                }
            }
        }
        //step 3: brute force to traverse all possible kcore
//        Set<Integer> denstcc = new HashSet<>(community);
//        double minavgdist = this.adistance.cal_subgraph_attr_dist(community,distancemap);

        //        System.out.println(denstcc);
        return new Kcore_find().findkore(pnbMap,new HashSet<>(), this.queryK, this.queryId,-1,this.adistance,distancemap);
    }

    public void output(Map<Integer, Set<Integer>> pnbmap) {
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println("id:" + entry.getKey() + ",neibor:" + transset(entry.getValue()));
            }
        }
        System.out.println();
    }

    private String transset(Set<Integer> set){
        StringBuffer str5 = new StringBuffer();
        for (int i : set) {
            str5.append(i + ",");
        }
        return str5.toString();
    }

    private Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap){
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            Set<Integer> newset = new HashSet<>();
            newset.addAll(entry.getValue());
            newMap.put(entry.getKey(),newset);
        }

        return newMap;
    }


    private Map<Integer, Set<Integer>> buildGraph(Set<Integer> keepSet) {

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

    private Set<Integer> findKCore(Map<Integer, Set<Integer>> pnbMap, Set<Integer> deletenodes) {
        Set<Integer> deleteSet = new HashSet<>(deletenodes);
        //step 0:delete the nodes and their neibors
        for(int deletenode:deleteSet){
            Set<Integer> pnbset = pnbMap.get(deletenode);
            for(int nei: pnbset){
                if(!deleteSet.contains(nei)){
                    pnbMap.get(nei).remove(deletenode);
                }
            }
            pnbMap.put(deletenode,new HashSet<>());
        }
        //simulate a queue
        Queue<Integer> queue = new LinkedList<Integer>(deletenodes);

        //step 1: find the vertices can be deleted in the first round
//        Set<Integer> deleteSet = new HashSet<Integer>();
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
            pnbMap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
        }

        //step 3: find the connected component containing q
        //找连通图
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
