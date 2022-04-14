package MCSH.util;

import java.util.*;

public class FCS_nodist {
    //delete 1 nodes
    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,Adistance_float adistance,int queryId,int queryK){
        System.out.println("new dist delete one");
        //step 1: initialization
//        long t1 = System.nanoTime();
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
//        long t2 = System.nanoTime();
//        System.out.println("cal all distance:"+(t2-t1));

        //step 3: Sort the distance and select n nodes among them
        Set<Integer> denstcc = new HashSet<>(keepset);
        Set<Integer> community = new HashSet<>(keepset);
//        Set<Integer> lastcc = new HashSet<>(keepset);
//        int i = 0;
        float[] minavgdist = new float[1];
//        long t3 = System.nanoTime();
        minavgdist[0] = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
//        long t4 = System.nanoTime();
//        System.out.println("cal communcity avgdist:"+(t4-t3));
        do {
            community = find_distsmallcc_deleteone_new(pnbmap,community,queryK,queryId,adistance);
            if(community==null){
                break;
            }else {
//                float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
//                if(dist<minavgdist[0]){
//                    minavgdist[0]=dist;
////                    return community;
//                }
                denstcc = community;
            }
        }while (true);

        return denstcc;
    }

    //delete M nodes
    public Set<Integer> findCompactC(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,int M,Adistance_float adistance,int queryId,int queryK){
        System.out.println("new try delete m");
        //step 1: initialization
        long t1 = System.nanoTime();
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
        long t2 = System.nanoTime();
        System.out.println("cal all distance:"+(t2-t1));

        //step 4: Sort the distance and select n nodes among them
        Set<Integer> denstcc = new HashSet<>(keepset);
        Set<Integer> community = new HashSet<>(keepset);
        float[] minavgdist = new float[1];
        minavgdist[0] = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
        do {
            long t3 = System.nanoTime();
            community = find_distsmallcc_new(pnbmap,community,distancemap,queryK,queryId,minavgdist,adistance,M);
            long t4 = System.nanoTime();
            System.out.println("one Iteration:"+(t4-t3));
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
//
//                denstcc.clear();
////                denstcc.addAll(community);
                denstcc = community;
            }
        }while (true);

        return denstcc;
    }

    public Set<Integer> findCompactC_protect(Map<Integer,Set<Integer>> pnbmap,Set<Integer> keepset,int queryM,Adistance_float adistance,int queryId,int queryK){
        System.out.println("new try delete mp");
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
        Set<Integer> jumpset = new HashSet<>();
        jumpset.add(queryId);
        float[] minavgdist = new float[1];
        minavgdist[0] = adistance.cal_subgraph_attr_dist_new(denstcc,distancemap);
        do {
            long t3 = System.nanoTime();
            community = find_distsmallcc_protect(pnbmap,community,distancemap,queryK,queryId,minavgdist,adistance,queryM,jumpset);
            long t4 = System.nanoTime();
            System.out.println("one Iteration:"+(t4-t3));
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
//
//                denstcc.clear();
////                denstcc.addAll(community);
                denstcc = community;
            }
        }while (true);
        return denstcc;
    }

    private Set<Integer> find_distsmallcc_deleteone_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, int k, int queryid, Adistance_float adistance) {
        //计算每个节点的属性得分并排序
//        long t1 = System.nanoTime();
        Map<Integer,Float> sumdistance = adistance.cal_attribute_contribute(keepset);

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });
//        long t2 = System.nanoTime();
//        System.out.println("cal the attribute score:"+(t2-t1));

        //初始化一个日志图，保存删除的点以及连边
        Map<Integer, Set<Integer>> logMap = new HashMap<>();

        //单独删除距离最大的点，判断是否可以找到一个属性更紧密的社区
        for (int deletesub = 0; deletesub < keepset.size(); deletesub++) {
            int deleteid = entryList.get(deletesub).getKey();//选取删除点
            if (deleteid == queryid) {
                continue;
            }

            //从图中删除点及其连边
//            long t3 = System.nanoTime();
            Set<Integer> pnbset = pnbmap.get(deleteid);
            for (Integer j : pnbset) {
                pnbmap.get(j).remove(deleteid);
            }
//            pnbmap.put(deleteid,new HashSet<>());
            pnbmap.remove(deleteid);
            logMap.put(deleteid,pnbset);
//            long t4 = System.nanoTime();
//            System.out.println("delete node:"+(t4-t3));

            //在删除之后的图中判断是否能找到一个属性更紧密的kcore
//            long t5 = System.nanoTime();
            Set<Integer> community = findCKcore(pnbmap, k, queryid,logMap);
//            long t6 = System.nanoTime();
//            System.out.println("find communcity:"+(t6-t5));


            if (community != null) {
                return community;
            }

            //将pnbmap恢复成初始状态
//            long t9 = System.nanoTime();
            for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
                pnbmap.put(logkey,logSet);
            }
            for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
