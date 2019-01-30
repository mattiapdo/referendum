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
import java.util.List;
import java.util.Map;

public class Kmeans 
        
{

    private int MaxIter;
    private double convergence_threshold;
    private int k;
    private char[][] centroids;
    
    private int original_size;
    private int alphabet_size;
    
    private char[][] sax;
    private int m;
    private int n;
    private char[] sax_alphabet;
	
    private double[][] lookup_table;
	
    
    // constructor
    Kmeans(char[][] sax, int k, int alphabet_size, int original_size, int MaxIter, double convergence_threshold) {
        
        // maximum number of iterations 
        this.MaxIter = MaxIter; 
        
        // convergence threshold to be tuned 
        //this.convergence_threshold = convergence_threshold;
        this.convergence_threshold = 0.1;  

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
        this.sax_alphabet = Arrays.copyOfRange(alphabet, 0, alphabet_size);
        
        //number of strings
        this.n = sax.length; 
        
        //length of each sax string 
        this.m = sax[0].length; 
        
        this.setLookUpTable();
        
    }
    
    
    public void setLookUpTable() {
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
        lookup_table = new double[alphabet_size][alphabet_size]; 

        Double[] breakpoints_arr = breakpoints.get(alphabet_size); 

        for(int i = 1; i<alphabet_size + 1 ; i++) { 					// i<= alphabet_size
            for(int j = 1; j < alphabet_size + 1 ; j ++ ) { 			// j<= alphabet_size
                if(Math.abs(i-j)<=1) {
                   lookup_table[i-1][j-1] = 0;
                } else {
                   lookup_table[i-1][j-1] = breakpoints_arr[(Math.max(i,j) - 2)] - breakpoints_arr[(Math.min(i, j) - 1)];
                }
            }
        }
    }
    
    
    public double[][] getLookupTable(){return this.lookup_table;}
    
    
    public char[][] initialize_centroids() {
        /*
        * randomly initilize centroids (i.e. medoids) 
        *
        * return a bidimensional array of random centroids 
        */ 

    // create array list object       
      List<Integer> list = new ArrayList<Integer>();
      
      // fill with integers from 0 to n-1 
      for(int i = 0; i < n; i++) { list.add(i); }
      
      // shuffle 
      Collections.shuffle(list); 
      
      // initialize centroids 
      char[][] centroids = new char[k][m]; 

   // for each cluster 
      for(int c = 0; c < k; c++) {
       // randomly sample without replacement a sax  
        centroids[c] = sax[(int) list.get(c)];

    }
       
      return centroids;
    }
    
    
    public double dist(char[] sax, char[] centroid) {
	      /*
	        * compute MinDist distance between two sax strings 
	        * 
	        * sax: single sax string 
	        * centroid : single centroid string 
	        *
	        * Return a double giving the distance between @sax and @centroid 
	        */ 
	     double norm_const = Math.sqrt(original_size / m);
	    
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
    
    
    public int[] update_partition(int[] partition) {
	    /*
	        * update clustering by assigning each sax to cluster with closest centtoid  
	        * partition : array giving the cluster memebership for each array 
	        *
	        * Return the array partition showing for each sax string the cluster it belongs to 
	    */ 
	          
	    //for each point 
	    for(int i=0; i<n; i++) {
	        
	       // initialize minimum distance 
	       double min = Double.POSITIVE_INFINITY;
	
	        //for each center 
	        for(int c=0; c<k; c++) {
	            
	            //compute distance
	           double d; 
	           d = dist(sax[i], centroids[c]);
	            
	                    if(d<min) {
	                        // update current minimum and partition array
	                        min = d; 
	                        partition[i] = c; 
	            }
	        }
	    }
	  return partition;   
	}
    

    public void update_centroid(int[] partition) {
     /*
        * update cluster centroids by computing medoids 
        * partition : array giving the cluster memebership for each array
        * 
        * Return updated cluster centroids 
    */ 
    	
   char median;
	
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
           Collections.sort(array_pos); // sort ArrayList<Character> ?
           
           // get median 
           try {
        	   median = (char) array_pos.get((int) Math.floor(array_pos.size()/2));		// {a,a,a,b,c,d,d,d,e,e,e,e,e,e,e,e,e,e,e,e,e,e,e} -> e
        	   // update centroid of cluster @c in position @i 
               centroids[c][i] = median; 
           }
           catch (Exception e) {
        	   //System.out.println(array_pos + "\n Non ho potuto calcolare mediana");
           }
           
           
           
          }
       }
   }
    
    
    public boolean check_convergence(char[][] old_centroids) {
       /*
       * check convergence : if all the cluster centroids are displaced by less than @convergence_threshold the method returns true 
       * old_centroids : previous centroids 
       * centrodis : current centroids 
       *
       * Return true if algorithm converged, false otherwise 
        */ 

       boolean converged = true; 
       
       double d; 
       // for each cluster 
       for(int c = 0; c < k ; c++) {
           
           //compute distance
           
           d = dist(old_centroids[c], centroids[c]);
         
           // check convergence for current cluster centroid
           if(d > convergence_threshold) {
               converged = false;
           }

       }
       return converged;
   }
    
        
    public int[] perform_clustering() {
        
        /*  Perform k-means clustering 
         * 
         * Return the array indicating the partition for each sax string 
         */
       
        // array storing cluster membership 
        int[] partition = new int[n]; 
        
        // centroids before update 
        char[][] old_centroids; 
        
        // randomly initialize centroids 
        centroids = initialize_centroids(); 
        
        // for each iteration 
        for(int iter = 0; iter< MaxIter; iter++) {
        	
        	System.out.println(Arrays.toString(partition));
        	
            // update clusters assigning each point to the cluster having the closest centroid 
            partition = update_partition(partition); 

            // centroids before updating for checking convergence 
            old_centroids = centroids; 
           
            // update cluster centroids 
            update_centroid(partition); 

            //check convergence 
            boolean converged = check_convergence(old_centroids);
            
            if(converged) {
            	System.out.println("\t.. kmeans convergend after " + (iter+1) + " iterations");
                break; 
            }

        }
        
        return partition;
        
     } 


}