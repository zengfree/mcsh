package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixIndex;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.IOException;
import java.util.Map;

public class IB {
    public static void main(String[] args) throws IOException {
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
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
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

//        int threadnum = Integer.parseInt(args[2]);
        String[] Mpath = args[1].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath Path = new MetaPath(vertex, edge);
        int M = Integer.parseInt(args[2]); //50
        int ef = Integer.parseInt(args[3]); //500


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

        String datafile = Config.IndexRoot+args[0]+"/"+Path.toString()+"data.txt";
        String idnexfile = Config.IndexRoot+args[0]+"/"+Path.toString()+"index.n2";
        System.out.println(datafile);
        Gweight_float gweight = new Gweight_float(main, text, cont, 2);
        Adistance_float adistance = new Adistance_float(attribute, gweight);

        MixIndex mix = new MixIndex(graph,vertexType,edgeType,attribute,adistance);
        mix.build(Path,idnexfile,datafile,M,ef);

    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
