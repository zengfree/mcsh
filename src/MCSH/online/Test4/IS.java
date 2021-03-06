package MCSH.online.Test4;

import MCSH.Config;
import MCSH.DataReader;
import MCSH.index.Nsw.MixIndex;
import MCSH.util.Adistance_float;
import MCSH.util.Gweight_float;
import MCSH.util.MetaPath;

import java.io.IOException;
import java.util.*;

public class IS {

    public static void main(String[] args) throws IOException {
      DataReader dataReader ;
        switch (args[3]){
            case "small":
                System.out.println("small");
                dataReader = new DataReader(Config.authorGraph, Config.authorVertex, Config.authorEdge, null,Config.authorattribute);
                break;
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

        int queryk = Integer.parseInt(args[0]);
        int queryN = Integer.parseInt(args[1]);
        String[] Mpath = args[2].split(";");
        int[] vertex = StringToInt(Mpath[0].split(","));
        int[] edge = StringToInt((Mpath[1].split(",")));
        MetaPath Path = new MetaPath(vertex, edge);


        int textnum = (int)attribute.get(-1)[0];
        int contnum = (int)attribute.get(-1)[1];
        int[] main = {1,1};
        int[] text = new int[textnum];
        int[] cont = new int[contnum];
        for(int i=0;i<textnum;i++){
            text[i] = 1;
        }
        for (int i = 0; i < contnum; i++) {
            cont[i] = 1;
        }

        Gweight_float gweight = new Gweight_float(main,text,cont,2);
        Adistance_float adistance= new Adistance_float(attribute,gweight);
        MixIndex mix = new MixIndex(graph,vertexType,edgeType,attribute,adistance);

        float[] f = new float[]{(float)0.1,(float)0.2,(float)0.3,(float)0.4,(float)0.5,(float)0.6,(float)0.7,(float) 0.8};

//        float[] f = new float[]{(float)0.2,(float)0.4,(float)0.6,(float)0.8,(float)1.0};
        for (int in = 0; in < f.length; in++) {
            float langda = f[in];
            System.out.println("langda"+f[in]);
            String datafile1 = args[3]+"/"+langda+Path.toString()+"data.txt";
            String idnexfile1 = args[3]+"/"+langda+Path.toString()+"index.n2";
            float sumpre = 0,sumrec=0;
            long sumt1 = 0,sumt2 = 0;
            int flag =0;
            while(flag<100){
//            Object[] list = attribute.keySet().toArray();
                Object[] list = mix.findK(Path,queryk).toArray();
                int queryid = (int)list[(int)(Math.random()*list.length)];
                System.out.println("queryid"+queryid);
                if(queryid==-1) continue;

                long t1 = System.nanoTime();
                MixIndex mixIndex = new MixIndex(graph,vertexType,edgeType,attribute,adistance);
//            long t13 = System.nanoTime();
//            mixIndex.build(Path,idnexfile1,datafile1,50,600);
                Set<Integer> set = mixIndex.search(queryid,queryk,queryN,datafile1,idnexfile1,langda);
                long t2 = System.nanoTime();
                System.out.println("nsw:"+(t2-t1));
//            System.out.println("nsw s:"+(t2-t13));
                sumt1 +=t2 -t1;

                long t3 = System.nanoTime();
                Map<Integer,Float> distancemap = new HashMap<>();
                Set<Integer> candiset = mixIndex.findK(Path,queryk);
                for (int nodeid:candiset){
                    distancemap.put(nodeid, adistance.cal_distance(queryid, nodeid));
                }

                List<Map.Entry<Integer, Float>> entryList = new ArrayList<>(distancemap.entrySet());
                entryList.sort(new Comparator<Map.Entry<Integer, Float>>() {
                    @Override
                    public int compare(Map.Entry<Integer, Float> o1, Map.Entry<Integer, Float> o2) {
                        return o1.getValue().compareTo(o2.getValue());//??????
//                return o2.getValue().compareTo(o1.getValue());//????????????
                    }
                });

                Set<Integer> keepSet2 = new HashSet<>();
                for (int i = 0; i < queryN; i++) {
                    int id = entryList.get(i).getKey();
                    keepSet2.add(id);
                    if(i== entryList.size()-1){
                        break;
                    }
                }
                long t4 = System.nanoTime();
                sumt2 += t4-t3;
                System.out.println("??????:"+(t4-t3));

                System.out.println("set:"+set.size());
//            System.out.println(dataReader.trans(set));
                System.out.println("truth:"+keepSet2.size());
//            System.out.println(dataReader.trans(keepSet2));
                Set<Integer> keepset3 = new HashSet<>(set);
                keepset3.retainAll(keepSet2);
                System.out.println("retain size:"+keepset3.size());

                float pre = (float) keepset3.size()/set.size();
                float rec = (float) keepset3.size()/queryN;
                sumpre +=pre;
                sumrec +=rec;
                System.out.println(rec);
                System.out.println(pre);
                flag++;
            }
            System.out.println(langda+",sumpre:"+sumpre/100);
            System.out.println(langda+",sumrec:"+sumrec/100);
            System.out.println(langda+",sumt1:"+sumt1/100);
            System.out.println(langda+",sumt2:"+sumt2/100);
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
