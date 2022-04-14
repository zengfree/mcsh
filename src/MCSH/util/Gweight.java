package MCSH.util;

import java.util.Arrays;

import static java.lang.Math.pow;

/**
 * Generate weight vector
 */
public class Gweight {
    private double parameter;
    private double[] weights_vector_main;
    private double[] weights_vector_text;
    private double[] weights_vector_continuous;

    public Gweight(int[] preference_vector_main, int[] preference_vector_text, int[] preference_vector_continuous, double parameter) {
        this.parameter = parameter;
        this.weights_vector_main = generatorvector(preference_vector_main);
        this.weights_vector_text = generatorvector(preference_vector_text);
        this.weights_vector_continuous = generatorvector(preference_vector_continuous);
    }

    private double[] generatorvector(int[] vector){
        double sum = 0;
        double[] result = new double[vector.length];
        for(int i=0;i< vector.length;i++){
            sum += 1/pow(vector[i], this.parameter);
        }
        for(int i=0;i< vector.length;i++){
            result[i] = (1/pow(vector[i],this.parameter))/sum;
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

    public double[] getWeights_vector_main() {
        return weights_vector_main;
    }

    public double[] getWeights_vector_text() {
        return weights_vector_text;
    }

    public double[] getWeights_vector_continuous() {
        return weights_vector_continuous;
    }

//    public static void main(String[] args){
//        int[] main = {1,1};
//        int[] text = {1,1,1,1};
//        int[] cont = {1,3,1,3};
//        Gweight gweight = new Gweight(main,text,cont,2);
//        System.out.println(gweight.toString());
//    }

}
