package com.aos.config;

import java.util.LinkedList;
import java.util.Queue;

public class SpanningTree {

	public static int[] parent;
	
	public static void initParent(int numNodes){
		parent = new int[numNodes];
	}
	
	public static void setParent(String nodeID, String parentID){
		parent[Integer.parseInt(nodeID)] = Integer.parseInt(parentID);
	}
	
	public static int getParent(int id) {
		return parent[id];
	}
	
	public static String getParent(String id){
		return Integer.toString(parent[Integer.parseInt(id)]);
	}
	public static void buildSpanningTree(boolean[][] adjMatrix){
		boolean[] visited = new boolean[adjMatrix.length];
		parent = new int[adjMatrix.length];
		Queue<Integer> queue = new LinkedList<Integer>();
		queue.add(0);
		parent[0] = 0;
		//If its already visited then no need to visit again since its done in bfs tree , nodes 
		//visited at first level will have direct parents and so on
		visited[0] = true;
		while(!queue.isEmpty()){
			int edge = queue.remove();
			for(int i = 0; i < adjMatrix[edge].length; i++){
				if(adjMatrix[edge][i] && !visited[i] ){
					queue.add(i);
					SpanningTree.parent[i] = edge;
					visited[i] = true;
				}
			}
		}
	}
	
	public static void print2DMatrix(int numNodes, boolean[][] adjMatrix){
		for(int i = 0; i < numNodes; i++){
			for(int j = 0; j < numNodes; j++){
				if( adjMatrix[i][j] )
					System.out.print("1 ");
				else
					System.out.print("0 ");
			}
			System.out.println();
		}
	}
}
