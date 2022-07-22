package MCSH.online.exact;

import MCSH.util.Adistance_float;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class K3 extends RecursiveTask<Set<Integer>> {
    private Map<Integer, Set<Integer>> pnbMap;
    private Map<Integer, Set<Integer>> logMap = new HashMap<>();
    private final int queryK;
    private final int queryId;
    private int deleteid;
    private Adistance_float adistance;
    private Map<Integer,Map<Integer,Float>> distanceMap;

    private Set<Integer> findKCore() {
        //step 0:delete the nodes and their neibors
        if(deleteid==-1) return pnbMap.keySet();

        Set<Integer> delneibor = pnbMap.get(deleteid);
        for(int nei: delneibor){
            pnbMap.get(nei).remove(deleteid);
        }
        pnbMap.put(deleteid,new HashSet<>());
        this.logMap.put(deleteid,new HashSet<>(delneibor));

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
//            pnbMap.remove(curId);
            pnbMap.put(curId,new HashSet<>());
            logMap.put(curId,pnbSet);
        }

        //step 3: find the connected component containing q
        //找连通图
        if(!pnbMap.containsKey(queryId)) return null;
        if(pnbMap.get(queryId).size() < queryK)   return null;
        Set<Integer> community = new HashSet<>();//vertices which have been put into queue
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

    public K3(Map<Integer, Set<Integer>> pnbMap, int queryK, int queryId, int deleteid, Adistance_float adistance, Map<Integer,Map<Integer,Float>> distancemap) {
        this.pnbMap = copyMap(pnbMap);
        this.queryK = queryK;
        this.queryId = queryId;
        this.deleteid = deleteid;
        this.adistance = adistance;
        this.distanceMap = distancemap;
//        this.logMap = copyMap(pnbMap);
//        this.deleteneibor = new HashSet<>(pnbMap.get(deleteid));
    }

    private void recover(){
        for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
            int logkey = entry.getKey();
            Set<Integer> logSet = entry.getValue();
            for (int setid: logSet) {
                pnbMap.get(setid).add(logkey);
            }
            pnbMap.put(logkey,logSet);
        }
    }

    private void redel(){
        for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
            int logkey = entry.getKey();
            Set<Integer> logSet = entry.getValue();
            for (int setid: logSet) {
                pnbMap.get(setid).add(logkey);
            }
            pnbMap.remove(logkey);
        }
    }

    private void clear(){
        this.pnbMap.clear();
        this.logMap.clear();
    }

    private Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap){
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(),newset);
        }

        return newMap;
    }

    @Override
    protected Set<Integer> compute() {

        Set<Integer> community = findKCore();
        if(community==null) return null;
        if(community.size()<=queryK) return null;
        Set<Integer> result = new HashSet<>(community);

//        recover();
        Set<Integer> set = findinfluence(logMap);
//        redel();
        Set<ForkJoinTask<Set<Integer>>> tasks =new HashSet<>();
        for(int nodeid:community){
            if(nodeid>deleteid&&!set.contains(nodeid))
            {
                tasks.add(new K3(this.pnbMap,this.queryK,this.queryId,nodeid,adistance,distanceMap));
//                System.out.println(nodeid);
            }
        }
//        clear();

        invokeAll(tasks);

        float mindist =this.adistance.cal_subgraph_attr_dist_compare(community,distanceMap);
        for (ForkJoinTask<Set<Integer>> task:tasks){
            Set<Integer> val = task.join();
            if(val==null) continue;
            float dist = this.adistance.cal_subgraph_attr_dist_compare(val,distanceMap);
            if(mindist-dist>1e-9){
                mindist = dist;
                result = val;
            }
        }

        return result;
    }

    public Set<Integer> findinfluence(Map<Integer,Set<Integer>> pnbMap){
        if(deleteid==-1) return new HashSet<>();
        Map<Integer,Set<Integer>> log = new HashMap<>();
        Set<Integer> set = new HashSet<>();
        Queue<Integer> canqueue = new LinkedList<>(pnbMap.get(deleteid));
        while (!canqueue.isEmpty()){
            int can = canqueue.poll();
            Set<Integer> canneibor = new HashSet<>(pnbMap.get(can));
            Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue
            Set<Integer> deleteSet = new HashSet<Integer>();
            queue.add(can);
            deleteSet.add(can);

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
                log.put(curId,pnbSet);
            }

            if(pnbMap.get(deleteid).size()<queryK){
                set.add(can);
                queue.addAll(canneibor);
            }
            for(Map.Entry<Integer,Set<Integer>> entry:log.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
                for (int setid: logSet) {
                    pnbMap.get(setid).add(logkey);
                }
                pnbMap.put(logkey,logSet);
            }
        }

        return set;
    }

    private String transset(Set<Integer> set){
        StringBuffer str5 = new StringBuffer();
        for (int i : set) {
            str5.append(i + ",");
        }
        return str5.toString();
    }
}
