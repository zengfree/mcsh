package MCSH.online.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.online.exact.E2;
import MCSH.online.exact.ExactAlgorithm_float;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;

public class E {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

//        String queryfile = "C:\\Users\\DELL\\Desktop\\实验\\query\\sq1.txt";
//        String queryfile = "C:\\zxj\\CSHDS\\query\\q1.txt";
        String queryfile = "dblp.txt";
//        int[] vertex = {0,1,0},edge = {0,0};
//        String queryfile = "C:\\Users\\DELL\\Desktop\\实验\\query\\dblp.txt";

        int[] vertex = {1,0,1},edge = {3,0};

        MetaPath queryMPath = new MetaPath(vertex, edge);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));

            int queryK = 30;
            int queryM = 10;
            //模拟参数输入 均值
            int textnum = (int)attribute.get(-1)[0];
            int contnum = (int)attribute.get(-1)[1];
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

            String line ;
            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }

            int num = 0;
            float avgpre = (float) 0;
            float avgrec = (float) 0;
            float avgdistE = (float) 0;
            float avgdist2 = (float) 0;
            long avgt1 = 0,avgt2=0;
            for (int queryid:querynodes){
                long t1 = System.nanoTime();
                E2 e = new E2(graph,vertexType,edgeType,attribute,adistance,40);
                Set<Integer> re = e.query(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                if(re!=null){
                    num++;
                    float dist = adistance.cal_subgraph_attr_dist(re);
                    avgdistE+=dist;
                    avgt1+=(t2-t1)/1e9;
                    System.out.println("dist:"+dist);
                    System.out.println("query time:" +(t2-t1)/1e6+"ms");
                    String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(re)+"\r\n" + adistance.cal_subgraph_attr_dist(re)+"\r\n" +"time=" + (t2-t1) +"ns" +"\r\n";
                    File logfile = new File("ex_30.txt");
                    FileWriter fw = new FileWriter(logfile, true);
                    fw.write(str);
                    fw.close();
                }else continue;


                adistance.setYu((float) 0.35);
                long t5 = System.nanoTime();
                Base_logmap2 basem = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
                Set<Integer> res1 = basem.queryM_protect(queryid,queryK,queryMPath,queryM);
                long t6 = System.nanoTime();
                if(res1!=null){
                    float dist = adistance.cal_subgraph_attr_dist(res1);
                    avgdistE+=dist;
                    avgt1+=(t6-t5)/1e9;
                    System.out.println("distm:"+dist);
                    System.out.println("query time:" +(t6-t5)/1e6+"s");
                    System.out.println("queryid:"+queryid+",write");
                    System.out.println("time"+(t6-t5)+"ns");
                    String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(res1)+"\r\n" + adistance.cal_subgraph_attr_dist(res1)+"\r\n" +"time=" + (t6-t5) +"ns" +"\r\n";
                    File logfile = new File("mp_30.txt");
                    FileWriter fw = new FileWriter(logfile, true);
                    fw.write(str);
                    fw.close();
                }

                Set<Integer> set = new HashSet<>(re);
                assert res1 != null;
                set.retainAll(res1);
                avgpre += (float) set.size()/re.size();
                avgrec += (float) set.size()/res1.size();

            }

//            System.out.println("avgdistE:"+avgdistE/num);
            System.out.println("avgdist2:"+avgdist2/num);
            System.out.println("avgdistE:"+avgdistE/num);
            System.out.println("avgpre:"+avgpre/num);
            System.out.println("avgrec:"+avgrec/num);
//            System.out.println("t1:"+avgt1/num);
            System.out.println("t2:"+avgt2/num);
            System.out.println("t1:"+avgt1/num);
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
