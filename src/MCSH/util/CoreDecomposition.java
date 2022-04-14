package MCSH.util;

import java.util.*;

public class CoreDecomposition {
    //delete ndoes and find a closer community
    public Set<Integer> findCompactC(Map<Integer, Set<Integer>> pnbmap, Adistance adistance, int deletenum, int k, int queryid) {
        //step 1: get the community nodes
        Set<Integer> keepset = new HashSet<>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            int key = entry.getKey();
            if (entry.getValue().size() > 0) {
                keepset.add(key);
            }
        }

        //step 2: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for (int i : keepset) {
            distancemap.put(i, new double[pnbmap.size()]);
        }

        //step 3: cal the distance
        Set<Integer> label = new HashSet<>();
        for (int i : keepset) {
            label.add(i);
            for (int j : keepset) {
                if (label.contains(j)) continue;
                double dist = adistance.cal_distance(i, j);
                distancemap.get(i)[j] += dist;
                distancemap.get(j)[i] += dist;
            }
        }
//        for (Map.Entry<Integer,double[]> entry:distancemap.entrySet()){
//           System.out.println(entry.getKey() +" dist "+ trans(entry.getValue()));
//        }

        //step 4: Sort the distance and select n nodes among them
        Set<Integer> communcity = new HashSet<>();
        do {
            communcity = findCC(pnbmap, k, queryid);
            pnbmap = findDenstCore(pnbmap, keepset, distancemap, deletenum, k, queryid);
        } while (pnbmap != null);

        return communcity;
    }

    //delete n nodes and find a new map
    public Map<Integer, Set<Integer>> findDenstCore(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, double[]> distancemap, int n, int k, int queryid) {
        //step 4: Sort the distance and select n nodes among them
        Map<Integer, Double> sumdistance = new HashMap<>();
        for (int i : keepset) {
            double[] idist = distancemap.get(i);
            sumdistance.put(i, sumdist(idist));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sumdistance.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Set<Integer> deletenodes = new HashSet<>();
        int z = 0;
        while (deletenodes.size() < n) {
            int deleteid = entryList.get(z).getKey();
            if (deleteid != queryid) {
                deletenodes.add(deleteid);
//                queue.add(deleteid);
            }
            z++;
        }

        //step 5: delete the ndoes and find a cc
        for (int i : deletenodes) {
            Set<Integer> pnbset = pnbmap.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
                    Set<Integer> tmpset = pnbmap.get(j);
                    tmpset.remove(i);
                }
            }
            pnbmap.put(i, new HashSet<>());
        }

        Map<Integer, Set<Integer>> newmap = findKcore(pnbmap, k, queryid);
        if (newmap != null) {
            keepset.removeAll(deletenodes);

            //update distancemap
            for (int i : keepset) {
                for (int j : deletenodes) {
                    distancemap.get(i)[j] = 0;
                }
            }
            for (int i : deletenodes) {
                distancemap.put(i, new double[1]);
            }

        }
        return newmap;
    }

    //delete 1 nodes and find a new map
    public Map<Integer, Set<Integer>> findDenstCore_deleteone(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, double[]> distancemap, int k, int queryid) {
        //step 4: Sort the distance and select 1 nodes among them
        Map<Integer, Double> sumdistance = new HashMap<>();
        for (int i : keepset) {
            double[] idist = distancemap.get(i);
            sumdistance.put(i, sumdist(idist));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sumdistance.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Map<Integer, Set<Integer>> newmap = new HashMap<>();
        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
        //step 5: delete the ndoes and find a cc
        for (int i = 0; i < keepset.size(); i++) {

            //get the delete node
            int deleteid = entryList.get(i).getKey();

            if (deleteid == queryid) {
                continue;
            } else {

            }
//            Map<Integer,Set<Integer>> map = copyMap(pnbmap);
            //delete the neibors of delete node
            Set<Integer> pnbset = pnbmap.get(deleteid);
            for (Integer j : pnbset) {
                Set<Integer> tmpset = pnbmap.get(j);
                tmpset.remove(deleteid);
            }
            pnbmap.put(deleteid, new HashSet<>());

            //find a new kcore
            newmap = findKcore(pnbmap, k, queryid);
            if (newmap != null) {
                keepset.remove(deleteid);

                //update distancemap
//                for(int j:keepset){
//                    distancemap.get(j)[deleteid] = 0;
//                }
//                distancemap.put(deleteid,new double[1]);
                return newmap;
            } else {
                pnbmap = copyMap(map);
            }
        }

        return newmap;
    }

    public Map<Integer, Set<Integer>> findDenstCore_deleteone(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, int k, int queryid, int graphlength, Adistance adistance) {

        //step 2: initialization
        Map<Integer, double[]> distancemap = new HashMap<>();
        for (int i : keepset) {
            distancemap.put(i, new double[graphlength]);
        }

        //step 3: cal the distance
        Set<Integer> label = new HashSet<>();
        for (int i : keepset) {
            label.add(i);
            for (int j : keepset) {
                if (!label.contains(j)) {
                    double dist = adistance.cal_distance(i, j);
//                    System.out.println(i+" "+j);
                    distancemap.get(i)[j] = dist;
                    distancemap.get(j)[i] = dist;
                }
            }
        }

        //step 4: Sort the distance and select 1 nodes among them
        Map<Integer, Double> sumdistance = new HashMap<>();
        for (int i : keepset) {
            double[] idist = distancemap.get(i);
            sumdistance.put(i, sumdist(idist));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sumdistance.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
//                return o1.getValue().compareTo(o2.getValue());//升序
                return o2.getValue().compareTo(o1.getValue());//降序排列
            }
        });

        Map<Integer, Set<Integer>> newmap = new HashMap<>();
        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
        //step 5: delete the ndoes and find a cc
        for (int i = 0; i < keepset.size(); i++) {

            //get the delete node
            int deleteid = entryList.get(i).getKey();

            if (deleteid == queryid)
                continue;
//            Map<Integer,Set<Integer>> map = copyMap(pnbmap);
            //delete the neibors of delete node
            Set<Integer> pnbset = pnbmap.get(deleteid);
            for (Integer j : pnbset) {
                Set<Integer> tmpset = pnbmap.get(j);
                tmpset.remove(deleteid);
            }
            pnbmap.put(deleteid, new HashSet<>());

            //find a new kcore
            newmap = findKcore(pnbmap, k, queryid);
//            Set<Integer> cc = findCC(newmap,k,queryid);
            if (newmap.get(queryid).size() >= k) {
//                //update distancemap
//                for(int j:keepset){
//                    distancemap.get(j)[deleteid] = 0;
//                }
//                distancemap.put(deleteid,new double[1]);
                return newmap;
            } else {
                pnbmap = copyMap(map);
            }
        }

        return null;
    }

    public Set<Integer> find_distsmallcc_deleteone(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, double[]> distancemap, int k, int queryid, double lastdistance, Adistance adistance) {
        System.out.println("try find smallcc_deleteone");
        Map<Integer, Double> sumdistance = new HashMap<>();
        for (int i : keepset) {
//            double[] idist = distancemap.get(i);
            sumdistance.put(i, sumdist(distancemap.get(i)));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
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

            System.out.println("deleteid:"+deleteid);
            Set<Integer> pnbset = map.get(deleteid);
            for (Integer j : pnbset) {
//                Set<Integer> tmpset = map.get(j);
//                tmpset.remove(deleteid);
                map.get(j).remove(deleteid);
            }
            map.put(deleteid, new HashSet<>());

            Set<Integer> community = findCKcore(map, k, queryid);
            if (community != null && adistance.cal_subgraph_attr_dist(community,distancemap) < lastdistance) {
                pnbmap.clear();
                pnbmap.putAll(map);
                map = null;
                System.out.println("return cc");
                return community;
            } else {
                map = copyMap(pnbmap);
            }
        }

        pnbmap.clear();
        pnbmap.putAll(map);
        map = null;
        return null;
    }

    public Set<Integer> find_distsmallcc(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, double[]> distancemap, int k, int queryid, double lastdistance, Adistance adistance, int m) {
        System.out.println("try find smallcc");
        Map<Integer, Double> sumdistance = new HashMap<>();
        for (int i : keepset) {
//            double[] idist = distancemap.get(i);
//            sumdistance.put(i, sumdist(idist));
            sumdistance.put(i,sumdist(distancemap.get(i)));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
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
        System.out.println("deletenodes:"+trans(deletenodes));

        //delete the nodes and find a cc
        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
//        System.out.println("map:");
//        output(map);
        for (int i : deletenodes) {
            Set<Integer> pnbset = map.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
//                    Set<Integer> tmpset = map.get(j);
//                    tmpset.remove(i);
                    map.get(j).remove(i);
                }
            }
            map.put(i, new HashSet<>());
        }

        Set<Integer> community = findCKcore(map, k, queryid);
        Set<Integer> result;
        if (community != null && adistance.cal_subgraph_attr_dist(community,distancemap) < lastdistance) {
            System.out.println("return closer cc");
            pnbmap.clear();
            pnbmap.putAll(map);
            return community;
        } else {
            map = copyMap(pnbmap);
            System.out.println("try to delete one node");
            for(int deletesub = 0; deletesub < keepset.size(); deletesub++) {
                int deleteid = entryList.get(deletesub).getKey();
                if (deleteid == queryid) {
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
                if (community != null && adistance.cal_subgraph_attr_dist(community,distancemap) < lastdistance) {
                    pnbmap.clear();
                    pnbmap.putAll(map);
                    map = null;
                    return community;
                } else {
                    map = copyMap(pnbmap);
                }
            }
//            result = find_distsmallcc_deleteone(map, keepset, distancemap, k, queryid, lastdistance, adistance);
        }

        pnbmap.clear();
        pnbmap.putAll(map);
        map = null;
        return null;
    }

    public Set<Integer> find_distsmallcc(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, double[]> distancemap, int k, int queryid, double lastdistance, Adistance adistance, int m,Set<Integer> jumpset) {
        System.out.println("try find smallcc");
        Map<Integer, Double> sumdistance = new HashMap<>();
        for (int i : keepset) {
            sumdistance.put(i,sumdist(distancemap.get(i)));
        }

        List<Map.Entry<Integer, Double>> entryList = new ArrayList<>(sumdistance.entrySet());
        entryList.sort(new Comparator<Map.Entry<Integer, Double>>() {
            @Override
            public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
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
        System.out.println("deletenodes:"+trans(deletenodes));

        //delete the nodes and find a cc
        Map<Integer, Set<Integer>> map = copyMap(pnbmap);
        for (int i : deletenodes) {
            Set<Integer> pnbset = map.get(i);
            for (Integer j : pnbset) {
                if (!deletenodes.contains(j)) {
//                    Set<Integer> tmpset = map.get(j);
//                    tmpset.remove(i);
                    map.get(j).remove(i);
                }
            }
            map.put(i, new HashSet<>());
        }

        Set<Integer> community = findCKcore(map, k, queryid);
        Set<Integer> result;
        if (community != null && adistance.cal_subgraph_attr_dist(community,distancemap) < lastdistance) {
            System.out.println("return closer cc");
            pnbmap.clear();
            pnbmap.putAll(map);
            return community;
        } else {
            map = copyMap(pnbmap);
            System.out.println("try to delete one node");
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
                if (community != null && adistance.cal_subgraph_attr_dist(community,distancemap) < lastdistance) {
                    pnbmap.clear();
                    pnbmap.putAll(map);
                    map = null;
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
        map = null;
        return null;
    }

    public Set<Integer> find_distsmallcc_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float lastdistance, Adistance_float adistance, int m,Set<Integer> jumpset) {
        System.out.println("try find smallcc");
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
        System.out.println("deletenodes:"+trans(deletenodes));

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
            System.out.println("return closer cc");
            pnbmap.clear();
            pnbmap.putAll(map);
            return community;
        } else {
            map = copyMap(pnbmap);
            System.out.println("try to delete one node");
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

    public Set<Integer> find_distsmallcc_deleteone_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, Float lastdistance, Adistance_float adistance) {
        System.out.println("try find smallcc_deleteone");
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

            System.out.println("deleteid:"+deleteid);
            Set<Integer> pnbset = map.get(deleteid);
            for (Integer j : pnbset) {
                map.get(j).remove(deleteid);
            }
            map.put(deleteid, new HashSet<>());

            Set<Integer> community = findCKcore(map, k, queryid);
            if (community != null && adistance.cal_subgraph_attr_dist_new(community,distancemap) < lastdistance) {
                pnbmap.clear();
                pnbmap.putAll(map);
                System.out.println("return cc");
                return community;
            } else {
                map = copyMap(pnbmap);
            }
        }

        pnbmap.clear();
        pnbmap.putAll(map);
        return null;
    }

    public Set<Integer> find_distsmallcc_new(Map<Integer, Set<Integer>> pnbmap, Set<Integer> keepset, Map<Integer, Map<Integer,Float>> distancemap, int k, int queryid, float lastdistance, Adistance_float adistance, int m) {
        System.out.println("try find smallcc");
        Map<Integer, Float> sumdistance = new HashMap<>();
        for (int i : keepset) {
//            double[] idist = distancemap.get(i);
//            sumdistance.put(i, sumdist(idist));
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
        System.out.println("deletenodes:"+trans(deletenodes));

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
            System.out.println("return closer cc");
            pnbmap.clear();
            pnbmap.putAll(map);
            return community;
        } else {
            map = copyMap(pnbmap);
            System.out.println("try to delete one node");
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


    public Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap) {
        Map<Integer, Set<Integer>> newMap = new WeakHashMap<>();
        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(), newset);
        }
        return newMap;
    }

    public Map<Integer, Set<Integer>> findKcore(Map<Integer, Set<Integer>> pnbmap, int K, int queryId) {
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
            pnbmap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
        }

        //step 3: find the connected component containing q
        //找连通图
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

        for (Map.Entry<Integer, Set<Integer>> entry : pnbmap.entrySet()) {
            int curid = entry.getKey();
            if (!community.contains(curid)) {
                pnbmap.put(curid, new HashSet<>());
            }
        }

        return pnbmap;
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
            pnbmap.put(curId, new HashSet<Integer>());//clean all the pnbs of curId
        }

        //step 3: find the connected component containing q
        //找连通图
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

    public Set<Integer> findCC(Map<Integer, Set<Integer>> pnbmap, int K, int queryId) {
        if (pnbmap == null) return null;
//        if(pnbmap.get(queryId)==null) return null;
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

//    public static void main(String[] args) {
//
//        Map<Integer,double[]> map = new HashMap<>();
//        double[] d1 = {10,2};
//        double[] d2 = {0,1,0,1,1,0,1,1,0,1,0.25,0.6};
//        double[] d3 = {1,1,1,1,0,1,0,1,1,1,0.25,0.8};
//        map.put(-1,d1);
//        map.put(0,d2);
//        map.put(1,d3);
//        map.put(2,d2);
//
//        int[] main = {1,1};
//        int[] text = {1,1,1,1,1,1,1,1,1,1};
//        int[] cont = {1,1};
//        Gweight gweight = new Gweight(main,text,cont,2);
//        Adistance adistance = new Adistance(map,gweight);
//
//        Map<Integer,Set<Integer>> nbmap = new HashMap<>();
//        Set<Integer> set0 = new HashSet<>();
//        Set<Integer> set1 = new HashSet<>();
//        Set<Integer> set2 = new HashSet<>();
//        set0.add(1);set0.add(2);
//        set1.add(0);set1.add(2);
//        set2.add(0);set2.add(1);
//        nbmap.put(0,set0);
//        nbmap.put(1,set1);
//        nbmap.put(2,set2);
//        CoreDecomposition c = new CoreDecomposition();
//        Set<Integer> cc =  c.findCompactC(nbmap,adistance,1,1,0);
//        System.out.println(c.transset(cc));
//    }
}
