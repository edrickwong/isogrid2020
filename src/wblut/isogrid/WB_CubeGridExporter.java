package wblut.isogrid;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class WB_CubeGridExporter {

	

	public static void export(String path,double cx, double cy, double cz, final int I,
			final int J, final int K, final double dx, final double dy,
			final double dz, boolean[] values,int li, int ui, int lj, int uj, int lk, int uk ) {
		int[][][] vertexIndices;
		List<double[]> vertices;
		List<int[]> faces;
		
		vertexIndices = new int[I + 1][J + 1][K + 1];
		for (int i = 0; i <= I; i++) {
			for (int j = 0; j <= J; j++) {
				for (int k = 0; k <= K; k++) {
					vertexIndices[i][j][k] = -1;
				}
			}
		}
		vertices = new ArrayList<double[]>();
		faces = new ArrayList<int[]>();
		int val0, valm, sum;
		double[] c = new double[] {cx-I * 0.5 * dx, cy-J * 0.5 * dy, cz-K * 0.5 * dz};
		int index;
		int JK=J*K;
		for (int i = li; i <= ui; i++) {
			for (int j = lj; j < uj; j++) {
				for (int k = lk; k < uk; k++) {
					index=index(i,j,k,JK,K,li,ui,lj,uj,lk,uk);
					val0 =index==-1?0: values[index] ? 1 : 0;
					index=index(i-1,j,k,JK,K,li,ui,lj,uj,lk,uk);
					valm = index==-1?0:values[index] ? 1 : 0;
					sum = val0 + valm;
					if (sum == 1) {
						faces.add(new int[] { getVertexIndex( i, j, k,vertexIndices,vertices,c,dx,dy,dz), getVertexIndex( i, j + 1, k,vertexIndices,vertices,c,dx,dy,dz),
								getVertexIndex( i, j + 1, k + 1,vertexIndices,vertices,c,dx,dy,dz), getVertexIndex( i, j, k + 1,vertexIndices,vertices,c,dx,dy,dz)

						});
					}
				}
			}
		}
		for (int i = li; i < ui; i++) {
			for (int j = lj; j <= uj; j++) {
				for (int k = lk; k < uk; k++) {
					index=index(i,j,k,JK,K,li,ui,lj,uj,lk,uk);
					val0 = index==-1?0:values[index] ? 1 : 0;
					index=index(i,j-1,k,JK,K,li,ui,lj,uj,lk,uk);
					valm =  index==-1?0:values[index] ? 1 : 0;
					sum = val0 + valm;
					if (sum == 1) {
						faces.add(new int[] { getVertexIndex( i, j, k,vertexIndices,vertices,c,dx,dy,dz), getVertexIndex( i + 1, j, k,vertexIndices,vertices,c,dx,dy,dz),
								getVertexIndex( i + 1, j, k + 1,vertexIndices,vertices,c,dx,dy,dz), getVertexIndex( i, j, k + 1,vertexIndices,vertices,c,dx,dy,dz) });
					}
				}
			}
		}
		for (int i = li; i < ui; i++) {
			for (int j = lj; j < uj; j++) {
				for (int k = lk; k <= uk; k++) {
					index=index(i,j,k,JK,K,li,ui,lj,uj,lk,uk);
					val0 =  index==-1?0:values[index] ? 1 : 0;
					index=index(i,j,k-1,JK,K,li,ui,lj,uj,lk,uk);
					valm = index==-1?0: values[index] ? 1 : 0;
					sum = val0 + valm;
					if (sum == 1) {
						faces.add(new int[] { getVertexIndex( i, j, k,vertexIndices,vertices,c,dx,dy,dz), getVertexIndex( i + 1, j, k,vertexIndices,vertices,c,dx,dy,dz),
								getVertexIndex( i + 1, j + 1, k,vertexIndices,vertices,c,dx,dy,dz), getVertexIndex( i, j + 1, k,vertexIndices,vertices,c,dx,dy,dz) });
					}
				}
			}
		}
		
		File f=new File(path);
		File dir=new File(f.getParent());
		dir.mkdirs();
		try( BufferedWriter objwriter = new BufferedWriter(new FileWriter(path))) {
			objwriter.write("# generated by WB_CubeGridExporter");
			objwriter.newLine();
			for(double[] vertex: vertices) {
				objwriter.write("v "+vertex[0]+" "+vertex[1]+" "+vertex[2]);
				objwriter.newLine();
			}
			for(int[] face: faces) {
				objwriter.write("f "+face[0]+" "+face[1]+" "+face[2]);
				objwriter.newLine();
				objwriter.write("f "+face[2]+" "+face[3]+" "+face[0]);
				objwriter.newLine();
			}
			
		
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		

	}
	
	private static int index(final int i,
			final int j, final int k, int JK, int K,int li, int ui, int lj, int uj, int lk, int uk) {
		if (i > li - 1 && j > lj - 1 && k > lk - 1 && i < ui && j < uj
				&& k < uk) {
			return k + j * K + i * JK;
		} else {
			return -1;
		}
	}
	


	private static int getVertexIndex (int i, int j, int k, int[][][] vertexIndices, List<double[]> vertices,double[] c, final double dx, final double dy,
	final double dz ) {
		
		if (vertexIndices[i][j][k] == -1) {
			vertexIndices[i][j][k] = vertices.size()+1;
			vertices.add(new double[] { c[0] + i * dx, c[1] + j * dy, c[2] + k * dz });
		}
		return vertexIndices[i][j][k];

	}

}