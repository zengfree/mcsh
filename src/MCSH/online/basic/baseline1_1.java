package MCSH.online.basic;


import MCSH.util.*;
import java.util.*;


/**
 * basic algorithm2 ：homebcore + Node deletion
 */
public class baseline1_1 {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,Integer> serial_number_correspondence = null;
    private Map<Integer,double[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance adistance = null;

    public baseline1_1(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, Integer> serial_number_correspondence, Map<Integer, double[]> attributed , Adistance adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.serial_number_correspondence = serial_number_correspondence;
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
        Map<Integer, Set<Integer>> pnbMap = buildGraph();

        //step 2: compute the connected k-core
        Set<Integer> cc = findKCore(pnbMap);
        if(cc==null){
            return new HashSet<>();
        }

        for(Map.Entry<Integer,Set<Integer>> entry:pnbMap.entrySet()){
            int key = entry.getKey();
            if(!cc.contains(key)){
                pnbMap.put(key,new HashSet<>());
            }
        }

        //step 3:
        Set<Integer> community = findCompactC(pnbMap,this.adistance,1,queryK,queryId);

        return community;
    }

    public Set<Integer> query2(int queryId, int queryK, MetaPath queryMPath){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

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

        //step 3:
        Set<Integer> community = findCompactC_deleteone(pnbMap,this.adistance,queryK,queryId,cc);

        return community;
    }

    public Set<Integer> query3(int queryId, int queryK, MetaPath queryMPath){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

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

        //step 3:
        Set<Integer> community = findCompactC(pnbMap,cc);

        return community;
    }

    public Set<Integer> query4(int queryId, int queryK, MetaPath queryMPath,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

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

        //step 3:
        Set<Integer> community = findCompactC(pnbMap,cc,queryM);

        return community;
    }

    public Set<Integer> query5(int queryId, int queryK, MetaPath queryMPath,int queryM){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

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

        //step 3:
        Set<Integer> community = findCompactC_jump(pnbMap,cc,queryM);

        return community;
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

    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap, Adistance adistance,int deletenum,int k, int queryid){
        //step 1: get the community nodes
        Set<Integer> keepset = new HashSet<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            int key = entry.getKey();
            if(entry.getValue().size()>0){
                keepset.add(key);
            }
        }

        //step 2: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for(int i:keepset){
            distancemap.put(i,new double[this.graph.length]);
        }

        //step 3: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:keepset){
            label.add(i);
            for (int j:keepset){
                if(!label.contains(j))
                {
                    double dist = adistance.cal_distance(i, j);
//                    System.out.println(i+" "+j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
                }
            }
        }
//        for (Map.Entry<Integer,double[]> entry:distancemap.entrySet()){
//           System.out.println(entry.getKey() +" dist "+ trans(entry.getValue()));
//        }

        //step 4: Sort the distance and select n nodes among them
//         communcity = new HashSet<>();
        CoreDecomposition coreDecomposition = new CoreDecomposition();
        Set<Integer> denstcc = new HashSet<>();
        double minavgdist = 1.0;
        do {
            Set<Integer>  communcity = coreDecomposition.findCC(pnbmap,k,queryid);
            pnbmap = coreDecomposition.findDenstCore(pnbmap,keepset,distancemap,deletenum,k,queryid);
            double dist = adistance.cal_subgraph_attr_dist(communcity,distancemap);
            if(dist<minavgdist){
                denstcc.clear();
                denstcc.addAll(communcity);
                minavgdist = dist;
            }
        }while (pnbmap!=null);

        return denstcc;
    }

    public Set<Integer> findCompactC_deleteone(Map<Integer,Set<Integer>> pnbmap, Adistance adistance,int k, int queryid,Set<Integer> cc){
        //step 1: get the community nodes
        Set<Integer> keepset = new HashSet<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            int key = entry.getKey();
            if(entry.getValue().size()>0){
                keepset.add(key);
            }
        }

        //step 2: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for(int i:keepset){
            distancemap.put(i,new double[this.graph.length]);
        }

        //step 3: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:keepset){
            label.add(i);
            for (int j:keepset){
                if(!label.contains(j))
                {
                    double dist = adistance.cal_distance(i, j);
//                    System.out.println(i+" "+j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
                }
            }
        }
//        for (Map.Entry<Integer,double[]> entry:distancemap.entrySet()){
//           System.out.println(entry.getKey() +" dist "+ trans(entry.getValue()));
//        }

        //step 4: Sort the distance and select n nodes among them
        Set<Integer> communcity = new HashSet<>();Set<Integer> lastcommuncity = new HashSet<>();
        communcity.addAll(cc);lastcommuncity.addAll(cc);
        Set<Integer> denstcc = new HashSet<>();
        double minavgdist = adistance.cal_subgraph_attr_dist(cc,distancemap);
        CoreDecomposition coreDecomposition = new CoreDecomposition();
        do {
            pnbmap = coreDecomposition.findDenstCore_deleteone(pnbmap,communcity,distancemap,k,queryid);
            communcity = coreDecomposition.findCC(pnbmap,k,queryid);
            if(communcity==null) break;
            double dist = adistance.cal_subgraph_attr_dist(communcity,distancemap);
            if(dist<minavgdist){
                denstcc.clear();
                denstcc.addAll(communcity);
                minavgdist = dist;
            }

            lastcommuncity.removeAll(communcity);
             for(int node:lastcommuncity){
                    for(int ccnode:communcity){
                        distancemap.get(ccnode)[node]=0;
                    }
                    distancemap.put(node,new double[1]);
                }

            lastcommuncity.clear();
            lastcommuncity.addAll(communcity);

        }while (true);

        return denstcc;
    }

    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset){

        //step 2: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for(int i:keepset){
            distancemap.put(i,new double[this.graph.length]);
        }

        //step 3: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:keepset){
            label.add(i);
            for (int j:keepset){
                if(!label.contains(j))
                {
                    double dist = adistance.cal_distance(i, j);
//                    System.out.println(i+" "+j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
                }
            }
        }

        //step 4: Sort the distance and select n nodes among them
