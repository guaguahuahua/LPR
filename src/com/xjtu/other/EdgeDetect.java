package com.xjtu.other;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class EdgeDetect {

	//sobel水平算子垂直算子模板
	private int [][]sobelx= {{-1,-2,-1},{0,0,0},{1,2,1}};
	private int [][]sobely= {{-1,0,1},{-2,0,2},{-1,0,1}};
	
	/**
	 * 在这块进行sobel边缘检测，
	 * 但是这块处理的时候对于图像的边界没有做处理，在原图像上四周
	 * 留下了宽度为1单位的边
	 * @param binMat	Mat
	 * @return	Mat	返回检测的结果
	 */
	 public static Mat edgeDetect(Mat binMat) {
		//这是存放边缘检测的图像
		Mat sobel=new Mat(binMat.size(), CvType.CV_8UC1);
		//遍历二值图像，计算水平和垂直方向的sobel
		for(int row=1; row<binMat.rows()-1; row++) {
			for(int col=1; col<binMat.cols()-1; col++) {
				//水平方向的sobel
				double e7=binMat.get(row+1, col-1)[0];
				double e8=binMat.get(row+1, col)[0];
				double e9=binMat.get(row+1, col+1)[0];
				double e1=binMat.get(row-1, col-1)[0];
				double e2=binMat.get(row-1, col)[0];
				double e3=binMat.get(row-1, col+1)[0];
				double sobelx=(e7+2*e8+e9)-(e1+2*e2+e3);
				//垂直方向的sobel
				double ey1=binMat.get(row-1, col-1)[0];
				double ey4=binMat.get(row, col-1)[0];
				double ey7=binMat.get(row+1, col-1)[0];
				double ey3=binMat.get(row-1, col+1)[0];
				double ey6=binMat.get(row, col+1)[0];
				double ey9=binMat.get(row+1, col+1)[0];
				double sobely=(ey3+2*ey6+ey9)-(ey1+2*ey4+ey7);
				//最终的sobel值
				int s=(int) (Math.abs(sobelx)+Math.abs(sobely));
				sobel.put(row, col, new byte[] {(byte)s});
			}
		}
		return sobel;
	}
	
	/**
	 * 灰度处理
	 * @param mat	Mat	输入彩色图
	 * @param cal	Calculate	采用什么方法计算灰度值
	 */
	public static Mat gray(Mat mat, Calculate cal) {
		//新创建灰度图，长宽和原图相同，但是为单通道
		Mat gray=new Mat(mat.size(), CvType.CV_8UC1);
		//遍历图像
		for(int row=0; row<mat.rows(); row++) {
			for(int col=0; col<mat.cols(); col++) {
				//获得原始图像上该点的像素值
				double []pixel=mat.get(row, col);
				//分量依次为RGB
				int val=cal.calcu(pixel[2], pixel[1], pixel[0]);
				//将灰度值写入到灰度图中
				gray.put(row, col, val);
			}
		}
		return gray;
	}
	
	/**
	 * 二值化处理
	 * @param grayMat	Mat	输入灰度图
	 * @param	threshold	int	二值化的阈值
	 * @return	Mat	返回二值图
	 */
	public static Mat binarize(Mat grayMat, int threshold) {
		//存放二值化的图像
		Mat bin=new Mat(grayMat.size(), CvType.CV_8UC1);
		//遍历灰度图
		for(int row=0; row<grayMat.rows(); row++) {
			for(int col=0; col<grayMat.cols(); col++) {
				//取出对应的灰度值
				double val=(int) grayMat.get(row, col)[0];
				//向二值图写入数据
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
	 * 使用OTSU获取阈值
	 * @param gray	Mat	输入的灰度图
	 * @return	int	分割阈值
	 */
	public static int threshold(Mat gray) {
		//存放每个灰度级的概率
		double []p=new double[256];
		//存放每个灰度级的像素个数
		int []nums=new int[256];
		for(int row=0; row<gray.rows(); row++) {
			for(int col=0; col<gray.cols(); col++) {
				//取出该位置的灰度值
				int val=(int) gray.get(row, col)[0];
				//将该灰度值放到频数表中去
				nums[val]++;
			}
		}
		//分母
		int all=gray.cols()*gray.rows();
		//根据频数获得频率值
		for(int i=0; i<p.length; i++) {
			p[i]=(double) nums[i]/(double) all;
		}
		
		//存放历史最大的类间方差，以及对应的灰度值
		double maxDelta=Double.MIN_VALUE;
		int threshold=0;
		
		//根据灰度值分割整幅图像
		for(int g=0; g<256; g++) {
			//这块分别统计阈值分割后两边的概率值
			double p1=0;
			double p2=0;
			//存放临时变量
			double m1=0;
			double m2=0;
			//存放灰度均值
			double mk1=0;
			double mk2=0;
			//存放整体均值
			double mean=0;
			//类间方差
			double delta=0;
			
			for(int i=0; i<256; i++) {
				//分别获得两个部分的灰度均值
				if(i<=g) {
					p1+=p[i];
					m1+= (double) i*p[i];
				}else {
					p2+=p[i];
					m2+= (double) i*p[i];
				}
				//计算两部分的灰度均值
				mk1=(1/p1)*m1;
				mk2=(1/p2)*m2;
				//计算总体均值
				mean=p1*mk1+p2*mk2;
				//计算类间方差
				delta=p1*(mk1-mean)*(mk1-mean)+p2*(mk2-mean)*(mk2-mean);
				//判断是否比历史类间方差要大，如果是，那么就更新历史类间方差以及对应的灰度值
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
		//原图像是500*500的我们检测的方格大小为5*5
		int [][]count=new int[sobelMat.rows()/cellSize][sobelMat.cols()/cellSize];
		//遍历所有的小格子
		for(int r=0; r<sobelMat.rows()/cellSize; r++) {
			for(int c=0; c<sobelMat.cols()/cellSize; c++) {
				int num=0;
				//遍历格子里面
				for(int i=cellSize*r; i<cellSize*r+cellSize; i++) {
					for(int j=cellSize*c; j<cellSize*c+cellSize; j++) {
						//如果小格子中的像素值为前景，计数器加一
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
		Imshow.imshow(face, "人脸原图");
		System.out.println("原图的大小为："+face.rows()+", "+face.cols());
		//灰度处理
//		Mat grayMat1=gray(face, new MeanValueMethod());
//		Imshow.imshow(grayMat1, "使用均值法获得的灰度图");
//		Mat grayMat2=gray(face, new MaxValueMethod());
//		Imshow.imshow(grayMat2, "使用最大值法获得灰度图");
		Mat grayMat3=gray(face, new WeightedMeanMethod());
		Imshow.imshow(grayMat3, "使用加权均值法获得的灰度图");
		//获取二值化的阈值
		int thresh=threshold(grayMat3);
		System.out.println("二值化阈值为："+thresh);
		Mat binMat=binarize(grayMat3, thresh);
		Imshow.imshow(binMat, "OTSU二值化的结果");
		//sobel边缘检测
		Mat res=edgeDetect(binMat);
		Imshow.imshow(res, "sobel边缘检测");
		//统计每个小格子中的前景的个数
		int [][]count=detect(res, 5);
		System.out.println("100*100：");
		for(int i=0; i<count.length; i++) {
			for(int j=0; j<count[0].length; j++) {
				System.out.print(count[i][j]+"\t");
			}
			System.out.println();
		}
	}

}
