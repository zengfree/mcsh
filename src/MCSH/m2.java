package MCSH;

import MCSH.online.basic.Base_logmap2;
import MCSH.online.exact.Kcore_find_float;
import MCSH.util.Adistance_float;
import MCSH.util.FCS_log_new;
import MCSH.util.Gweight_float;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class m2 {
    public static void main(String[] args) {
        Map<Integer,float[]> attribute = new HashMap<>();
        attribute.put(-1,new float[]{10,2});
        attribute.put(1,new float[]{1,0,1,0,1,1,0,0,0,1,(float) 0.55,(float)0.55});
        attribute.put(2,new float[]{1,1,0,1,0,0,1,1,1,1,(float) 0.33,(float)0.55});
        attribute.put(3,new float[]{1,1,0,1,1,1,0,0,1,0,(float) 1.0,(float)1.0});
        attribute.put(4,new float[]{0,1,1,1,0,0,1,1,0,1,(float) 1.0,(float)0.1});
        attribute.put(5,new float[]{1,0,1,1,0,0,1,0,0,1,(float) 0.33,(float)0.55});
        attribute.put(6,new float[]{1,1,0,0,0,1,0,1,0,0,(float) 0.33,(float)0.5});
        attribute.put(7,new float[]{0,0,1,1,0,0,0,0,0,1,(float) 0.0,(float)1.0});
        attribute.put(8,new float[]{0,0,0,0,0,1,0,1,0,0,(float) 1.0,(float)0.0});
//        attribute.put(7,new float[]{0,1,0,1,0,0,1,1,0,1,(float) 0.6421,(float)0.4});
//        attribute.put(8,new float[]{0,1,0,1,0,1,0,0,0,1,(float) 0.505,(float)0.466});
//        attribute.put(9,new float[]{0,1,0,1,0,0,1,0,0,1,(float) 0.031,(float)0.666});

        int textnum = 10;
        int contnum = 2;
        int[] main = {1,1};
        int[] text = new int[textnum];
        int[] cont = new int[contnum];
        for(int i = 0;i<textnum;i++){
            text[i] = 1;
        }
        for(int i = 0;i<contnum;i++){
            cont[i] = 1;
        }
        Gweight_float gweight = new Gweight_float(main, text, cont, 2);
        System.out.println(gweight);
        Adistance_float adistance = new Adistance_float(attribute, gweight);

        Map<Integer,Set<Integer>> pnbMap = new HashMap<>();
        pnbMap.put(1,new HashSet<>(Arrays.asList(2,3,6,5)));
        pnbMap.put(2,new HashSet<>(Arrays.asList(3,6,1)));
        pnbMap.put(3,new HashSet<>(Arrays.asList(1,2,4,5,6,7,8)));
        pnbMap.put(4,new HashSet<>(Arrays.asList(3,5,6)));
        pnbMap.put(5,new HashSet<>(Arrays.asList(1,3,4,6,7)));
        pnbMap.put(6,new HashSet<>(Arrays.asList(1,2,3,4,5,7,8)));
        pnbMap.put(7,new HashSet<>(Arrays.asList(3,6,8,5)));
        pnbMap.put(8,new HashSet<>(Arrays.asList(3,6,7)));
//        pnbMap.put(9,new HashSet<>(Arrays.asList(4,7,8)));


        Map<Integer, Map<Integer,Float>> distancemap = new HashMap<>();
        for(int i: pnbMap.keySet()){
            distancemap.put(i,new HashMap<>());
        }

        //step 2: cal the distance
        Set<Integer> label = new HashSet<>();
        for(int i: pnbMap.keySet()){
            label.add(i);
            for (int j: pnbMap.keySet()){
                if(!label.contains(j))
                {
                    float dist = adistance.cal_distance(i, j);
                    distancemap.get(i).put(j,dist);
                    distancemap.get(j).put(i,dist);
                }
            }
        }

        Set<Integer> cc = new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8));
        System.out.println(adistance.cal_subgraph_attr_dist(cc));
        int queryid = 1;
//        for (int i:cc) {
//            System.out.print(i+", ");
//            System.out.println(adistance.cal_distance(queryid,i));
//        }
        ForkJoinPool pool = new ForkJoinPool(32);
        ForkJoinTask<Set<Integer>> task = pool.submit(new Kcore_find_float(pnbMap,3, queryid,-1,adistance,distancemap));
        Set<Integer> set1 = task.join();
        System.out.println(adistance.cal_subgraph_attr_dist(set1));
//
        Map<Integer,Set<Integer>> m =copyMap(pnbMap);
        FCS_log_new fcs = new FCS_log_new();
        Set<Integer> s =  fcs.findCompactC(m,cc, adistance,queryid,3);
        System.out.println();

        Map<Integer,Set<Integer>> map =copyMap(pnbMap);
        Set<Integer> set2 =  fcs.findCompactC_protect(map,cc,2, adistance,queryid,3);
        System.out.println();

        Map<Integer,Set<Integer>> map1 =copyMap(pnbMap);
        Set<Integer> set3 =  fcs.findCompactC(map1,cc,2, adistance,queryid,3);
        System.out.println(adistance.cal_subgraph_attr_dist(set2));
        for (int i: set1) {
            System.out.print(i+",");
        }
        System.out.println();
        for (int i: s) {
            System.out.print(i+",");
        }
        System.out.println();
        for (int i: set2) {
            System.out.print(i+",");
        }
        System.out.println();
        for (int i: set3) {
            System.out.print(i+",");
        }
    }

    public static Map<Integer, Set<Integer>> copyMap(Map<Integer, Set<Integer>> pnbmap){
        Map<Integer, Set<Integer>> newMap = new HashMap<>();
        for (Map.Entry<Integer,Set<Integer>> entry:pnbmap.entrySet()){
            Set<Integer> newset = new HashSet<>(entry.getValue());
            newMap.put(entry.getKey(),newset);
        }

        return newMap;
    }
}
