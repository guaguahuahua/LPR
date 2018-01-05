package com.xjtu.charseperate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.xjtu.util.DrawBoundingRectangle;
import com.xjtu.util.Imshow;

public class SeperateChars {

	
	/**
	 * 车牌字符的切分，首先二值化，然后使用找轮廓的方式进行切分
	 * @param plateImg 车牌的图像
	 * @param charsImg 切分开之后所有字符的集合
	 */
	public static int seperateChars(Mat plateImg, List<Mat> charsImg) {
		Imshow.imshow(plateImg);
		//灰度化
		Mat grayImg=new Mat();
		Imgproc.cvtColor(plateImg, grayImg, Imgproc.COLOR_BGR2GRAY);
//		Imshow.imshow(grayImg);
		//二值化
		Mat binaryImg=new Mat();
		//灰度图，二值图，二值图的较大值，使用什么样的局部自适应方法
		Imgproc.adaptiveThreshold(grayImg, binaryImg, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 25, -20);//25,10，25，0
		Imshow.imshow(binaryImg);
		Mat noRivetImg=cleanRivet(binaryImg);
		//主要是为了防止清除柳钉的过程中发现了未定位到车牌的图像
		if(noRivetImg!=null) {
			Imshow.imshow(noRivetImg);
		}else {
			System.out.println("SeperateChars.java:"+"清除铆钉的过程发现不是车牌图像");
			return -1;
		}
		//定位特殊字符，并且切分车牌所有字符
		findSpecificContour(noRivetImg, charsImg);
		return 1;
	}
	
	/**
	 * 清除柳钉
	 * @param binaryMat
	 * @return
	 */
	public static Mat cleanRivet(Mat binaryMat) {
		//记录每一行灰度跳变的次数
		int count;
		//存放每一行跳变的次数
		List<Integer> times=new ArrayList<Integer>();
		//记录所有的白色像素点
		int white=0;
		for(int row=0; row<binaryMat.rows(); row++) {
			count=0;
			for(int col=0; col<binaryMat.cols()-1; col++) {
				//一旦两个相邻像素不相等，认为发生了一次跳变
				if(binaryMat.get(row, col)[0]!=binaryMat.get(row, col+1)[0]) {
					count++;
				}
				//如果不为背景，统计次数+1
				if(binaryMat.get(row, col)[0]==255) {
					white++;
				}
			}
			//将第row行的跳变的次数存放到动态数组中
			times.add(count);
		}
		//查看每一行灰度跳变的次数，如果符合阈值跳变的行数少于0.6，那么认为这张图定位失败了
		int jumps=0;
		for(int i=0; i<times.size(); i++) {
			if(times.get(i)>=16 && times.get(i)<=45) {
				jumps++;
			}
		}
		if(jumps*1.0/times.size()*1.0<0.4) {
			System.out.println("SeperateChars.java:"+"跳变行数不合条件：定位到的区域不是车牌，请重新拍照");
			return null;
		}
		//查看白色对象区域面积占比
		if(white*1.0/(binaryMat.rows()*binaryMat.cols())<0.15 || white*1.0/(binaryMat.rows()*binaryMat.cols())>0.5) {
			System.out.println("SperateChars.java:"+"对象区域面积占比不合条件:定位到的区域不是车牌，请重新拍照");
			return null;
		}
		//将柳钉所在的行置为背景，消除影响
		for(int i=0; i<times.size(); i++) {
			int t=times.get(i);
			//如果跳变次数<16 那么就将该行置为背景
			if(t<16) {
				for(int col=0; col<binaryMat.cols(); col++) {
					binaryMat.put(i, col, new byte[] {0});
				}
			}
		}
		return binaryMat;
	}
	
