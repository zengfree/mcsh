package MCSH.index.kcore3;

import MCSH.util.*;

import java.util.*;

public class IBSearch {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;
    private String dataset = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;

    public IBSearch(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attribute, Adistance_float adistance, String dataset){
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attribute;
        this.adistance = adistance;
        this.dataset = dataset;
    }

    //带节点保护的查询
    public Set<Integer> query_protect(int queryId,int queryK,MetaPath queryMPath,float percpercentage,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;


        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;
//        String indexpath = Config.IndexRoot+queryMPath.toString();
        KHindex_3 hindex = new KHindex_3(this.graph,this.vertexType,this.edgeType,this.attribute);
        hindex.loadIndex(this.queryMPath,this.dataset);
        Set<Integer> candidateSet = hindex.search(queryId,queryK,this.adistance.getPreference_weights(),percpercentage);
        if(candidateSet==null){
            return null;
        }
//        System.out.println(candidateSet.size());
        candidateSet.add(queryId);
        Map<Integer,Set<Integer>> pnbMap = build(candidateSet);

        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }
//        System.out.println(this.adistance.cal_subgraph_attr_dist(cc));
        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
        return findCloseCommunity.findCompactC_protect(pnbMap,cc,queryM,adistance,queryId,queryK);
    }

    //单节点删除
    public Set<Integer> query(int queryId,int queryK,MetaPath queryMPath,float percpercentage){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;


        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;
//        String indexpath = Config.IndexRoot+queryMPath.toString();
        KHindex_3 hindex = new KHindex_3(this.graph,this.vertexType,this.edgeType,this.attribute);
        hindex.loadIndex(this.queryMPath,this.dataset);
        Set<Integer> candidateSet = hindex.search(queryId,queryK,this.adistance.getPreference_weights(),percpercentage);
        if(candidateSet==null){
            return null;
        }
//        System.out.println(candidateSet.size());
        candidateSet.add(queryId);

        Map<Integer,Set<Integer>> pnbMap = build(candidateSet);

        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }
//        System.out.println(this.adistance.cal_subgraph_attr_dist(cc));

        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
        return findCloseCommunity.findCompactC(pnbMap,cc,adistance,queryId,queryK);
    }

    //批量删除
    public Set<Integer> queryM(int queryId,int queryK,MetaPath queryMPath,float percpercentage,int M){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;


        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;
//        String indexpath = Config.IndexRoot+queryMPath.toString();
        KHindex_3 hindex = new KHindex_3(this.graph,this.vertexType,this.edgeType,this.attribute);
        hindex.loadIndex(this.queryMPath,this.dataset);
        Set<Integer> candidateSet = hindex.search(queryId,queryK,this.adistance.getPreference_weights(),percpercentage);
        hindex = null;
        if(candidateSet==null){
            return null;
        }

//        System.out.println(candidateSet.size());
        candidateSet.add(queryId);
        Map<Integer,Set<Integer>> pnbMap = build(candidateSet);

        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return null;
        }
//        System.out.println(this.adistance.cal_subgraph_attr_dist(cc));
        FindCloseCommunity findCloseCommunity = new FindCloseCommunity();
        return findCloseCommunity.findCompactC(pnbMap,cc,M,adistance,queryId,queryK);
    }

    private Map<Integer,Set<Integer>> build(Set<Integer> keepSet){
        //step1:对keepSet的每个节点进行遍历，找其half P-邻居并保存在<v,set<>>
        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
        int half_type = queryMPath.vertex[(queryMPath.pathLen+1)/2];

        //构建倒排索引 key=half_type node
        Map<Integer,Set<Integer>> index = new HashMap<>();
//        for(int curId = 0;curId < graph.length;curId ++) {
//            if(vertexType[curId]==half_type) {
//                index.put(curId,new HashSet<>());
//            }
//        }

        for(Map.Entry<Integer,Set<Integer>> entry:halfneibors.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            int key = entry.getKey();
            for (Integer p: pneibor){
                if(!index.containsKey(p)) {
                    index.put(p, new HashSet<>());
                }
                index.get(p).add(key);
            }
        }

        //step3:build the graph
        Map<Integer,Set<Integer>> pnbMap = new HashMap<>();//完整路径的邻居
        for (int i:keepSet){
            pnbMap.put(i,new HashSet<>());
        }

        for(Map.Entry<Integer,Set<Integer>> entry:index.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            for(Integer integer:pneibor){
                Set<Integer> pnbSet = pnbMap.get(integer);
                pnbSet.addAll(pneibor);
                pnbSet.remove(integer);
            }
        }

        return pnbMap;
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

    private Map<Integer,Set<Integer>> build2(Set<Integer> keepSet){
        //对keepSet的每个节点进行遍历，找其P-邻居并保存在<v,set<>>
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
        for(int curId:keepSet) {
            Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
            pnbMap.put(curId, pnbSet);
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
        if(!pnbMap.containsKey(queryId)){
            System.out.println("no include queryid");
            return null;
        }
        if(pnbMap.get(queryId).size() < queryK)  {
            System.out.println("queryid have no k neibors");
            return null;
        }
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