//         communcity = new HashSet<>();
        CoreDecomposition coreDecomposition = new CoreDecomposition();
        Set<Integer> denstcc = new HashSet<>(keepset);
        Set<Integer> community = new HashSet<>(keepset);
        Set<Integer> lastcommunity = new HashSet<>(keepset);
        double minavgdist = this.adistance.cal_subgraph_attr_dist(denstcc,distancemap);
        do {
            community = coreDecomposition.find_distsmallcc_deleteone(pnbmap,community,distancemap,this.queryK,this.queryId,minavgdist,this.adistance);
            if(community==null){
                break;
            }else {
                double dist = adistance.cal_subgraph_attr_dist(community,distancemap);
                denstcc.clear();
                denstcc.addAll(community);
                minavgdist = dist;

                lastcommunity.removeAll(community);
                for(int node:lastcommunity){
                    for(int ccnode:community){
                        distancemap.get(ccnode)[node]=0;
                    }
                    distancemap.put(node,new double[1]);
                }

                lastcommunity.clear();
                lastcommunity.addAll(community);
            }

        }while (true);

        return denstcc;
    }

    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,int queryM){
        //step 1: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for(int i:keepset){
            distancemap.put(i,new double[this.graph.length]);
        }

        //step 2: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:keepset){
            label.add(i);
//            int z=0;
            for (int j:keepset){
                if(!label.contains(j))
                {
                    double dist = adistance.cal_distance(i, j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
//                    System.out.println("i:" + i +",j:" +j);
//                    z++;
                }
            }
//            System.out.println("i:"+i+",z:"+z);
        }


        //step 3: Sort the distance and select n nodes among them
        CoreDecomposition coreDecomposition = new CoreDecomposition();
        Set<Integer> denstcc = new HashSet<>();Set<Integer> community = new HashSet<>();
        Set<Integer> lastcommunity = new HashSet<>();
        denstcc.addAll(keepset);community.addAll(keepset); lastcommunity.addAll(keepset);
//        Set<Integer> jumpset = new HashSet<>();
//        jumpset.add(this.queryId);
        double minavgdist = this.adistance.cal_subgraph_attr_dist(denstcc,distancemap);
        do {
            community = coreDecomposition.find_distsmallcc(pnbmap,community,distancemap,this.queryK,this.queryId,minavgdist,this.adistance,queryM);
            if(community==null){
                break;
            }else {
                double dist = adistance.cal_subgraph_attr_dist(community,distancemap);
                denstcc.clear();
                denstcc.addAll(community);
                minavgdist = dist;

                lastcommunity.removeAll(community);
                for(int node:lastcommunity){
                    for(int ccnode:community){
                        distancemap.get(ccnode)[node]=0;
                    }
                    distancemap.put(node,new double[1]);
                }

                lastcommunity.clear();
                lastcommunity.addAll(community);
            }
        }while (true);
        return denstcc;
    }

    public Set<Integer> findCompactC_jump(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,int queryM){
        //step 1: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for(int i:keepset){
            distancemap.put(i,new double[this.graph.length]);
        }

        //step 2: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:keepset){
            label.add(i);
//            int z=0;
            for (int j:keepset){
                if(!label.contains(j))
                {
                    double dist = adistance.cal_distance(i, j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
//                    System.out.println("i:" + i +",j:" +j);
//                    z++;
                }
            }
//            System.out.println("i:"+i+",z:"+z);
        }


        //step 3: Sort the distance and select n nodes among them
        CoreDecomposition coreDecomposition = new CoreDecomposition();
        Set<Integer> denstcc = new HashSet<>();Set<Integer> community = new HashSet<>();
        Set<Integer> lastcommunity = new HashSet<>();
        denstcc.addAll(keepset);community.addAll(keepset); lastcommunity.addAll(keepset);
        Set<Integer> jumpset = new HashSet<>();
        jumpset.add(this.queryId);
//        double minavgdist = this.adistance.cal_subgraph_attr_dist(denstcc);
        double minavgdist = this.adistance.cal_subgraph_attr_dist(denstcc,distancemap);
        do {
            community = coreDecomposition.find_distsmallcc(pnbmap,community,distancemap,this.queryK,this.queryId,minavgdist,this.adistance,queryM,jumpset);
            if(community==null){
                break;
            }else {
                double dist = this.adistance.cal_subgraph_attr_dist(denstcc,distancemap);
                denstcc.clear();
                denstcc.addAll(community);
                minavgdist = dist;

                lastcommunity.removeAll(community);
                for(int node:lastcommunity){
                    for(int ccnode:community){
                        distancemap.get(ccnode)[node]=0;
                    }
                    distancemap.put(node,new double[1]);
                }

                lastcommunity.clear();
                lastcommunity.addAll(community);
            }
        }while (true);
        return denstcc;
    }

    public void output(Map<Integer,Set<Integer>> pnbmap){
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

    private String trans(double[] V){
        StringBuffer str5 = new StringBuffer();
        for (double i : V) {
            str5.append(i+", ");
        }
        return str5.toString();
    }
}
