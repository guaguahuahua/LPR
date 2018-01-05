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
		System.out.println("android 向服务器通信成功！！！");
//		response.getOutputStream().print("ok");
		InputStream inputStream=request.getInputStream();
		inputStream.skip(54);
		//存放文件名称
		byte []fileName=new byte[19];		
		inputStream.read(fileName, 0, 19);
		inputStream.skip(4);
		String name="";
		for(int i=1; i<fileName.length-1; i++) {
			name+=(char) fileName[i];
		}
		String path="E:/ImagesFromAndroid";
		//下面的是android端图像文件在服务端的存放地址路径
		FileOutputStream outputStream=new FileOutputStream(new File(path, name));
		//写缓存
		byte[]b=new byte[1024];
		//写入文件的长度
		int len=0;
		//将接收到的图像文件写入到服务器本地
		while((len=inputStream.read(b))!=-1) {
			outputStream.write(b, 0, len);
		}
		//关闭输入输出流
		inputStream.close();
		outputStream.flush();
		outputStream.close();
		
		String filePath=path+"/"+name; 
		//下面开始调用服务端代码进行图像处理过程，并将图像的识别结果以字符串的形式进行返回
		String str=Test.test(filePath);
		System.out.println("返回服务器的信息："+str);
		//将识别结果转化为字节流
		//服务器端将结果返回给android端
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out=response.getWriter();
		out.println(str);
//		response.getOutputStream().print("ok");
		
		//首先说明一下，这块是存在漏洞的，因为只是实现了，这块跳过的164B只是跳过了图像的属性信息的，但是再往后的图像信息是否为164B？ 那是不确定的，所以只能说是实现
		//跳过前面的无用的属性信息，直接到上传的文件名称位置

	}
}
