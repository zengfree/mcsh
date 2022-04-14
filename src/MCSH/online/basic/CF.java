package MCSH.online.basic;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class CF {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();
        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];

        String queryfile = Config.QueryFile+"q2.txt";
//        String queryfile = "/home/hadoop/dblequerynodes.txt";
        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        MetaPath queryMPath = new MetaPath(vertex, edge);
        int queryK = Integer.parseInt(args[0]);
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
        File logfile = new File("cf_"+queryK+".txt");
//        File logfile = new File("/home/hadoop/NDist/basenodist_"+queryK+".txt");
//        File logfile2 = new File("/home/hadoop/NDist/base"+queryK+".txt");

        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            System.out.println("querynode size:"+querynodes.size());
            stdin.close();

            FileWriter fw = new FileWriter(logfile, false);
//            FileWriter fw2 = new FileWriter(logfile2, false);
            for (int queryid:querynodes){
                long t1 = System.nanoTime();
                baseline1 bs1 = new baseline1(graph,vertexType,edgeType,attribute,adistance);
                Set<Integer> set = bs1.query2(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                if(set!=null){
                    fw.write("queryId=" + "---" + queryid + "\r\n");
                    fw.write(dataReader.trans(set)+"\r\n");
                    fw.write(adistance.cal_subgraph_attr_dist(set)+"\r\n");
                    fw.write("time=" + (t2-t1) +"ns" +"\r\n");
                }
            }
            fw.flush();
//            fw2.flush();
            fw.close();
//            fw2.close();
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
