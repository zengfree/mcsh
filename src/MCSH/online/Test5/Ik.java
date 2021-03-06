package MCSH.online.Test5;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixBasedSearch;
import MCSH.online.basic.Base_logmap2;
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

public class Ik {
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
        int K = Integer.parseInt(args[2]);
        int queryM = Integer.parseInt(args[3]);
        String[] Mpath = args[4].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath queryMPath = new MetaPath(vertex, edge);
        float yu = Float.parseFloat(args[5]);
        int add = Integer.parseInt(args[6]);
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
            //??????????????????
            BufferedReader stdin = new BufferedReader(new FileReader(queryfile));
            String line;

            Queue<Integer> querynodes = new LinkedList<>();
            while((line = stdin.readLine()) != null){
                int queryid = Integer.parseInt(line);
                querynodes.add(queryid);
            }
            stdin.close();

            int[] k = new int[5];
            for (int i = 0; i < 5; i++) {
                k[i] = K+i*add;
            }
            for (int in = 0; in < k.length; in++) {
                int queryK = k[in];

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
                    Base_logmap2 app = new Base_logmap2(graph,vertexType,edgeType,attribute,adistance);
				    Set<Integer> result = app.query(queryid,queryK,queryMPath);
                    long t2 = System.nanoTime();
                    if(result!=null){
                        num++;
                        avgt1+=(t2-t1)/1e6;
                        float d1 = adistance.cal_subgraph_attr_dist(result);//????????????
//                        float d2 = adistance.cal_maxdist(result);//????????????
//                        float d3 = adistance.cal_fugailv(result,text);//?????????
//                        float d4 = adistance.maxnum(result,text);//???????????????s
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
                    Set<Integer> result2 = app.queryM(queryid,queryK,queryMPath,queryM);
                    long t4 = System.nanoTime();
                    if(result2!=null){
//                        num++;
                        avgtm+=(t4-t3)/1e6;
                        float d1 = adistance.cal_subgraph_attr_dist(result2);//????????????
//                        float d2 = adistance.cal_maxdist(result2);//????????????
//                        float d3 = adistance.cal_fugailv(result2,text);//?????????
//                        float d4 = adistance.maxnum(result2,text);//???????????????s
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
                    Set<Integer> result3 = app.queryM_protect(queryid,queryK,queryMPath,queryM);
                    long t6 = System.nanoTime();
                    if(result3!=null){
//                        num++;
                        avgtmp+=(t6-t5)/1e6;
                        float d1 = adistance.cal_subgraph_attr_dist(result3);//????????????
//                        float d2 = adistance.cal_maxdist(result3);//????????????
//                        float d3 = adistance.cal_fugailv(result3,text);//?????????
//                        float d4 = adistance.maxnum(result3,text);//???????????????s
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
                System.out.println("k:"+queryK);
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
