package MCSH.online;

import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.online.exact.E2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.util.Map;
import java.util.Set;

public class ExactSearch {
    public static void main(String[] args) {
        String Root = args[0] ;
        String Graph = Root + "/graph.txt";
        String Vertex = Root + "/vertex.txt";
        String Edge = Root + "/edge.txt";
        String Attribute = Root + "/attribute.txt";
        DataReader dataReader = new DataReader(Graph,Vertex,Edge,null,Attribute);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> integerMap = dataReader.readattributed_float();
        int textnum = (int)integerMap.get(-1)[0];
        int contnum = (int)integerMap.get(-1)[1];

        int queryId = Integer.parseInt(args[1]);
        int queryK = Integer.parseInt(args[2]);
        String[] Mpath = args[3].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);

        int[] main = {1,1};
        int[] text = new int[textnum];
        int[] cont = new int[contnum];
        for(int i = 0;i<textnum;i++){
            text[i] = 1;
        }
        for(int i = 0;i<contnum;i++){
            cont[i] = 1;
        }
        Gweight_float gweight_float = new Gweight_float(main,text,cont,2);
        Adistance_float adistance_float = new Adistance_float(integerMap,gweight_float);
        E2 e2 = new E2(graph,vertexType,edgeType,integerMap,adistance_float,20);
        Set<Integer> result1 = e2.query(queryId,queryK,queryMPath);
        System.out.println("Exact: queryId:"+queryId+",queryK:"+queryK+",queryMPath:"+queryMPath);
        if(result1!=null){
            System.out.println("find:"+result1.size());
            for (int id:result1){
                System.out.print(id+" ");
            }
        }else {
            System.out.println("no this kcore");
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
