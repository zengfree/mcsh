package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixBasedSearch;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ilangda {
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

        String queryfile = Config.QueryFile+args[1];
        int queryK = Integer.parseInt(args[2]);
        int queryM = Integer.parseInt(args[3]);
        String[] Mpath = args[4].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        int n = Integer.parseInt(args[5]);
        float yu = Float.parseFloat(args[6]);
        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];
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

            float[] f = new float[]{(float)0.1,(float)0.2,(float)0.3,(float)0.4,(float)0.5,(float)0.6,(float)0.7,(float) 0.8};
            for (int in = 0; in < f.length; in++) {
                float langda = f[in];
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
                    int[] main = {1,1};
                    int[] text = new int[textnum];//查询社区
                    int[] text2 = new int[textnum];//计算度量值
                    int[] cont = new int[contnum];
                    for(int i = 0;i<textnum;i++){
                        if(attribute.get(queryid)[i]==1){
                            text[i] = 1;
                            text2[i] = 1;
                        }else {
                            text[i] = 0;
                            text2[i] = 1;
                        }
                    }
                    for(int i = 0;i<contnum;i++){
                        cont[i] = 1;
                    }
                    Gweight_float gweight = new Gweight_float(main, text2, cont, 2);
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
                System.out.println("langda:"+langda);
                System.out.println("num"+num);
                System.out.println("d1:"+avgd1/num);
//                System.out.println("d2:"+avgd2/num);
//                System.out.println("d3:"+avgd3/num);
//                System.out.println("d4:"+avgd4/num);
                System.out.println("t1:"+avgt1/num);
                System.out.println("dm1:"+avgdm1/num);
//                System.out.println("dm2:"+avgdm2/num);
//                System.out.println("dm3:"+avgdm3/num);
//                System.out.println("dm4:"+avgdm4/num);
                System.out.println("tm:"+avgtm/num);
                System.out.println("dmp1:"+avgdmp1/num);
//                System.out.println("dmp2:"+avgdmp2/num);
//                System.out.println("dmp3:"+avgdmp3/num);
//                System.out.println("dmp4:"+avgdmp4/num);
                System.out.println("tmp:"+avgtmp/num);
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
