import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;

public class LinkState {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter the number of routers: ");
		int routerCount = Integer.parseInt(scanner.nextLine());

		System.out.println("Enter the rows: ");
		List<String> rows = new ArrayList<String>();
		for (int i = 0; i < routerCount; i++) {
			rows.add(scanner.nextLine());
		}
		System.out.println("Enter the gateway routers: ");
		String gatewayRouters = scanner.nextLine();
		scanner.close();
		String[] values;
		String[] gatewayRouter;
		gatewayRouter = gatewayRouters.split(" ");

		List<RouterNode> nodes = new ArrayList<RouterNode>();
		for (int i = 0; i < routerCount; i++) {
			RouterNode temp = new RouterNode();
			nodes.add(temp);
		}

		for (int i = 0; i < routerCount; i++) {
			String row = rows.get(i);
			values = row.split(" ");
			for (int cur = 0; cur < routerCount; cur++) {
				int cost = Integer.parseInt(values[cur]);
				if (cost != -1)
					nodes.get(i).addEdge(new GraphEdge(nodes.get(cur), (cost)));
			}
		}
		for (RouterNode source : nodes) {
			resetDistances(nodes);
			ShortestPaths(source, nodes);
			int gr_count = 0;

			List<ArrayList<RouterNode>> totalPaths = new ArrayList<ArrayList<RouterNode>>();
			Queue<RouterNode> destinations = new LinkedList<RouterNode>();

			for (RouterNode destination : nodes) {
				if (destination == source)
					continue;

				List<RouterNode> shortestPath = getShortestPath(destination);
				totalPaths.add((ArrayList<RouterNode>) shortestPath);
				destinations.add(destination);
				if(destination._name == Integer.parseInt(gatewayRouter[0])  || destination._name == Integer.parseInt(gatewayRouter[1]))
					gr_count++;
			}
			if(gr_count == 2) {
				printForwardingTableForList(totalPaths, destinations, source, gatewayRouter);
			}
		}
	}

	private static void printForwardingTableForList(List<ArrayList<RouterNode>> totalPaths, Queue<RouterNode> destinations, RouterNode source, String[] gatewayRouter) {	
		System.out.println("Forwarding Table for " + source);
		System.out.format("%15s%15s%15s\n", new Object[] { "To", "Cost", "Next Hop" });
		
		for (ArrayList<RouterNode> list : totalPaths) {
			RouterNode destination = destinations.remove();
			if(destination._name == Integer.parseInt(gatewayRouter[0]) || destination._name == Integer.parseInt(gatewayRouter[1])) {
				int cost = destination.smallestCost;
				System.out.format("%15s", destination);
				
	
				if (list.size() > 0) {
					list.add(destination);
					System.out.format("%15s", cost);
					System.out.format("%15s", list.get(1));
				} else {
					System.out.format("%15s", "--");
					System.out.format("%15s", "-1");
				}
				System.out.println();
			}
		}
	}

	private static List<RouterNode> getShortestPath(RouterNode destination) {
		List<RouterNode> order = new ArrayList<RouterNode>();
		RouterNode current = destination.previous;

		while (current != null) {
			order.add(current);
			if (current.previous != null)
				current = current.previous;
			else
				current = null;
		}
		Collections.reverse(order);

		return order;
	}

	private static void resetDistances(List<RouterNode> nodes) {
		for (RouterNode node : nodes) {
			node.smallestCost = Integer.MAX_VALUE;
			node.previous = null;
		}
	}

	private static void ShortestPaths(RouterNode source, List<RouterNode> nodes) {
		source.smallestCost = 0;
		PriorityQueue<RouterNode> p = new PriorityQueue<RouterNode>();
		p.add(source);

		while (!p.isEmpty()) {
			RouterNode smallestNode = p.poll();
			for (GraphEdge e : smallestNode.getEdges()) {

				RouterNode neighbour = e.getTo();
				int alt = smallestNode.smallestCost + e.getCost();

				if (alt < neighbour.smallestCost) {
					p.remove(neighbour);
					neighbour.smallestCost = alt;
					neighbour.previous = smallestNode;
					p.add(neighbour);
				}
			}
		}
	}

	public static class WeightedDirectedGraph {
		private List<RouterNode> _nodes = new ArrayList<RouterNode>();
	}

	public static class GraphEdge {
		private final RouterNode _to;
		
		private final int _cost;
		
		public RouterNode getTo() {
			return _to;
		}
		
		public int getCost() {
			return _cost;
		}
		public GraphEdge(RouterNode _to, int _cost) {
			this._to = _to;
			this._cost = _cost;
		}
	}

	public static class RouterNode implements Comparable<RouterNode> {

		private static int _internalNameCounter = 1;
		private int _name;

		private List<GraphEdge> _edges = new ArrayList<GraphEdge>();

		public int smallestCost = Integer.MAX_VALUE;
		public RouterNode previous = null;

		public RouterNode() {
			_name = _internalNameCounter;
			_internalNameCounter++;
		}

		public List<GraphEdge> getEdges() {
			return _edges;
		}

		public void addEdge(GraphEdge edge) {
			_edges.add(edge);
		}

		@Override
		public int compareTo(RouterNode o) {
			return Integer.compare(this.smallestCost, o.smallestCost);
		}

		@Override
		public String toString() {
			return Integer.toString(_name);
		}
	}
}