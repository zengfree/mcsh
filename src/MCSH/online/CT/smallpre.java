package MCSH.online.CT;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.online.exact.ExactAlgorithm_float;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

//短路径
public class smallpre {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

//        String queryfile = Config.QueryFile+args[5];
//        String queryfile = Config.QueryFile+"q1.txt";
        String queryfile ="C:\\Users\\DELL\\Desktop\\实验\\query\\sq1.txt";
        int queryK = 5;
        int queryM = 4;
//        int threadnum = Integer.parseInt(args[2]);
//        String[] Mpath = args[3].split(";");
//
//        int[] vertex = StringToInt(Mpath[0].split(","));
//        int[] edge = StringToInt((Mpath[1].split(",")));
        int[] vertex = {0,1,0},edge={0,0};
        MetaPath queryMPath = new MetaPath(vertex, edge);
        System.out.println(queryMPath);

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
            System.out.println("querynodes size:"+querynodes.size());
            stdin.close();

            Map<Integer,Set<Integer>> exact = new HashMap<>();
            for(int queryid:querynodes){
                System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
                ExactAlgorithm_float base = new ExactAlgorithm_float(graph,vertexType,edgeType,attribute,adistance,8);
                Set<Integer> res1 = base.query(queryid,queryK,queryMPath);
                if(res1!=null){
                    exact.put(queryid,res1);
                }
            }

                adistance.setYu((float) 0.2);
                float avgdist = (float) 0;
                long avgt = 0;
                double pre=0,rec=0;
                int num = 0;
                for(int queryid:querynodes){
//                    System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
                    long t3 = System.nanoTime();
                    Base_logmap2 base = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
                    Set<Integer> res1 = base.query(queryid,queryK,queryMPath);
                    long t4 = System.nanoTime();
                    if(res1!=null){
                        num++;
                        float dist = adistance.cal_subgraph_attr_dist(res1);
//                        System.out.println(dist);
                        avgdist+=dist;
                        avgt+=(t4-t3)/1e6;
//                        System.out.println("dist:"+dist);
//                        System.out.println("query time:" +(t4-t3)/1e6+"ms");
                        Set<Integer> set2 = new HashSet<>(exact.get(queryid));
                        set2.retainAll(res1);
                        pre += (double) set2.size()/res1.size();
                        rec += (double) set2.size()/exact.get(queryid).size();
                    }
                System.out.println("avgdist:"+avgdist/num);
                System.out.println("avgt:"+avgt/num);
                System.out.println("pre:"+pre/num);
                System.out.println("rec:"+rec/num);
            }

        }catch (IOException e) {
            System.out.println(e);
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
