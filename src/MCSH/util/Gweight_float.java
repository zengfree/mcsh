package MCSH.util;

import java.util.Arrays;

import static java.lang.Math.pow;

/**
 * Generate weight vector
 */
public class Gweight_float {
    private float parameter;
    private float[] weights_vector_main;
    private float[] weights_vector_text;
    private float[] weights_vector_continuous;
    private int textnum;
    private int contnum;

    public Gweight_float(int[] preference_vector_main, int[] preference_vector_text, int[] preference_vector_continuous, float parameter) {
        this.parameter = parameter;
        this.weights_vector_main = generatorvector(preference_vector_main);
        this.weights_vector_text = generatorvector(preference_vector_text);
        this.weights_vector_continuous = generatorvector(preference_vector_continuous);
        this.textnum = preference_vector_text.length;
        this.contnum = preference_vector_continuous.length;
    }

    public int getTextnum() {
        return textnum;
    }

    public int getContnum() {
        return contnum;
    }

    private float[] generatorvector(int[] vector){
        float sum = 0;
        float[] result = new float[vector.length];
        for(int i=0;i< vector.length;i++){
            sum += 1/pow(vector[i], this.parameter);
        }
        for(int i=0;i< vector.length;i++){
            result[i] = (float) ((1/pow(vector[i],this.parameter))/sum);
        }
        return result;
    }

    @Override
    public String toString() {
        return "Gweight{" +
                "weights_vector_main=" + Arrays.toString(weights_vector_main) +
                ", weights_vector_text=" + Arrays.toString(weights_vector_text) +
                ", weights_vector_continuous=" + Arrays.toString(weights_vector_continuous) +
                '}';
    }

    public float[] getWeights_vector_main() {
        return weights_vector_main;
    }

    public float[] getWeights_vector_text() {
        return weights_vector_text;
    }

    public float[] getWeights_vector_continuous() {
        return weights_vector_continuous;
    }

    public float[] getWeights_vector() {
        int weightlength = this.weights_vector_text.length+this.weights_vector_continuous.length;;
        int startindex = 0;
        float[] weight = new float[weightlength];
        System.arraycopy(this.weights_vector_text,0,weight,startindex,this.weights_vector_text.length);
        startindex = startindex + this.weights_vector_text.length;
        System.arraycopy(this.weights_vector_continuous,0,weight,startindex,this.weights_vector_continuous.length);

        return weight;
    }

    public float[] getbuildweight(){
        int weightlength = this.weights_vector_main.length+this.weights_vector_text.length+this.weights_vector_continuous.length;;
        int startindex = 0;
        float[] buildweight = new float[weightlength];
        System.arraycopy(this.weights_vector_main,0,buildweight,startindex,this.weights_vector_main.length);
        startindex = startindex + this.weights_vector_main.length;
        System.arraycopy(this.weights_vector_text,0,buildweight,startindex,this.weights_vector_text.length);
        startindex = startindex + this.weights_vector_text.length;
        System.arraycopy(this.weights_vector_continuous,0,buildweight,startindex,this.weights_vector_continuous.length);

        return buildweight;
    }
//    public static void main(String[] args){
////        int[] main = {1,1};
////        int[] text = {1,3,1,2};
////        int[] cont = {1,3,1,2};
////        Gweight_float gweight_float = new Gweight_float(main,text,cont,2);
////        System.out.println(gweight_float.toString());
////
////        Gweight gweight = new Gweight(main,text,cont,2);
////        System.out.println(gweight.toString());
//    }

}
