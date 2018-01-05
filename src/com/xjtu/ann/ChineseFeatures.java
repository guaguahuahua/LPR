package com.xjtu.ann;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.Imshow;

public class ChineseFeatures {
	//定义水平和垂直方向
	private static final int VERTICAL=1;
	private static final int HORIZONTAL=0;
	
	/**
	 * 保证传递进来额图像是单通道的灰度图
	 * 获取字符特征
	 * @param grayImg 字符的灰度图
	 * @return 抽取的特征
	 */
	public static Mat chineseFeatures(Mat grayImg) {
		if(grayImg.channels()!=1) {
			Imgproc.cvtColor(grayImg, grayImg, Imgproc.COLOR_RGB2GRAY);
		}
		//首先对图像进行大小的转化过程
//		Imshow.imshow(grayImg);
		Mat resizedImg=new Mat();
		Rect rect=getRect(grayImg);
		Mat cuttedImg=cutRect(rect, grayImg);
//		Imshow.imshow(cuttedImg);
		//20*20--->32*20，图像变大，所以使用了线性插值的过程填补增大部分的区域
		Imgproc.resize(grayImg, resizedImg, new Size(20, 32), 0, 0, Imgproc.INTER_LINEAR);
		//这块是给后面投影做备份
		Mat mat=resizedImg.clone();
//		Imshow.imshow(mat);
		//
//		for(int row=0; row<resizedImg.rows(); row++) {
//			for(int col=0; col<resizedImg.cols(); col++) {
//				System.out.print(resizedImg.get(row, col)[0]+"\t");
//			}
//			System.out.println();
//		}
//		Imshow.imshow(mat);
		//首先将图像转化为浮点类型的图像
		Mat normImg=new Mat();
		resizedImg.convertTo(normImg, CvType.CV_32FC1);
		//对灰度图像整体像素点进行除以255操作
		double alpha=1.f/255;
		Mat meanImg=new Mat();
		normImg.convertTo(meanImg, CvType.CV_32FC1, alpha);
		//计算整个图像的均值
		Scalar means=Core.mean(meanImg);
		//在原图上减掉整副图像的均值
		for(int row=0; row<meanImg.rows(); row++) {
			for(int col=0; col<meanImg.cols(); col++) {
				//获得原图像在该位置的值，并求的元像素值与均值的差
				double diff=meanImg.get(row, col)[0]-means.val[0];
				//将他们的差值作为结果置入到原图像中
				meanImg.put(row, col, new float[] { (float) diff });				
			}
		}		
		//将原图像整体拉伸为1*640维向量,第一个参数是通道数目，如果为0表示与原来的通道数一样，
		Mat ori=meanImg.reshape(1, 1);
		//对原图像二值化
		Mat dst=new Mat();
		Imgproc.threshold(mat, dst, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY, 255, 0);
//		Imshow.imshow(mat);
//		Imshow.imshow(dst);
		//将二值图改变大小为32*32
		Imgproc.resize(dst, dst, new Size(32, 32), 0, 0, Imgproc.INTER_LINEAR);
		//进行水平垂直投影
		Mat vertical=project(dst, 0);
		Mat horizontal=project(dst, 1);	
		
		//建立最后返回的特征
		Mat feature=new Mat(1, (horizontal.cols()+vertical.cols()) + ori.cols(),  CvType.CV_32FC1);
		//依次的将提取的特征放置在最后的返回向量里面
		int index=0;
		for(int i=0; i<vertical.cols(); i++) {
			float val=(float) vertical.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		for(int i=0; i<horizontal.cols(); i++) {
			float val=(float) horizontal.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
				
		for(int i=0; i<ori.cols(); i++) {
			float val=(float) ori.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		return feature;
	}
	
	
	/**
	 * 提取图像的gabor特征，计划分4个方向，共4*20*20=1600维向量
	 * @param grayImg 灰度图
	 * @return Mat
	 */
	public static Mat gaborFeatures(Mat grayImg) {
		Mat features=new Mat(1, 400*4, CvType.CV_32FC1);
		Rect rect=getRect(grayImg);
		Mat cutted=cutRect(rect, grayImg);
//		Imshow.imshow(cutted);
		Mat resized=new Mat();
		Imgproc.resize(cutted, resized, new Size(20, 20));
		//获取gabor核
		Mat gaborKernal=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, 0, 2*Math.PI/3.3, 1);
		Mat dst=new Mat();
		Imgproc.filter2D(resized, dst, -1, gaborKernal);
		Mat gaborKernal1=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, Math.PI/4, 2*Math.PI/3.3, 1);
		Mat second=new Mat();
		Imgproc.filter2D(resized, second, -1, gaborKernal1);
		Mat gaborKernal2=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, Math.PI/2, 2*Math.PI/3.3, 1);
		Mat third=new Mat();
		Imgproc.filter2D(resized, third, -1, gaborKernal2);
		Mat gaborKernal3=Imgproc.getGaborKernel(new Size(4, 4), Math.PI, 3*Math.PI/4, 2*Math.PI/3.3, 1);
		Mat forth=new Mat();
		Imgproc.filter2D(resized, forth, -1, gaborKernal3);
		int index=0;
		//将20*20标准化，并放入到特征向量中去
		for(int row=0; row<dst.rows(); row++) {
			for(int col=0; col<dst.cols(); col++) {
				double d=dst.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		for(int row=0; row<second.rows(); row++) {
			for(int col=0; col<second.cols(); col++) {
				double d=second.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		for(int row=0; row<third.rows(); row++) {
			for(int col=0; col<third.cols(); col++) {
				double d=third.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		for(int row=0; row<forth.rows(); row++) {
			for(int col=0; col<forth.cols(); col++) {
				double d=forth.get(row, col)[0];
				features.put(0, index++, new float[] { (float) d/255 });
			}
		}
		//添加两个投影特征
//		Mat h=project(resized, 0);
//		Mat v=project(resized, 1);
//		for(int i=0; i<h.cols(); i++) {
//			double d=h.get(0, i)[0];
//			features.put(0, index++, new float[] { (float) d });
//		}
//		for(int i=0; i<v.cols(); i++) {
//			double d=v.get(0, i)[0];
//			features.put(0, index++, new float[] {(float) d});
//		}
		
		
		//
//		for(int i=0; i<features.cols(); i++) {
//			System.out.print(features.get(0, i)[0]+"\t");
//		}
//		Imshow.imshow(dst);
		return features;
	}
	/**
	 * 使用LBP和投影特征
	 * 方法1：直接使用lbp特征的投影直方图，即对得到的lbp特征矩阵进行一个灰度统计，每个灰度值的频率作为最终特征共计256个特征向量，外加水平垂直方向的投影
	 * 方法2：得到lbp特征矩阵之后，对整个灰度矩阵做归一化处理之后（除255），直接拉伸为1维向量，作为特征外加水平垂直投影
	 * 方法3：对lbp特征矩阵32*32进行分块，分为4*4的小块，对每个小块进行水平垂直投影
	 * @param grayImg
	 * @return Mat
	 */
	public static Mat LBPAndProjectFeatures(Mat grayImg) {
		if(grayImg.channels()!=1) {
			Imgproc.cvtColor(grayImg, grayImg, Imgproc.COLOR_RGB2GRAY);
		}
		//首先对图像进行大小的转化过程
		Mat resizedImg=new Mat();
		Rect rect=getRect(grayImg);
		Mat cuttedImg=cutRect(rect, grayImg);
//		Imshow.imshow(cuttedImg);
		//20*20--->32*20，图像变大，所以使用了线性插值的过程填补增大部分的区域
		Imgproc.resize(grayImg, resizedImg, new Size(32, 32), 0, 0, Imgproc.INTER_LINEAR);
		//这块是给后面投影做备份
		Mat mat=resizedImg.clone();
		//获取图像LBP特征
		int radius=1;
		int neighbor=8;
		Mat lbp=new Mat(resizedImg.rows(), resizedImg.cols(), CvType.CV_32FC1);
		elbp(resizedImg, lbp, radius, neighbor);
		int indice=0;
		//这块主要是放置特征，小区块一共64个，每个里面提取8个特征
		float []pFeature=new float[64*8];
		for(int i=0; i<8; i++) {
			for(int j=0; j<8; j++) {
				//裁剪4*4小区域,并进行水平和垂直方向的投影
				Mat temp=new Mat(lbp, new Range(4*i, 4*i+4), new Range(4*j, 4*j+4));
				Mat h=project(temp, HORIZONTAL);
				Mat v=project(temp, VERTICAL);
				//将水平和垂直方向的投影存放在数组中
				for(int k=0; k<h.cols(); k++) {
					double t=h.get(0, k)[0];
					pFeature[indice++]=(float) t;
				}
				for(int k=0; k<v.cols(); k++) {
					double t=v.get(0, k)[0];
					pFeature[indice++]=(float) t;
				}
			}
		}
		//输出一下抽取的新的特征
//		for(int i=0; i<pFeature.length; i++) {
//			System.out.print(pFeature[i]+"\t");
//		}
		
		
//		lbp.convertTo(lbp, CvType.CV_8UC1); //这块转化主要是为了显示图像
//		Imshow.imshow(lbp);
		//用来统计lbp中像素出现的频数
		//对lbp特征进行一个处理，归一化
//		for(int row=0; row<lbp.rows(); row++) {
//			for(int col=0; col<lbp.cols(); col++) {
////				table[(int) lbp.get(row, col)[0]]++;
//				double val=lbp.get(row, col)[0];
//				lbp.put(row, col, new float[] {(float) ( val/255 )});
//			}
//		}
		//拉伸为一行向量
//		Mat reshaped=lbp.reshape(1, 1);
//		int max=Integer.MIN_VALUE;
//		//找到频数最大的
//		for(int i=0; i<table.length; i++) {
//			if(table[i]>max) {
//				max=table[i];
//			}
//		}
//		float []features=new float[256];
//		//归一化,得到特征
//		if(max>0) {
//			for(int i=0; i<table.length; i++) {
//				features[i]=(float) table[i]/max;
//			}
//		}
		//对原图像二值化
		Mat dst=new Mat();
		Imgproc.threshold(mat, dst, Imgproc.THRESH_OTSU+Imgproc.THRESH_BINARY, 255, 0);

		
		//将二值图改变大小为32*32
		Imgproc.resize(dst, dst, new Size(32, 32), 0, 0, Imgproc.INTER_LINEAR);
		//进行水平垂直投影
		Mat vertical=project(dst, 0);
		Mat horizontal=project(dst, 1);	
		
		//建立最后返回的特征
		Mat feature=new Mat(1, (horizontal.cols()+vertical.cols()+pFeature.length) ,  CvType.CV_32FC1);
		//依次的将提取的特征放置在最后的返回向量里面
		int index=0;
		for(int i=0; i<vertical.cols(); i++) {
			float val=(float) vertical.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		for(int i=0; i<horizontal.cols(); i++) {
			float val=(float) horizontal.get(0, i)[0];
			feature.put(0, index++, new float[] { val });
		}
		for(int i=0; i<pFeature.length; i++) {
			feature.put(0, index++, new float[] {pFeature[i]});
		}
		
//		for(int i=0; i<features.length; i++) {
//			feature.put(0, index++, new float[] { features[i] });
//		}		
		
//		for(int i=0; i<reshaped.cols(); i++) {
//			float val=(float) reshaped.get(0, i)[0];
//			feature.put(0, index++, new float[] { val });
//		}
		return feature;
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
	 * 使用lbp算法提取特征
	 * @param src 输入图像，
	 * @param dst 输入图像
	 * @param radius 半径
	 * @param neighbors 计算当前点的LBP值所需要的领域像素点的数目
	 */
	private static void elbp(Mat src, Mat dst, int radius, int neighbors) {
		
		for(int n=0; n<neighbors; n++) {
			float x=(float) (-radius*Math.sin(2.0*n*Math.PI/(float)(neighbors)));
			float y=(float) ( radius*Math.cos(2.0*n*Math.PI/(float)(neighbors)));
			//取整
			int fx=(int) Math.floor(x);
			int fy=(int) Math.floor(y);
			int cx=(int) Math.ceil(x);
			int cy=(int) Math.ceil(y);
			
			//小数部分
			float ty=y-fy;
			float tx=x-fx;
			
			//设置插值权重
			float w1=(1-tx)*(1-ty);
			float w2=tx*(1-ty);
			float w3=(1-tx)*ty;
			float w4=tx*ty;
			
			//循环处理图像数据
			for(int i=radius; i<src.rows()-radius; i++) {
				for(int j=radius; j<src.cols()-radius; j++) {
					//计算插值
					float t=(float) (w1*src.get(i+fy, j+fx)[0]+w2*src.get(i+fy, j+cx)[0]
							+w3*src.get(i+cy, j+fx)[0]+w4*src.get(i+cy, j+cx)[0]);
					//编码
					double a= dst.get(i-radius, j-radius)[0];
					if(t-src.get(i, j)[0] >= 0) {
						a+=(1<<n);
					}else {
						a+=0;
					}
					dst.put(i-radius, j-radius, new float[] { (float) a });
				}
			}
		}
	}
	
	/**
	 * 测试上面的方法
	 * @param args
	 */
	public static void main(String []args) {
		//System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		System.load("C:\\opencv-3.3.0\\build\\java\\x64\\opencv_java330.dll");
		//处理流程图像的位置
		String file = "E:\\ann\\chinese\\zh_cuan\\chuan_11.jpg";
//		String file="E:\\ImagesFromAndroid\\1513257059606.jpg";
		Mat img=Imgcodecs.imread(file);
		Mat grayImg=new Mat();
		Imgproc.cvtColor(img, grayImg, Imgproc.COLOR_RGB2GRAY);
		gaborFeatures(grayImg);
		
//		Mat features=LBPAndProjectFeatures(img);
//		
//		
//		System.out.println("全部结果："+features.cols());
//		for(int i=0; i<features.cols(); i++) {
//			System.out.print(features.get(0, i)[0]+"\t");
//		}
		
//		Imshow.imshow(img);
//		int radius=1;
//		int neighbor=8;
//		Mat dst=new Mat(img.rows(), img.cols(), CvType.CV_32FC1);
//		elbp(img, dst, radius, neighbor);
//		dst.convertTo(dst, CvType.CV_8UC1);
//		Imshow.imshow(dst);
	}
	
	
}
