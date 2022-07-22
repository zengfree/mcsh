package MCSH.compare;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.Test.E;
import MCSH.online.basic.Base_logmap2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.util.Map;
import java.util.Set;

public class VACSearch {
    public static void main(String[] args) {
        String Root = args[0] ;
        String Graph = Root + "/graph.txt";
        String Vertex = Root + "/vertex.txt";
        String Edge = Root + "/edge.txt";
        String Attribute = Root + "/attribute.txt";

        int queryId = Integer.parseInt(args[1]);
        int queryK = Integer.parseInt(args[2]);
        String[] Mpath = args[3].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        dataInput dataInput = new dataInput(Graph,Vertex, Edge,Attribute,queryMPath);
        VAC myquery = new VAC(queryId,queryK, dataInput.G, dataInput.Att);
        Map<Integer,Set<Integer>> result1 = myquery.query(dataInput.G,queryId,queryK);
        System.out.println("VAC: queryId:"+queryId+",queryK:"+queryK+",queryMPath:"+queryMPath);
        if(result1==null) System.out.println("no this kcore");
        else {
            Set<Integer> res1 = result1.keySet();
            System.out.println("find:"+result1.size());
            for (int id:res1){
                System.out.print(id+" ");
            }
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
