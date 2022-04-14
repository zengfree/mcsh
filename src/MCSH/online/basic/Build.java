package MCSH.online.basic;


import MCSH.util.*;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * build
 */
public class Build {
    private int graph[][] = null;//data graph, including vertex IDs, edge IDs, and their link relationships
    private int vertexType[] = null;//vertex -> type
    private int edgeType[] = null;//edge -> type
    private Map<Integer,float[]> attribute = null;

    private int queryId = -1;//the query vertex id
    private MetaPath queryMPath = null;//the query meta-path
    private int queryK = -1;//the threshold k
    private Adistance_float adistance = null;

    public Build(int[][] graph, int[] vertexType, int[] edgeType, Map<Integer, float[]> attributed , Adistance_float adistance) {
        this.graph = graph;
        this.vertexType = vertexType;
        this.edgeType = edgeType;
        this.attribute = attributed;
        this.adistance = adistance;
    }


    public Map<Integer,Set<Integer>> build1(MetaPath queryMPath,int queryId){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        //step 1: build the connected homogeneous graph
        return buildGraph();
    }

    public Map<Integer,Set<Integer>> buildm(MetaPath queryMPath,int queryId,int threadnum){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])   return null;

        return buildGraph_m(threadnum);
    }

    public Map<Integer,Set<Integer>> build3(MetaPath queryMPath,int queryId){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])
            return null;

        //step 1: build the connected homogeneous graph
        return buildGraph3();
    }

    public Map<Integer,Set<Integer>> bm2(MetaPath queryMPath,int queryId){
        this.queryId = queryId;
        this.queryMPath = queryMPath;
//        this.queryK = queryK;

        //step 0: check whether queryId's type matches with the meta-path
        if(queryMPath.vertex[0] != vertexType[queryId])
            return null;

        //step 1: build the connected homogeneous graph
        return b_m2(20);
    }


    private Map<Integer, Set<Integer>> buildGraph() {
        //step 1: compute the connected subgraph via batch-search with labeling (BSL)
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);
        System.out.println(keepSet.size()+":size");
        //step 2: build the graph
        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
        Map<Integer, Set<Integer>> pnbMap = new HashMap<Integer, Set<Integer>>();
        BatchSearch batchSearch = new BatchSearch(graph, vertexType, edgeType, queryMPath);
//        for(int curId = 0;curId < graph.length;curId ++) {
//            if(vertexType[curId] == queryMPath.vertex[0]) {
//                Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
//                pnbMap.put(curId, pnbSet);
//            }
//        }
        for (int curId: keepSet) {
            Set<Integer> pnbSet = batchSearch.collect(curId, keepSet);
            pnbMap.put(curId,pnbSet);
        }

        return pnbMap;
    }


//    private Map<Integer, Set<Integer>> buildGraph2() {
//        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
//        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);
//        System.out.println(keepSet.size()+":size");
//        //step 2: build the graph
//        //对图中的每个节点进行遍历，若节点标签与路径初始值相同则找其P-邻居并保存在<v,set<>>
//        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
//        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
//        for(int i:keepSet){
//            neibors.put(i,new HashSet<>());
//        }
//
//        Queue<Integer> queue = new LinkedList<>();
//        Set<Integer> connected = new HashSet<>();
//        queue.add(queryId);
//        while (queue.size()>0){
//            int start = queue.poll();
//            connected.add(start);
//            System.out.println(start);
//            Set<Integer> neibor = neibors.get(start);
//            for(Integer keepnode:keepSet){
//                if(!connected.contains(keepnode)){
////                    System.out.println(start+":"+keepnode);
//                    Set<Integer> set1 = halfneibors.get(start);Set<Integer> set2 = halfneibors.get(keepnode);
//                    if(set1==null||set2==null){
//                        continue;
//                    }
////                    Set<Integer> set3 = new HashSet<>(set1);
//                    Set<Integer> set = set1.size()>set2.size()?set1:set2;
//                    boolean flag = true;
//                    for(int i:set){
//                        if(set2.contains(i)){
//                            flag = false;
//                            break;
//                        }
//                    }
//                    if(!flag){
//                        queue.add(keepnode);
//                        neibor.add(keepnode);
//                        neibors.get(keepnode).add(start);
//                    }
//                }
//            }
//        }
//
//        return neibors;
//    }


    private Map<Integer, Set<Integer>> buildGraph3() {
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);

        //step 2: build the index
        Map<Integer,Set<Integer>> halfneibors = findhalf_neibors(keepSet);
        Map<Integer,Set<Integer>> index = new HashMap<>();//index

        for(Map.Entry<Integer,Set<Integer>> entry:halfneibors.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            int key = entry.getKey();
            for (Integer p: pneibor){
                Set<Integer> set = index.getOrDefault(p,new HashSet<>());
                set.add(key);
                if (set.size()==1){
                    index.put(p,set);
                }
            }
        }

        //step3:build the graph
        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
        for(int id:keepSet){
            neibors.put(id,new HashSet<>());
        }

        for(Map.Entry<Integer,Set<Integer>> entry:index.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            for(Integer integer:pneibor){
                Set<Integer> pnbSet = neibors.get(integer);
                pnbSet.addAll(pneibor);
//                pnbSet.remove(integer);
            }
        }

        for (int i:neibors.keySet()){
            neibors.get(i).remove(i);
        }

        return neibors;
    }

    private Map<Integer, Set<Integer>> buildGraph_m(int threadnum) {
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);

        //step 2: build the index
        Map<Integer,Set<Integer>> index = InventIndex(keepSet);

        //step3:build the graph
