
package unimelb.farrugiulian.hexifence.board;

public class Cell extends Index {
	
	private Edge[] edges = new Edge[6];
	private int nedges = 0;

	public Cell(int i, int j){
		super(i,j);
	}
	
	public void link(Edge edge) {
		edges[nedges++] = edge;
	}

	public int numEmptyEdges(){
		int n = 0;
		
		// loop and count edges that are still free
		for(Edge edge : edges){
			if(edge.isEmpty()){
				n++;
			}
		}
		return n;
	}
	
	public Edge[] getEdges() {
		return edges;
	}
	
	public Edge[] getEmptyEdges(){
		
		Edge[] edges = new Edge[this.numEmptyEdges()];
		int n = 0;
		
		for(Edge edge : this.edges){
			if(edge.isEmpty()){
				edges[n++] = edge;
			}
		}
		
		return edges;
	}
}
