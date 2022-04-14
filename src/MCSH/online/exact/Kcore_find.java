package MCSH.online.exact;

import MCSH.util.Adistance;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

public class Kcore_find  extends RecursiveTask<Set<Integer>> {
    private Map<Integer, Set<Integer>> pnbMap;
    private int queryK;
    private int queryId;
    private Set<Integer> deletenodes;
    private Set<Integer> result;
    private int max;
    private Adistance adistance;
    private Map<Integer,double[]> distanceMap;

    public Kcore_find() {

    }

    private Set<Integer> findKCore() {
        Set<Integer> deleteSet = new HashSet<>(deletenodes);
//        deleteSet.add(this.max);
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
        Queue<Integer> queue = new LinkedList<Integer>(deleteSet);

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

    public Kcore_find(Map<Integer, Set<Integer>> pnbMap, int queryK, int queryId, Set<Integer> deletenodes,Adistance adistance,Map<Integer,double[]> distancemap) {
        this.pnbMap = copyMap(pnbMap);
        this.queryK = queryK;
        this.queryId = queryId;
        this.deletenodes = new HashSet<>(deletenodes);
//        this.result = result;
        if(deletenodes.size()==0){
            this.max = -1;
        }else {
            this.max = Collections.max(deletenodes);
        }
        this.result = new HashSet<>();
        this.adistance = adistance;
        this.distanceMap = distancemap;
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


    public Set<Integer> findkore(Map<Integer,Set<Integer>> pnbMap, Set<Integer> deletenodes, int queryK, int queryId, int max,Adistance adistance,Map<Integer,double[]> distanceMap){
        this.pnbMap = copyMap(pnbMap);
        this.queryK = queryK;
        this.queryId = queryId;
        this.deletenodes = new HashSet<>(deletenodes);
        this.max = max;
        this.result = new HashSet<>();
        this.adistance = adistance;
        this.distanceMap = distanceMap;
        return compute();
    }

    @Override
    protected Set<Integer> compute() {
        Set<Integer> community = findKCore();
        if(community==null) return null;
        result.addAll(community);

        Set<Integer> CandidateSet = new HashSet<>(deletenodes);
        Set<Integer> label = new HashSet<>();
//        PriorityQueue<Integer> queue = new PriorityQueue<>(community);
        Set<ForkJoinTask<Set<Integer>>> tasks =new HashSet<>();
        for(int nodeid:community){
            if(nodeid<max) continue;
            else if(label.contains(nodeid)) continue;
            if(!deletenodes.contains(nodeid)){
                CandidateSet.add(nodeid);
                tasks.add(new Kcore_find(this.pnbMap,this.queryK,this.queryId,CandidateSet,adistance,distanceMap));
                CandidateSet.remove(nodeid);
            }
            label.add(nodeid);
        }

//        while (queue.size()>0){
//            int nodeid = queue.poll();
//            if(nodeid<max) continue;
//            else if (label.contains(nodeid)) continue;
//            if(!deletenodes.contains(nodeid)){
//                CandidateSet.add(nodeid);
//                tasks.add(new Kcore_find(this.pnbMap,this.queryK,this.queryId,CandidateSet,adistance,distanceMap));
//                CandidateSet.remove(nodeid);
//            }
//            label.add(nodeid);
//        }


//        System.out.println("path"+transset(deletenodes)+",tasksize:"+tasks.size());
        invokeAll(tasks);

        double mindist =this.adistance.cal_subgraph_attr_dist(community,distanceMap);
        for (ForkJoinTask<Set<Integer>> task:tasks){
            Set<Integer> val = task.join();
            if(val==null) continue;
            double dist = this.adistance.cal_subgraph_attr_dist(val,distanceMap);
            if(dist<mindist){
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
