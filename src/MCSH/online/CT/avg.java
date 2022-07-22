package MCSH.online.CT;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.Base_logmap2;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//短路径
public class avg {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
        DataReader dataReader ;
        String s = "fsq";
        switch (s){
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



        int[] vertex = {1,0,1},edge ={21,9};
        MetaPath metaPath = new MetaPath(vertex,edge);
//        int num=0;
//        Set<Integer> set = new HashSet<>();
//        for (int i = 0; i < graph.length; i++) {
//            if(vertexType[i]==0){
//                num++;
//                float[] f = attribute.getOrDefault(i,null);
//                if(f==null){
//                    System.out.print(i+" ");
//                    attribute.put(i, attribute.get(i-1).clone());
//                }
//            }
//        }
//        System.out.println(num);

//        try {
//            FileWriter fw = new FileWriter("FoursquarePersonPrefernceAttributes.txt",false);
//            for (int i = 0; i < num; i++) {
//                float[] f = attribute.getOrDefault(i,null);
////                if(f!=null){
////                System.out.print(i+" ");
//                    StringBuffer stringBuffer = new StringBuffer();
//                    stringBuffer.append(i).append(" ");
//                    for (int j = 0; j < f.length; j++) {
//                        stringBuffer.append(f[j]).append(" ");
//                    }
//                    stringBuffer.append("\r\n");
//                    fw.write(stringBuffer.toString());
////                }
//
//            }
//            fw.close();
//        }catch (IOException e){
//            e.printStackTrace();
//        }



//        int textnum = (int)attribute.get(-1)[0];
//        int contnum = (int)attribute.get(-1)[1];
//        int[] main = {1,1};
//        int[] text = new int[textnum];
//        int[] cont = new int[contnum];
//        for(int i = 0;i<textnum;i++){
//            text[i] = 1;
//        }
//        for(int i = 0;i<contnum;i++){
//            cont[i] = 1;
//        }
//        Gweight_float gweight = new Gweight_float(main, text, cont, 2);
//        Adistance_float adistance_float =new Adistance_float(attribute,gweight);
////        Build b = new Build(graph,vertexType,edgeType,attribute,adistance_float);
////        b.query(1331989,40,metaPath);
//        adistance_float.tji(set);



    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