//                pnbmap.get(logkey).addAll(logSet);
                for (int setid: logSet) {
                    pnbmap.get(setid).add(logkey);
                }
            }
            logMap.clear();
//            long t10 = System.nanoTime();
//            System.out.println("recover the map:"+(t10-t9));

        }

        return null;
    }

    private Set<Integer> find_distsmallcc_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float[] lastdistance, Adistance_float adistance, int m) {
        //计算每个节点的属性得分并排序
        long t1 = System.nanoTime();
        Map<Integer,Float> sumdistance = adistance.cal_attribute_contribute(keepset);

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });
        long t2 = System.nanoTime();
        System.out.println("cal the attribute score:"+(t2-t1));

        //取前m个得分最大的作为删除节点集
        long t3 = System.nanoTime();
        Set<Integer> deletenodes = new HashSet<>();
        int z = 0;
        while (deletenodes.size() < m && z<keepset.size()) {
            int deleteid = entryList.get(z).getKey();
            if (deleteid != queryid) {
                deletenodes.add(deleteid);
            }
            z++;
        }

        //删除这m个点及其连边
        Map<Integer, Set<Integer>> logMap = new HashMap<>();
        for (int i : deletenodes) {
            Set<Integer> pnbset = pnbmap.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
                    pnbmap.get(j).remove(i);
                }
            }
//            pnbmap.put(i,new HashSet<>());
            pnbmap.remove(i);
            logMap.put(i,pnbset);
        }
        long t4 = System.nanoTime();
        System.out.println("delete m node:"+(t4-t3));

        //在删除之后的图中判断是否能找到一个属性更紧密的kcore
        long t5 = System.nanoTime();
        Set<Integer> community = findCKcore(pnbmap, k, queryid,logMap);
        long t6 = System.nanoTime();
        System.out.println("find communcity:"+(t6-t5));

        if (community != null) {
            long t7 = System.nanoTime();
            float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
            if(dist<lastdistance[0]){
                lastdistance[0] = dist;
                return community;
            }
            long t8 = System.nanoTime();
            System.out.println("determine:"+(t8-t7));
        }

        //将pnbmap恢复成初始状态
//        for(int logkey:logMap.keySet()){
//            pnbmap.put(logkey,new HashSet<>());
//        }
        long t9 = System.nanoTime();
        for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
            int logkey = entry.getKey();
            Set<Integer> logSet = entry.getValue();
            pnbmap.put(logkey,logSet);
        }
        for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
            int logkey = entry.getKey();
            Set<Integer> logSet = entry.getValue();
//            pnbmap.get(logkey).addAll(logSet);
            for (int setid: logSet) {
                pnbmap.get(setid).add(logkey);
            }
        }
        logMap.clear();
        long t10 = System.nanoTime();
        System.out.println("recover the map:"+(t10-t9));

        //单独删除距离最大的点，判断是否可以找到一个属性更紧密的社区
        for(int deletesub = 0; deletesub < keepset.size(); deletesub++) {
            int deleteid = entryList.get(deletesub).getKey();
            if (deleteid == queryid) {
                continue;
            }

            Set<Integer> pnbset = pnbmap.get(deleteid);
            for (Integer j : pnbset) {
                pnbmap.get(j).remove(deleteid);
            }
//            pnbmap.put(deleteid,new HashSet<>());
            pnbmap.remove(deleteid);
            logMap.put(deleteid,pnbset);

            community = findCKcore(pnbmap, k, queryid,logMap);
            if (community != null) {
                float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
                if(dist<lastdistance[0]){
                    lastdistance[0] = dist;
                    return community;
                }
            }
//            for(int logkey:logMap.keySet()){
//                pnbmap.put(logkey,new HashSet<>());
//            }
            for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
                pnbmap.put(logkey,logSet);
            }
            for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
