package MCSH.online;

import MCSH.DataReader;
import MCSH.online.Test2.Base;
import MCSH.online.basic.Base_logmap2;
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
        int queryM = Integer.parseInt(args[3]);
        float yu = Float.parseFloat(args[4]);
        String[] Mpath = args[5].split(";");
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
        adistance_float.setYu(yu);
        Base_logmap2 base = new Base_logmap2(graph,vertexType,edgeType,integerMap,adistance_float);
        Set<Integer> result1 =base.query(queryId,queryK,queryMPath);
        System.out.println("MCD: queryId:"+queryId+",queryK:"+queryK+",queryMPath:"+queryMPath+","+yu);
        if(result1!=null){
            System.out.println("find:"+result1.size());
            for (int id:result1){
                System.out.print(id+" ");
            }
        }else {
            System.out.println("no this kcore");
        }
        Set<Integer> result2 =base.queryM(queryId,queryK,queryMPath,queryM);
        System.out.println("BD: queryId:"+queryId+",queryK:"+queryK+",queryMPath:"+queryMPath+",tao"+yu+",querym:"+queryM);
        if(result2!=null){
            System.out.println("find:"+result2.size());
            for (int id:result2){
                System.out.print(id+" ");
            }
        }else {
            System.out.println("no this kcore");
        }
        Set<Integer> result3 =base.queryM_protect(queryId,queryK,queryMPath,queryM);
        System.out.println("BDP: queryId:"+queryId+",queryK:"+queryK+",queryMPath:"+queryMPath+",tao"+yu+",querym:"+queryM);
        if(result3!=null){
            System.out.println("find:"+result3.size());
            for (int id:result3){
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
