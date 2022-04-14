package MCSH.online.Test2;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.baseline1;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//短路径
public class Base {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
       DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();
        int textnum =(int)attribute.get(-1)[0];
        int contnum =(int)attribute.get(-1)[1];

        String queryfile = Config.QueryFile+args[0];
        int queryK = Integer.parseInt(args[1]);
//        int queryM = Integer.parseInt(args[2]);
        int threadnum = Integer.parseInt(args[2]);

//        int[] vertex = {1, 0, 1}; //APA
//        int[] edge = {3, 0};
        String[] Mpath = args[3].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);

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
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;
            //获取查询条件
            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }


            File logfile = new File(Config.ResultFile + "base_imdb" + queryK + "_"   + "_"+ queryMPath.toString() + ".txt");
//            File logfile2 = new File(Config.ResultFile + "K/basem" + queryK + "_" + queryM + "_" +queryMPath.toString() + ".txt");
//            File logfile3 = new File(Config.ResultFile + "K/basemp" + queryK + "_" + queryM + "_" +queryMPath.toString() + ".txt");
            ExecutorService exec = Executors.newFixedThreadPool(threadnum);
            FileWriter fw = new FileWriter(logfile, true);
//            FileWriter fw2 = new FileWriter(logfile2, true);
//            FileWriter fw3 = new FileWriter(logfile3, true);
            for (int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                exec.submit(()->{
                    try{
                        long t3 = System.nanoTime();
                        baseline1 baseline1 = new baseline1(graph,vertexType,edgeType,attribute,adistance);
                        Set<Integer> result2 = baseline1.query(queryid,queryK,queryMPath);
                        long t4 = System.nanoTime();
                        //                System.out.println(result);
                        if(result2!=null){
                            System.out.println("queryid:"+queryid+",write");
                            System.out.println("time"+(t4-t3)+"ns");
                            String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(result2)+"\r\n" + adistance.cal_subgraph_attr_dist(result2)+"\r\n" +"time=" + (t4-t3) +"ns" +"\r\n";
                            fw.write(str);
                        }
                    }catch (NullPointerException | IOException e){
                        e.printStackTrace();
                        System.out.println(queryid+",base error!");
                    }
                });
            }
            exec.shutdown();
            while(true){
                if(exec.isTerminated()){
                    System.out.println("所有的子线程都已经结束了");
                    fw.close();
                    break;
                }
                Thread.sleep(10000);
            }
        }catch (IOException | InterruptedException ioException) {
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
