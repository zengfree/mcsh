package MCSH.online.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.BAttribute;
import MCSH.online.exact.E2;
import MCSH.online.exact.ExactAlgorithm_float;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Exactsmall {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.a2);
        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();


        int[] vertex = {0, 1, 0}, edge = {0, 0}; //APA
        MetaPath queryMPath = new MetaPath(vertex, edge);

        try {
            Set<Integer> querynodes = new HashSet<>();
            int queryK = 5;
            for (int i = 0; i < graph.length; i++) {
                if(vertexType[i]==0){
                    querynodes.add(i);
                }
            }


            for (int queryid:querynodes){
                int textnum = (int)attribute.get(-1)[0];
                int contnum = (int)attribute.get(-1)[1];
                int[] main = {1,1};
                int[] text = new int[textnum];
                int[] text2 = new int[textnum];
                int[] cont = new int[contnum];
                for(int i = 0;i<textnum;i++){
                    if(attribute.get(queryid)[i]!=0){
                        text[i] = 1;
                        text2[i] = 1;
                    }else {
                        text[i] = 0;
                        text2[i] = 1;
                    }
                }
                for(int i = 0;i<contnum;i++){
                    cont[i] = 0;
                }
                Gweight_float gweight = new Gweight_float(main, text2, cont, 2);
                Adistance_float adistance = new Adistance_float(attribute, gweight);

                long t1 = System.nanoTime();
                E2 exactAlgorithm = new E2(graph,vertexType,edgeType,attribute,adistance,32);
                Set<Integer> res1 = exactAlgorithm.query(queryid,queryK,queryMPath);
                long t2 = System.nanoTime();
                System.out.println("t"+(t2-t1)/1e6);
                if(res1!=null){
                    System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
                    float d1 = adistance.cal_subgraph_attr_dist(res1);//平均距离
                    float d2 = adistance.cal_maxdist(res1);//最大距离
                    float d3 = adistance.cal_fugailv(res1,text);//覆盖率
                    float d4 = adistance.maxnum(res1,text);//最大共享数s
//                avgd1 +=d1;avgd2+=d2;avgd3+=d3;avgd4 +=d4;
                    System.out.println("d1:"+d1);
                    System.out.println("d2:"+d2);
                    System.out.println("d3:"+d3);
                    System.out.println("d4:"+d4);
                    System.out.println();
                }
            }

        }catch (NullPointerException ioException) {
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
