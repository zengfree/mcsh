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


        int queryK = Integer.parseInt(args[2]);
        System.out.println(queryK);

        String[] paths = args[1].split(";");
        int[] vertex = StringToInt(paths[0].split(","));
        int[] edge = StringToInt(paths[1].split(","));
        MetaPath metaPath = new MetaPath(vertex,edge);

        try{
            File logfile = new File("random"+ args[0] + "_" + metaPath.toString() +  ".txt");
            CoreDec coreDec =new CoreDec(graph,vertexType,edgeType);
            Set<Integer> result = coreDec.queryK(metaPath,metaPath.vertex[0],queryK);
            if(result==null) {
                System.out.println(metaPath.toString()+"is null,queryk:"+queryK);
            }
            Set<Integer> random = new HashSet<>();
            Object[] list = result.toArray();
            while(random.size()<30){
                int queryid = (int)list[(int)(Math.random()*list.length)];
                random.add(queryid);
                System.out.println(queryid);
            }
            FileWriter fw = new FileWriter(logfile,true);
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
