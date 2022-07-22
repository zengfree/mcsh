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

public class langdan {
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
        int queryM = Integer.parseInt(args[3]);
        String[] Mpath = args[4].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        float yu = Float.parseFloat(args[5]);
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
            int[] nvalues=new int[]{400,450,500,550,600,650,700,750,800};
            for(int z=0;z<nvalues.length;z++){
                int n= nvalues[z];
                for (int in = 0; in < f.length; in++) {
                    float langda = f[in];
                    String datafile = args[0]+"/"+langda+queryMPath+"data.txt";
                    String idnexfile = args[0]+"/"+langda+queryMPath+"index.n2";

                    long avgt1 = 0;
                    float avgd1 = 0;
                    long avgtm = 0;
                    float avgdm1 = 0;
                    long avgtmp = 0;
                    float avgdmp1 = 0;
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
                            avgd1 +=d1;
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
                            avgdm1 +=d1;
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
                            avgdmp1 +=d1;
                        }
                        else {
                            System.out.println("queryid:"+queryid+" has no kcore");
                        }
                    }
                    System.out.println("langda:"+langda+",n"+n);
                    System.out.println("num"+num);
                    if(num>0){
                        System.out.println("d1:"+avgd1/num);
                        System.out.println("t1:"+avgt1/num);
                        System.out.println("dm1:"+avgdm1/num);
                        System.out.println("tm:"+avgtm/num);
                        System.out.println("dmp1:"+avgdmp1/num);
                        System.out.println("tmp:"+avgtmp/num);
                    }

                }
            }
//            for (int in = 0; in < f.length; in++) {
//                float langda = f[in];
//                String datafile = args[0]+"/"+langda+queryMPath+"data.txt";
//                String idnexfile = args[0]+"/"+langda+queryMPath+"index.n2";
//
//                long avgt1 = 0;
//                float avgd1 = 0,avgd2 = 0,avgd3 =0, avgd4=0;
//                long avgtm = 0;
//                float avgdm1 = 0,avgdm2 = 0,avgdm3 =0, avgdm4=0;
//                long avgtmp = 0;
//                float avgdmp1 = 0,avgdmp2 = 0,avgdmp3 =0, avgdmp4=0;
//                int num=0;
//                for (int queryid:querynodes) {
//                    int[] main = {1,1};
//                    int[] text = new int[textnum];//查询社区
//                    int[] text2 = new int[textnum];//计算度量值
//                    int[] cont = new int[contnum];
//                    for(int i = 0;i<textnum;i++){
//                        if(attribute.get(queryid)[i]==1){
//                            text[i] = 1;
//                            text2[i] = 1;
//                        }else {
//                            text[i] = 0;
//                            text2[i] = 1;
//                        }
//                    }
//                    for(int i = 0;i<contnum;i++){
//                        cont[i] = 1;
//                    }
//                    Gweight_float gweight = new Gweight_float(main, text2, cont, 2);
//                    Adistance_float adistance = new Adistance_float(attribute, gweight);
//                    System.out.println("Mp:"+queryMPath+",queryid:"+queryid);
//                    adistance.setYu(yu);
//                    long t1 = System.nanoTime();
//                    MixBasedSearch mix = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
//                    Set<Integer> result = mix.query(queryid,queryK,queryMPath,n,langda);
//                    long t2 = System.nanoTime();
//                    if(result!=null){
//                        num++;
//                        avgt1+=(t2-t1)/1e6;
//                        float d1 = adistance.cal_subgraph_attr_dist(result);//平均距离
//                        avgd1 +=d1;
//                    }
//                    else {
//                        System.out.println("queryid:"+queryid+" has no kcore");
//                    }
//
//                    long t3 = System.nanoTime();
////                    MixBasedSearch mix2 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
//                    Set<Integer> result2 = mix.queryM(queryid,queryK,queryMPath,n,queryM,langda);
//                    long t4 = System.nanoTime();
//                    if(result2!=null){
////                        num++;
//                        avgtm+=(t4-t3)/1e6;
//                        float d1 = adistance.cal_subgraph_attr_dist(result2);//平均距离
//                        avgdm1 +=d1;
//                    }
//                    else {
//                        System.out.println("queryid:"+queryid+" has no kcore");
//                    }
//
//                    long t5 = System.nanoTime();
////                    MixBasedSearch mix2 = new MixBasedSearch(graph,vertexType,edgeType,attribute,adistance,datafile,idnexfile);
//                    Set<Integer> result3 = mix.queryMP(queryid,queryK,queryMPath,n,queryM,langda);
//                    long t6 = System.nanoTime();
//                    if(result3!=null){
////                        num++;
//                        avgtmp+=(t6-t5)/1e6;
//                        float d1 = adistance.cal_subgraph_attr_dist(result3);//平均距离
//                        avgdmp1 +=d1;
//                    }
//                    else {
//                        System.out.println("queryid:"+queryid+" has no kcore");
//                    }
//                }
//                System.out.println("langda:"+langda+",n"+n);
//                System.out.println("num"+num);
//                System.out.println("d1:"+avgd1/num);
//                System.out.println("t1:"+avgt1/num);
//                System.out.println("dm1:"+avgdm1/num);
//                System.out.println("tm:"+avgtm/num);
//                System.out.println("dmp1:"+avgdmp1/num);
//                System.out.println("tmp:"+avgtmp/num);
//            }
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
