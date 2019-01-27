/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Kmeans 
        
{

    private final int MaxIter;
    private final char[][] sax;
    private final int k;
    private final int original_size;
    private final int alphabet_size;
    
    // constructor
    public Kmeans(char[][] sax, int k, int alphabet_size, int original_size, int MaxIter) {
        
        // maximum number of iterations 
        this.MaxIter = MaxIter; 

        // sax strings array 
        this.sax = sax; 
        
        // number of clusters 
        this.k = k; 
        
        //length of the original time series 
        this.original_size = original_size; 
        
        // size of the alphabet 
        this.alphabet_size = alphabet_size; 

        // array of distinct sax symbols 
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        char[] sax_alphabet = Arrays.copyOfRange(alphabet, 0, alphabet_size);
        
        //number of strings
        int n = sax.length; 
        
        //length of each sax string 
        int m = sax[0].length; 
        
        //Create table for distance (MINDIST) computation 
        
        //sax breakpoints 
        Map<Integer, Double[]> breakpoints = new HashMap<Integer, Double[]>();
        breakpoints.put(3, new Double[] {-0.43,0.43}); 
        breakpoints.put(4, new Double[] {-0.67,0.0, 0.067}); 
        breakpoints.put(5,new Double[] {-0.84,-0.25, 0.25, 0.84}); 
        breakpoints.put(6,new Double[] {-0.97,-0.43,0.0, 0.43, 0.97}); 
        breakpoints.put(7,new Double[] {-1.07, -0.57, -0.18, 0.18, 0.57, 1.07}); 
        breakpoints.put(8,new Double[] {-1.15, -0.67, -0.32, 0.0, 0.32, 0.67, 1.15}); 
        breakpoints.put(9,new Double[] {-1.22, -0.76, -0.43, -0.14, 0.14, 0.43, 0.76, 1.22}); 
        breakpoints.put(10,new Double[] {-1.28, -0.84, -0.52, -0.25, 0.0, 0.25, 0.52, 0.84, 1.28});
        
        //lookup table for distance
        double[][] lookup_table = new double[alphabet_size][alphabet_size]; 

        Double[] breakpoints_arr = breakpoints.get(alphabet_size); 

        for(int i = 1; i<=alphabet_size; i++) {
            for(int j = 1; j <= alphabet_size; j ++ ) {
                if(Math.abs(i-j)<=1) {
                   lookup_table[i][j] = 0;
                } else {
                   lookup_table[i][j] = breakpoints_arr[(Math.max(i,j) - 2)] - breakpoints_arr[(Math.min(i, j) - 1)];
                }
            }
        }
        
        
    }
    
    
    public static char[][] initialize_centroid(int m, int k, char[] sax_alphabet) {
        /*
        * randomly initilize centroids (i.e. medoids) 
        * m : sax length 
        * k : number of clusters 
        * sax_alphabet : array of distinct sax symbols 
        * return a bidimensional array of random centroids 
        */ 
        Random random = new Random();
        
         //initilize centroids array (each row represent the centroid of a cluster) 
        char[][] centroids = new char[k][m]; 
        
        // for each cluster 
        for(int c = 0; c< k; c++) {
            
            // for each character position 
            for(int i = 0; i<m; i++) {
         
                // randomly sample a character 
                centroids[c][i] = sax_alphabet[ random.nextInt(sax_alphabet.length) ];
            
        }
            
        }
        
   
        return centroids;
    }
    
    
    public static double dist(double[][] lookup_table, char[] sax_alphabet,  int original_len, int m, int alphabet_size, char[] sax, char[] centroid) {
	      /*
	        * compute MinDist distance between two sax strings 
	        * lookup_table : table for character distance lookup 
	        * sax_alphabet : array of distinct sax symbols 
	        * original_len : length of the original time series  
	        * m : sax length 
	        * alphabet_size : size of the alphabet 
	        * sax: single sax string 
	        * centroid : single centroid string 
	        *
	        * Return a double giving the distance between @sax and @centroid 
	        */ 
	     double norm_const = Math.sqrt(original_len / m);
	    
	     double total_sum = 0; 
	     
	     // for each position 
	     for(int p = 0; p < m; p++) {
	         
	         // get table indices 
	         int i = new String(sax_alphabet).indexOf(sax[p]);
	         int j = new String(sax_alphabet).indexOf(centroid[p]);
	         
	         //update distance 
	         total_sum += lookup_table[i][j]; 
	         
	     }
	 
	     // compute distance 
	     double dist = norm_const * Math.sqrt(total_sum);  
	     
	     return dist; 
	 }
    
    
    public static int[] update_partition(double[][] lookup_table, char[] sax_alphabet,  int original_len, int m, int alphabet_size, char sax[][], char[][] centroids, int[] partition, int n, int k) {
	    /*
	        * update clustering by assigning each sax to cluster with closest centtoid  
	        * lookup_table : table for character distance lookup 
	        * sax_alphabet : array of distinct sax symbols 
	        * original_len : length of the original time series  
	        * m : sax length 
	        * alphabet_size : size of the alphabet 
	        * sax: bidemensional array of (all) sax strings 
	        * centroids : bidimensional array with all centroids 
	        * partition : array giving the cluster memebership for each array 
	        * n : number of sax strings 
	        * k : number of clusters 
	        *
	        * Return the array @partition showing for each sax string the cluster it belongs to 
	    */ 
	          
	    //for each point 
	    for(int i=0; i<n; i++) {
	        
	       // initialize minimum distance 
	       double min = Double.POSITIVE_INFINITY;
	
	        //for each center 
	        for(int c=0; c<k; c++) {
	            
	            //compute distance
	           double d; 
	           d = dist(lookup_table, sax_alphabet,  original_len,  m, alphabet_size, sax[i], centroids[c]);
	            
	                    if(d<min) {
	                        // update current minimum and @partition array
	                        min = d; 
	                        partition[i] = c; 
	            }
	        }
	    }
	  return partition;   
	}
    

    public static char[][] update_centroid(char[][] sax, char[][] centroids, int[] partition, int m, int n, int k, char[] sax_alphabet) {
     /*
        * update cluster centroids by computing medoids 
        * sax: bidemensional array of (all) sax strings 
        * centroids : bidimensional array with all centroids 
        * partition : array giving the cluster memebership for each array
        * m : sax length 
        * n : number of sax strings 
        * k : number of clusters 
        * sax_alphabet : array of distinct sax symbols 
        * 
        * Return updated cluster centroids 
    */ 

       // for each cluster 
       for(int c = 0; c<k; c++) {

          //for each character position  
          for(int i = 0; i<m; i ++) {

              //initialize array 
              ArrayList<Character> array_pos = new ArrayList<Character>();

              //for each sax string in the dataset 
              for(int j = 0; j< n ; j++ ) {

                  // create array with all characters n position @i for sax strings belonging to cluster @c 
                  if(partition[j] == c) {
                   array_pos.add(sax[j][i]);    
                  }
        
              }
              // sort the array 
               Collections.sort(array_pos); 
               char median;
               // get median 
               median = (char) array_pos.get((int) Math.floor(array_pos.size()/2));
               
               // update centroid of cluster @c in position @i 
               centroids[c][i] = median; 

          }
        

       }
       return centroids;
   }
    
    
    public static boolean check_convergence(double[][] lookup_table, char[] sax_alphabet,  int original_len, int m, int alphabet_size, int k, char[][] old_centroids, char[][] centroids ) {
       /*
       * check convergence : if all the cluster centroids are displaced by less than @convergence_threshold the method returns true 
       * lookup_table :  table for character distance lookup 
       * sax_alphabet : array of distinct sax symbols 
       * original_len : length of the original time series  
       * m : sax length 
       * alphabet_size : size of the alphabet 
       * k : number of clusters 
       * old_centroids : previous centroids 
       * centrodis : current centroids 
        
       * Return true if algorithm converged, false otherwise 
        
        
        */ 

       boolean converged = true; 
       
       double convergence_threshold = 0.1;  // to be tuned 
       
       // for each cluster 
       for(int c = 0; c < k ; c++) {
           
           //compute distance
           double d; 
           d = dist(lookup_table, sax_alphabet,  original_len,  m, alphabet_size, old_centroids[c], centroids[c]);
         
           // check convergence for current cluster centroid
           if(d > convergence_threshold) {
               converged = false;
           }

       }
       return converged;
   }
    
        
    public static int[] perform_clustering(int MaxIter, double[][] lookup_table, int original_len, int alphabet_size,  char[][] sax, int k, int n, int m, char[] sax_alphabet) {
        
        /*  Perform k-means clustering 
         * MaxIter : maximum number of iteration 
         * lookup_table :  table for character distance lookup
         * original_len : length of the original time series  
         * alphabet_size : size of the alphabet
         * sax : array containing sax strings 
         *  k : number of clusters 
         *  n : number of sax strings 
         *  m : string length 
         *  sax : 2D array 
         *  sax_alphabet : alphabet of sax strings 
         * 
         * Return the array indicating the partition for each sax string 
         */
       
        // array storing cluster membership 
        int[] partition = new int[n]; 
        
        // centroids before update 
        char[][] old_centroids; 
        
        // randomly initialize centroids 
        char[][] centroids = initialize_centroid( m,  k, sax_alphabet); 
        
        // for each iteration 
        for(int iter = 0; iter< MaxIter; iter++) {

            // update clusters assigning each point to the cluster having the closest centroid 
            partition = update_partition( lookup_table, sax_alphabet, original_len, m, alphabet_size,  sax, centroids,  partition,  n,  k); 

            // centroids before updating for checking convergence 
            old_centroids = centroids; 
           
            // update cluster centroids 
            centroids =  update_centroid(sax, centroids, partition, m, n, k, sax_alphabet); 

            //check convergence 
            boolean converged = check_convergence(lookup_table, sax_alphabet,  original_len, m,  alphabet_size, k, old_centroids,  centroids);
            if(converged) {
                break; 
            }

        }
        
        return partition;
        
     } 


}