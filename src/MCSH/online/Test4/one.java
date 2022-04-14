package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Baseone;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.util.Map;
import java.util.Set;

//短路径
public class one {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader ;
        switch (args[0]){
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


//        String queryfile ="/home/hadoop/dblequerynodes.txt";
        int queryK = Integer.parseInt(args[1]);
        int queryid = Integer.parseInt(args[2]);
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

        System.out.println("Mp:" + queryMPath.toString() + ",queryid:" + queryid);
        long t1 = System.nanoTime();
        Baseone b1 = new Baseone(graph, vertexType, edgeType, attribute, adistance);
        Set<Integer> result = b1.query(queryid, queryK, queryMPath);
        long t2 = System.nanoTime();
        if (result != null) {
            System.out.println("queryid:" + queryid + ",write");
            System.out.println("time" + (t2 - t1) + "ns");
//                        String str = "queryId=" + "---" + queryid + "\r\n" + dataReader.trans(result) + "\r\n" + adistance.cal_subgraph_attr_dist(result) + "\r\n" + "time=" + (t2 - t1) + "ns" + "\r\n";
        } else {
            System.out.println("queryid:" + queryid + " has no kcore");
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
