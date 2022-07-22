package MCSH.online.T5;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.online.exact.ExactAlgorithm;
import MCSH.online.exact.ExactAlgorithm_advance2;
import MCSH.online.exact.ExactAlgorithm_float;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;


public class MV {
    public static void main(String[] args) {
        DataReader dataReader ;
        switch (args[0]){
            case "s":
                System.out.println("small");
                dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
                break;
            case "dblp":
                System.out.println("dblp");
                dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
                break;
            case "imdb":
                System.out.println("imdb");
                dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBmovieattributed);
                break;
            case "fsq":
                System.out.println("fsq");
                dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
                break;
            default:
                dataReader = new  DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        }
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();
//        Map<Integer,double[]> attribute = dataReader.readattributed();

        String queryfile = Config.QueryFile+args[1];
//        String queryfile ="/home/hadoop/dblequerynodes.txt";
        int queryK = Integer.parseInt(args[2]);
//        int queryM = Integer.parseInt(args[3]);
        String[] Mpath = args[4].split(";");

        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        System.out.println(queryMPath);

        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];

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
            float randomcov = 0;
            int[] t = new int[textnum];
            for(int queryid:querynodes){
                System.out.println("random:queryid:"+queryid);
                int[] main = {1,1};
                int[] text = new int[textnum];
                int[] cont = new int[contnum];
                for(int i = 0;i<textnum;i++){
//                    if(attribute.get(queryid)[i]!=0&&Math.random()>0.5){
                    if(attribute.get(queryid)[i]!=0){
                        text[i] = 1;
                        t[i]=1;
                    }else {
                        text[i] = 0;
                        t[i]=0;
                    }
                }
                for(int i = 0;i<contnum;i++){
                    cont[i] = 1;
                }
//                System.out.println(Arrays.toString(text));

                Gweight_float gweight = new Gweight_float(main, text, cont, 2);
                Adistance_float adistance = new Adistance_float(attribute, gweight);
                System.out.println(gweight.toString());

                long t1 = System.nanoTime();
                ExactAlgorithm_float exactAlgorithm = new ExactAlgorithm_float(graph,vertexType,edgeType,attribute,adistance,32);
                Set<Integer> result = exactAlgorithm.query(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                if(result!=null){
                    float cover = adistance.cal_coverage(result,text);
                    randomcov +=cover;
                    System.out.println(cover);
                    System.out.println(result.size());
                    System.out.println(adistance.cal_subgraph_attr_dist(result));
                    System.out.println((t2-t1)/1e9+"s");
                }
                else {
                    System.out.println("queryid:"+queryid+" has no kcore");
                }
            }

            float avgcov =0;
            for(int queryid:querynodes){
                System.out.println("average:queryid:"+queryid);
                int[] main = {1,1};
                int[] text = new int[textnum];
                int[] cont = new int[contnum];
                for(int i = 0;i<textnum;i++){
                        text[i] = 1;
                }
                for(int i = 0;i<contnum;i++){
                    cont[i] = 0;
                }

                Gweight_float gweight = new Gweight_float(main, text, cont, 2);
                Adistance_float adistance = new Adistance_float(attribute, gweight);

                long t1 = System.nanoTime();
                ExactAlgorithm_float exactAlgorithm = new ExactAlgorithm_float(graph,vertexType,edgeType,attribute,adistance,32);
                Set<Integer> result = exactAlgorithm.query(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                if(result!=null){
                    float cover = adistance.cal_coverage(result,t);
                    avgcov +=cover;
                    System.out.println(cover);
                    System.out.println(result.size());
                    System.out.println(adistance.cal_subgraph_attr_dist(result));
                    System.out.println((t2-t1)/1e9+"s");
                }
                else {
                    System.out.println("queryid:"+queryid+" has no kcore");
                }
            }
            System.out.println();
            System.out.println(randomcov/querynodes.size());
            System.out.println(avgcov/querynodes.size());
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
