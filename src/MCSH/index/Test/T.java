package MCSH.index.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixBasedSearch;
import MCSH.online.basic.baseline1;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class T {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();
        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];

//        String queryfile = Config.QueryFile+"dblequerynodes.txt";
        String queryfile = "/home/hadoop/dblequerynodes.txt";
        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        MetaPath queryMPath = new MetaPath(vertex, edge);
        int queryK = 30;
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
        //        System.out.println(gweight.toString());
        Adistance_float adistance = new Adistance_float(attribute, gweight);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            System.out.println("querynode size:"+querynodes.size());

//                BigDecimal b = new BigDecimal(i * (float) 0.05);
//                float v2 = b.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
//                System.out.println(v2);
            float avgdist1 =0,avgdist2 = 0;
            float sumpre =0;
            for (int queryid:querynodes){
                System.out.println("base");
                long t1 = System.nanoTime();
                baseline1 bs1 = new baseline1(graph,vertexType,edgeType,attribute,adistance);
                Set<Integer> set = bs1.query_test(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                System.out.println("base time:"+(t2-t1)/1000000);
                System.out.println("set1:"+set.size());
                float dist1 = adistance.cal_subgraph_attr_dist(set);
                avgdist1 += dist1;
                System.out.println("dist1:"+dist1);
//                    System.out.println(dataReader.trans(result));
                System.out.println();

                long t3= System.nanoTime();
                String datafile = "/home/hadoop/newindex/dblpdata.txt";
                String idnexfile = "/home/hadoop/newindex/dblpindex.n2";
                MixBasedSearch mbs1 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                Set<Integer> set1 = mbs1.query(queryid,queryK,queryMPath,1600);
                long t4 = System.nanoTime();
                System.out.println("index time:"+(t4-t3)/1000000);
                System.out.println("set2:"+set1.size());
                float dist2 = adistance.cal_subgraph_attr_dist(set1);
                avgdist2 += dist2;
                System.out.println("dist2:"+dist2);

                Set<Integer> set2 = new HashSet<>(set);
                set2.retainAll(set1);
                sumpre+= (float)set2.size()/set.size();
            }

            System.out.println(avgdist1/querynodes.size());
            System.out.println(avgdist2/querynodes.size());
            System.out.println(sumpre/querynodes.size());
        }catch (IOException ioException) {
            System.out.println(ioException);
        }

    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
