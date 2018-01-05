package com.xjtu.ann;

import java.io.File;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class PatternMatch {
	//图片库的绝对地址
	private static final String path="E:\\ann\\library";
	//以二维数组的形式存放所有的模板的特征
	public static float [][]feature=new float[31][52+40]; 
	//字符集
    static String []cCharacter= {"皖","京","渝","闽", "甘","粤","桂","贵","琼","冀","黑","豫","鄂","湘",
    		"苏","赣","吉","辽","蒙","宁","青","陕","鲁","沪","晋","川","津",      
            "新", "藏","云","浙"  
            };
	
	/**
	 * 这块使用模板匹配提取当前字符的特征和模板库进行比对
	 * @param mat 输入一张字符的mat图像
	 * return int
	 */
	public static int patternMatch(Mat mat) {
		
		
		//提取mat的特征
		Rect rect=getRect(mat);
		Mat cutted=cutRect(rect, mat);
		Mat resized=new Mat();
		Imgproc.resize(cutted, resized, new Size(20, 32));
		//灰度化，二值化
		Mat grayMat=new Mat();
		Mat binMat=new Mat();
		if(mat.channels()!=1) {
			Imgproc.cvtColor(resized, grayMat, Imgproc.COLOR_RGB2GRAY);
			Imgproc.adaptiveThreshold(grayMat, binMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 25, -20);
		}else {
			binMat=resized;
		}
		System.out.println("cols, rows: "+binMat.cols()+", "+binMat.rows());
		//水平垂直投影
		Mat h=project(binMat, 0);
		Mat v=project(binMat, 1);
		float []oFeature=cuttedArea(binMat);
		//存放当前特征
		int index=0;
		float []cur=new float[52+40];
		for(int i=0; i<h.cols(); i++) {
			cur[index++]=(float) h.get(0, i)[0];
		}
		for(int i=0; i<v.cols(); i++) {
			cur[index++]=(float) v.get(0, i)[0];
		}
		for(int i=0; i<oFeature.length; i++) {
			cur[index++]=oFeature[i];
		}
		//存放欧氏距离最小的那个字符及位置
		float min=Integer.MAX_VALUE;
		int pos=-1;
		//计算当前特征和模板库中欧氏距离，并找到最小的那个
		for(int start=0; start<feature.length; start++) {
			//累加平方和的零时变量
			float temp=0;
			//计算欧式距离
			for(int j=0; j<feature[start].length; j++) {
				temp+=Math.pow((feature[start][j]-cur[j]), 2);
			}
			//判断
			if(temp<min) {
				System.out.println("距离： "+temp);
				min=temp;
				pos=start;
			}
		}
		return pos;
	}
	
	/**
	 * 这块是创建模板库
	 */
	public static void createPatternLibrary() {
		//这块是从文件路径中加载所有的图像
		File file=new File(path);
		File []fileArray=file.listFiles();
//		System.out.println("len: "+fileArray.length);
		//遍历文件数组，获得所有的文件的绝对的路径
		for(int i=0; i<fileArray.length; i++) {
			//获得单个文件的绝对路径
			String singleFile=fileArray[i].getAbsolutePath();
//			System.out.println(singleFile);
			//将单个文件以mat形式读入
			Mat fileMat=Imgcodecs.imread(singleFile);
//			Imshow.imshow(fileMat);
			//抽取特征并存放
			Rect rect=getRect(fileMat);
			Mat detail=cutRect(rect, fileMat);
			//裁剪区域并进行拉伸变换
			Mat resized=new Mat();
			Imgproc.resize(detail, resized, new Size(20, 32));
			//灰度化，二值化
			Mat grayMat=new Mat();
			Imgproc.cvtColor(resized, grayMat, Imgproc.COLOR_RGB2GRAY);
			Mat binMat=new Mat();
			Imgproc.adaptiveThreshold(grayMat, binMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 25, -20);
//			Imshow.imshow(binMat);
			
			float [] oFeature=cuttedArea(binMat);
			
			//水平垂直投影
			Mat h=project(binMat, 0);
			Mat v=project(binMat, 1);
			//将特征合并存放在浮点数组中
			int index=0;
			for(int j=0; j<h.cols(); j++) {
				feature[i][index++]=(float) h.get(0, j)[0];
			}
			for(int j=0; j<v.cols(); j++) {
				feature[i][index++]=(float) v.get(0, j)[0];
			}		
			for(int j=0; j<oFeature.length; j++) {
				feature[i][index++]=oFeature[j];
			}
		}
	}
	
	/**
	 * 在原图像上面进行分块，并统计每一个分块内的像素数，最后由最大值归一化
	 * @param mat 输入的二值图
	 * @return float[] 40维特征向量
	 */
	private static float[] cuttedArea(Mat mat) {
		//存放特征的数组
		float []oFeature=new float[40];
		int index=0; //索引
		int max=Integer.MIN_VALUE;
		for(int i=0; i<8; i++) {
			for(int j=0; j<5; j++) {
				int count=0;
				for(int row=4*i; row<4*i+4; row++) {
					for(int col=4*j; col<4*j+4; col++) {
						if(mat.get(row, col)[0]==255) {
							count++;
						}
					}
				}
				//获取最大值
				if(max<count) {
					max=count;
				}
				oFeature[index++]=count;				
			}
		}
//		System.out.println("分块特征");
		//归一化
		if(max>0) {
			for(int i=0; i<oFeature.length; i++) {
				oFeature[i]=oFeature[i]/max;
				System.out.print(oFeature[i]+"\t");
			}
		}
		System.out.println();
		return oFeature;
	}	
	
	
	/**
	 * 细化算法，将原字符细化后提取特征
	 * @param mat 二值化的mat
	 * @return Mat
	 */
	private static Mat thinning(Mat mat) {	
		
		for(int i=0; i<5; i++) {
			
		//original保存原图像的备份
		Mat original=mat.clone();
		//下面进入fpa算法
		for(int row=1; row<mat.rows()-1; row++) {
			for(int col=1; col<mat.cols()-1; col++) {
				//判断一下该像素点是否为对象
				if(mat.get(row, col)[0]!=0) {
					String zero="";
					//8个位置的判定
					int count=0;
					if(mat.get(row-1, col)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row-1, col+1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row, col+1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row+1, col+1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row+1, col)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row+1, col-1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row, col-1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(mat.get(row-1, col-1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					//0，1数量的判定
					int zeroCount=0;
					for(int i1=0; i1<zero.length()-1; i1++) { //这里可能会发生越界的情形
						String sub=zero.substring(i1, i1+2);
						if(sub.equals("01")) {
							zeroCount++;
						}
					}
					//东南角的两个条件
					int r1=(int) (mat.get(row-1, col)[0]*mat.get(row, col+1)[0]*mat.get(row+1, col)[0]);
					int r2=(int) (mat.get(row, col+1)[0]*mat.get(row+1, col)[0]*mat.get(row, col-1)[0]);
					//判断是否同时满足4个条件
					if((count>=2 && count<=6) && (zeroCount==1) && (r1==0) && (r2==0)) {
						original.put(row, col, new byte[] { 0 });
					}
				}
			}
		}
		//第二次扫描，去除西北角的像素点
		Mat second=original.clone();
		for(int row=1; row<original.rows()-1; row++) {
			for(int col=1; col<original.cols()-1; col++) {
				if(original.get(row, col)[0]!=0) {
					String zero="";
					//8个位置的判定
					int count=0;
					if(original.get(row-1, col)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row-1, col+1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row, col+1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row+1, col+1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row+1, col)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row+1, col-1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row, col-1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					
					if(original.get(row-1, col-1)[0]==255) {
						count++;
						zero=zero+"1";
					}else {
						zero=zero+"0";
					}
					//0，1数量的判定
					int zeroCount=0;
					for(int i1=0; i1<zero.length()-1; i1++) {
						String sub=zero.substring(i1, i1+2);
						if(sub.equals("01")) {
							zeroCount++;
						}
					}
					//西北角的两个条件
					int r1=(int) (original.get(row-1, col)[0]*original.get(row, col+1)[0]*original.get(row, col-1)[0]);
					int r2=(int) (original.get(row-1, col)[0]*original.get(row+1, col)[0]*original.get(row, col-1)[0]);
					//判断是否同时满足4个条件
					if((count>=2 && count<=6) && (zeroCount==1) && (r1==0) && (r2==0)) {
						second.put(row, col, new byte[] { 0 });
					}	
				}
			}
		}
		mat=second.clone();
	}
		
		Imshow.imshow(mat);
		for(int row=0; row<mat.rows(); row++) {
			for(int col=0; col<mat.cols(); col++) {
				System.out.print(mat.get(row, col)[0]+"\t");
			}
			System.out.println();
		}

		return null;
	}
	
	
	/**
	 * 水平和垂直方向进行投影，并对投影进行规格化
	 * @param mat 原图像
	 * @param direction 投影方向, 规定0为水平，1为垂直
	 * @return double[]
	 */
	public static Mat project(Mat mat, int direction) {
		//记录特征根据输入向量的大小建立特征
		int col=direction==0 ? mat.cols() : mat.rows();
		Mat features=new Mat(1, col, CvType.CV_32FC1);
		//存放遍历结束点的变量
		int end=0;
		if(direction==0) {
			end=mat.cols(); //水平方向
		}else {
			end=mat.rows(); //垂直方向
		}
		int max=0;
		//按照方向投影
		for(int i=0; i<end; i++) {
			Mat data= (direction==0) ? mat.col(i) : mat.row(i);
			int featI=countObject(data);
			//取水平或者垂直方向的投影中的最大值
			max=featI > max ? featI : max;
			features.put(0, i, new float[] { featI });
		}
		//规格化
		if(max>0) {
			for(int i=0; i<features.cols(); i++) {
				double val=features.get(0, i)[0] / max;
				features.put(0, i, new float[] { (float) val});
			}
		}
		return features;
	}
	
	/**
	 * 这块使用灰度跳变的方式来作为统计特征，而不是每一行的对象点的个数
	 * @param mat 二值化图像
	 * @param direction 投影的方向，0表示水平，1表示垂直
	 * @return Mat
	 */
	private static Mat project_1(Mat mat, int direction) {
		//得到行数或者列数
		int col= direction==0 ? mat.cols() : mat.rows();
		//创建存放返回的对象
		Mat features=new Mat(1, col, CvType.CV_32FC1);
		int index=0;
		int max=0;
		//按照方向投影
		for(int i=0; i<col; i++) {
			//获取某一个行或者是某一列
			Mat data= direction==0 ? mat.col(i) : mat.row(i);
			//统计这一行或者列的灰度跳变次数
			int nums=count(data);
			//记录一行或者一列中最大值
			max= nums>max ? nums : max;
			features.put(0, index++, new float[] { nums });
		}
		//规格化
		if(max>0) {
			for(int i=0; i<features.cols(); i++) {
				float val=(float) (features.get(0, i)[0] / max);
				features.put(0, i, new float[] { val });
			}
		}
		
		
		return features;
	} 
	
	/**
	 * 统计一行或者是一列灰度跳变的次数
	 * @param mat 一行或者一列的数据
	 * @return int
	 */
	private static int count(Mat mat) {
		
		int times=0;
		//如果是一列
		if(mat.cols()==1) {
			for(int row=1; row<mat.rows(); row++) {
				//如果发现两行的灰度发生了跳变
				if(mat.get(row, 0)[0] != mat.get(row-1, 0)[0]) {
					times++;
				}
			}
		//如果是一行	
		}else if(mat.rows()==1) {
			for(int col=1; col<mat.cols(); col++) {
				if(mat.get(0, col)[0] != mat.get(0, col-1)[0]) {
					times++;
				}
			}
		}
		return times;
	}
	
	/**
	 * 计算一行或者是一列中非背景元素的个数
	 * @param data 一行或者是一列元素
	 * @return int
	 */
	public static int countObject(Mat data) {
		int count=0;
		if(data.cols()==1) { //返回了一列数据
			//遍历这一列数如果不是背景，那么count++
			for(int i=0; i<data.rows(); i++) {
				if(data.get(i, 0)[0]!=0) {
					count++;
				}
			}
		}else if(data.rows()==1){
			//遍历一行，非背景元count++
			for(int i=0; i<data.cols(); i++) {
				if(data.get(0, i)[0]!=0) {
					count++;
				}
			}
		}		
		return count;
	}
	
	/**
	 * 获取字符的类似外接矩形
	 * @param mat 灰度图
	 * @return 矩形
	 */
	private static Rect getRect(Mat mat) {
		
		//确定矩形的上下边界
		int top=0;
		int bottom=mat.rows();
		boolean flag=false;
		for(int row=0; row<mat.rows(); row++) {
			for(int col=0; col<mat.cols(); col++) {
				if(mat.get(row, col)[0]>20) {
					top=row;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//下边界
		for(int row=mat.rows()-1; row>-1; row--) {
			for(int col=0; col<mat.cols(); col++) {
				if(mat.get(row, col)[0]>20) {
					bottom=row;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//左右边界
		int left=0;
		int right=mat.cols();
		for(int col=0; col<mat.cols(); col++) {
			for(int row=0; row<mat.rows(); row++) {
				if(mat.get(row, col)[0]>20) {
					left=col;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		flag=false;
		//右边界
		for(int col=mat.cols()-1; col>-1; col--) {
			for(int row=0; row<mat.rows(); row++) {
				if(mat.get(row, col)[0]>20) {
					right=col;
					flag=true;
					break;
				}
			}
			if(flag) {
				break;
			}
		}
		
		//获得矩形参数
		int x=left;
		int y=top;
		int width=right-left+1;
		int height=bottom-top+1;
		Rect rect=new Rect(x, y, width, height);
		return rect;
	}
	
	/**
	 * 将字符区域从原mat上面裁剪下来
	 * @param rect 要裁剪的区域
	 * @param mat 原图
	 * @return Mat
	 */
	private static Mat cutRect(Rect rect, Mat mat) {
		//创建一个返回的mat,将裁剪区域的图像依次复制过来
		Mat cutted=new Mat(mat, rect);	
		return cutted;
	}
	
	
	/**
	 * 测试上面的方法 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		createPatternLibrary();
//		//从字符库中依次的读入图片，并进行判定
		String fileName="E:\\ann\\chinese\\zh_shan\\debug_chineseMat542.jpg";
		Mat mat=Imgcodecs.imread(fileName);
		//先进行拉伸操作
		Mat dst=new Mat();
		Imgproc.resize(mat, dst, new Size(20, 32));
		Mat grayMat=new Mat();
		Imgproc.cvtColor(dst, grayMat, Imgproc.COLOR_RGB2GRAY);
		Mat binMat=new Mat();
		Imgproc.adaptiveThreshold(grayMat, binMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 25, -20);
		Imshow.imshow(binMat);
		thinning(binMat);
//		cuttedArea(binMat);
		
//		//
//		for(int row=0; row<binMat.rows(); row++) {
//			for(int col=0; col<binMat.cols(); col++) {
//				System.out.print(binMat.get(row, col)[0]+"\t");
//			}
//			System.out.println();
//		}
		
		
		//
//		for(int row=0; row<feature.length; row++) {
//			for(int col=0; col<feature[0].length; col++) {
//				System.out.print(feature[row][col]+"\t");
//			}
//			System.out.println();
//		}
	}

}
