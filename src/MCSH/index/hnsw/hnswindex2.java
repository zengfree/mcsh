package MCSH.index.hnsw;

import java.util.List;

public class hnswindex2 {
    public hnswindex2(){super();}

    //q:查询节点id，K：返回的结果数量，ef：查询参数（需要>K）,datasetfile:查询文件，indexfile：hnsw索引文件，vector：偏好向量，textnum：文本属性数，contnum：连续属性数
    //该函数并未完全实现
    public native int[] search(int q,int K,int ef,String datasetfile,String indexfile ,float[] vector,int textnum,int contnum);

    //qvec:查询向量，K：返回的结果数量，ef：查询参数（需要>K），indexfile：hnsw索引文件，vector：偏好向量，textnum：文本属性数，contnum：连续属性数
    public native int[] search(float[] qvec,int K,int ef,String indexfile, float[] vector,int textnum,int contnum);

    //构造向量文件，hnsw索引文件，vector：偏好向量，textnum：文本属性数，contnum：连续属性数，M：最大连边数，ef：参数，需>M值
    public native void build(String datafile,String modelfile,float[] vector,int textnum,int contnum,int M,int ef);

    public native void increbuild(String datafile,String modelfile1,String modelfile_new,int startid,float[] vector,int textnum,int contnum,int M,int ef);

    //未实现
    public native void build(List<float[]> data,String modelfile,float[] vector,int textnum,int contnum,int M,int ef);

    static {
        System.loadLibrary("MCSH_index_hnsw_hnswindex2");
    }

//    public static void main(String[] argss){
//        String datafile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/indextest.txt";
//        String idnexfile = "/home/hadoop/MCSH/CSH/CSHDS/DBLP/index.n2";
//        hnswindex index = new hnswindex();
//
//        int[] main = {1,1};hnswindex
//        int[] text = new int[220];
//        int[] cont = {1,1};
//        for(int i=0;i<220;i++){
//            text[i] = 1;
//        }
//
//        List<float[]> floats = new ArrayList<>();
//
//        Gweight_float gweight = new Gweight_float(main,text,cont,2);
////        index.build(floats,idnexfile, gweight.getbuildweight(), 220,2,10,300);
//        //M 默认50  ef 500
////        index.build(datafile,idnexfile, gweight.getbuildweight(), 220,2,10,500);
//
//        DataReader dataReader = new DataReader(Config.dblpGraph, Config.dblpVertex, Config.dblpEdge, null,Config.dblpattributed);
//        Map<Integer,double[]> attribute = dataReader.readattributed();
//        double[] searchvec = attribute.get(6);
//        float[] floats1 = new float[searchvec.length];
//        for(int i=0;i<searchvec.length;i++){
//            floats1[i]=(float) searchvec[i];
//        }
//
//        int m[] = index.search(floats1,1000,2000,idnexfile,gweight.getbuildweight(),220,2);
////        System.out.println(m.length);
//    }
}
