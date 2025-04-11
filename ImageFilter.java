/*
 * The reading a image file code was taken from my Project 1 code for Q2
 * The raster was obtained from https://stackoverflow.com/questions/54296035/how-can-i-read-every-pixel-of-a-writableimage-with-raster
 */
package com.mycompany.imagefilter;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author birfa
 */
public class ImageFilter extends JFrame{
    private File selectedFile;
    private JPanel ImagePanel;
    BufferedImage Image;
    BufferedImage ImageLeft;
    BufferedImage ImageRight;
    private int Width;
    private int Height;
    private int CurrentStage;
    private int[][] DitherMatrix;

    public ImageFilter() {
        setTitle("ImageFilter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1600, 1000);
        setResizable(false);
        JButton openButton = new JButton("Open .TIF File");
        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.getName().toLowerCase().endsWith(".tif") || f.isDirectory();
                    }
                    @Override
                    public String getDescription() {
                        return "TIF Files";
                    }
                });
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    selectedFile = fileChooser.getSelectedFile();
                    loadImage(selectedFile);
                    CurrentStage = 0;
                    SetMatrix();
                    DrawImage(CurrentStage);
                    ImagePanel.repaint();
                }
            }
        });
        JButton EXITButton = new JButton("EXIT");
        EXITButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        JButton NextButton = new JButton("Next");
        NextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(Image != null){
                CurrentStage++;
                CurrentStage = CurrentStage % 4;
                DrawImage(CurrentStage);
                ImagePanel.repaint();
            }
            }
        });
        ImagePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                displayImage(g);
            }
        };
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(0,3));
        buttonPanel.add(openButton);
        buttonPanel.add(EXITButton);
        buttonPanel.add(NextButton);
        setLayout(new BorderLayout());
        add(ImagePanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void displayImage(Graphics g) {
        if (ImageLeft != null) {
            int xshift = ((ImagePanel.getWidth())/2 - Width)/2;
            int yshift = (ImagePanel.getHeight() - Height)/2;
            g.drawImage(ImageLeft, xshift, yshift, this);
            g.drawImage(ImageRight, xshift+((ImagePanel.getWidth())/2), yshift, this);
        }
    }

    private void loadImage(File file) {
        try {
            Image = ImageIO.read(file);
            Width = Image.getWidth();
            Height = Image.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void DrawImage(int Stage){
        switch (Stage) {
            case 0:
                DrawStage1();
                break;
            case 1:
                DrawStage2();
                break;
            case 2:
                DrawStage3();
                break;
            case 3:
                DrawStage4();
                break;
        }
    }
    
    private void DrawStage1(){
        ImageLeft = Image;
        ImageRight = new BufferedImage(Width,Height,BufferedImage.TYPE_INT_RGB);
        Raster Pixels = Image.getData();
        int[] RGBarray = new int[3];
        for(int i = 0;i<Height;i++){
            for(int j = 0;j<Width;j++){
                Pixels.getPixel(j, i, RGBarray);
                int[] YUVvalues = RGBtoYUV(RGBarray);
                int RGBImage = (256*256*YUVvalues[0])+(256*YUVvalues[0])+(YUVvalues[0]);
                ImageRight.setRGB(j, i, RGBImage);
            }
        }
    }
    
    private void DrawStage2(){
        ImageLeft = new BufferedImage(Width,Height,BufferedImage.TYPE_INT_RGB);
        ImageRight = new BufferedImage(Width,Height,BufferedImage.TYPE_INT_RGB);
        Raster Pixels = Image.getData();
        int[] RGBarray = new int[3];
        for(int i = 0;i<Height;i++){
            for(int j = 0;j<Width;j++){
                Pixels.getPixel(j, i,RGBarray);
                int[] YUVvalues = RGBtoYUV(RGBarray);
                int tmp = YUVvalues[0]/2;
                YUVvalues[0] = tmp;
                int[] RGBvalues = YUVtoRGB(YUVvalues);
                int RGBImageRight = (256*256*YUVvalues[0])+(256*YUVvalues[0])+(YUVvalues[0]);
                int RGBImageLeft = (256*256*(RGBvalues[0]))+(256*(RGBvalues[1]))+(RGBvalues[2]);
                ImageRight.setRGB(j, i, RGBImageRight);
                ImageLeft.setRGB(j, i, RGBImageLeft);
                
            }
        }
    }
    
    private void DrawStage3(){
        ImageLeft = new BufferedImage(Width,Height,BufferedImage.TYPE_INT_RGB);
        ImageRight = new BufferedImage(Width,Height,BufferedImage.TYPE_INT_RGB);
        Raster Pixels = Image.getData();
        int[] RGBarray = new int[3];
        for(int i = 0;i<Height;i++){
            for(int j = 0;j<Width;j++){
                Pixels.getPixel(j, i, RGBarray);
                int[] YUVvalues = RGBtoYUV(RGBarray);
                int DitherImage;
                if(CheckDither(YUVvalues[0],i,j) == true){
                    DitherImage = (256*256*0)+(256*0)+(0);
                }
                else{
                    DitherImage = (256*256*255)+(256*255)+(255);
                }
                int RGBImage = (256*256*YUVvalues[0])+(256*YUVvalues[0])+(YUVvalues[0]);
                ImageLeft.setRGB(j, i, RGBImage);
                ImageRight.setRGB(j, i, DitherImage);
            }
        }
    }
    
    private void DrawStage4(){
        ImageLeft = Image;
        ImageRight = new BufferedImage(Width,Height,BufferedImage.TYPE_INT_RGB);
        Raster Pixels = Image.getData();
        int[] RGBarray = new int[3];
        int[] RCount = new int[256];
        int[] GCount = new int[256];
        int[] BCount = new int[256];
        int PixelCount = Width * Height;
        for(int i = 0;i<Height;i++){
            for(int j = 0;j<Width;j++){
                Pixels.getPixel(j, i, RGBarray);
                RCount[RGBarray[0]]++;
                GCount[RGBarray[1]]++;
                BCount[RGBarray[2]]++;
            }
        }
        int Rsum = 0; 
        int Gsum = 0; 
        int Bsum = 0;
        int[] RMap =new int[256];
        int[] GMap =new int[256];
        int[] BMap =new int[256];
        for (int i = 0; i < 256; i++) {
            Rsum = Rsum + RCount[i];
            Gsum = Gsum + GCount[i];
            Bsum = Bsum + BCount[i];
            RMap[i] = (255 * Rsum / PixelCount);
            GMap[i] = (255 * Gsum / PixelCount);
            BMap[i] = (255 * Bsum / PixelCount);
        }
        for(int i = 0;i<Height;i++){
            for(int j = 0;j<Width;j++){
                Pixels.getPixel(j, i, RGBarray);
                RGBarray[0] = RMap[RGBarray[0]];
                RGBarray[1] = GMap[RGBarray[1]];
                RGBarray[2] = BMap[RGBarray[2]];
                int RGBImage = (256*256*RGBarray[0])+(256*RGBarray[1])+(RGBarray[2]);
                ImageRight.setRGB(j, i, RGBImage);
            }
        }
    }
    
    private int[] RGBtoYUV(int[] RGBarray){
        int[] YUV = new int[3];
        YUV[0] = (int) (0.299 * RGBarray[0] + 0.587 * RGBarray[1] + 0.114* RGBarray[2]);
        YUV[1] = (int) ((-0.299) * RGBarray[0] + (-0.587) * RGBarray[1] + 0.886* RGBarray[2]);
        YUV[2] = (int) (0.701 * RGBarray[0] + (-0.587) * RGBarray[1] + (-0.114)* RGBarray[2]);
        if(YUV[0] > 255){
            YUV[0]=255;
        }
        else if(YUV[0] < 0){
            YUV[0] =0;
        }
        return YUV;
    }

    private int[] YUVtoRGB(int[] YUV){
        int[] RGB = new int[3];
        RGB[0] = (int) (1 * YUV[0] + 0 * YUV[1]+ 1* YUV[2]);
        RGB[1] = (int) (1 * YUV[0] + (-0.1942) * YUV[1]+ (-0.5094)* YUV[2]);
        RGB[2] = (int) (1 * YUV[0] + 1 * YUV[1]+ 0* YUV[2]);
        for(int i = 0;i<3;i++){
            if(RGB[i]>255){
                RGB[i]=255;
            }
            else if(RGB[i]<0){
                RGB[i]=0;
            }
        }
        return RGB;
    }
    
    private boolean CheckDither(int Graylevel, int x, int y){
        // 2x2
        //return ((255-Graylevel)/(256/5) > DitherMatrix[x%2][y%2]);
        
        //4x4
        return ((255-Graylevel)/(256/17) > DitherMatrix[x%4][y%4]);
        
        //8x8
        //return ((255-Graylevel)/(256/65) > DitherMatrix[x%8][y%8]);
    }
    
    private void SetMatrix(){
        //2x2 matrix
        /*
        DitherMatrix = new int[2][2];
        DitherMatrix[0][0] = 3;
        DitherMatrix[0][1] = 1;
        DitherMatrix[1][0] = 0;
        DitherMatrix[1][1] = 2;
        */
        
        //4x4 matrix
        int[][] DitherMatrix1 = {{15,7,13,5},
            {3,11,1,9},
            {12,4,14,6},
            {0,8,2,10}};
        DitherMatrix = DitherMatrix1;
        
        // 8x8 matrix
        /*
        int[][] DitherMatrix1 = {{63, 31, 55, 23, 61, 29, 53, 21},
                {15, 47, 7,  39, 13, 45, 5,  37},
                {51, 19, 59, 27, 49, 17, 57, 25},
                {3,  35, 11, 43, 1,  33, 9,  41},
                {60, 28, 52, 20, 62, 30, 54, 22},
                {12, 44, 4,  36, 14, 46, 6,  38},
                {48, 16, 56, 24, 50, 18, 58, 26},
                {0,  32, 8,  40, 2,  34, 10, 42}};
        System.out.printf("%d\n",DitherMatrix1[0][0]);
        DitherMatrix = DitherMatrix1;
        */
        
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
        ImageFilter displayimage = new ImageFilter();
        displayimage.setVisible(true);
        });
    }
}
