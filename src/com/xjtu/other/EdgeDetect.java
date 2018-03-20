package com.xjtu.other;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class EdgeDetect {

	//sobelˮƽ���Ӵ�ֱ����ģ��
	private int [][]sobelx= {{-1,-2,-1},{0,0,0},{1,2,1}};
	private int [][]sobely= {{-1,0,1},{-2,0,2},{-1,0,1}};
	
	/**
	 * ��������sobel��Ե��⣬
	 * ������鴦���ʱ�����ͼ��ı߽�û����������ԭͼ��������
	 * �����˿��Ϊ1��λ�ı�
	 * @param binMat	Mat
	 * @return	Mat	���ؼ��Ľ��
	 */
	 public static Mat edgeDetect(Mat binMat) {
		//���Ǵ�ű�Ե����ͼ��
		Mat sobel=new Mat(binMat.size(), CvType.CV_8UC1);
		//������ֵͼ�񣬼���ˮƽ�ʹ�ֱ�����sobel
		for(int row=1; row<binMat.rows()-1; row++) {
			for(int col=1; col<binMat.cols()-1; col++) {
				//ˮƽ�����sobel
				double e7=binMat.get(row+1, col-1)[0];
				double e8=binMat.get(row+1, col)[0];
				double e9=binMat.get(row+1, col+1)[0];
				double e1=binMat.get(row-1, col-1)[0];
				double e2=binMat.get(row-1, col)[0];
				double e3=binMat.get(row-1, col+1)[0];
				double sobelx=(e7+2*e8+e9)-(e1+2*e2+e3);
				//��ֱ�����sobel
				double ey1=binMat.get(row-1, col-1)[0];
				double ey4=binMat.get(row, col-1)[0];
				double ey7=binMat.get(row+1, col-1)[0];
				double ey3=binMat.get(row-1, col+1)[0];
				double ey6=binMat.get(row, col+1)[0];
				double ey9=binMat.get(row+1, col+1)[0];
				double sobely=(ey3+2*ey6+ey9)-(ey1+2*ey4+ey7);
				//���յ�sobelֵ
				int s=(int) (Math.abs(sobelx)+Math.abs(sobely));
				sobel.put(row, col, new byte[] {(byte)s});
			}
		}
		return sobel;
	}
	
	/**
	 * �Ҷȴ���
	 * @param mat	Mat	�����ɫͼ
	 * @param cal	Calculate	����ʲô��������Ҷ�ֵ
	 */
	public static Mat gray(Mat mat, Calculate cal) {
		//�´����Ҷ�ͼ�������ԭͼ��ͬ������Ϊ��ͨ��
		Mat gray=new Mat(mat.size(), CvType.CV_8UC1);
		//����ͼ��
		for(int row=0; row<mat.rows(); row++) {
			for(int col=0; col<mat.cols(); col++) {
				//���ԭʼͼ���ϸõ������ֵ
				double []pixel=mat.get(row, col);
				//��������ΪRGB
				int val=cal.calcu(pixel[2], pixel[1], pixel[0]);
				//���Ҷ�ֵд�뵽�Ҷ�ͼ��
				gray.put(row, col, val);
			}
		}
		return gray;
	}
	
	/**
	 * ��ֵ������
	 * @param grayMat	Mat	����Ҷ�ͼ
	 * @param	threshold	int	��ֵ������ֵ
	 * @return	Mat	���ض�ֵͼ
	 */
	public static Mat binarize(Mat grayMat, int threshold) {
		//��Ŷ�ֵ����ͼ��
		Mat bin=new Mat(grayMat.size(), CvType.CV_8UC1);
		//�����Ҷ�ͼ
		for(int row=0; row<grayMat.rows(); row++) {
			for(int col=0; col<grayMat.cols(); col++) {
				//ȡ����Ӧ�ĻҶ�ֵ
				double val=(int) grayMat.get(row, col)[0];
				//���ֵͼд������
				if(val<=threshold) {
					bin.put(row, col, new byte[] {0});
				}else {
					bin.put(row, col, new byte[] {(byte) 255});
				}
			}
		}		
		return bin;
	}
	
	/**
	 * ʹ��OTSU��ȡ��ֵ
	 * @param gray	Mat	����ĻҶ�ͼ
	 * @return	int	�ָ���ֵ
	 */
	public static int threshold(Mat gray) {
		//���ÿ���Ҷȼ��ĸ���
		double []p=new double[256];
		//���ÿ���Ҷȼ������ظ���
		int []nums=new int[256];
		for(int row=0; row<gray.rows(); row++) {
			for(int col=0; col<gray.cols(); col++) {
				//ȡ����λ�õĻҶ�ֵ
				int val=(int) gray.get(row, col)[0];
				//���ûҶ�ֵ�ŵ�Ƶ������ȥ
				nums[val]++;
			}
		}
		//��ĸ
		int all=gray.cols()*gray.rows();
		//����Ƶ�����Ƶ��ֵ
		for(int i=0; i<p.length; i++) {
			p[i]=(double) nums[i]/(double) all;
		}
		
		//�����ʷ������䷽��Լ���Ӧ�ĻҶ�ֵ
		double maxDelta=Double.MIN_VALUE;
		int threshold=0;
		
		//���ݻҶ�ֵ�ָ�����ͼ��
		for(int g=0; g<256; g++) {
			//���ֱ�ͳ����ֵ�ָ�����ߵĸ���ֵ
			double p1=0;
			double p2=0;
			//�����ʱ����
			double m1=0;
			double m2=0;
			//��ŻҶȾ�ֵ
			double mk1=0;
			double mk2=0;
			//��������ֵ
			double mean=0;
			//��䷽��
			double delta=0;
			
			for(int i=0; i<256; i++) {
				//�ֱ����������ֵĻҶȾ�ֵ
				if(i<=g) {
					p1+=p[i];
					m1+= (double) i*p[i];
				}else {
					p2+=p[i];
					m2+= (double) i*p[i];
				}
				//���������ֵĻҶȾ�ֵ
				mk1=(1/p1)*m1;
				mk2=(1/p2)*m2;
				//���������ֵ
				mean=p1*mk1+p2*mk2;
				//������䷽��
				delta=p1*(mk1-mean)*(mk1-mean)+p2*(mk2-mean)*(mk2-mean);
				//�ж��Ƿ����ʷ��䷽��Ҫ������ǣ���ô�͸�����ʷ��䷽���Լ���Ӧ�ĻҶ�ֵ
				if(delta>maxDelta) {
					maxDelta=delta;
					threshold=g;
				}
			}
		}
		return threshold;
	}

	/**
	 * 
	 * @param sobelMat
	 */
	public static int[][] detect(Mat sobelMat, int cellSize) {
		//ԭͼ����500*500�����Ǽ��ķ����СΪ5*5
		int [][]count=new int[sobelMat.rows()/cellSize][sobelMat.cols()/cellSize];
		//�������е�С����
		for(int r=0; r<sobelMat.rows()/cellSize; r++) {
			for(int c=0; c<sobelMat.cols()/cellSize; c++) {
				int num=0;
				//������������
				for(int i=cellSize*r; i<cellSize*r+cellSize; i++) {
					for(int j=cellSize*c; j<cellSize*c+cellSize; j++) {
						//���С�����е�����ֵΪǰ������������һ
						int temp=(int) sobelMat.get(i, j)[0];
						if(temp>0) {
							num++;
						}
					}
				}
				count[r][c]=num;
			}
		}
		return count;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.load("C:\\\\opencv-3.3.0\\\\build\\\\java\\\\x64\\\\opencv_java330.dll");
		String filename="C:\\Users\\Administrator\\Desktop\\face.jpg";
		Mat face=Imgcodecs.imread(filename);
		Imshow.imshow(face, "����ԭͼ");
		System.out.println("ԭͼ�Ĵ�СΪ��"+face.rows()+", "+face.cols());
		//�Ҷȴ���
//		Mat grayMat1=gray(face, new MeanValueMethod());
//		Imshow.imshow(grayMat1, "ʹ�þ�ֵ����õĻҶ�ͼ");
//		Mat grayMat2=gray(face, new MaxValueMethod());
//		Imshow.imshow(grayMat2, "ʹ�����ֵ����ûҶ�ͼ");
		Mat grayMat3=gray(face, new WeightedMeanMethod());
		Imshow.imshow(grayMat3, "ʹ�ü�Ȩ��ֵ����õĻҶ�ͼ");
		//��ȡ��ֵ������ֵ
		int thresh=threshold(grayMat3);
		System.out.println("��ֵ����ֵΪ��"+thresh);
		Mat binMat=binarize(grayMat3, thresh);
		Imshow.imshow(binMat, "OTSU��ֵ���Ľ��");
		//sobel��Ե���
		Mat res=edgeDetect(binMat);
		Imshow.imshow(res, "sobel��Ե���");
		//ͳ��ÿ��С�����е�ǰ���ĸ���
		int [][]count=detect(res, 5);
		System.out.println("100*100��");
		for(int i=0; i<count.length; i++) {
			for(int j=0; j<count[0].length; j++) {
				System.out.print(count[i][j]+"\t");
			}
			System.out.println();
		}
	}

}
