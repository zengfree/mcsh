package MCSH.online.exact;

import MCSH.util.Adistance_float;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class Kcore_find_float extends RecursiveTask<Set<Integer>> {
    private Map<Integer, Set<Integer>> pnbMap;
    private int queryK;
    private int queryId;
    private int deleteid;
    private Adistance_float adistance;
    private Map<Integer,Map<Integer,Float>> distanceMap;

    private Set<Integer> findKCore() {
        //step 0:delete the nodes and their neibors

        if(deleteid!=-1){
            Set<Integer> delneibor = pnbMap.get(deleteid);
            for(int nei: delneibor){
                pnbMap.get(nei).remove(deleteid);
            }
            pnbMap.put(deleteid,new HashSet<>());
        }

        //simulate a queue
        Queue<Integer> queue = new LinkedList<Integer>();

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

    public Kcore_find_float(Map<Integer, Set<Integer>> pnbMap, int queryK, int queryId, int deleteid, Adistance_float adistance, Map<Integer,Map<Integer,Float>> distancemap) {
        this.pnbMap = copyMap(pnbMap);
        this.queryK = queryK;
        this.queryId = queryId;
        this.deleteid = deleteid;
        this.adistance = adistance;
        this.distanceMap = distancemap;
    }

    private Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap){
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(),newset);
        }

        return newMap;
    }

//    public Set<Integer> findkore(Map<Integer,Set<Integer>> pnbMap, Set<Integer> deletenodes, int queryK, int queryId, int max,Adistance_float adistance,Map<Integer,Map<Integer,Float>> distanceMap){
//        this.pnbMap = copyMap(pnbMap);
//        this.queryK = queryK;
//        this.queryId = queryId;
////        Set<Integer> deletenodes1 = new HashSet<>(deletenodes);
////        this.max = max;
////        this.result = new HashSet<>();
//        this.adistance = adistance;
//        this.distanceMap = distanceMap;
//        return compute();
//    }

    @Override
    protected Set<Integer> compute() {
        Set<Integer> community = findKCore();
        if(community==null) return null;
        Set<Integer> result = new HashSet<>(community);

        Set<ForkJoinTask<Set<Integer>>> tasks =new HashSet<>();
        for(int nodeid:community){
            if(nodeid>deleteid)
            {
                tasks.add(new Kcore_find_float(this.pnbMap,this.queryK,this.queryId,nodeid,adistance,distanceMap));
            }
        }

        invokeAll(tasks);

        float mindist =this.adistance.cal_subgraph_attr_dist_compare(community,distanceMap);
        for (ForkJoinTask<Set<Integer>> task:tasks){
            Set<Integer> val = task.join();
            if(val==null) continue;
            float dist = this.adistance.cal_subgraph_attr_dist_compare(val,distanceMap);
            if(dist<=mindist){
                mindist = dist;
                result.clear();
                result.addAll(val);
            }
        }

        return result;
    }

    private String transset(Set<Integer> set){
        StringBuffer str5 = new StringBuffer();
        for (int i : set) {
            str5.append(i + ",");
        }
        return str5.toString();
    }
}