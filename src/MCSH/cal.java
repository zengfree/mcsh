package MCSH;

public class cal {
    public static void main(String[] args) {
//        float x = 76;
        float[] x = {51,36,41,44,38,47,42,43,46};
        int max = 51,min = 36;
        for (int i = 0; i <x.length ; i++) {
            float y = (x[i]-min)/(max - min);
            System.out.println(y);
        }
//        System.out.println(Math.pow(0,2));

    }
}