//        Map<Integer,Set<Integer>> map2 = new ConcurrentHashMap<>();//完整路径的邻居
        Map<Integer,Set<Integer>> map2 = new HashMap<>();//完整路径的邻居

        for(int id:keepSet){
//            map2.put(id, new HashSet<>());
            map2.put(id, Collections.synchronizedSet(new HashSet<>()));
        }

//        Lock lock = new ReentrantLock();
//        KeyLock keyLock = new KeyLock();
        ExecutorService exec = Executors.newFixedThreadPool(threadnum);
        for(Map.Entry<Integer,Set<Integer>> entry:index.entrySet()){
            exec.submit(()->{
                Set<Integer> pneibor = entry.getValue();
                for(Integer integer:pneibor){
                    map2.get(integer).addAll(pneibor);
//                    while(true) {
//                        if (keyLock.lock(integer)) {
//                            try {
//                                map2.get(integer).addAll(pneibor);
//                            } finally {
//                                keyLock.unlock(integer);
//                            }break;
//                        } else {
//                            try {
//                                System.out.println("get error"+integer);
//                                Thread.sleep(1);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
                }
            });
        }
        exec.shutdown();
        while (true){
            if(exec.isTerminated()){
//                System.out.println(j);
                break;
            }else {
                try{
                    Thread.sleep(1);
                }catch (InterruptedException exception){
                    exception.printStackTrace();
                }
            }
        }

        for (int i:map2.keySet()){
            map2.get(i).remove(i);
        }

        return map2;
    }


    private Map<Integer, Set<Integer>> b_m2(int threadnum) {
        BatchLinker batchLinker = new BatchLinker( graph,vertexType, edgeType);
        Set<Integer> keepSet = batchLinker.link(queryId, queryMPath);

        //step 2: build the index
        Map<Integer,Set<Integer>> index = InventIndex(keepSet);

        //step3:build the graph
//        Map<Integer,Set<Integer>> map2 = new ConcurrentHashMap<>();//完整路径的邻居
        Map<Integer,Set<Integer>> map2 = new HashMap<>();//完整路径的邻居

        for(int id:keepSet){
//            map2.put(id, new HashSet<>());
            map2.put(id, Collections.synchronizedSet(new HashSet<>()));
        }

//        Lock lock = new ReentrantLock();
//        KeyLock keyLock = new KeyLock();
        ExecutorService exec = Executors.newFixedThreadPool(threadnum);
        for(Map.Entry<Integer,Set<Integer>> entry:index.entrySet()){
            Set<Integer> pneibor = entry.getValue();
            for(Integer integer:pneibor){
                exec.submit(()->{
                    map2.get(integer).addAll(pneibor);
                });
            }
        }
        exec.shutdown();
        while (true){
            if(exec.isTerminated()){
//                System.out.println(j);
                break;
            }else {
                try{
                    Thread.sleep(1);
                }catch (InterruptedException exception){
                    exception.printStackTrace();
                }
            }
        }

        for (int i:map2.keySet()){
            map2.get(i).remove(i);
        }

        return map2;
    }

    private Map<Integer, Set<Integer>> buildGraph4() {
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

//                Set<Integer> set = index.getOrDefault(p,new HashSet<>());
//                set.add(key);
//                if (set.size()==1){
//                    index.put(p,set);
//                }
            }
        }

        //step3:build the graph
        Map<Integer,Set<Integer>> neibors = new HashMap<>();//完整路径的邻居
        for(int id:keepSet){
            neibors.put(id,new HashSet<>());
        }

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
            Set<Integer> nb =ms.collect(keepnode);
            halfneibors.put(keepnode,nb);
        }
        return halfneibors;
    }

    private Map<Integer,Set<Integer>> InventIndex(Set<Integer> keepSet){
        Map<Integer,Set<Integer>> iindex = new HashMap<>();
        HalfPathSearch ms = new HalfPathSearch(graph,vertexType,edgeType, queryMPath);
        for(int keepnode:keepSet){
            Set<Integer> nb =ms.collect(keepnode);
            for(int i:nb){
                Set<Integer> set = iindex.getOrDefault(i,new HashSet<>());
                set.add(keepnode);
                if (set.size()==1){
                    iindex.put(i,set);
                }
            }
        }
        return iindex;
    }
}
