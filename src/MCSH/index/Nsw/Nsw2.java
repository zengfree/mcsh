package MCSH.index.Nsw;

public class Nsw2 {
    public native int[] search(float[] qvec,int queryK,int N,int ef,String indexfile, float[] vector,int textnum,int contnum,float langda);

    public native void build(String datafile,String modelfile,float[] vector,int textnum,int contnum,int M,int ef,float langda);

    static {
        System.loadLibrary("MCSH_index_Nsw_Nsw2");
    }

//    public static void main(String[] args) {
//        System.out.println("not");
//    }
}
