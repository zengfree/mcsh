package MCSH.index.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.hnsw.hnswindex;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;

import java.util.*;

public class hnswTest {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        Map<Integer,float[]> attribute = dataReader.readattributed_float();
        Map<Integer,Integer> correspond = dataReader.getCorrespond();
        String datafile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/indextest.txt";
//        String idnexfile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/indexM_10ef_600.n2";
//        String idnexfile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/indexM_50ef_600.n2";
//        String idnexfile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/indexM_100ef_800.n2";
        String idnexfile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/indexM_50ef_600_new.n2";
        hnswindex index = new hnswindex();

        int[] main = {1,1};
        int[] text = new int[220];
        int[] cont = {1,1};
        for(int i=0;i<220;i++){
            text[i] = 1;
        }


        Gweight_float gweight = new Gweight_float(main,text,cont,2);
        Adistance_float adistance_float = new Adistance_float(attribute,gweight);

//        System.out.println(adistance_float.cal_distance(6,33));

//        index.build(datafile,idnexfile, gweight.getbuildweight(), 220,2,50,600);
        float sumpre = 0,sumrec=0;
        int flag =0;
        while(flag<100){
            Object[] list = attribute.keySet().toArray();
            int queryid = (int)list[(int)(Math.random()*list.length)];
            System.out.println("queryid"+queryid);
            int K = 100;
            int ef = 300;
            long t1 = System.nanoTime();
            int[] m =  index.search(attribute.get(queryid),K,ef,idnexfile,gweight.getbuildweight(),220,2);
            long t2 = System.nanoTime();
//            System.out.println("time="+ (t2-t1) );

            Set<Integer> keepSet1 = new HashSet<>();
            for (int i = 0; i < m.length; i++) {
                int id = correspond.get(m[i]);
                keepSet1.add(id);
//                System.out.println(adistance_float.cal_distance(id,6));
            }
            System.out.println(keepSet1.contains(queryid));
            System.out.println("keepset1:"+keepSet1.size());
//
            long t3 = System.nanoTime();
            Map<Integer,Float> distancemap = new HashMap<>();
            for(int nodeid:attribute.keySet()){
                if(nodeid!=-1) {
//                System.out.println(nodeid);
                    distancemap.put(nodeid, adistance_float.cal_distance(queryid, nodeid));
                }
            }

            List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(distancemap.entrySet());
            entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
                @Override
                public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                    return o1.getValue().compareTo(o2.getValue());//升序
//                return o2.getValue().compareTo(o1.getValue());//降序排列
                }
            });

            Set<Integer> keepSet2 = new HashSet<>();
            for (int i = 0; i < K; i++) {
                int id = entryList.get(i).getKey();
                keepSet2.add(id);
//                System.out.println(entryList.get(i).getValue());
//            System.out.println(entryList.get(i).getValue());
                if(i== entryList.size()-1){
                    break;
                }
//            System.out.println(id+","+entryList.get(i).getValue());
            }
            long t4 = System.nanoTime();
//            System.out.println("time="+ (t4-t3) );

            System.out.println(keepSet2.size());
            Set<Integer> keepset3 = new HashSet<>(keepSet1);
            keepset3.retainAll(keepSet2);
            System.out.println("size:"+keepset3.size());
            float pre = (float) keepset3.size()/keepSet1.size();
            float rec = (float) keepset3.size()/K;
            sumpre +=pre;
            sumrec +=rec;
            System.out.println(rec);
            System.out.println(pre);

            flag++;
        }
        System.out.println(sumpre/100);
        System.out.println(sumrec/100);



//        System.out.println(dataReader.trans(keepset3));

    }
}
