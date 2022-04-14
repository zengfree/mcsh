package MCSH.index.Nsw;

public class Nsw {
    //qvec:查询向量，queryk:查询K值,N：返回的结果数量，ef：查询参数（需要>N），indexfile：hnsw索引文件，vector：偏好向量，textnum：文本属性数，contnum：连续属性数
    public native int[] search(float[] qvec,int queryK,int N,int ef,String indexfile, float[] vector,int textnum,int contnum);

    //数据文件，vector：偏好向量，textnum：文本属性数，contnum：连续属性数，M：最大连边数，ef：参数，需>M值
    public native void build(String datafile,String modelfile,float[] vector,int textnum,int contnum,int M,int ef);

    static {
        System.loadLibrary("MCSH_index_Nsw_Nsw");
    }

//    public static void main(String[] args) {
//        System.out.println("not");
//    }
}
