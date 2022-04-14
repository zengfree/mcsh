package MCSH.online.Test2;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.advanced.Advance2;
import MCSH.online.basic.baseline1;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class base_ad2 {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();


//        String queryfile = "C:\\Users\\Sward\\Desktop\\query\\smalldblequery1.txt";
        String queryfile = Config.QueryFile+"dblequery_mptest.txt";
//        File logfile = new File(Config.ResultFile+"base1_result_dblp.txt");

//        List<MetaPath> Mplist = new ArrayList<>();
        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
//        int vertex[] = {1, 0, 1},edge[] = {9 , 21};//PMP
        MetaPath queryMPath = new MetaPath(vertex, edge);
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line = stdin.readLine();
            String[] Mpath = line.split("\t");

            line = stdin.readLine();
            //                    System.out.println(line);
            int queryK = Integer.parseInt(line);

            //模拟参数输入 均值
            line = stdin.readLine();
            String[] vector = line.split("\t");
            int main[] = StringToInt(vector[0].split(" "));
            int text[] = StringToInt(vector[1].split(" "));
            int cont[] = StringToInt(vector[2].split(" "));

            Gweight_float gweight = new Gweight_float(main, text, cont, 2);
            //        System.out.println(gweight.toString());
            Adistance_float adistance = new Adistance_float(attribute, gweight);

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }

            File logfile = new File(Config.ResultFile + "Basemp" + 4 + "_" + queryMPath.toString() + ".txt");
//            File logfile2 = new File(Config.ResultFile + "advance1mp" + 4 + "_" + queryMPath.toString() + ".txt");
            File logfile3 = new File(Config.ResultFile + "advance2mp" + 4 + "_" + queryMPath.toString() + ".txt");

            for (int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                long t1 = System.nanoTime();
                baseline1 baseline1 = new baseline1(graph,vertexType,edgeType,attribute,adistance);
                Set<Integer> result = baseline1.queryM_protect(queryid,queryK,queryMPath,4);
                long t2 = System.nanoTime();
                //                System.out.println(result);
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


                long t5 = System.nanoTime();
                Advance2 ad2 = new Advance2(graph,vertexType,edgeType,attribute,adistance);
                Set<Integer> result3 = ad2.queryM_protect(queryid,queryK,queryMPath,4);
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