//                pnbmap.get(logkey).addAll(logSet);
                for (int setid: logSet) {
                    pnbmap.get(setid).add(logkey);
                }
            }
            logMap.clear();
        }

        return null;
    }

    private Set<Integer> find_distsmallcc_protect(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float[] lastdistance, Adistance_float adistance, int m,Set<Integer> jumpset) {
        //计算每个节点的属性得分并排序
        long t1 = System.nanoTime();
        Map<Integer,Float> sumdistance = adistance.cal_attribute_contribute(keepset);

        List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
            @Override
            public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });
        long t2 = System.nanoTime();
        System.out.println("cal the attribute score:"+(t2-t1));

        //取前m个得分最大的作为删除节点集
        long t3 = System.nanoTime();
        Set<Integer> deletenodes = new HashSet<>();
        int z = 0;
        while (deletenodes.size() < m && z< keepset.size()) {
            int deleteid = entryList.get(z).getKey();
            if (!jumpset.contains(deleteid)) {
                deletenodes.add(deleteid);
            }
            z++;
        }

        //删除这m个点及其连边
        Map<Integer,Set<Integer>> logMap = new HashMap<>();
        for (int i : deletenodes) {
            Set<Integer> pnbset = pnbmap.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
                    pnbmap.get(j).remove(i);
                }
            }
//            pnbmap.put(i, new HashSet<>());
            pnbmap.remove(i);
            logMap.put(i,pnbset);
        }
        long t4 = System.nanoTime();
        System.out.println("delete node:"+(t4-t3));

        //在删除之后的图中判断是否能找到一个属性更紧密的kcore
        long t5 = System.nanoTime();
        Set<Integer> community = findCKcore(pnbmap, k, queryid,logMap);
        long t6 = System.nanoTime();
        System.out.println("find communcity:"+(t6-t5));
        if (community != null) {
            long t7 = System.nanoTime();
            float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
            if(dist<lastdistance[0]){
                lastdistance[0] = dist;
                return community;
            }
            long t8 = System.nanoTime();
            System.out.println("determine:"+(t8-t7));
        }

        //将pnbmap恢复成初始状态
//        for(int logkey:logMap.keySet()){
//            pnbmap.put(logkey,new HashSet<>());
//        }
        long t9 = System.nanoTime();
        for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
            int logkey = entry.getKey();
            Set<Integer> logSet = entry.getValue();
            pnbmap.put(logkey,logSet);
        }
        for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
            int logkey = entry.getKey();
            Set<Integer> logSet = entry.getValue();
//            pnbmap.get(logkey).addAll(logSet);
            for (int setid: logSet) {
                pnbmap.get(setid).add(logkey);
            }
        }
        logMap.clear();
        long t10 = System.nanoTime();
        System.out.println("recover the map:"+(t10-t9));

        //单独删除距离最大的点，判断是否可以找到一个属性更紧密的社区
        for(int deletesub = 0; deletesub < keepset.size(); deletesub++) {
            int deleteid = entryList.get(deletesub).getKey();
            if (jumpset.contains(deleteid)) {
                continue;
            }

            Set<Integer> pnbset = pnbmap.get(deleteid);
            for (Integer j : pnbset) {
                pnbmap.get(j).remove(deleteid);
            }
            pnbmap.remove(deleteid);
//            pnbmap.put(deleteid,new HashSet<>());
            logMap.put(deleteid,pnbset);

            community = findCKcore(pnbmap, k, queryid,logMap);
            if (community != null) {
                float dist = adistance.cal_subgraph_attr_dist_new(community,distancemap);
                if(dist<lastdistance[0]){
                    lastdistance[0] = dist;
                    return community;
                }
            }
            jumpset.add(deleteid);
//            for(int logkey:logMap.keySet()){
//                pnbmap.put(logkey,new HashSet<>());
//            }
            for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
                pnbmap.put(logkey,logSet);
            }
            for(Map.Entry<Integer,Set<Integer>> entry:logMap.entrySet()){
                int logkey = entry.getKey();
                Set<Integer> logSet = entry.getValue();
//                pnbmap.get(logkey).addAll(logSet);
                for (int setid: logSet) {
                    pnbmap.get(setid).add(logkey);
                }
            }
            logMap.clear();
        }

        return null;
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

            pnbmap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
//            pnbmap.remove(curId);
            logMap.put(curId,pnbSet);
        }

        //step 3: find the connected component containing q
        //找连通图
//        if(!pnbmap.containsKey(queryId)) return null;
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
