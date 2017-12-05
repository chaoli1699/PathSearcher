package cn.cienet.pathsearcher.astar;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class AStar {
	
	public final static int BAR = 1; // �ϰ�ֵ
	public final static int PATH = 2; // ·��
	public final static int DIRECT_VALUE = 10; // �����ƶ�����
	public final static int OBLIQUE_VALUE = 14; // б�ƶ�����
	
	Queue<Node> openList = new PriorityQueue<Node>(); // ���ȶ���(����)
	List<Node> closeList = new ArrayList<Node>();
	
	/**
	 * ��ʼѰ·
	 */
	public List<int[]> start(PathInfo mapInfo)
	{
		if(mapInfo==null) return null;
		// clean
		openList.clear();
		closeList.clear();
		// ��ʼ����
		openList.add(mapInfo.start);
		return moveNodes(mapInfo);
	}

	/**
	 * �ƶ���ǰ���
	 */
	private List<int[]> moveNodes(PathInfo mapInfo)
	{
		while (!openList.isEmpty())
		{
			if (isCoordInClose(mapInfo.end.coord))
			{
				return drawPath(mapInfo.maps,mapInfo.end);
			}
			Node current = openList.poll();
			closeList.add(current);
			addNeighborNodeInOpen(mapInfo,current);
		}
		return null;
	}
	
	/**
	 * �ڶ�ά�����л���·��
	 */
	private List<int[]> drawPath(int[][] maps, Node end)
	{
		@SuppressWarnings({ "unchecked", "rawtypes" })
		List<int[]> pointList=new ArrayList();
		if(end==null||maps==null) return null;
	    System.out.println("�ܴ��ۣ�" + end.G);
		while (end != null)
		{
			Coord c = end.coord;
			maps[c.y][c.x] = PATH;
			int[] point=new int[2];
			point[0]=c.x;
			point[1]=c.y;
			pointList.add(point);
			end = end.parent;
		}
		
		return pointList;
	}

	/**
	 * ��������ڽ�㵽open��
	 */
	private void addNeighborNodeInOpen(PathInfo mapInfo,Node current)
	{
		int x = current.coord.x;
		int y = current.coord.y;
		// ��
		addNeighborNodeInOpen(mapInfo,current, x - 1, y, DIRECT_VALUE);
		// ��
		addNeighborNodeInOpen(mapInfo,current, x, y - 1, DIRECT_VALUE);
		// ��
		addNeighborNodeInOpen(mapInfo,current, x + 1, y, DIRECT_VALUE);
		// ��
		addNeighborNodeInOpen(mapInfo,current, x, y + 1, DIRECT_VALUE);
		// ����
		addNeighborNodeInOpen(mapInfo,current, x - 1, y - 1, OBLIQUE_VALUE);
		// ����
		addNeighborNodeInOpen(mapInfo,current, x + 1, y - 1, OBLIQUE_VALUE);
		// ����
		addNeighborNodeInOpen(mapInfo,current, x + 1, y + 1, OBLIQUE_VALUE);
		// ����
		addNeighborNodeInOpen(mapInfo,current, x - 1, y + 1, OBLIQUE_VALUE);
	}

	/**
	 * ���һ���ڽ�㵽open��
	 */
	private void addNeighborNodeInOpen(PathInfo mapInfo,Node current, int x, int y, int value)
	{
		if (canAddNodeToOpen(mapInfo,x, y))
		{
			Node end=mapInfo.end;
			Coord coord = new Coord(x, y);
			int G = current.G + value; // �����ڽ���Gֵ
			Node child = findNodeInOpen(coord);
			if (child == null)
			{
				int H=calcH(end.coord,coord); // ����Hֵ
				if(isEndNode(end.coord,coord))
				{
					child=end;
					child.parent=current;
					child.G=G;
					child.H=H;
				}
				else
				{
					child = new Node(coord, current, G, H);
				}
				openList.add(child);
			}
			else if (child.G > G)
			{
				child.G = G;
				child.parent = current;
				openList.add(child);
			}
		}
	}

	/**
	 * ��Open�б��в��ҽ��
	 */
	private Node findNodeInOpen(Coord coord)
	{
		if (coord == null || openList.isEmpty()) return null;
		for (Node node : openList)
		{
			if (node.coord.equals(coord))
			{
				return node;
			}
		}
		return null;
	}


	/**
	 * ����H�Ĺ�ֵ���������١���������ֱ�ȡ��ֵ���
	 */
	private int calcH(Coord end,Coord coord)
	{
		return Math.abs(end.x - coord.x)
				+ Math.abs(end.y - coord.y);
	}
	
	/**
	 * �жϽ���Ƿ������ս��
	 */
	private boolean isEndNode(Coord end,Coord coord)
	{
		return coord != null && end.equals(coord);
	}

	/**
	 * �жϽ���ܷ����Open�б�
	 */
	private boolean canAddNodeToOpen(PathInfo pathInfo,int x, int y)
	{
		// �Ƿ��ڵ�ͼ��
		if (x < 0 || x >= pathInfo.width || y < 0 || y >= pathInfo.hight) {
			return false;
		}
		// �ж��Ƿ��ǲ���ͨ���Ľ��
		if (pathInfo.maps[y][x] == BAR) {
			return false;
		}
		// �жϽ���Ƿ����close��
		if (isCoordInClose(x, y)) return false;

		return true;
	}

	/**
	 * �ж������Ƿ���close����
	 */
	private boolean isCoordInClose(Coord coord)
	{
		return coord!=null&&isCoordInClose(coord.x, coord.y);
	}

	/**
	 * �ж������Ƿ���close����
	 */
	private boolean isCoordInClose(int x, int y)
	{
		if (closeList.isEmpty()) return false;
		for (Node node : closeList)
		{
			if (node.coord.x == x && node.coord.y == y)
			{
				return true;
			}
		}
		return false;
	}

}
