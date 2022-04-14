package MCSH.util;

import java.util.*;

public class FCS_corefind {
    //delete 1 nodes
    public Set<Integer> findC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,Adistance_float adistance,int queryId,int queryK){
        System.out.println("cf find all");
        //step 1: initialization
        Map<Integer, Map<Integer,Float>> distancemap = new HashMap<>();
        for(int i:keepset){
            distancemap.put(i,new HashMap<>());
        }

        //step 2: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i:keepset){
            label.add(i);
            for (int j:keepset){
                if(!label.contains(j))
                {
                    float dist = adistance.cal_distance(i, j);
                    distancemap.get(i).put(j,dist);
                    distancemap.get(j).put(i,dist);
                }
            }
        }

        //step 3: Sort the distance and select n nodes among them
        Set<Integer> denstcc = new HashSet<>(keepset);
        Set<Integer> community = new HashSet<>(keepset);
        float[] minavgdist = new float[1];
        minavgdist[0] = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
        do {
            community = findSmallCC(pnbmap,community,distancemap,queryK,queryId,minavgdist,adistance);
            if(community==null){
                break;
            }else {
//                denstcc.removeAll(community);
//                for(int node:denstcc){
//                    for(int ccnode:community){
//                        distancemap.get(ccnode).remove(node);
//                    }
//                    distancemap.remove(node);
//                }
                denstcc = community;
//                denstcc.clear();
//                denstcc.addAll(community);
            }
        }while (true);

        return denstcc;
    }

    private Set<Integer> findSmallCC(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float[] lastdistance, Adistance_float adistance) {
        //计算每个节点的属性得分并排序
        Map<Integer,Float> map = new HashMap<>();

        //初始化一个日志图，保存删除的点以及连边
        Map<Integer, Set<Integer>> logMap = new HashMap<>();
        List<Integer> list = new ArrayList<>(keepset);
        //单独删除距离最大的点，判断是否可以找到一个属性更紧密的社区
        //选取删除点
        for (int deleteid : list) {
            if (deleteid == queryid) {
                continue;
            }

            //从图中删除点及其连边
            Set<Integer> pnbset = pnbmap.get(deleteid);
            for (Integer j : pnbset) {
                pnbmap.get(j).remove(deleteid);
            }
            pnbmap.remove(deleteid);
            logMap.put(deleteid, pnbset);

            //在删除之后的图中判断是否能找到一个属性更紧密的kcore
            Set<Integer> community = findCKcore(pnbmap, k, queryid, logMap);
            if (community != null) {
                float dist = adistance.cal_subgraph_attr_dist_new(community, distancemap);
                map.put(deleteid,lastdistance[0]-dist);
            }

            //将pnbmap恢复成初始状态
            for (int logkey : logMap.keySet()) {
                pnbmap.put(logkey, new HashSet<>());
            }
            for (Map.Entry<Integer, Set<Integer>> entry : logMap.entrySet()) {
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
                pnbmap.get(logkey).addAll(logSet);
                for (int setid : logSet) {
                    pnbmap.get(setid).add(logkey);
                }
            }
            logMap.clear();
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(map.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        //取出其中差值最大的作为下一状态的结果
        Map.Entry<Integer, Float> dn = entryList.get(0);
        if(dn.getValue()<0){
            return null;
        }else {
            return findCKcore(pnbmap, k, queryid, logMap);
        }

//        return null;
    }

    private Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap) {
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(), newset);
        }
        return newMap;
    }

    private Set<Integer> findCKcore(Map<Integer, Set<Integer>> pnbmap, int K, int queryId) {
        Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue

        //step 1: find the vertices can be deleted in the first round
        Set<Integer> deleteSet = new HashSet<Integer>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            int curId = entry.getKey();
            Set<Integer> pnbSet = entry.getValue();
            if (pnbSet.size() < K) {
                queue.add(curId);
                deleteSet.add(curId);
            }
        }

        //step 2: delete vertices whose degrees are less than k
        while (queue.size() > 0) {
            int curId = queue.poll();//delete curId
            Set<Integer> pnbSet = pnbmap.get(curId);//找到curID对应的邻居
            for (int pnb : pnbSet) {//update curId's pnb
                if (!deleteSet.contains(pnb)) {
                    Set<Integer> tmpSet = pnbmap.get(pnb);
                    tmpSet.remove(curId);
                    if (tmpSet.size() < K) {
                        queue.add(pnb);
                        deleteSet.add(pnb);
                    }
                }
            }
//            pnbmap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
            pnbmap.remove(curId);
        }

        //step 3: find the connected component containing q
        //找连通图
        if(!pnbmap.containsKey(queryId)) return null;
        if (pnbmap.get(queryId).size() < K) return null;
        Set<Integer> community = new HashSet<Integer>();//vertices which have been put into queue
        Queue<Integer> ccQueue = new LinkedList<Integer>();
        ccQueue.add(queryId);
        community.add(queryId);
        while (ccQueue.size() > 0) {
            int curId = ccQueue.poll();
            for (int pnb : pnbmap.get(curId)) {//enumerate curId's neighbors
                if (!community.contains(pnb)) {
                    ccQueue.add(pnb);
                    community.add(pnb);
                }
            }
        }

        return community;
    }

    private Set<Integer> findCKcore(Map<Integer, Set<Integer>> pnbmap, int K, int queryId, Map<Integer,Set<Integer>> logMap) {
        Queue<Integer> queue = new LinkedList<Integer>();//simulate a queue

        //step 1: find the vertices can be deleted in the first round
        Set<Integer> deleteSet = new HashSet<Integer>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            int curId = entry.getKey();
            Set<Integer> pnbSet = entry.getValue();
            if (pnbSet.size() < K) {
                queue.add(curId);
                deleteSet.add(curId);
            }
        }

        //step 2: delete vertices whose degrees are less than k
        while (queue.size() > 0) {
            int curId = queue.poll();//delete curId
            Set<Integer> pnbSet = pnbmap.get(curId);//找到curID对应的邻居
//            if(!logMap.containsKey(curId)){
//                logMap.put(curId,new HashSet<>());
//            }
//            Set<Integer> curid_deletenode = logMap.get(curId);
            for (int pnb : pnbSet) {//update curId's pnb
                if (!deleteSet.contains(pnb)) {
                    Set<Integer> tmpSet = pnbmap.get(pnb);
                    tmpSet.remove(curId);
//                    curid_deletenode.add(pnb);//添加 删除了curid的节点
                    if (tmpSet.size() < K) {
                        queue.add(pnb);
                        deleteSet.add(pnb);
                    }
                }
            }

//            pnbmap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
            pnbmap.remove(curId);
            logMap.put(curId,pnbSet);
        }

        //step 3: find the connected component containing q
        //找连通图
        if(!pnbmap.containsKey(queryId)) return null;
        if (pnbmap.get(queryId).size() < K) return null;
        Set<Integer> community = new HashSet<Integer>();//vertices which have been put into queue
        Queue<Integer> ccQueue = new LinkedList<Integer>();
        ccQueue.add(queryId);
        community.add(queryId);
        while (ccQueue.size() > 0) {
            int curId = ccQueue.poll();
            for (int pnb : pnbmap.get(curId)) {//enumerate curId's neighbors
                if (!community.contains(pnb)) {
                    ccQueue.add(pnb);
                    community.add(pnb);
                }
            }
        }

        return community;
    }
}
