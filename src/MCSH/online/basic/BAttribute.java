package MCSH.online.basic;


import MCSH.util.*;

import java.util.*;


/**
 * basic algorithm2 ：homebcore + Node deletion
 */
public class BAttribute {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;

    public BAttribute(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attributed , Adistance_float adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attributed;
        this.adistance = adistance;
    }


    public Set<Integer> query(int queryId, int queryK, MetaPath queryMPath,int queryN){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        long t1=System.nanoTime();
        Map<Integer, Set<Integer>> pnbMap = buildGraph();
        long t2 = System.nanoTime();
        System.out.println("build time:"+(t2-t1)/1000000);

        //step 2: compute the connected k-core
//        long t3 = System.nanoTime();
        Set<Integer> cc = findKCore(pnbMap);
//        long t4 = System.nanoTime();
//        System.out.println("CORE time:"+(t4-t3)/1000000);

        if(cc==null){
            return null;
        }
        pnbMap.clear();

        Map<Integer,Float> integerFloatMap = new HashMap<>();
        for(int i:cc){
            float dist = adistance.cal_distance(i,queryId);
            integerFloatMap.put(i,dist);
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(integerFloatMap.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o1.getValue().compareTo(o2.getValue());//升序
//                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> keepset = new HashSet<>();
        int z = 0;
        while (keepset.size() < queryN && z< cc.size()) {
            int id = entryList.get(z).getKey();
            keepset.add(id);
            z++;
        }

        Map<Integer,Set<Integer>> map2 = buildGraph(keepset);
        cc = findKCore(map2);
        if(cc == null){
            return null;
        }
//        System.out.println("map2 size:"+map2.keySet().size());
//        output(map2);

        //step 3:
        FCS_logMap fcs =new FCS_logMap();

        return fcs.findCompactC(map2,cc, this.adistance,queryId,queryK);
    }

    public Set<Integer> queryM(int queryId, int queryK, MetaPath queryMPath,int m,int queryN){
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
//        long t3 = System.nanoTime();
        Set<Integer> cc = findKCore(pnbMap);
//        long t4 = System.nanoTime();
//        System.out.println("CORE time:"+(t4-t3)/1000000);
        if(cc==null){
            return null;
        }
        pnbMap.clear();

        Map<Integer,Float> integerFloatMap = new HashMap<>();
        for(int i:cc){
            float dist = adistance.cal_distance(i,queryId);
            integerFloatMap.put(i,dist);
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(integerFloatMap.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o1.getValue().compareTo(o2.getValue());//升序
//                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> keepset = new HashSet<>();
        int z = 0;
        while (keepset.size() < queryN && z< cc.size()) {
            int id = entryList.get(z).getKey();
            keepset.add(id);
            z++;
        }

        Map<Integer,Set<Integer>> map2 = buildGraph(keepset);

        //step 3:
        FCS_logMap fcs = new FCS_logMap();

        return fcs.findCompactC(map2,keepset,m, this.adistance,queryId,queryK);
    }

    public Set<Integer> queryM_protect(int queryId, int queryK, MetaPath queryMPath,int m,int queryN){
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
//        long t3 = System.nanoTime();
        Set<Integer> cc = findKCore(pnbMap);
//        long t4 = System.nanoTime();
//        System.out.println("CORE time:"+(t4-t3)/1000000);
        if(cc==null){
            return null;
        }
        pnbMap.clear();

        Map<Integer,Float> integerFloatMap = new HashMap<>();
        for(int i:cc){
            float dist = adistance.cal_distance(i,queryId);
            integerFloatMap.put(i,dist);
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(integerFloatMap.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o1.getValue().compareTo(o2.getValue());//升序
//                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> keepset = new HashSet<>();
        int z = 0;
        while (keepset.size() < queryN && z< cc.size()) {
            int id = entryList.get(z).getKey();
            keepset.add(id);
            z++;
        }

        Map<Integer,Set<Integer>> map2 = buildGraph(keepset);

        //step 3:
        FCS_logMap fcs = new FCS_logMap();

        return fcs.findCompactC_protect(map2,keepset,m, this.adistance,queryId,queryK);
    }


    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: compute the connected subgraph via batch-search with labeling (BSL)
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);

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

    private Map<Integer, Set<Integer>> buildGraph(Set<Integer> keepSet) {
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
//        for(int curId = 0;curId < graph.length;curId ++) {
//            if(vertexType[curId] == queryMPath.vertex[0]) {
//                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
//                pnbMap.put(curId, pnbSet);
//            }
//        }
        for(int curId:keepSet){
            Set<Integer> pnbSet = batchSearch.collect(curId,keepSet);
            pnbMap.put(curId,pnbSet);
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


    private void output(Map<Integer,Set<Integer>> pnbmap){
        for(Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            if(!entry.getValue().isEmpty()){
                System.out.println("id:"+entry.getKey()+",neibor:"+trans(entry.getValue()));
            }
        }
        System.out.println();
    }
    private String trans(Set<Integer> V){
//		if(V==null) return "";
        StringBuffer str5 = new StringBuffer();
        for (int i : V) {
            str5.append(i+", ");
        }
        return str5.toString();
    }
}
