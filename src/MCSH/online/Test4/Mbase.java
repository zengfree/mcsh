package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
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
public class Mbase {
    public static void main(String[] args) {
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
        int textnum =(int)attribute.get(-1)[0];
        int contnum =(int)attribute.get(-1)[1];

        String queryfile = Config.QueryFile+args[1];
        int queryK = Integer.parseInt(args[2]);
        int threadnum = Integer.parseInt(args[3]);

        String[] Mpath = args[4].split(";");
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
            stdin.close();

//            for(int i=2;i<=20;i = i+2) {
                File logfile = new File(Config.ResultFile + args[0] + "/mtest/base" + "_" +queryK  + "_"+ queryMPath.toString() + ".txt");
                ExecutorService exec = Executors.newFixedThreadPool(threadnum);
                FileWriter fw = new FileWriter(logfile, true);
                for (int queryid:querynodes){
                    System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                    exec.submit(()->{
                        try{
                            long t3 = System.nanoTime();
                            Base_logmap2 bl2 = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
                            Set<Integer> result2 = bl2.query(queryid,queryK,queryMPath);
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
//            }


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
