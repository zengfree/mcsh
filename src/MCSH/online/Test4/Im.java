package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixBasedSearch;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class Im {
    public static void main(String[] args) {
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

//        String queryfile = Config.QueryFile+args[1];
        String queryfile = args[1];
        int queryK = Integer.parseInt(args[2]);
//        int queryM = Integer.parseInt(args[3]);
        String[] Mpath = args[4].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        int n = Integer.parseInt(args[5]);
        float yu = Float.parseFloat(args[6]);
        float langda = Float.parseFloat(args[8]);
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
        try {
            //获取查询条件
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            stdin.close();

            int[] m = new int[10];
            for (int i = 0; i < 10; i++) {
                m[i] = (i+1)*2;
            }
            for (int in = 0; in < m.length; in++) {
                int queryM = m[in];
                String datafile = args[0]+"/"+langda+queryMPath+"data.txt";
                String idnexfile = args[0]+"/"+langda+queryMPath+"index.n2";

                long avgt1 = 0;
                float avgd1 = 0,avgd2 = 0,avgd3 =0, avgd4=0;
                long avgtm = 0;
                float avgdm1 = 0,avgdm2 = 0,avgdm3 =0, avgdm4=0;
                long avgtmp = 0;
                float avgdmp1 = 0,avgdmp2 = 0,avgdmp3 =0, avgdmp4=0;
                int num=0;
                for (int queryid:querynodes) {

                    Gweight_float gweight = new Gweight_float(main, text, cont, 2);
                    Adistance_float adistance = new Adistance_float(attribute, gweight);
                    System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
                    adistance.setYu(yu);
                    long t1 = System.nanoTime();
                    MixBasedSearch mix = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                    Set<Integer> result = mix.query(queryid,queryK,queryMPath,n,langda);
                    long t2 = System.nanoTime();
                    if(result!=null){
                        num++;
                        avgt1+=(t2-t1)/1e6;
                        float d1 = adistance.cal_subgraph_attr_dist(result);//平均距离
//                        float d2 = adistance.cal_maxdist(result);//最大距离
//                        float d3 = adistance.cal_fugailv(result,text);//覆盖率
//                        float d4 = adistance.maxnum(result,text);//最大共享数s
                        avgd1 +=d1;
//                        avgd2+=d2;avgd3+=d3;avgd4 +=d4;
//                        System.out.println("d1:"+d1);
//                        System.out.println("d2:"+d2);
//                        System.out.println("d3:"+d3);
//                        System.out.println("d4:"+d4);
                    }
                    else {
                        System.out.println("queryid:"+queryid+" has no kcore");
                    }

                    long t3 = System.nanoTime();
//                    MixBasedSearch mix2 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                    Set<Integer> result2 = mix.queryM(queryid,queryK,queryMPath,n,queryM,langda);
                    long t4 = System.nanoTime();
                    if(result2!=null){
//                        num++;
                        avgtm+=(t4-t3)/1e6;
                        float d1 = adistance.cal_subgraph_attr_dist(result2);//平均距离
//                        float d2 = adistance.cal_maxdist(result2);//最大距离
//                        float d3 = adistance.cal_fugailv(result2,text);//覆盖率
//                        float d4 = adistance.maxnum(result2,text);//最大共享数s
                        avgdm1 +=d1;
//                        avgdm2+=d2;avgdm3+=d3;avgdm4 +=d4;
//                        System.out.println("d1:"+d1);
//                        System.out.println("d2:"+d2);
//                        System.out.println("d3:"+d3);
//                        System.out.println("d4:"+d4);
                    }
                    else {
                        System.out.println("queryid:"+queryid+" has no kcore");
                    }

                    long t5 = System.nanoTime();
//                    MixBasedSearch mix2 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
                    Set<Integer> result3 = mix.queryMP(queryid,queryK,queryMPath,n,queryM,langda);
                    long t6 = System.nanoTime();
                    if(result3!=null){
//                        num++;
                        avgtmp+=(t6-t5)/1e6;
                        float d1 = adistance.cal_subgraph_attr_dist(result3);//平均距离
//                        float d2 = adistance.cal_maxdist(result3);//最大距离
//                        float d3 = adistance.cal_fugailv(result3,text);//覆盖率
//                        float d4 = adistance.maxnum(result3,text);//最大共享数s
                        avgdmp1 +=d1;
//                        avgdmp2+=d2;avgdmp3+=d3;avgdmp4 +=d4;
//                        System.out.println("d1:"+d1);
//                        System.out.println("d2:"+d2);
//                        System.out.println("d3:"+d3);
//                        System.out.println("d4:"+d4);
                    }
                    else {
                        System.out.println("queryid:"+queryid+" has no kcore");
                    }
                }
                if(num>0){
                    System.out.println("d1:"+avgd1/num);
                    System.out.println("t1:"+avgt1/num);
                    System.out.println("dm1:"+avgdm1/num);
                    System.out.println("tm:"+avgtm/num);
                    System.out.println("dmp1:"+avgdmp1/num);
                    System.out.println("tmp:"+avgtmp/num);
                }
            }
        }catch (IOException ioException ) {
            System.out.println(ioException);
            System.out.println("sleep error");
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
