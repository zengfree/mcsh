package MCSH.index.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.kcore.IndexBasedSearch;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class KHindexTest {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();


        String queryfile = "/home/hadoop/smalldblequery1.txt";
//        String queryfile = Config.QueryFile+"dblequery_35.txt";
//        File logfile = new File(Config.ResultFile+"base1_result_dblp.txt");

//        List<MetaPath> Mplist = new ArrayList<>();
        int vertex[] = {0, 1, 0}, edge[] = {0, 0}; //APA
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

//            Set<Integer> querynodes = new HashSet<>();
            Queue<Integer> querynodes = new LinkedList<>();

            int queryId = 511;
//            long t = System.nanoTime();
//            ExactAlgorithm_advance2 exactAlgorithm = new ExactAlgorithm_advance2(graph,vertexType,edgeType,null,attribute,adistance);
//            Set<Integer> result0 = exactAlgorithm.query(queryId,queryK,queryMPath);
//            long t0 = System.nanoTime();
//            if(result0!=null){
//                System.out.println();
//                System.out.println(dataReader.trans(result0));
//                System.out.println(t0-t+"ns");
//                System.out.println((t0-t)/1000000+"ms");
//                System.out.println(adistance.cal_subgraph_attr_dist(result0));
//            }
//            else {
//                System.out.println();
//                System.out.println(t0-t+"ns");
//                System.out.println((t0-t)/1000000+"ms");
//                System.out.println();
//            }

            long t1 = System.nanoTime();
            IndexBasedSearch search = new IndexBasedSearch(graph,vertexType,edgeType,attribute,adistance);
            Set<Integer> result = search.query(queryId,queryK,queryMPath,(float) 1.0);
            long t2 = System.nanoTime();
            if(result!=null){
                System.out.println();
                System.out.println(dataReader.trans(result));
                System.out.println(t2-t1+"ns");
                System.out.println((t2-t1)/1000000+"ms");
                System.out.println(adistance.cal_subgraph_attr_dist(result));
            }
            else {
                System.out.println();
                System.out.println(t2-t1+"ns");
                System.out.println((t2-t1)/1000000+"ms");
                System.out.println();
            }


            long t3 = System.nanoTime();
//            IndexBasedSearch search = new IndexBasedSearch(graph,vertexType,edgeType,attribute,adistance);
            Set<Integer> result1 = search.queryM(queryId,queryK,queryMPath,(float) 1.0,1);
            long t4 = System.nanoTime();
            if(result!=null){
                System.out.println();
                System.out.println(dataReader.trans(result1));
                System.out.println(t4-t3+"ns");
                System.out.println((t4-t3)/1000000+"ms");
                System.out.println(adistance.cal_subgraph_attr_dist(result1));
            }
            else {
                System.out.println();
                System.out.println(t4-t3+"ns");
                System.out.println((t4-t3)/1000000+"ms");
                System.out.println();
            }


            long t5 = System.nanoTime();
//            IndexBasedSearch search = new IndexBasedSearch(graph,vertexType,edgeType,attribute,adistance);
            Set<Integer> result2 = search.query_save(queryId,queryK,queryMPath,(float) 1.0,1);
            long t6 = System.nanoTime();
            if(result!=null){
                System.out.println();
                System.out.println(dataReader.trans(result2));
                System.out.println(t6-t5+"ns");
                System.out.println((t6-t5)/1000000+"ms");
                System.out.println(adistance.cal_subgraph_attr_dist(result2));
            }
            else {
                System.out.println();
                System.out.println(t6-t5+"ns");
                System.out.println((t6-t5)/1000000+"ms");
                System.out.println();
            }



//            long t1 = System.nanoTime();
//            baseline1_1 base1 = new baseline1_1(graph,vertexType,edgeType,null,attribute,adistance);
//            Set<Integer> result1 = base1.query5(511,queryK,queryMPath,1);
//            long t2 = System.nanoTime();
//            System.out.println("base1.query5");
//            if(result1!=null){
//                System.out.println(dataReader.trans(result1));
//                System.out.println(t2-t1+"ns");
//                System.out.println((t2-t1)/1000000+"ms");
//                System.out.println(adistance.cal_subgraph_attr_dist(result1));
//            }
//            else {
//                System.out.println();
//                System.out.println(t2-t1+"ns");
//                System.out.println((t2-t1)/1000000+"ms");
//                System.out.println();
//            }
//            System.out.println();

//            while((line = stdin.readLine()) != null){
//                int queryid = Integer.parseInt(line);
//                querynodes.add(queryid);
//            }
//
//            File logfile = new File("/home/hadoop/result.txt");
//            if (!logfile.exists()) {
//                logfile.createNewFile();                //如果指定文件不存在，新建文件
//            }
//            for (int queryid:querynodes){
//                System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
//                long t1 = System.nanoTime();
//                IndexBasedSearch search = new IndexBasedSearch(graph,vertexType,edgeType,attribute,adistance);
//                Set<Integer> result = search.query(queryid,queryK,queryMPath,(float) 1.0,1);
//                long t2 = System.nanoTime();
//                //                System.out.println(result);htop
//                FileWriter fw = new FileWriter(logfile, true);
//                if(result!=null){
//                    fw.write("queryId=" + "---" + queryid + "\r\n");
//                    fw.write(dataReader.trans(result)+"\r\n");
//                    fw.write(adistance.cal_subgraph_attr_dist(result)+"\r\n");
//                    fw.write("time=" + (t2-t1) +"ns" +"\r\n");
//                }
//                else {
//                    fw.write("queryId=" + "---" + queryid + "\r\n");
//                    fw.write(" \r\n");
//                    fw.write(" \r\n");
//                    fw.write("time=" + (t2-t1) +"ns" +"\r\n");
//                }
//                fw.close();
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
