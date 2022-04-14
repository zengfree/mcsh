package MCSH.online;

import MCSH.DataReader;
import MCSH.online.basic.baseline1;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.util.Map;
import java.util.Set;

public class ASearch {
    public static void main(String[] args) {
        String Root = args[0] ;
        String Graph = Root + "/graph.txt";
        String Vertex = Root + "/vertex.txt";
        String Edge = Root + "/edge.txt";
        String Attribute = Root + "/DBLPdata.txt";
        DataReader dataReader = new DataReader(Graph,Vertex,Edge,null,Attribute);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> integerMap = dataReader.readattributed_float();
        int textnum = (int)integerMap.get(-1)[0];
        int contnum = (int)integerMap.get(-1)[1];

        int queryId = Integer.parseInt(args[1]);
        int queryK = Integer.parseInt(args[2]);
        int queryM = Integer.parseInt(args[3]);
        String[] Mpath = args[4].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
//        System.out.println(queryMPath);

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
        baseline1 baseline1 = new baseline1(graph,vertexType,edgeType,integerMap,adistance_float);
        Set<Integer> result2 = baseline1.queryM_protect_test(queryId,queryK,queryMPath,queryM);
        System.out.println("queryId:"+queryId+",queryK:"+queryK+",queryMPath:"+queryMPath);
        if(result2!=null){
            System.out.println("find:"+result2.size());
            for (int id:result2){
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
