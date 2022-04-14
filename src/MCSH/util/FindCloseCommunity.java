package MCSH.util;

import java.util.*;

public class FindCloseCommunity {
    //delete 1 nodes
    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,Adistance_float adistance,int queryId,int queryK){
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
        Set<Integer> lastcommunity = new HashSet<>(keepset);
        float minavgdist = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
        do {
            community = find_distsmallcc_deleteone_new(pnbmap,community,distancemap,queryK,queryId,minavgdist,adistance);
            if(community==null){
                break;
            }else {
                float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
                denstcc.clear();
                denstcc.addAll(community);
                minavgdist = dist;

                lastcommunity.removeAll(community);
                for(int node:lastcommunity){
                    for(int ccnode:community){
                        distancemap.get(ccnode).put(node, (float) 0);
                    }
                    distancemap.put(node,new HashMap<>());
                }

                lastcommunity.clear();
                lastcommunity.addAll(community);
            }
        }while (true);

        return denstcc;
    }

    //delete M nodes
    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,int M,Adistance_float adistance,int queryId,int queryK){
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

        //step 4: Sort the distance and select n nodes among them
        Set<Integer> denstcc = new HashSet<>(keepset);
        Set<Integer> community = new HashSet<>(keepset);
        Set<Integer> lastcommunity = new HashSet<>(keepset);
        float minavgdist = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
        do {
            community = find_distsmallcc_new(pnbmap,community,distancemap,queryK,queryId,minavgdist,adistance,M);
            if(community==null){
                break;
            }else {
                float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
                denstcc.clear();
                denstcc.addAll(community);
                minavgdist = dist;

                lastcommunity.removeAll(community);
                for(int node:lastcommunity){
                    for(int ccnode:community){
                        distancemap.get(ccnode).put(node, (float) 0);
                    }
                    distancemap.put(node,new HashMap<>());
                }

                lastcommunity.clear();
                lastcommunity.addAll(community);
            }
        }while (true);

        return denstcc;
    }

    public Set<Integer> findCompactC_protect(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,int queryM,Adistance_float adistance,int queryId,int queryK){
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
        Set<Integer> lastcommunity = new HashSet<>(keepset);
        Set<Integer> jumpset = new HashSet<>();
        jumpset.add(queryId);
        float minavgdist = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
        do {
            community = find_distsmallcc_protect(pnbmap,community,distancemap,queryK,queryId,minavgdist,adistance,queryM,jumpset);
            if(community==null){
                break;
            }else {
                float dist = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
                denstcc.clear();
                denstcc.addAll(community);
                minavgdist = dist;

                lastcommunity.removeAll(community);
                for(int node:lastcommunity){
                    for(int ccnode:community){
                        distancemap.get(ccnode).put(node, (float) 0);
                    }
                    distancemap.put(node,new HashMap<>());
                }

                lastcommunity.clear();
                lastcommunity.addAll(community);
            }
        }while (true);
        return denstcc;
    }

    public Set<Integer> find_distsmallcc_deleteone_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, Float lastdistance, Adistance_float adistance) {
//        System.out.println("try find smallcc_deleteone");
        Map<Integer, Float> sumdistance = new HashMap<>();
        for (int i : keepset) {
            sumdistance.put(i,sumdist(distancemap.get(i)));
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
        for (int deletesub = 0; deletesub < keepset.size(); deletesub++) {
            int deleteid = entryList.get(deletesub).getKey();
            if (deleteid == queryid) {
                continue;
            }

//            System.out.println("deleteid:"+deleteid);
            Set<Integer> pnbset = map.get(deleteid);
            for (Integer j : pnbset) {
                map.get(j).remove(deleteid);
            }
            map.put(deleteid, new HashSet<>());

            Set<Integer> community = findCKcore(map, k, queryid);
            if (community != null && adistance.cal_subgraph_attr_dist_new(community,distancemap) < lastdistance) {
                pnbmap.clear();
                pnbmap.putAll(map);
//                System.out.println("return cc");
                return community;
            } else {
                map = copyMap(pnbmap);
//                map.clear();
//                map.putAll(pnbmap);
            }
        }

//        pnbmap.clear();
//        pnbmap.putAll(map);
        return null;
    }

    public Set<Integer> find_distsmallcc_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float lastdistance, Adistance_float adistance, int m) {
//        System.out.println("try find smallcc");
        Map<Integer, Float> sumdistance = new HashMap<>();
        for (int i : keepset) {
            sumdistance.put(i,sumdist(distancemap.get(i)));
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> deletenodes = new HashSet<>();
        int z = 0;
        while (deletenodes.size() < m && z<keepset.size()) {
            int deleteid = entryList.get(z).getKey();
            if (deleteid != queryid) {
                deletenodes.add(deleteid);
            }
            z++;
        }
//        System.out.println("deletenodes:"+trans(deletenodes));

        //delete the nodes and find a cc
        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
        for (int i : deletenodes) {
            Set<Integer> pnbset = map.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
                    map.get(j).remove(i);
                }
            }
            map.put(i, new HashSet<>());
        }

        Set<Integer> community = findCKcore(map, k, queryid);
        if (community != null && adistance.cal_subgraph_attr_dist_new(community,distancemap) < lastdistance) {
//            System.out.println("return closer cc");
            pnbmap.clear();
            pnbmap.putAll(map);
            return community;
        } else {
            map = copyMap(pnbmap);
//            System.out.println("try to delete one node");
            for(int deletesub = 0; deletesub < keepset.size(); deletesub++) {
                int deleteid = entryList.get(deletesub).getKey();
                if (deleteid == queryid) {
                    continue;
                }

                Set<Integer> pnbset = map.get(deleteid);
                for (Integer j : pnbset) {
                    map.get(j).remove(deleteid);
                }
                map.put(deleteid, new HashSet<>());

                community = findCKcore(map, k, queryid);
                if (community != null && adistance.cal_subgraph_attr_dist_new(community,distancemap) < lastdistance) {
                    pnbmap.clear();
                    pnbmap.putAll(map);
                    return community;
                } else {
                    map = copyMap(pnbmap);
                }
            }
        }

        pnbmap.clear();
        pnbmap.putAll(map);
        return null;
    }

    public Set<Integer> find_distsmallcc_protect(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float lastdistance, Adistance_float adistance, int m,Set<Integer> jumpset) {
//        System.out.println("try find smallcc");
        Map<Integer, Float> sumdistance = new HashMap<>();
        for (int i : keepset) {
            sumdistance.put(i,sumdist(distancemap.get(i)));
        }

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> deletenodes = new HashSet<>();
        int z = 0;
        while (deletenodes.size() < m && z< keepset.size()) {
            int deleteid = entryList.get(z).getKey();
            if (!jumpset.contains(deleteid)) {
                deletenodes.add(deleteid);
            }
            z++;
        }
//        System.out.println("deletenodes:"+trans(deletenodes));

        //delete the nodes and find a cc
        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
        for (int i : deletenodes) {
            Set<Integer> pnbset = map.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
                    map.get(j).remove(i);
                }
            }
            map.put(i, new HashSet<>());
        }

        Set<Integer> community = findCKcore(map, k, queryid);
        if (community != null && adistance.cal_subgraph_attr_dist_new(community,distancemap) < lastdistance) {
//            System.out.println("return closer cc");
            pnbmap.clear();
            pnbmap.putAll(map);
            return community;
        } else {
            map = copyMap(pnbmap);
//            System.out.println("try to delete one node");
            for(int deletesub = 0; deletesub < keepset.size(); deletesub++) {
                int deleteid = entryList.get(deletesub).getKey();
                if (jumpset.contains(deleteid)) {
                    continue;
                }

                Set<Integer> pnbset = map.get(deleteid);
                for (Integer j : pnbset) {
//                Set<Integer> tmpset = map.get(j);
//                tmpset.remove(deleteid);
                    map.get(j).remove(deleteid);
                }
                map.put(deleteid, new HashSet<>());

                community = findCKcore(map, k, queryid);
                if (community != null && adistance.cal_subgraph_attr_dist_new(community,distancemap) < lastdistance) {
                    pnbmap.clear();
                    pnbmap.putAll(map);
                    return community;
                } else {
                    jumpset.add(deleteid);
                    map = copyMap(pnbmap);
                }
            }
//            result = find_distsmallcc_deleteone(map, keepset, distancemap, k, queryid, lastdistance, adistance);
        }

        pnbmap.clear();
        pnbmap.putAll(map);
        return null;
    }


    public Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap) {
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(), newset);
        }
        return newMap;
    }

    public Set<Integer> findCKcore(Map<Integer, Set<Integer>> pnbmap, int K, int queryId) {
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



    private String trans(double[] V) {
        StringBuffer str5 = new StringBuffer();
        for (double i : V) {
            str5.append(i + ", ");
        }
        return str5.toString();
    }

    private String trans(Set<Integer> set) {
        StringBuffer str5 = new StringBuffer();
        for (int i : set) {
            str5.append(i + ", ");
        }
        return str5.toString();
    }

    public void output(Map<Integer, Set<Integer>> pnbmap) {
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                System.out.println("id:" + entry.getKey() + ",neibor:" + trans(entry.getValue()));
            }
        }
        System.out.println();
    }

    private double sumdist(double[] V) {
        double sum = 0;
        for (double i : V) {
            sum += i;
        }
        return sum;
    }

    private float sumdist(Map<Integer,Float> V) {
        float sum = 0;
        for (float i : V.values()) {
            sum += i;
        }
        return sum;
    }
}
