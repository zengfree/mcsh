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
//短路径
public class Ktest {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();


//        String queryfile = "C:\\Users\\Sward\\Desktop\\query\\smalldblequery1.txt";
        String queryfile = Config.QueryFile+"dblequerynodes.txt";
//        File logfile = new File(Config.ResultFile+"base1_result_dblp.txt");

//        List<MetaPath> Mplist = new ArrayList<>();
        int[] vertex = {1, 0, 1}; //APA
        int[] edge = {3, 0};
//        int vertex[] = {1, 0, 1},edge[] = {9 , 21};//PMP
        MetaPath queryMPath = new MetaPath(vertex, edge);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line = stdin.readLine();
            String[] Mpath = line.split("\t");

//            line = stdin.readLine();
//            //                    System.out.println(line);
//            int queryK = Integer.parseInt(line);

            int queryK = Integer.parseInt(args[0]);
            int queryM = Integer.parseInt(args[1]);

            //模拟参数输入 均值
            line = stdin.readLine();
            String[] vector = line.split("\t");
            int[] main = StringToInt(vector[0].split(" "));
            int[] text = StringToInt(vector[1].split(" "));
            int[] cont = StringToInt(vector[2].split(" "));

            Gweight_float gweight = new Gweight_float(main, text, cont, 2);
            //        System.out.println(gweight.toString());
            Adistance_float adistance = new Adistance_float(attribute, gweight);

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            baseline1 baseline1 = new baseline1(graph,vertexType,edgeType,attribute,adistance);

            File logfile = new File(Config.ResultFile + "K/base" + queryK + "_"  + queryM + "_"+ queryMPath.toString() + ".txt");
            File logfile2 = new File(Config.ResultFile + "K/basem" + queryK + "_" + queryM + "_" +queryMPath.toString() + ".txt");
            File logfile3 = new File(Config.ResultFile + "K/basemp" + queryK + "_" + queryM + "_" +queryMPath.toString() + ".txt");
//            if (!logfile.exists()) {
//                logfile.createNewFile();                //如果指定文件不存在，新建文件
//            }
//            if (!logfile2.exists()) {
//                logfile2.createNewFile();                //如果指定文件不存在，新建文件
//            }
//            if(!logfile3.exists()){
//                logfile3.createNewFile();                //如果指定文件不存在，新建文件
//            }
            for (int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                try{
                    long t1 = System.nanoTime();
                    Set<Integer> result = baseline1.query(queryid,queryK,queryMPath);
                    long t2 = System.nanoTime();
                    FileWriter fw = new FileWriter(logfile, true);
                    if(result!=null){
                        fw.write("queryId=" + "---" + queryid + "\r\n");
                        fw.write(dataReader.trans(result)+"\r\n");
                        fw.write(adistance.cal_subgraph_attr_dist(result)+"\r\n");
                        fw.write("time=" + (t2-t1) +"ns" +"\r\n");
                    }
                    else {
                        fw.write("queryId=" + "---" + queryid + "\r\n");
                        fw.write(" \r\n");
                        fw.write(" \r\n");
                        fw.write("time=" + (t2-t1) +"ns" +"\r\n");
                    }
                    fw.close();
                }catch (NullPointerException exception){
//                    exception.printStackTrace();
                    System.out.println(exception);
                    FileWriter fw = new FileWriter(logfile, true);
                    fw.write("queryId=" + "---" + queryid + "\r\n");
                    fw.write(" \r\n");
                    fw.write(" \r\n");
                    fw.write("time=" + (0) +"ns" +"\r\n");
                    fw.close();
                }
                //                System.out.println(result);

                try{
                    long t3 = System.nanoTime();
                    Set<Integer> result2 = baseline1.queryM(queryid,queryK,queryMPath,queryM);
                    long t4 = System.nanoTime();
                    //                System.out.println(result);
                    FileWriter fw2 = new FileWriter(logfile2, true);
                    if(result2!=null){
                        fw2.write("queryId=" + "---" + queryid + "\r\n");
                        fw2.write(dataReader.trans(result2)+"\r\n");
                        fw2.write(adistance.cal_subgraph_attr_dist(result2)+"\r\n");
                        fw2.write("time=" + (t4-t3) +"ns" +"\r\n");
                    }
                    else {
                        fw2.write("queryId=" + "---" + queryid + "\r\n");
                        fw2.write(" \r\n");
                        fw2.write(" \r\n");
                        fw2.write("time=" + (t4-t3) +"ns" +"\r\n");
                    }
                    fw2.close();
                }catch (NullPointerException e){
//                    e.printStackTrace();
                    System.out.println(e);
                    FileWriter fw = new FileWriter(logfile2, true);
                    fw.write("queryId=" + "---" + queryid + "\r\n");
                    fw.write(" \r\n");
                    fw.write(" \r\n");
                    fw.write("time=" + (0) +"ns" +"\r\n");
                    fw.close();
                }

                try{
                    long t5 = System.nanoTime();
                    Set<Integer> result3 = baseline1.queryM_protect(queryid,queryK,queryMPath,queryM);
                    long t6 = System.nanoTime();
                    FileWriter fw3 = new FileWriter(logfile3, true);
                    if(result3!=null){
                        fw3.write("queryId=" + "---" + queryid + "\r\n");
                        fw3.write(dataReader.trans(result3)+"\r\n");
                        fw3.write(adistance.cal_subgraph_attr_dist(result3)+"\r\n");
                        fw3.write("time=" + (t6-t5) +"ns" +"\r\n");
                    }
                    else {
                        fw3.write("queryId=" + "---" + queryid + "\r\n");
                        fw3.write(" \r\n");
                        fw3.write(" \r\n");
                        fw3.write("time=" + (t6-t5) +"ns" +"\r\n");
                    }
                    fw3.close();
                }catch (NullPointerException e){
//                    e.printStackTrace();
                    System.out.println(e);
                    FileWriter fw = new FileWriter(logfile3, true);
                    fw.write("queryId=" + "---" + queryid + "\r\n");
                    fw.write(" \r\n");
                    fw.write(" \r\n");
                    fw.write("time=" + (0) +"ns" +"\r\n");
                    fw.close();
                }
            }
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
