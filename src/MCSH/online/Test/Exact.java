package MCSH.online.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.exact.ExactAlgorithm;
import MCSH.util.Adistance;
import MCSH.util.Gweight;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Exact {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,double[]> attribute = dataReader.readattributed();

//        String queryfile = "C:\\Users\\Sward\\Desktop\\query\\smalldblequery1.txt";

        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        MetaPath queryMPath = new MetaPath(vertex, edge);
        String queryfile = Config.QueryFile+"dblequery_mptest.txt";
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

            Gweight gweight = new Gweight(main, text, cont, 2);
            //        System.out.println(gweight.toString());
            Adistance adistance = new Adistance(attribute, gweight);

            Set<Integer> querynodes = new HashSet<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }

            File logfile = new File(Config.ResultFile + "exact_result_small_"  + queryMPath.toString() + ".txt");
            if (!logfile.exists()) {
                logfile.createNewFile();                //如果指定文件不存在，新建文件
            }
            for (int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                long t1 = System.nanoTime();
                ExactAlgorithm exactAlgorithm = new ExactAlgorithm(graph,vertexType,edgeType,null,attribute,adistance);
                Set<Integer> result = exactAlgorithm.query(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                //                System.out.println(result);htop
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
