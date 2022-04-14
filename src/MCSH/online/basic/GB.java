package MCSH.online.basic;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.Adistance_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

//短路径
public class GB {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader ;
        switch (args[0]){
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

        String queryfile = Config.QueryFile+ args[1];
//        String queryfile = "/home/hadoop/q2.txt";
        String[] Mpath = args[2].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));

        MetaPath queryMPath = new MetaPath(vertex, edge);
        System.out.println(queryMPath);
        Adistance_float adistance = new Adistance_float();
        int threadnum = Integer.parseInt(args[3]);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            //模拟参数输入 均值
            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            System.out.println("querynodes size:"+querynodes.size());
            stdin.close();
            long ts1=0,ts2=0;
            for(int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);

                long t1 = System.nanoTime();
                Build b = new Build(graph,vertexType,edgeType,attribute,adistance);
                b.build1(queryMPath,queryid);
                long t2 = System.nanoTime();
                System.out.println("time1:"+(t2-t1)/1000000+"ms");
                ts1 += (t2-t1)/1000000;

                long t3 = System.nanoTime();
                Build b2 = new Build(graph,vertexType,edgeType,attribute,adistance);
                b2.buildm(queryMPath,queryid,threadnum);
                long t4 = System.nanoTime();
                System.out.println("time3:"+(t4-t3)/1000000+"ms");
                ts2 += (t4-t3)/1000000;
            }
            System.out.println("t1:"+(ts1/querynodes.size()));
            System.out.println("t2:"+(ts2/querynodes.size()));
//            fw.close();

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
