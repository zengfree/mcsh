package MCSH.online.Test2;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.CoreDec;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;

public class main {
    public static void main(String[] args) {
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
//        DataReader dataReader = new DataReader(Config.IMDBGraph, Config.IMDBVertex, Config.IMDBEdge, null,Config.IMDBpersonattributed);
//        DataReader dataReader = new DataReader(Config.FsqGraph, Config.FsqVertex, Config.FsqEdge, null,Config.Fsqattributed);
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
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

        String queryfile = Config.QueryFile+args[1];
//        String queryfile = "/home/hadoop/fsq.txt";
        System.out.println(queryfile);
        ArrayList<MetaPath> queryMpath = new ArrayList<>();
        try{
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;
            while((line = stdin.readLine()) !=null){
                String[] paths = line.split(";");
                int[] vertex = StringToInt(paths[0].split(","));
                int[] edge = StringToInt(paths[1].split(","));
                MetaPath metaPath = new MetaPath(vertex,edge);
                queryMpath.add(metaPath);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            for (MetaPath qPath: queryMpath) {
                System.out.println(qPath.toString());
                File logfile = new File( "coreD_" + args[0] +qPath.toString() + ".txt");
                FileWriter fw = new FileWriter(logfile, false);
                CoreDec coreDec = new CoreDec(graph,vertexType,edgeType);
                coreDec.query(qPath,qPath.vertex[0],fw);
                fw.close();
            }
        }catch (IOException | NullPointerException exception){
            exception.printStackTrace();
            System.out.println(exception);
        }

    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }


    private String transset(Set<Integer> set){
        StringBuffer str5 = new StringBuffer();
        for (int i : set) {
            str5.append(i + ", ");
        }
        return str5.toString();
    }

}
