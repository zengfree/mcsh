package MCSH.index;

import MCSH.DataReader;
import MCSH.index.Nsw.MixIndex;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.IOException;
import java.util.Map;

public class IBBuild {
    public static void main(String[] args) throws IOException {
        String Root = args[0];
        String Graph = Root + "/graph.txt";
        String Vertex = Root + "/vertex.txt";
        String Edge = Root + "/edge.txt";
        String Attribute = Root + "/data.txt";
        DataReader dataReader = new DataReader(Graph,Vertex,Edge,null,Attribute);

        int[][] graph = dataReader.readGraph();
        int[] vertexType = dataReader.readVertexType();
        int[] edgeType = dataReader.readEdgeType();
        Map<Integer,float[]> attribute = dataReader.readattributed_float();

        String[] Mpath = args[1].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath Path = new MetaPath(vertex, edge);
        int M = Integer.parseInt(args[2]);//50
        int ef = M*8;//100
        float langda = Float.parseFloat((args[3]));

        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];
        int[] main = {1,1};
        int[] text = new int[textnum];
        int[] cont = new int[contnum];
        for(int i=0;i<textnum;i++){
            text[i] = 1;
        }
        for(int i=0;i<contnum;i++){
            cont[i] = 1;
        }
        String indexfile = Root+ "/"+Path+".n2";
        String datafile = Root+ "/"+Path+".data";
        Gweight_float gweight = new Gweight_float(main,text,cont,2);
        Adistance_float adistance= new Adistance_float(attribute,gweight);
        MixIndex mix = new MixIndex(graph,vertexType,edgeType,attribute,adistance);
        mix.build(Path,indexfile,datafile,M,ef,langda);
        System.out.println("build finish!");
    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
