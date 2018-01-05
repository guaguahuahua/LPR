package com.xjtu.util;

import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class ShowImage {
	
	static JFrame frame;
	/**
	 * 对传进来的图片进行显示
	 * @param bfImage
	 */
	public static void showImage(BufferedImage bfImage) {
		//创建显示窗口
		frame=new JFrame();
		//窗口的大小，参数依次是左上角坐标，宽度，高度
		frame.setBounds(100,100,1024,768);
		//用户单击关闭按钮时窗口执行的操作
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		//设置窗体是否可见，必须设置为true，否则窗体的存在将无意义
		frame.setVisible(true);
		
		//设置标签文本
		JLabel label=new JLabel("");
		//图像的大小，参数依次是左上角坐标，图像的宽度，高度
		label.setBounds(0, 0, 1024, 768);
		//设置窗口的显示的对象
		frame.getContentPane().add(label);
		label.setIcon(new ImageIcon(bfImage));
	}
	
	/**
	 * 方法的重载，添加了显示对象的名称
	 * @param bfImage BufferedImg
	 * @param name String
	 */
	public static void showImage(BufferedImage bfImage, String name) {
		//创建显示窗口
		frame=new JFrame();
		//窗口的大小，参数依次是左上角坐标，宽度，高度
		frame.setBounds(100,100,1024,768);
		//用户单击关闭按钮时窗口执行的操作
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		//设置窗体是否可见，必须设置为true，否则窗体的存在将无意义
		frame.setVisible(true);
		//设置窗体名字
		frame.setTitle(name);
		
		//设置标签文本
		JLabel label=new JLabel("");
		//图像的大小，参数依次是左上角坐标，图像的宽度，高度
		label.setBounds(0, 0, 1024, 768);
		//设置窗口的显示的对象
		frame.getContentPane().add(label);
		label.setIcon(new ImageIcon(bfImage));
	}
}
