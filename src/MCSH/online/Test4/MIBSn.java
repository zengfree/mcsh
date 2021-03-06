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

public class MIBSn {
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
        float langda = Float.parseFloat(args[8]);

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
            //??????????????????
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            stdin.close();


            String datafile = args[4]+"/"+langda+queryMPath+"data.txt";
            String idnexfile = args[4]+"/"+langda+queryMPath+"index.n2";
            System.out.println("langda:"+langda);
            for (int i = 0 ; i <= 7; i++) {
                int v2 = i * interval + v1;
                File logfile = new File(args[4] +"/Index/" + v2 + "_" + queryMPath.toString() + ".txt");
                File logfile2 = new File(args[4] +"/Indexm/" + v2 + "_" + queryMPath.toString() + ".txt");
                File logfile3 = new File(args[4] +"/Indexmp/" + v2 + "_" + queryMPath.toString() + ".txt");
                FileWriter fw = new FileWriter(logfile, false);
                FileWriter fw2 = new FileWriter(logfile2, false);
                FileWriter fw3 = new FileWriter(logfile3, false);
                ExecutorService exec = Executors.newFixedThreadPool(threadnum);
                for (int queryid:querynodes){
                    System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid+",queryK:"+queryK+",queryN:"+v2);
                    exec.submit(()->{
                        try{
                            long t1 = System.nanoTime();
                            MixBasedSearch mix = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                            Set<Integer> result = mix.query(queryid,queryK,queryMPath,v2,langda);
                            long t2 = System.nanoTime();
                            if(result!=null){
                                System.out.println("index:"+queryid);
                                String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(result)+"\r\n" + adistance.cal_subgraph_attr_dist(result)+"\r\n" +"time=" + (t2-t1) +"ns" +"\r\n";
                                fw.write(str);
                            }
                            else {
                                System.out.println("queryid:"+queryid+" has no kcore");
                            }

                            long t3 = System.nanoTime();
                            MixBasedSearch mix2 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                            Set<Integer> result2 = mix2.queryM(queryid,queryK,queryMPath,v2,queryM,langda);
                            long t4 = System.nanoTime();
                            //                System.out.println(result);
                            if(result!=null){
                                System.out.println("indexm:"+queryid);
                                String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(result2)+"\r\n" + adistance.cal_subgraph_attr_dist(result2)+"\r\n" +"time=" + (t4-t3) +"ns" +"\r\n";
                                fw2.write(str);
                            }
                            else {
                                System.out.println("queryid:"+queryid+" has no kcore");
                            }

                            long t5 = System.nanoTime();
                            MixBasedSearch mix3 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                            Set<Integer> result3 = mix3.queryMP(queryid,queryK,queryMPath,v2,queryM,langda);
                            long t6 = System.nanoTime();
                            if(result!=null){
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
                        System.out.println("????????????????????????????????????");
                        fw.close();
                        fw2.close();
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
