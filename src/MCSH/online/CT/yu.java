package MCSH.online.CT;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.util.Adistance_float;
import MCSH.util.BatchSearch;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import javax.naming.PartialResultException;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//短路径
public class yu {
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

        String queryfile = args[5];
//        String queryfile = Config.QueryFile+"dblequerynodes.txt";
//        String queryfile ="C:\\Users\\DELL\\Desktop\\实验\\query\\randomimdb_1-21-0-9-1.txt";
        int queryK = Integer.parseInt(args[0]);
        int queryM = Integer.parseInt(args[1]);
        int threadnum = Integer.parseInt(args[2]);
        String[] Mpath = args[3].split(";");

        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        System.out.println(queryMPath);

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
            System.out.println("querynodes size:"+querynodes.size());
            stdin.close();

//            for(int curId = 0;curId < graph.length;curId ++) {
//                if(vertexType[curId] == queryMPath.vertex[0]) {
//                    System.out.println(curId);
//                    attribute.get(curId);
//                }
//            }
//            System.out.println(Arrays.toString(attribute.get(1183761)));

            float[] yus = new float[]{(float) 0.1,(float) 0.2,(float) 0.3,(float) 0.4,(float) 0.5,(float) 0.6,(float) 0.7,(float) 0.8,(float) 0.9};
            for (int i = 0; i < yus.length; i++) {
                adistance.setYu(yus[i]);
                float avgdist = (float) 0;
                long avgt = 0;
                int num = 0;
                for(int queryid:querynodes){
                    System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
                    long t3 = System.nanoTime();
                    Base_logmap2 base = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
                    Set<Integer> res1 = base.queryM_protect(queryid,queryK,queryMPath,queryM);
                    long t4 = System.nanoTime();
                    if(res1!=null){
                        num++;
                        float dist = adistance.cal_subgraph_attr_dist(res1);
                        avgdist+=dist;
                        avgt+=(t4-t3)/1e6;
                        System.out.println("dist:"+dist);
                        System.out.println("query time:" +(t4-t3)/1e6+"ms");
                    }
                }
                System.out.println(yus[i]+ ",avgdist:"+avgdist/num);
                System.out.println(yus[i]+ ",avgt:"+avgt/num);
            }

        }catch (IOException e) {
            System.out.println(e);
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
