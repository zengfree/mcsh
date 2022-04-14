package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixBasedSearch;
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

public class MIBSmp {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
        DataReader dataReader ;
        switch (args[4]){
            case "dblp":
                System.out.println("dblp");
                dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
                break;
            case "imdb":
                System.out.println("imdb");
                dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
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

        String queryfile = Config.QueryFile+args[5];
//        String queryfile = "/home/hadoop/q1.txt";

//        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        int queryK = Integer.parseInt(args[0]);
        int queryM = Integer.parseInt(args[1]);
        int threadnum = Integer.parseInt(args[2]);
        String[] Mpath = args[3].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        int v1 = Integer.parseInt(args[6]);
        int interval = Integer.parseInt(args[7]);

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
            stdin.close();

//            String datafile = "/home/star/zxj/code2/dblpdata.txt";
//            String idnexfile = "/home/star/zxj/code2/dblpindex.n2";

            String datafile = Config.IndexRoot+args[4]+"/"+queryMPath.toString()+"data.txt";
            String idnexfile = Config.IndexRoot+args[4]+"/"+queryMPath.toString()+"index.n2";

            for (int i = 0 ; i <= 7; i++) {
                int v2 = i * interval + v1;
                File logfile3 = new File(Config.ResultFile + args[4] +"/IB/" + v2 + "_" + queryMPath.toString() + ".txt");
                FileWriter fw3 = new FileWriter(logfile3, false);
                ExecutorService exec = Executors.newFixedThreadPool(threadnum);
                for (int queryid:querynodes){
                    System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid+",queryK:"+queryK+",queryN:"+v2);
                    exec.submit(()->{
                        try{
                            long t5 = System.nanoTime();
                            MixBasedSearch mix3 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                            Set<Integer> result3 = mix3.queryMP(queryid,queryK,queryMPath,v2,queryM);
                            long t6 = System.nanoTime();
                            if(result3!=null){
                                System.out.println("indexmp:"+queryid);
                                String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(result3)+"\r\n" + adistance.cal_subgraph_attr_dist(result3)+"\r\n" +"time=" + (t6-t5) +"ns" +"\r\n";
                                fw3.write(str);
                            }
                            else {
                                System.out.println("queryid:"+queryid+" has no kcore");
                            }
                        }catch (IOException | NullPointerException exception){
                            exception.printStackTrace();
                            System.out.println("error:");
                        }
                    });

                }
                exec.shutdown();
                while(true){
                    if(exec.isTerminated()){
                        System.out.println("所有的子线程都已经结束了");
                        fw3.close();
                        break;
                    }
                    Thread.sleep(10000);
                }
            }
        }catch (IOException | InterruptedException ioException ) {
            System.out.println(ioException);
            System.out.println("sleep error");
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
