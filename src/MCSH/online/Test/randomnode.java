package MCSH.online.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.CoreDec;
import MCSH.util.MetaPath;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class randomnode {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
//        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);

        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();


        int queryK = 30;
        System.out.println(queryK);
        int[] vertex = {1,0,1},edge={3,0};
        MetaPath queryMPath = new MetaPath(vertex, edge);

        try{
            File logfile = new File("dblp.txt");
            CoreDec coreDec =new CoreDec(graph,vertexType,edgeType);
            Set<Integer> result = coreDec.queryK(queryMPath,queryMPath.vertex[0],queryK);
            if(result==null) {
                System.out.println(queryMPath.toString()+"is null,queryk:"+queryK);
            }
            Set<Integer> random = new HashSet<>();
            Object[] list = result.toArray();
            while(random.size()<500){
                int queryid = (int)list[(int)(Math.random()*list.length)];
                if(random.contains(queryid)) continue;
                random.add(queryid);
                System.out.println(queryid);
            }
            FileWriter fw = new FileWriter(logfile,false);
            for (int queryid: random) {
                fw.write(queryid+"\n");
            }
            fw.close();
        }catch (IOException e){
            System.out.println(e);
        }
//        System.out.println("Finished " + count + " queries.\ntime1=" + time1 + "\ntime2=" + time2);
    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }

}
