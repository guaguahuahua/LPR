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
	//ͼƬ��ľ��Ե�ַ
	private static final String path="E:\\ann\\library";
	//�Զ�ά�������ʽ������е�ģ�������
	public static float [][]feature=new float[31][52+40]; 
	//�ַ���
    static String []cCharacter= {"��","��","��","��", "��","��","��","��","��","��","��","ԥ","��","��",
    		"��","��","��","��","��","��","��","��","³","��","��","��","��",      
            "��", "��","��","��"  
            };
	
	/**
	 * ���ʹ��ģ��ƥ����ȡ��ǰ�ַ���������ģ�����бȶ�
	 * @param mat ����һ���ַ���matͼ��
	 * return int
	 */
	public static int patternMatch(Mat mat) {
		
		
		//��ȡmat������
		Rect rect=getRect(mat);
		Mat cutted=cutRect(rect, mat);
		Mat resized=new Mat();
		Imgproc.resize(cutted, resized, new Size(20, 32));
		//�ҶȻ�����ֵ��
		Mat grayMat=new Mat();
		Mat binMat=new Mat();
		if(mat.channels()!=1) {
			Imgproc.cvtColor(resized, grayMat, Imgproc.COLOR_RGB2GRAY);
			Imgproc.adaptiveThreshold(grayMat, binMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 25, -20);
		}else {
			binMat=resized;
		}
		System.out.println("cols, rows: "+binMat.cols()+", "+binMat.rows());
		//ˮƽ��ֱͶӰ
		Mat h=project(binMat, 0);
		Mat v=project(binMat, 1);
		float []oFeature=cuttedArea(binMat);
		//��ŵ�ǰ����
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
		//���ŷ�Ͼ�����С���Ǹ��ַ���λ��
		float min=Integer.MAX_VALUE;
		int pos=-1;
		//���㵱ǰ������ģ�����ŷ�Ͼ��룬���ҵ���С���Ǹ�
		for(int start=0; start<feature.length; start++) {
			//�ۼ�ƽ���͵���ʱ����
			float temp=0;
			//����ŷʽ����
			for(int j=0; j<feature[start].length; j++) {
				temp+=Math.pow((feature[start][j]-cur[j]), 2);
			}
			//�ж�
			if(temp<min) {
				System.out.println("���룺 "+temp);
				min=temp;
				pos=start;
			}
		}
		return pos;
	}
	
	/**
	 * ����Ǵ���ģ���
	 */
	public static void createPatternLibrary() {
		//����Ǵ��ļ�·���м������е�ͼ��
		File file=new File(path);
		File []fileArray=file.listFiles();
//		System.out.println("len: "+fileArray.length);
		//�����ļ����飬������е��ļ��ľ��Ե�·��
		for(int i=0; i<fileArray.length; i++) {
			//��õ����ļ��ľ���·��
			String singleFile=fileArray[i].getAbsolutePath();
//			System.out.println(singleFile);
			//�������ļ���mat��ʽ����
			Mat fileMat=Imgcodecs.imread(singleFile);
//			Imshow.imshow(fileMat);
			//��ȡ���������
			Rect rect=getRect(fileMat);
			Mat detail=cutRect(rect, fileMat);
			//�ü����򲢽�������任
			Mat resized=new Mat();
			Imgproc.resize(detail, resized, new Size(20, 32));
			//�ҶȻ�����ֵ��
			Mat grayMat=new Mat();
			Imgproc.cvtColor(resized, grayMat, Imgproc.COLOR_RGB2GRAY);
			Mat binMat=new Mat();
			Imgproc.adaptiveThreshold(grayMat, binMat, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 25, -20);
//			Imshow.imshow(binMat);
			
			float [] oFeature=cuttedArea(binMat);
			
			//ˮƽ��ֱͶӰ
			Mat h=project(binMat, 0);
			Mat v=project(binMat, 1);
			//�������ϲ�����ڸ���������
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
	 * ��ԭͼ��������зֿ飬��ͳ��ÿһ���ֿ��ڵ�����������������ֵ��һ��
	 * @param mat ����Ķ�ֵͼ
	 * @return float[] 40ά��������
	 */
	private static float[] cuttedArea(Mat mat) {
		//�������������
		float []oFeature=new float[40];
		int index=0; //����
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
				//��ȡ���ֵ
				if(max<count) {
					max=count;
				}
				oFeature[index++]=count;				
			}
		}
//		System.out.println("�ֿ�����");
		//��һ��
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
	 * ϸ���㷨����ԭ�ַ�ϸ������ȡ����
	 * @param mat ��ֵ����mat
	 * @return Mat
	 */
	private static Mat thinning(Mat mat) {	
		
		for(int i=0; i<5; i++) {
			
		//original����ԭͼ��ı���
		Mat original=mat.clone();
		//�������fpa�㷨
		for(int row=1; row<mat.rows()-1; row++) {
			for(int col=1; col<mat.cols()-1; col++) {
				//�ж�һ�¸����ص��Ƿ�Ϊ����
				if(mat.get(row, col)[0]!=0) {
					String zero="";
					//8��λ�õ��ж�
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
					//0��1�������ж�
					int zeroCount=0;
					for(int i1=0; i1<zero.length()-1; i1++) { //������ܻᷢ��Խ�������
						String sub=zero.substring(i1, i1+2);
						if(sub.equals("01")) {
							zeroCount++;
						}
					}
					//���Ͻǵ���������
					int r1=(int) (mat.get(row-1, col)[0]*mat.get(row, col+1)[0]*mat.get(row+1, col)[0]);
					int r2=(int) (mat.get(row, col+1)[0]*mat.get(row+1, col)[0]*mat.get(row, col-1)[0]);
					//�ж��Ƿ�ͬʱ����4������
					if((count>=2 && count<=6) && (zeroCount==1) && (r1==0) && (r2==0)) {
						original.put(row, col, new byte[] { 0 });
					}
				}
			}
		}
		//�ڶ���ɨ�裬ȥ�������ǵ����ص�
		Mat second=original.clone();
		for(int row=1; row<original.rows()-1; row++) {
			for(int col=1; col<original.cols()-1; col++) {
				if(original.get(row, col)[0]!=0) {
					String zero="";
					//8��λ�õ��ж�
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
					//0��1�������ж�
					int zeroCount=0;
					for(int i1=0; i1<zero.length()-1; i1++) {
						String sub=zero.substring(i1, i1+2);
						if(sub.equals("01")) {
							zeroCount++;
						}
					}
					//�����ǵ���������
					int r1=(int) (original.get(row-1, col)[0]*original.get(row, col+1)[0]*original.get(row, col-1)[0]);
					int r2=(int) (original.get(row-1, col)[0]*original.get(row+1, col)[0]*original.get(row, col-1)[0]);
					//�ж��Ƿ�ͬʱ����4������
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
	 * ˮƽ�ʹ�ֱ�������ͶӰ������ͶӰ���й��
	 * @param mat ԭͼ��
	 * @param direction ͶӰ����, �涨0Ϊˮƽ��1Ϊ��ֱ
	 * @return double[]
	 */
	public static Mat project(Mat mat, int direction) {
		//��¼�����������������Ĵ�С��������
		int col=direction==0 ? mat.cols() : mat.rows();
		Mat features=new Mat(1, col, CvType.CV_32FC1);
		//��ű���������ı���
		int end=0;
		if(direction==0) {
			end=mat.cols(); //ˮƽ����
		}else {
			end=mat.rows(); //��ֱ����
		}
		int max=0;
		//���շ���ͶӰ
		for(int i=0; i<end; i++) {
			Mat data= (direction==0) ? mat.col(i) : mat.row(i);
			int featI=countObject(data);
			//ȡˮƽ���ߴ�ֱ�����ͶӰ�е����ֵ
			max=featI > max ? featI : max;
			features.put(0, i, new float[] { featI });
		}
		//���
		if(max>0) {
			for(int i=0; i<features.cols(); i++) {
				double val=features.get(0, i)[0] / max;
				features.put(0, i, new float[] { (float) val});
			}
		}
		return features;
	}
	
	/**
	 * ���ʹ�ûҶ�����ķ�ʽ����Ϊͳ��������������ÿһ�еĶ����ĸ���
	 * @param mat ��ֵ��ͼ��
	 * @param direction ͶӰ�ķ���0��ʾˮƽ��1��ʾ��ֱ
	 * @return Mat
	 */
	private static Mat project_1(Mat mat, int direction) {
		//�õ�������������
		int col= direction==0 ? mat.cols() : mat.rows();
		//������ŷ��صĶ���
		Mat features=new Mat(1, col, CvType.CV_32FC1);
		int index=0;
		int max=0;
		//���շ���ͶӰ
		for(int i=0; i<col; i++) {
			//��ȡĳһ���л�����ĳһ��
			Mat data= direction==0 ? mat.col(i) : mat.row(i);
			//ͳ����һ�л����еĻҶ��������
			int nums=count(data);
			//��¼һ�л���һ�������ֵ
			max= nums>max ? nums : max;
			features.put(0, index++, new float[] { nums });
		}
		//���
		if(max>0) {
			for(int i=0; i<features.cols(); i++) {
				float val=(float) (features.get(0, i)[0] / max);
				features.put(0, i, new float[] { val });
			}
		}
		
		
		return features;
	} 
	
	/**
	 * ͳ��һ�л�����һ�лҶ�����Ĵ���
	 * @param mat һ�л���һ�е�����
	 * @return int
	 */
	private static int count(Mat mat) {
		
		int times=0;
		//�����һ��
		if(mat.cols()==1) {
			for(int row=1; row<mat.rows(); row++) {
				//����������еĻҶȷ���������
				if(mat.get(row, 0)[0] != mat.get(row-1, 0)[0]) {
					times++;
				}
			}
		//�����һ��	
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
	 * ����һ�л�����һ���зǱ���Ԫ�صĸ���
	 * @param data һ�л�����һ��Ԫ��
	 * @return int
	 */
	public static int countObject(Mat data) {
		int count=0;
		if(data.cols()==1) { //������һ������
			//������һ����������Ǳ�������ôcount++
			for(int i=0; i<data.rows(); i++) {
				if(data.get(i, 0)[0]!=0) {
					count++;
				}
			}
		}else if(data.rows()==1){
			//����һ�У��Ǳ���Ԫcount++
			for(int i=0; i<data.cols(); i++) {
				if(data.get(0, i)[0]!=0) {
					count++;
				}
			}
		}		
		return count;
	}
	
	/**
	 * ��ȡ�ַ���������Ӿ���
	 * @param mat �Ҷ�ͼ
	 * @return ����
	 */
	private static Rect getRect(Mat mat) {
		
		//ȷ�����ε����±߽�
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
		//�±߽�
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
		//���ұ߽�
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
		//�ұ߽�
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
		
		//��þ��β���
		int x=left;
		int y=top;
		int width=right-left+1;
		int height=bottom-top+1;
		Rect rect=new Rect(x, y, width, height);
		return rect;
	}
	
	/**
	 * ���ַ������ԭmat����ü�����
	 * @param rect Ҫ�ü�������
	 * @param mat ԭͼ
	 * @return Mat
	 */
	private static Mat cutRect(Rect rect, Mat mat) {
		//����һ�����ص�mat,���ü������ͼ�����θ��ƹ���
		Mat cutted=new Mat(mat, rect);	
		return cutted;
	}
	
	
	/**
	 * ��������ķ��� 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
//		createPatternLibrary();
//		//���ַ��������εĶ���ͼƬ���������ж�
		String fileName="E:\\ann\\chinese\\zh_shan\\debug_chineseMat542.jpg";
		Mat mat=Imgcodecs.imread(fileName);
		//�Ƚ����������
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
