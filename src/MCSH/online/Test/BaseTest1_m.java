package MCSH.online.Test;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.online.basic.baseline1_1;
import MCSH.util.Adistance;
import MCSH.util.Gweight;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BaseTest1_m {
    public static void main(String[] args) {
        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        DataReader dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, Config.authorA2G,Config.authorattribute);
        int graph[][] = dataReader.readGraph();
        int vertexType[] = dataReader.readVertexType();
        int edgeType[] = dataReader.readEdgeType();
        Map<Integer,double[]> attribute = dataReader.readattributed();
//        int textnum = (int)attribute.get(-1)[0];
//        int contnum = (int)attribute.get(-1)[1];
//        int queryPnum = attribute.size();

//        System.out.println(textnum);
//        System.out.println(contnum);

//        String queryfile = "C:\\Users\\Sward\\Desktop\\query\\smalldblequery1.txt";

        List<MetaPath> Mplist = new ArrayList<>();
        int vertex[] = {1, 0, 1}, edge[] = {3, 0}; //APA
        MetaPath queryMPath = new MetaPath(vertex, edge);
        Mplist.add(queryMPath);
//        int vertex2[] = {1, 0, 2, 0, 1}, edge2[] = {3, 1, 4, 0};
//        MetaPath queryMPath2 = new MetaPath(vertex2, edge2);
//        Mplist.add(queryMPath2);
//        for(MetaPath queryMp:Mplist){
            String queryfile = Config.QueryFile+"dblequery_mptest.txt";
            for(int i=5;i<=30;i = i+5){
                File logfile = new File(Config.ResultFile+"basem/base_result_small_"+ i +"_" +queryMPath.toString() + ".txt");
                try{
                    if (!logfile.exists()) {
                        logfile.createNewFile();                //如果指定文件不存在，新建文件
                    }
                    FileWriter fw = new FileWriter(logfile, true);

                    //获取查询条件
                    BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
                    String line = stdin.readLine();
                    String[] Mpath = line.split("\t");
//                    int vertex[] = StringToInt(Mpath[0].split(" "));
//                    int edge[] =StringToInt(Mpath[1].split(" "));
//                    MetaPath queryMPath = new MetaPath(vertex, edge);
                    //        System.out.println(queryMPath);
                    line  = stdin.readLine();
//                    System.out.println(line);
                    int queryK = Integer.parseInt(line);

                    //模拟参数输入 均值
                    line = stdin.readLine();
                    String[] vector = line.split("\t");
                    int main[] = StringToInt(vector[0].split(" "));
                    int text[] = StringToInt(vector[1].split(" "));
                    int cont[] = StringToInt(vector[2].split(" "));

                    Gweight gweight = new Gweight(main,text,cont,2);
                    //        System.out.println(gweight.toString());
                    Adistance adistance = new Adistance(attribute,gweight);

                    //query
                    while((line = stdin.readLine()) != null){
                        int queryid = Integer.parseInt(line);
                        System.out.println("Mp:"+queryMPath.toString()+",queryid:"+queryid);
                        long t1 = System.nanoTime();
                        baseline1_1 base = new baseline1_1(graph,vertexType,edgeType,null,attribute,adistance);
                        Set<Integer> result = base.query4(queryid,queryK,queryMPath,i);
                        long t2 = System.nanoTime();
                        //                System.out.println(result);
                        if(result!=null){
                            fw.write("queryId=" + "---" + queryid + "\r\n");
                            fw.write(dataReader.trans(result)+"\r\n");
                            fw.write(adistance.cal_subgraph_attr_dist(result)+"\r\n");
                            fw.write("time=" + (t2-t1) +"ns" +"\r\n");
                        }
                        else {
                            fw.write("queryId=" + "---" + queryid + "\r\n");
                            fw.write(" \r\n");
                            fw.write(" \r\n");
                            fw.write("time=" + (t2-t1) +"ns" +"\r\n");
                        }
                    }
                    fw.close();

                }catch (Exception e){
                    System.out.println(e);
                }
            }
//        }


    }

    public static int[] StringToInt(String[] arr){
        int[] array = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            array[i] = Integer.parseInt(arr[i]);
        }
        return array;
    }
}
