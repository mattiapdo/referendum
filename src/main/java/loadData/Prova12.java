/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package loadData;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import it.stilo.g.algo.ConnectedComponents;
import it.stilo.g.algo.HubnessAuthority;
import it.stilo.g.algo.SubGraph;
import it.stilo.g.structures.DoubleValues;
import it.stilo.g.structures.WeightedDirectedGraph;
import it.stilo.g.util.NodesMapper;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.queryparser.classic.ParseException;
import twitter4j.TwitterException;

/**
 *
 * @author sanch
 */
public class Prova12 {
    
    @SuppressWarnings("resource")
	public static void main(String[] args) throws FileNotFoundException, IOException, TwitterException, ParseException, InterruptedException{
    	
    	long startTime = System.currentTimeMillis();
    	
        // read file of users id and put themi into users HashSet
    	System.out.println("Reading file of users id and put themi into users HashSet");
        HashSet<Long> retrieved_users = new HashSet<>(); 
        String filePath = "output\\userIds.txt";
        BufferedReader br = null;
		FileReader fr = null;

		fr = new FileReader(filePath);
		br = new BufferedReader(fr);

		String sCurrentLine;

		while ((sCurrentLine = br.readLine()) != null) {
			retrieved_users.add(Long.parseLong(sCurrentLine));
		}
		br.close();
		fr.close();
		
		System.out.println("Collected user ids... they are " + retrieved_users.size());
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - startTime) + " msec"); 
		long now = System.currentTimeMillis();
		
        NodesMapper<Long> mapper = new NodesMapper<>();
        
 		// read file containing the graph and create the graph	
		System.out.println("Reading file containing the graph and create the graph");
		WeightedDirectedGraph g = new WeightedDirectedGraph(retrieved_users.size()+1);
        filePath = "data\\test-dataset-sbn2017\\Official_SBN-ITA-2016-Net";
		fr = new FileReader(filePath);
		br = new BufferedReader(fr);

		while ((sCurrentLine = br.readLine()) != null) {
			String[] splittedLine = sCurrentLine.split("\t");
			Long source = Long.parseLong(splittedLine[0]);
			Long destination = Long.parseLong(splittedLine[1]);
			Integer weight = Integer.parseInt(splittedLine[2]);
			// if both nodes are in our users, add source and destination
			if(retrieved_users.containsAll(Arrays.asList(source, destination))) {
				g.testAndAdd(mapper.getId(source), mapper.getId(destination), weight);
			}
		}
		br.close();
		fr.close();
		
		System.out.println("Built graph... size = " + g.size);
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now) + " msec");
		now = System.currentTimeMillis();
		 
        System.out.println("Creating roots array to find connected components");
        // collect ids of the users as roots to be used to find largest connected component
 		int i = 0;
 		int[] roots = new int[retrieved_users.size()];
 		for(long user: retrieved_users) {
 			roots[i] = mapper.getId(user);
 			i++;
 		}
 		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now) + " msec");
		now = System.currentTimeMillis();
 		
 		System.out.println("Finding largest connected component - LCC...");
 		int numThreads = 4;
 		Set<Set<Integer>> connComponents = ConnectedComponents.rootedConnectedComponents(g, roots, numThreads);
 		
 		int maxCCsize = -1;
 		Set<Integer> LCC = new HashSet<>();
		for(Set<Integer> component: connComponents) {
			if(component.size()>maxCCsize) {
				LCC = component;
			}
		}
		
		int[] LCC_arr = new int[LCC.size()]; 
		int j = 0;
		for(Integer node: LCC) {
			LCC_arr[j] = node;
			j++;
		}
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now) + " msec");
		now = System.currentTimeMillis();
		
		System.out.println("Finding largest connected components");
		WeightedDirectedGraph g_LCC = SubGraph.extract(g, LCC_arr, numThreads);		
		System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now) + " msec");
		now = System.currentTimeMillis();
		
		System.out.println("Running HITS ...");
		ArrayList<ArrayList<DoubleValues>> authorities = HubnessAuthority.compute(g_LCC, 0.00001, numThreads); //graph, precision and number of cores
        ArrayList<DoubleValues> authorityScore =  authorities.get(0); //
        ArrayList<DoubleValues> hubnessScore =  authorities.get(1); 
        System.out.println("Elapsed time: " + (long) (System.currentTimeMillis() - now) + " msec");
		
        System.out.println("Total elapsed time: " + (long) (System.currentTimeMillis() - startTime) + " msec");

		}// end of main

    }

       