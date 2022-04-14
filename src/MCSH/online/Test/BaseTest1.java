package MCSH.online.Test;

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

public class BaseTest1 {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();


        String queryfile = "/home/hadoop/smalldblequery1.txt";
//        String queryfile = Config.QueryFile+"dblequeryAPA.txt";
//        String queryfile = Config.QueryFile+"dblequery_15.txt";
//        File logfile = new File(Config.ResultFile+"base1_result_dblp.txt");

//        List<MetaPath> Mplist = new ArrayList<>();
//        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        int vertex[] = {0,1,0},edge[] = {0,0};
        MetaPath queryMPath = new MetaPath(vertex, edge);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line = stdin.readLine();
            String[] Mpath = line.split("\t");

            line = stdin.readLine();
            //                    System.out.println(line);
//            int queryK = Integer.parseInt(args[0]);
            int queryK = 5;
            //模拟参数输入 均值
            line = stdin.readLine();
            String[] vector = line.split("\t");
            int main[] = StringToInt(vector[0].split(" "));
            int text[] = StringToInt(vector[1].split(" "));
            int cont[] = StringToInt(vector[2].split(" "));

            Gweight_float gweight = new Gweight_float(main, text, cont, 2);
            //        System.out.println(gweight.toString());
            Adistance_float adistance = new Adistance_float(attribute, gweight);

//            Set<Integer> querynodes = new HashSet<>();
            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }

//            for (int i = 1; i <= 5; i++) {
            File logfile = new File("/home/hadoop/base1.txt");
//                File logfile = new File(Config.ResultFile +"base_"+queryK+"_DBLP.txt");
//            File logfile = new File(Config.ResultFile + "base_" + queryK + "_" + queryMPath.toString() + ".txt");
                if (!logfile.exists()) {
                    logfile.createNewFile();                //如果指定文件不存在，新建文件
                }
                for (int queryid:querynodes){
                    System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                    long t1 = System.nanoTime();
                    baseline1 base = new baseline1(graph,vertexType,edgeType,attribute,adistance);
                    Set<Integer> result = base.queryM_protect(queryid,queryK,queryMPath,10);
//                    advanced2 advanced = new advanced2(graph,vertexType,edgeType,attribute,adistance);
//                    Set<Integer> result =  advanced.query(queryid,queryK,queryMPath);
                    long t2 = System.nanoTime();
                    //                System.out.println(result);
                    FileWriter fw = new FileWriter(logfile, true);
                    if(result!=null){
                        fw.write("queryId=" + "---" + queryid + "\r\n");
                        fw.write(dataReader.trans(result)+"\r\n");
                        fw.write(adistance.cal_subgraph_attr_dist(result)+"\r\n");
                        fw.write("time=" + (t2-t1) +"ns" +"\r\n");
                    }
//                    else {
//                        fw.write("queryId=" + "---" + queryid + "\r\n");
//                        fw.write(" \r\n");
//                        fw.write(" \r\n");
//                        fw.write("time=" + (t2-t1) +"ns" +"\r\n");
//                    }
                    fw.close();
                }

//            }

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
