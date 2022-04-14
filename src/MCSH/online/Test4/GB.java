package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Build;
import MCSH.util.Adistance_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

//短路径
public class GB {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader ;
        switch (args[4]){
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

        int threadnum = Integer.parseInt(args[0]);
        String queryfile = Config.QueryFile+ args[1];
//        String queryfile = "/home/hadoop/q2.txt";
        int iternum = Integer.parseInt(args[2]);
        String[] Mpath = args[3].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));

        MetaPath queryMPath = new MetaPath(vertex, edge);
        System.out.println(queryMPath);
        Adistance_float adistance = new Adistance_float();

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

            File logfile = new File(args[3] +"buildtime.txt");
            ExecutorService exec = Executors.newFixedThreadPool(threadnum);
            FileWriter fw = new FileWriter(logfile, false);
//            ReentrantLock lock = new ReentrantLock();
            AtomicLong tsum = new AtomicLong();
            AtomicLong tsum1= new AtomicLong();
            for(int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                exec.submit(()->{
                    try{
                        long tall1 = 0;
                        for (int i = 0; i < iternum; i++) {
                            long t1 = System.nanoTime();
                            Build b = new Build(graph,vertexType,edgeType,attribute,adistance);
                            b.build1(queryMPath,queryid);
                            long t2 = System.nanoTime();
                            System.out.println(i+":time1:"+(t2-t1)/1000000+"ms");
                            tall1+=(t2-t1)/1000000;
                        }

                        long tall2 = 0;
                        for (int i = 0; i < iternum; i++) {
                            long t1 = System.nanoTime();
                            Build b = new Build(graph,vertexType,edgeType,attribute,adistance);
                            b.build3(queryMPath,queryid);
                            long t2 = System.nanoTime();
                            System.out.println(i+":time3:"+(t2-t1)/1000000+"ms");
                            tall2+=(t2-t1)/1000000;
                        }
                        System.out.println("queryid:"+queryid+",write");
                        String str = "queryId=" + "---" + queryid + ";" +"base time=" + (tall1) +"ms" +"\r\n";
                        str +="queryId=" + "---" + queryid + ";" +"invert time2=" + (tall2) +"ms" +"\r\n";
                        fw.write(str);
                        fw.flush();
//                        lock.lock();
                        tsum.addAndGet(tall1/iternum);
                        tsum1.addAndGet(tall2/iternum);
//                        lock.unlock();
                    }catch (IOException exception){
                        exception.printStackTrace();
                    }
                });
            }
//            fw.close();
            exec.shutdown();
            while(true){
                if(exec.isTerminated()){
                    System.out.println("所有的子线程都已经结束了");
                    tsum.set(tsum.get()/querynodes.size());
                    tsum1.set(tsum1.get()/querynodes.size());
                    System.out.println("base:"+(tsum)+"ms");
                    System.out.println("base:"+(tsum1)+"ms");
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
