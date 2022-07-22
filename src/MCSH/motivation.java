package MCSH;

import MCSH.online.exact.Kcore_find_float;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;

import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class motivation {
    public static void main(String[] args) {
        Map<Integer,float[]> attribute = new HashMap<>();
        attribute.put(-1,new float[]{10,2});
        attribute.put(1,new float[]{1, 0, 1, 0, 1, 1, 0, 0, 0, 1,(float) 1.0,(float)1.0});
        attribute.put(2,new float[]{1,1,0,1,0,0,1,1,1,1,(float) 0.0,(float)0.0});
        attribute.put(3,new float[]{1,1,0,1,1,1,0,0,1,1,(float) 0.684,(float)0.334});
        attribute.put(4,new float[]{0,1,1,1,0,0,1,1,0,1,(float) 0.7894,(float)0.533});
        attribute.put(5,new float[]{1,0,1,1,0,0,1,0,0,1,(float) 0.1789,(float)0.13333});
        attribute.put(6,new float[]{1,1,0,1,0,1,0,1,0,1,(float) 0.8315,(float)0.733});
        attribute.put(7,new float[]{0,1,0,1,0,0,1,1,0,1,(float) 0.6421,(float)0.4});
        attribute.put(8,new float[]{0,1,0,1,0,1,0,0,0,1,(float) 0.505,(float)0.466});
        attribute.put(9,new float[]{0,1,0,1,0,0,1,0,0,1,(float) 0.031,(float)0.666});

        int textnum = 10;
        int contnum = 2;
        int[] main = {1,1};
        int[] text = new int[textnum];
        int[] cont = new int[contnum];
        int[] text2 = new int[textnum];
        for(int i = 0;i<textnum;i++){
            text[i] = 1;
            if(attribute.get(7)[i]==1){
                text[i] = 1;
                text2[i] = 1;
            }else {
                text[i] = 1;
                text2[i] = 0;
            }
        }
        for(int i = 0;i<contnum;i++){
            cont[i] = 0;
        }
        Gweight_float gweight = new Gweight_float(main, text, cont, 2);
        System.out.println(gweight);
        System.out.println(Arrays.toString(text2));
        Adistance_float adistance = new Adistance_float(attribute, gweight);
        System.out.println(adistance.cal_distance(1,5));
        Set<Integer> s = new HashSet<>(Arrays.asList(1,2,3,4,5,6,7,8,9));
        System.out.println("ATC:"+adistance.cal_subgraph_attr_dist(s));
        System.out.println("ATC:"+adistance.cal_maxdist(s));
        System.out.println("ATC:"+adistance.cal_fugailv(s,text2));
        System.out.println("ATC:"+adistance.maxnum(s,text2));

        Set<Integer> set1 = new HashSet<>(Arrays.asList(3,4,6,7,8,9));
        System.out.println("ACQ:"+adistance.cal_subgraph_attr_dist(set1));
        System.out.println("ACQ:"+adistance.cal_maxdist(set1));
        System.out.println("ACQ:"+adistance.cal_fugailv(set1,text2));
        System.out.println("ACQ:"+adistance.maxnum(set1,text2));

        Set<Integer> set2 = new HashSet<>(Arrays.asList(4,6,7,8,9));
        System.out.println("VAC:"+adistance.cal_subgraph_attr_dist(set2));
        System.out.println("VAC:"+adistance.cal_maxdist(set2));
        System.out.println("VAC:"+adistance.cal_fugailv(set2,text2));
        System.out.println("VAC:"+adistance.maxnum(set2,text2));

        Set<Integer> set3 = new HashSet<>(Arrays.asList(4,7,8,9));
        System.out.println("Ours1:"+adistance.cal_subgraph_attr_dist(set3));
        System.out.println("Ours1:"+adistance.cal_maxdist(set3));
        System.out.println("Ours1:"+adistance.cal_fugailv(set3,text2));
        System.out.println("Ours1:"+adistance.maxnum(set3,text2));

        Set<Integer> set4 = new HashSet<>(Arrays.asList(4,7,8,6));
        System.out.println("Ours2:"+adistance.cal_subgraph_attr_dist(set4));
        System.out.println("Ours2:"+adistance.cal_maxdist(set4));
        System.out.println("Ours2:"+adistance.cal_fugailv(set4,text2));
        System.out.println("Ours2:"+adistance.maxnum(set4,text2));
//
//        Map<Integer,Set<Integer>> pnbMap = new HashMap<>();
//        pnbMap.put(1,new HashSet<>(Arrays.asList(2,3,7,6,5)));
//        pnbMap.put(2,new HashSet<>(Arrays.asList(3,6,1)));
//        pnbMap.put(3,new HashSet<>(Arrays.asList(1,2,4,6,7,8)));
//        pnbMap.put(4,new HashSet<>(Arrays.asList(3,5,6,7,8,9)));
//        pnbMap.put(5,new HashSet<>(Arrays.asList(1,4,6)));
//        pnbMap.put(6,new HashSet<>(Arrays.asList(1,2,3,4,5,7,8)));
//        pnbMap.put(7,new HashSet<>(Arrays.asList(8,3,1,6,4,9)));
//        pnbMap.put(8,new HashSet<>(Arrays.asList(3,6,4,7,9)));
//        pnbMap.put(9,new HashSet<>(Arrays.asList(4,7,8)));


//        Map<Integer, Map<Integer,Float>> distancemap = new HashMap<>();
//        for(int i: pnbMap.keySet()){
//            distancemap.put(i,new HashMap<>());
//        }
//
//        //step 2: cal the distance
//        Set<Integer> label = new HashSet<>();
//        for(int i: pnbMap.keySet()){
//            label.add(i);
//            for (int j: pnbMap.keySet()){
//                if(!label.contains(j))
//                {
//                    float dist = adistance.cal_distance(i, j);
//                    distancemap.get(i).put(j,dist);
//                    distancemap.get(j).put(i,dist);
//                }
//            }
//        }
//
//
//        ForkJoinPool pool = new ForkJoinPool(32);
//        ForkJoinTask<Set<Integer>> task = pool.submit(new Kcore_find_float(pnbMap,3, 7,-1,adistance,distancemap));
//        Set<Integer> set = task.join();
//        for (int i: set) {
//            System.out.print(i+",");
//        }
    }
}