	/**
	 * 定位特殊字符，并且切分车牌所有字符
	 * @param noRivetMat
	 */
	public static void findSpecificContour(Mat noRivetMat, List<Mat>chars) {
		//记录字符轮廓中边框中最大的宽和高
		int width=0;
		int height=0;
		//下面主要是为了存放查找到的轮廓做准备
		List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
		Mat hierarchy=new Mat();
		//轮廓查找
		Imgproc.findContours(noRivetMat, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		//存放所有的外接矩形
		List<Rect> rectangles=new ArrayList<Rect>();
		//获得所有轮廓的垂直外接矩形		
		for(int i=0; i<contours.size(); i++) {
			//获取一个轮廓的垂直外界矩形
			Rect rect=Imgproc.boundingRect(contours.get(i));
			//这块是获得字符轮廓中最大的宽和高，然后用他们作为裁剪字符的标准宽和高
			if(rect.width>width) {
				width=rect.width;
			}
			if(rect.height>height) {
				height=rect.height;
			}
			rectangles.add(rect);			
		}
		//输出找到垂直外界轮廓的数量
		System.out.println("外接矩形的集合大小为："+rectangles.size());
		//绘制字符的外接轮廓
//		DrawBoundingRectangle.drawBoundingRectangle_1(noRivetMat, rectangles);
		//展示绘制了外接矩形的车牌图像
		Imshow.imshow(noRivetMat);
		//上面已经找到了所有的外接矩形，现在要做的就是找到中文字符前面的那个英文字符，从而得到中文字符，并且将坐标大于该英文字符的
		//找到特殊字符的位置
		List<Integer> index=findSpecificChar(noRivetMat, rectangles, width, height);
		if(index.get(0)<19) {
			System.out.println("SeperateChars:图像噪点太多，请重新拍照");
			return;
		}
		System.out.println("特殊位置的字符坐标："+index);
		//存放切分出来的7个字符
//		List<Mat> chars=new ArrayList<Mat>();
		//计算中文字符的位置
		int x=(int) (index.get(0)-1.15*width);
		//这是为了防止线型的噪点，线型的噪点会造成负值  13是字符的宽度
		boolean flag=false;
		if(x<0) {
			x=(int) (index.get(0)-1.15*14);
			//一旦噪点很多，就需要时默认值，而不能使用最大的外接矩形去裁剪，否则会出现越界
			flag=true; 
		}
		//裁剪中文字符
		Rect cut=new Rect(x, index.get(1), width, height);
		//噪点多，使用默认值，防止越界
		if(flag) {
			cut=new Rect(x, index.get(1), 14, 21);
		}
		Mat chinese=new Mat(noRivetMat, cut);
		chars.add(chinese);
		//裁剪特殊字符
		Rect sCut=new Rect(index.get(0), index.get(1), width, height);
		//噪点多，使用默认值，防止越界
		if(flag) {
			sCut=new Rect(index.get(0), index.get(1), 14, 21);
		}
		Mat special=new Mat(noRivetMat, sCut);
		chars.add(special);
		//对轮廓进行x的排序，方便后面筛出字符,给很大的空间主要是为了防止后面的噪点影响
		List<Integer>xIndex=new ArrayList<Integer>();
		for(int i=0; i<rectangles.size(); i++) {
			Rect t=rectangles.get(i);
			//只对x坐标大于特殊字符的轮廓进行排序
			if(t.x>index.get(0)) {
				xIndex.add(t.x);
			}
		}
		//将动态数组转化为数组进行排序
		Object []charIndex=xIndex.toArray();
		Arrays.sort(charIndex);
		//为了防止噪点和字符坐标相同时，会将统一个字符输出多次，我们在某位置剪切结束之后更新后面比对的x坐标的值
		int renewIndex=index.get(0);
		//遍历轮廓集，将横坐标大于特殊字符的5个字符切分出来
		for(int k=0, i=0; k<charIndex.length && i<5; k++) {
			for(int j=0; j<rectangles.size(); j++) {
				//从中取出一个矩形
				Rect r=rectangles.get(j);
				//判断这个矩形是否在特殊矩形的右侧，并且不是噪点
				if((int)charIndex[k]==r.x && r.x>renewIndex && (r.height>0.8*height)) {
					//裁剪字符还是应该按照字符的外接矩形的大小进行裁剪，否则按照最大的边框可能出现越界的这种情况，还有可能裁到其他字符
					//裁剪该区域
					Mat oridinary=new Mat(noRivetMat, r);
//					Imshow.imshow(oridinary);
					chars.add(oridinary);
					//车牌字符数量++，到5结束，之前已经裁剪了中文和特殊字符
					i++;
					renewIndex=r.x;
				}
			}
		}
//		for(Mat K: chars) {
//			Imshow.imshow(K);
//		}
	}
	/**
	 * 寻找位于整幅图像1/7--2/7之间的字符
	 * @param noRivetMat 清除铆钉之后的二值图像
	 * @param rectangles 所有字符的外接矩形数组
	 * @param maxWidth   外接矩形的最大宽
	 * @param maxHeight  外接矩形的最大高
	 * @return
	 */
	public static List<Integer> findSpecificChar(Mat noRivetMat, List<Rect> rectangles, int maxWidth, int maxHeight) {		
		//记录特殊字符的位置，0为x坐标，1为y坐标
		List<Integer> position=new ArrayList<Integer>();
		//计算最大边框的0.8宽和高，主要是为了防止噪点的轮廓
		double width=(double) (0.8*maxWidth);
		double height=(double) (0.8*maxHeight);
		//如果x的位置位于整副图像的1/7到2/7之间，那么认为这个矩形是中文字符右侧的矩形
		double se1=(double) (noRivetMat.cols())/7;
		double se2=(double) (noRivetMat.cols()*2)/7;
		System.out.println(se1+", "+se2);
		//遍历所有的轮廓，找到一个轮廓的外接矩形的x坐标在整个图像的1/7--2/7之间
		for(int i=0; i<rectangles.size(); i++) {
			//首先取出一个外接垂直矩形
			Rect rect=rectangles.get(i);
			//求这个矩形的中心x坐标位置
			int x=(int) (rect.x+(double) (rect.width*0.5));
			if((rect.width> width || rect.height>height) && (x>se1 && x<se2)) {
				if(position.isEmpty()) {
					position.add(rect.x);
					position.add(rect.y);
				}else {
					if(position.get(0)<rect.x) {
						position.clear();
						position.add(rect.x);
						position.add(rect.y);
					}
				}
//				//一旦找到第一个位置，后面就不用再次查找，结束遍历
//				break;
			}
		}
		return position;
	} 
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
