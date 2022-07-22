package MCSH.online.CT;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
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

//短路径
public class ccavg {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);

        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

//        String queryfile = Config.QueryFile+args[5];
//        String queryfile = Config.QueryFile+"dblequerynodes.txt";
        String queryfile ="C:\\Users\\DELL\\Desktop\\实验\\query\\q2.txt";
        int queryK = 30;
        int queryM = 10;
        int threadnum = 0;

        int[] vertex = {1,0,1};
        int[] edge = {3,0};
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
            Build b = new Build(graph,vertexType,edgeType,attribute,adistance);
            for (int queryid:querynodes) {
                System.out.println("queryid:"+queryid);
                b.query(queryid,queryK,queryMPath);
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
