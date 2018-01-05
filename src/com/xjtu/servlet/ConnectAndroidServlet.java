package com.xjtu.servlet;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xjtu.Test;

/**
 * Servlet implementation class ConnectAndroid
 */
@WebServlet("/ConnectAndroid")
public class ConnectAndroidServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String dir=null;
	private File folderFile=null;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConnectAndroidServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
//		response.getWriter().append("Served at: ").append(request.getContextPath());
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		System.out.println("android �������ͨ�ųɹ�������");
//		response.getOutputStream().print("ok");
		InputStream inputStream=request.getInputStream();
		inputStream.skip(54);
		//����ļ�����
		byte []fileName=new byte[19];		
		inputStream.read(fileName, 0, 19);
		inputStream.skip(4);
		String name="";
		for(int i=1; i<fileName.length-1; i++) {
			name+=(char) fileName[i];
		}
		String path="E:/ImagesFromAndroid";
		//�������android��ͼ���ļ��ڷ���˵Ĵ�ŵ�ַ·��
		FileOutputStream outputStream=new FileOutputStream(new File(path, name));
		//д����
		byte[]b=new byte[1024];
		//д���ļ��ĳ���
		int len=0;
		//�����յ���ͼ���ļ�д�뵽����������
		while((len=inputStream.read(b))!=-1) {
			outputStream.write(b, 0, len);
		}
		//�ر����������
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		
		String filePath=path+"/"+name; 
		//���濪ʼ���÷���˴������ͼ������̣�����ͼ���ʶ�������ַ�������ʽ���з���
		String str=Test.test(filePath);
		System.out.println("���ط���������Ϣ��"+str);
		//��ʶ����ת��Ϊ�ֽ���
		//�������˽�������ظ�android��
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out=response.getWriter();
		out.println(str);
//		response.getOutputStream().print("ok");
		
		//����˵��һ�£�����Ǵ���©���ģ���Ϊֻ��ʵ���ˣ����������164Bֻ��������ͼ���������Ϣ�ģ������������ͼ����Ϣ�Ƿ�Ϊ164B�� ���ǲ�ȷ���ģ�����ֻ��˵��ʵ��
		//����ǰ������õ�������Ϣ��ֱ�ӵ��ϴ����ļ�����λ��

	}
}
