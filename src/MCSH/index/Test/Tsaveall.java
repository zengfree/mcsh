package MCSH.index.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.kcore2.KHindex_saveall;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.*;

public class Tsaveall {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();


//        String queryfile = "C:\\Users\\Sward\\Desktop\\query\\smalldblequery1.txt";
        String queryfile = Config.QueryFile+"dblequery_15.txt";
//        File logfile = new File(Config.ResultFile+"base1_result_dblp.txt");

//        List<MetaPath> Mplist = new ArrayList<>();
        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
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

            for (int queryid:querynodes){
                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                long t1 = System.nanoTime();
                KHindex_saveall n2 = new KHindex_saveall(graph,vertexType,edgeType,attribute);
                n2.loadIndex(queryMPath,"");
                Set<Integer> result = n2.search(queryid,queryK,gweight,(float) 0.8);
                long t2 = System.nanoTime();
                System.out.println("hnsw");
                System.out.println("hnsw time:"+(t2-t1)/1000000);
                System.out.println(result.size());
                System.out.println(dataReader.trans(result));
                System.out.println();

                long t3= System.nanoTime();
                KHindex_saveall n20 = new KHindex_saveall(graph,vertexType,edgeType,attribute);
                n20.loadIndex(queryMPath,"");
                Set<Integer> keepSet2 = n2.search2(queryid,queryK,gweight,(float) 0.8);
                long t4 = System.nanoTime();
                System.out.println("truth time:"+(t4-t3)/1000000);
                System.out.println("truth");
                System.out.println(keepSet2.size());
                System.out.println(dataReader.trans(keepSet2));
                System.out.println();

                Set<Integer> set = new HashSet<>(result);
                set.retainAll(keepSet2);
                System.out.println(set.size());
                System.out.println(dataReader.trans(set));
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
